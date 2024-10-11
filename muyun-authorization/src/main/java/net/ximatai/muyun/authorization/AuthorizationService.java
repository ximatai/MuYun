package net.ximatai.muyun.authorization;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.vertx.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.MuYunConfig;
import net.ximatai.muyun.database.IDatabaseOperationsStd;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.AuthorizedResource;
import net.ximatai.muyun.service.IAuthorizationService;

import java.util.List;
import java.util.Map;
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

    @Inject
    EventBus eventBus;

    LoadingCache<String, Map<String, Object>> moduleCache = Caffeine.newBuilder().build(this::loadModule);
    LoadingCache<String, Map<String, Object>> actionCache = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build(this::loadAction);

    LoadingCache<String, List<String>> userToRoles = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build(this::loadRoles);

    @PostConstruct
    private void init() {
        List<Map<String, Object>> list = db.query("select * from platform.app_module ");
        list.forEach(map -> {
            moduleCache.put(map.get("v_alias").toString(), map);
        });

        // 当模块数据发生变化时，清空所有缓存
        eventBus.consumer("data.change.platform.app_module")
            .handler(message -> {
                moduleCache.invalidateAll();
            });
    }

    private Map<String, Object> loadModule(String moduleAlias) {
        return db.row("select * from platform.app_module where v_alias = ?", moduleAlias);
    }

    private Map<String, Object> loadAction(String actionAtModule) { // 形如  view@user
        String[] split = actionAtModule.split("@");
        String action = split[0];
        String moduleAlias = split[1];
        return db.row("select * from platform.app_module_action where id_at_app_module = ? and v_alias = ?", moduleAlias, action);
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

        request.setModuleID(moduleRow.get("id").toString());

        String action = request.getAction();
        if("tree".equals(action)) {
            action = "view"; // tree 接口权限实际走 view 权限即可
        }

        Map<String, Object> actionRow = actionCache.get("%s@%s".formatted(action, request.getModule()));

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

        return false;
    }

    @Override
    public String getAuthCondition(String userID, String module, String action) {
        return "";
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
    public Set<AuthorizedResource> getAuthorizedResources(String userID) {
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
                return Set.of();
            }

            query = """
                select v_alias_at_app_module, v_alias_at_app_module_action
                from platform.auth_role_action
                where id_at_auth_role in (%s)
                """.formatted(roles.stream().map(n -> "?").collect(Collectors.joining(",")));
            params = roles.toArray(); // 普通用户需要角色ID
        }

        List<Map<String, Object>> result = db.query(query, params);

        return result.stream().map(it ->
            new AuthorizedResource(it.get("v_alias_at_app_module").toString(), it.get("v_alias_at_app_module_action").toString())
        ).collect(Collectors.toSet());
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
