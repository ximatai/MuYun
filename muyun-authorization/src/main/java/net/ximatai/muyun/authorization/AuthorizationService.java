package net.ximatai.muyun.authorization;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.MuYunConfig;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.IDatabaseOperationsStd;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.service.IAuthorizationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class AuthorizationService implements IAuthorizationService {

    @Inject
    IDatabaseOperationsStd db;

    @Inject
    MuYunConfig config;

    private final LoadingCache<String, Map<String, Object>> moduleCache = Caffeine.newBuilder()
        .expireAfterWrite(3, TimeUnit.MINUTES)
        .build(this::loadModule);

    private final LoadingCache<String, Map<String, Object>> userinfoCache = Caffeine.newBuilder()
        .expireAfterWrite(3, TimeUnit.MINUTES)
        .build(this::loadUserinfo);

    private final LoadingCache<String, Map<String, Object>> actionCache = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build(this::loadAction);

    private final LoadingCache<String, List<String>> userToRoles = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build(this::loadRoles);

    private final LoadingCache<String, List<Map<String, Object>>> allOrgAndDept = Caffeine.newBuilder()
        .expireAfterWrite(3, TimeUnit.MINUTES)
        .build(this::loadOrgAndDept);

    public void invalidateAll() {
        moduleCache.invalidateAll();
        userinfoCache.invalidateAll();
        actionCache.invalidateAll();
        userToRoles.invalidateAll();
        allOrgAndDept.invalidateAll();
    }

    @PostConstruct
    private void init() {
        List<Map<String, Object>> list = db.query("select * from platform.app_module ");
        list.forEach(map -> {
            moduleCache.put(map.get("v_alias").toString(), map);
        });
    }

    private Map<String, Object> loadModule(String moduleAlias) {
        return db.row("select * from platform.app_module where v_alias = ?", moduleAlias);
    }

    private Map<String, Object> loadUserinfo(String userID) {
        return db.row("select * from platform.auth_userinfo where id = ?", userID);
    }

    private Map<String, Object> loadAction(String actionAtModule) { // 形如  view@xxxxxx-xxx-xxx
        String[] split = actionAtModule.split("@");
        String action = split[0];
        String moduleID = split[1];
        return db.row("select * from platform.app_module_action where id_at_app_module = ? and v_alias = ?", moduleID, action);
    }

    public List<String> loadRoles(String userID) {
        List<Map<String, Object>> rows = db.query("""
            select id_at_auth_role
            from platform.auth_user_role
            where id_at_auth_user = ?
            and exists(select * from platform.auth_role where auth_role.id = id_at_auth_role)
            """, userID);
        return rows.stream().map(it -> it.get("id_at_auth_role").toString()).toList();
    }

    public List<Map<String, Object>> loadOrgAndDept(String type) {
        return switch (type) {
            case "org" -> db.query("select id,pid from platform.org_organization");
            case "dept" -> db.query("select id,pid from platform.org_department");
            default -> List.of();
        };
    }

    @Override
    public boolean isAuthorized(ApiRequest request) {
        String userID = request.getUser().getId();
        if (request.isSkip() || config.isSuperUser(userID)) {
            return true;
        }

        Map<String, Object> moduleRow = moduleCache.get(request.getModule());

        if (moduleRow == null) { // 模块未配置，不参与权限
            request.setSkip();
            return true;
        }

        String moduleID = moduleRow.get("id").toString();
        request.setModuleID(moduleID);

        String action = request.getAction();
        if ("tree".equals(action)) {
            action = "view"; // tree 接口权限实际走 view 权限即可
        }

        Map<String, Object> actionRow = actionCache.get("%s@%s".formatted(action, moduleID));

        if (actionRow == null || (boolean) actionRow.get("b_white")) { // 功能未配置，或者是白名单功能，不参与权限
            request.setSkip();
            return true;
        }

        boolean authorized = isAuthorized(userID, request.getModule(), action);

        if (!authorized) { // 功能权限验证失败
            request.setError((String) moduleRow.get("v_name"), (String) actionRow.get("v_name"));
            return false;
        }

        if (request.getDataID() != null) {
            boolean dataAuthorized = isDataAuthorized(userID, request.getModule(), action, request.getDataID());
            if (!dataAuthorized) {
                request.setError((String) moduleRow.get("v_name"), (String) actionRow.get("v_name"), request.getDataID());
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isAuthorized(String userID, String module, String action) {
        if (config.isSuperUser(userID)) {
            return true;
        }

        List<String> roles = getUserAvailableRoles(userID);

        if (roles.isEmpty()) {
            return false;
        }

        List<Map<String, Object>> result = db.query("""
                select 1
                from platform.auth_role_action
                where v_alias_at_app_module = ? and v_alias_at_app_module_action = ?
                and id_at_auth_role in (%s)
                """.formatted(roles.stream().map(n -> "?").collect(Collectors.joining(","))),
            Stream.concat(Stream.of(module, action), roles.stream()).toArray());

        return !result.isEmpty();
    }

    @Override
    public boolean isDataAuthorized(String userID, String module, String action, String dataID) {
        if (config.isSuperUser(userID)) {
            return true;
        }

        List<String> roles = getUserAvailableRoles(userID);

        if (roles.isEmpty()) {
            return false;
        }

        String authCondition = getAuthCondition(userID, module, action);

        Map<String, Object> moduleRow = moduleCache.get(module);

        String tableName = moduleRow.get("v_table").toString();

        Map<String, Object> hit = db.row("select 1 from %s where 1=1 and id = ? %s".formatted(tableName, authCondition), dataID);

        if (hit != null) {
            return true;
        } else {
            hit = db.row("select 1 from %s where 1=1 and id = ? ".formatted(tableName), dataID);
            if (hit != null) {
                return false;
            } else {
                throw new MyException("请求的数据不存在");
            }
        }
    }

    @Override
    public String getAuthCondition(String userID, String module, String action) {
        if (config.isSuperUser(userID)) {
            return "and 1=1";
        }

        List<String> roles = getUserAvailableRoles(userID);

        if (roles.isEmpty()) {
            return "and 1=2";
        }

        List<Map<String, Object>> result = db.query("""
                select auth_role_action.*, app_module_action.i_order
                from platform.auth_role_action
                         left join platform.app_module_action on auth_role_action.id_at_app_module_action = app_module_action.id
                where auth_role_action.v_alias_at_app_module = ?
                  and auth_role_action.id_at_auth_role in (%s)
                order by app_module_action.i_order
                """.formatted(roles.stream().map(n -> "?").collect(Collectors.joining(","))),
            Stream.concat(Stream.of(module), roles.stream()).toArray());

        List<Map<String, Object>> hitList = result.stream().filter(it -> it.get("v_alias_at_app_module_action").equals(action)).toList();

        if (hitList.isEmpty()) { // 说明未对此 Action 授权，正常不会发生，因为前面还有功能权限校验：isAuthorized
            return "and 1=2";
        }

        int iOrder = (int) hitList.getFirst().get("i_order");

        // 按角色分组
        Map<String, List<Map<String, Object>>> roleGroup = result.stream().collect(Collectors.groupingBy(it -> it.get("id_at_auth_role").toString()));

        List<String> eachRoleCondition = new ArrayList<>();

        roleGroup.forEach((k, v) -> {
            Optional<Map<String, Object>> isHit = v.stream().filter(it -> it.get("v_alias_at_app_module_action").equals(action)).findFirst();
            if (isHit.isPresent()) { // 如果为 false 说明当前角色就没有对目标 Action 授权，所以不参与拼接
                // 角色内部的权限关系是 and
                String condition = v.stream().filter(it -> {
                        int order = (int) it.get("i_order");
                        if (it.get("v_alias_at_app_module_action").equals(action)) { // 就是 当前功能这一行
                            return true;
                        }
                        return order > 0 && order < iOrder; // order 大于0参与级联权限，同时权限order小于当前功能
                    })
                    .map(row -> {
                        if ("custom".equals(row.get("dict_data_auth"))) {
                            return row.get("v_custom_condition").toString();
                        } else {
                            return dictDataAuthToCondition(userID, module, (String) row.get("dict_data_auth"));
                        }
                    })
                    .collect(Collectors.joining(" and "));

                eachRoleCondition.add(condition);
            }
        });

        // 角色之间的权限关系是 or
        String joined = eachRoleCondition.stream().map("(%s)"::formatted).collect(Collectors.joining(" or "));

        return "and (%s)".formatted(joined);
    }

    /**
     * 数据权限配置转化成可以执行的SQL条件
     *
     * @param userID       用户id
     * @param module       模块Alias
     * @param dictDataAuth 权限配置
     * @return 生成后的sql条件
     */
    private String dictDataAuthToCondition(String userID, String module, String dictDataAuth) {
        Map<String, Object> userInfo = userinfoCache.get(userID);

        String organizationID = (String) userInfo.get("id_at_org_organization");
        String departmentID = (String) userInfo.get("id_at_org_department");

        String organizationColumn = "id_at_org_organization__perms";
        String departmentColumn = "id_at_org_department__perms";
        String userColumn = "id_at_auth_user__perms";

        //下面三个表的权限过滤字段比较特殊
        if ("userinfo".equals(module)) {
            organizationColumn = "id_at_org_organization";
            departmentColumn = "id_at_org_department";
            userColumn = "id";
        }

        if ("organization".equals(module)) {
            organizationColumn = "id";
        }

        if ("department".equals(module)) {
            organizationColumn = "id_at_org_organization";
            departmentColumn = "id";
        }

        return switch (dictDataAuth) {
            case "open" -> "1=1";
            case "organization" -> "%s='%s'".formatted(organizationColumn, organizationID);
            case "organization_and_subordinates" -> "%s in (%s)"
                .formatted(organizationColumn, organizationAndSubordinates(organizationID)
                    .stream().map("'%s'"::formatted)
                    .collect(Collectors.joining(",")));
            case "department" -> "%s='%s'".formatted(departmentColumn, departmentID);
            case "department_and_subordinates" -> "%s in (%s)"
                .formatted(departmentColumn, departmentAndSubordinates(organizationID)
                    .stream().map("'%s'"::formatted)
                    .collect(Collectors.joining(",")));
            case "self" -> "%s='%s'".formatted(userColumn, userID);
            default -> "1=2";
        };
    }

    private Set<String> organizationAndSubordinates(String organizationID) {
        List<Map<String, Object>> list = allOrgAndDept.get("org");
        return meAndChildren(organizationID, list);
    }

    private Set<String> departmentAndSubordinates(String departmentID) {
        List<Map<String, Object>> list = allOrgAndDept.get("dept");
        return meAndChildren(departmentID, list);
    }

    private Set<String> meAndChildren(String me, List<Map<String, Object>> list) {
        if (me == null) {
            return Set.of("-1");
        }

        HashSet<String> all = new HashSet<>();
        all.add(me);

        List<String> children = new ArrayList<>();
        for (Map<String, Object> dep : list) {
            if (me.equals(dep.get("pid"))) {
                children.add((String) dep.get("id"));
            }
        }

        all.addAll(children);

        for (String child : children) {
            all.addAll(meAndChildren(child, list));
        }

        return all;
    }

    @Override
    public List<String> getAllowedActions(String userID, String module) {
        if (config.isSuperUser(userID)) {
            List<Map<String, Object>> result = db.query("""
                select v_alias
                from platform.app_module_action
                where id_at_app_module = (select id from platform.app_module where app_module.v_alias = ?)
                order by i_order
                """, module);

            return result.stream().map(it -> it.get("v_alias").toString()).collect(Collectors.toList());
        }

        List<String> roles = getUserAvailableRoles(userID);

        if (roles.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> result = db.query("""
                select distinct v_alias_at_app_module_action as v_action
                from platform.auth_role_action
                where v_alias_at_app_module = ?
                and id_at_auth_role in (%s)
                """.formatted(roles.stream().map(n -> "?").collect(Collectors.joining(","))),
            Stream.concat(Stream.of(module), roles.stream()).toArray());

        return result.stream().map(it -> it.get("v_action").toString()).collect(Collectors.toList());
    }

    @Override
    public Map<String, Set<String>> getAuthorizedResources(String userID) {
        String query;
        Object[] params;

        if (config.isSuperUser(userID)) {
            query = """
                select app_module.v_alias as v_alias_at_app_module,
                       app_module_action.v_alias as v_alias_at_app_module_action
                from platform.app_module_action
                left join platform.app_module on app_module_action.id_at_app_module = app_module.id
                """;
            params = new Object[]{}; // 超级用户不需要参数
        } else {
            List<String> roles = getUserAvailableRoles(userID);

            if (roles.isEmpty()) {
                return Map.of();
            }

            query = """
                select v_alias_at_app_module, v_alias_at_app_module_action
                from platform.auth_role_action
                where id_at_auth_role in (%s)
                """.formatted(roles.stream().map(n -> "?").collect(Collectors.joining(",")));
            params = roles.toArray(); // 普通用户需要角色ID
        }

        List<Map<String, Object>> result = db.query(query, params);

        Map<String, List<Map<String, Object>>> moduleGroup = result.stream().collect(Collectors.groupingBy(it -> it.get("v_alias_at_app_module").toString()));

        HashMap<String, Set<String>> moduleGroupMap = new HashMap<>();

        moduleGroup.forEach((k, v) -> {
            moduleGroupMap.put(k, new HashSet<>(v.stream().map(it -> it.get("v_alias_at_app_module_action").toString()).toList()));
        });

        return moduleGroupMap;
    }

    @Override
    public List<String> getUserAvailableRoles(String userID) {
        if (config.debug()) {
            return loadRoles(userID);
        } else {
            return userToRoles.get(userID);
        }
    }
}
