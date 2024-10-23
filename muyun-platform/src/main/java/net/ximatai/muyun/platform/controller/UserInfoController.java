package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.ISoftDeleteAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.ability.IModuleRegisterAbility;
import net.ximatai.muyun.platform.model.Dict;
import net.ximatai.muyun.platform.model.DictCategory;
import net.ximatai.muyun.platform.model.ModuleAction;
import net.ximatai.muyun.platform.model.ModuleConfig;
import net.ximatai.muyun.service.IAuthorizationService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;
import static net.ximatai.muyun.platform.controller.UserInfoController.MODULE_ALIAS;

@Startup
@Tag(name = "用户管理")
@Path(BASE_PATH + "/" + MODULE_ALIAS)
public class UserInfoController extends ScaffoldForPlatform implements IReferableAbility, IReferenceAbility, ISoftDeleteAbility, IQueryAbility, IModuleRegisterAbility {

    public final static String MODULE_ALIAS = "userinfo";

    @Inject
    UserController userController;

    @Inject
    DepartmentController departmentController;

    @Inject
    OrganizationController organizationController;

    @Inject
    DictController dictController;

    @Inject
    DictCategoryController dictCategoryController;

    @Inject
    IAuthorizationService authorizationService;

    @Inject
    ModuleController moduleController;

    @Inject
    MuYunConfig config;

    @Override
    public String getMainTable() {
        return "auth_userinfo";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper.setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("v_name", "姓名")
            .addColumn("v_work_code", "工号")
            .addColumn("d_birth", "出生日期")
            .addColumn("v_phone", "手机号")
            .addColumn("v_email", "邮箱")
            .addColumn("v_address", "通讯地址")
            .addColumn("id_at_org_department")
            .addColumn("id_at_org_organization")
            .addColumn("file_photo", "头像")
            .addColumn(Column.of("b_user").setDefaultValue(false).setComment("是否用户"))
            .addColumn("dict_user_gender", "性别")
            .addColumn("j_conf", "用户个性化配置");
    }

    @Override
    protected void afterInit() {
        super.afterInit();
        this.registerModule();
        dictCategoryController.putDictCategory(new DictCategory("user_gender", "platform_dir", "人员性别", 1).setDictList(new Dict("0", "未知"), new Dict("1", "男"), new Dict("2", "女")), false);

        String superUserId = config.superUserId();

        Map<String, ?> superUser = this.view(superUserId);
        if (superUser == null) {
            String adminUsername = System.getenv("MUYUN_USERNAME");
            String adminPassword = System.getenv("MUYUN_PASSWORD");

            if (adminUsername == null || adminPassword == null) {
                System.out.println("SuperUser not found in the database.");
                System.out.println("Please input the initial admin account details:");

                Scanner scanner = new Scanner(System.in);

                adminUsername = promptForInput(scanner, "Admin Username: ");
                adminPassword = promptForInput(scanner, "Admin Password: ");
            } else {
                logger.info("USE ENVIRONMENT INFORMATION");
                logger.info("ADMIN USERNAME: {}", adminUsername);
                logger.info("ADMIN PASSWORD: {}", adminPassword);
            }

            this.create(Map.of("id", superUserId, "v_name", "超级管理员"));
            this.setUser(superUserId, Map.of("v_username", adminUsername, "v_password", adminPassword, "v_password2", adminPassword));
        }
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(organizationController.toReferenceInfo("id_at_org_organization"), departmentController.toReferenceInfo("id_at_org_department"), dictController.toReferenceInfo("dict_user_gender"), userController.toReferenceInfo("id").add("b_enabled").add("v_username", "v_username"));
    }

    @POST
    @Path("/setUser/{id}")
    @Transactional
    @Operation(summary = "设置为可登录账户")
    public String setUser(@PathParam("id") String id, Map<String, Object> params) {
        String username = (String) params.get("v_username");
        String password = (String) params.get("v_password");
        String password2 = (String) params.get("v_password2");
        Objects.requireNonNull(username, "必须提供用户名");
        Objects.requireNonNull(password, "必须提供密码");
        Objects.requireNonNull(password2, "必须提供二次输入密码");

        if (!password.equals(password2)) {
            throw new MyException("两次输入的密码不一致");
        }

        Map<String, ?> userInfo = this.view(id);
        if ((boolean) userInfo.get("b_user")) {
            throw new MyException("已经设置用户信息，无法再次设置");
        }
        this.update(id, Map.of("id", id, "b_user", true));

        userController.create(Map.of("id", id, "v_username", username, "v_password", password));

        if (params.containsKey("roles") && params.get("roles") instanceof List roles) {
            this.setRoles(id, roles);
        }

        return id;
    }

    @POST
    @Path("/setPassword/{id}")
    @Transactional
    @Operation(summary = "设置账户密码")
    public int setPassword(@PathParam("id") String id, Map<String, Object> params) {
        if (!config.isSuperUser(getUser().getId())) {
            throw new MyException("非管理员用户，禁止访问此功能");
        }

        String password = (String) params.get("v_password");
        String password2 = (String) params.get("v_password2");

        Objects.requireNonNull(password, "必须提供密码");
        Objects.requireNonNull(password2, "必须提供二次输入密码");

        if (!password.equals(password2)) {
            throw new MyException("两次输入的密码不一致");
        }

        Map<String, ?> userInfo = this.view(id);
        if ((boolean) userInfo.get("b_user")) {
            return userController.update(id, Map.of("v_password", password));
        } else {
            throw new MyException("尚未创建对应的用户");
        }
    }

    @POST
    @Path("/setPasswordSelf/{id}")
    @Transactional
    @Operation(summary = "自助修改账户密码")
    public int setPasswordSelf(@PathParam("id") String id, Map<String, Object> params) {
        String oldPassword = (String) Objects.requireNonNull(params.get("v_old_password"), "必须提供原始密码");
        String password = (String) Objects.requireNonNull(params.get("v_password"), "必须提供密码");
        String password2 = (String) Objects.requireNonNull(params.get("v_password2"), "必须提供二次输入密码");

        Map<String, ?> user = userController.view(id);

        if (!user.get("v_password").equals(oldPassword)) {
            throw new MyException("原密码不正确");
        }

        if (!password.equals(password2)) {
            throw new MyException("两次输入的密码不一致");
        }

        return userController.update(id, Map.of("v_password", password));
    }

    @Override
    @Transactional
    public Integer delete(String id) {
        Integer deleted = super.delete(id);
        Map<String, ?> user = userController.view(id);
        if (user != null) {
            userController.delete(id);
        }
        return deleted;
    }

    @GET
    @Path("/disableUser/{id}")
    @Operation(summary = "禁用账户")
    public String disableUser(@PathParam("id") String id) {
        userController.update(id, Map.of("b_enabled", false));
        return id;
    }

    @GET
    @Path("/enableUser/{id}")
    @Operation(summary = "启用账户")
    public String enableUser(@PathParam("id") String id) {
        userController.update(id, Map.of("b_enabled", true));
        return id;
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("v_email"),
            QueryItem.of("id_at_org_organization"),
            QueryItem.of("id_at_org_department"),
            QueryItem.of("b_user"),
            QueryItem.of("v_name").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("v_phone").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("v_username").setSymbolType(QueryItem.SymbolType.LIKE)
        );
    }

    @GET
    @Path("/roles/{userID}")
    @Operation(summary = "获取账户拥有的角色")
    public Set<String> roles(@PathParam("userID") String userID) {
        return authorizationService.getUserAvailableRoles(userID);
    }

    @POST
    @Path("/setRoles/{userID}")
    @Operation(summary = "设置账户拥有的角色")
    public Integer setRoles(@PathParam("userID") String userID, List<String> roles) {
        userController.putChildTableList(userID, "auth_user_role", List.of());
        return userController.putChildTableList(userID, "auth_user_role", roles.stream().map(it -> Map.of("id_at_auth_role", it)).toList()).getCreate();
    }

    @Override
    public ModuleController getModuleController() {
        return moduleController;
    }

    @Override
    public ModuleConfig getModuleConfig() {
        return ModuleConfig.ofName("用户管理")
            .setAlias(MODULE_ALIAS)
            .setTable(getMainTable())
            .setUrl("platform/userinfo/index")
            .addAction(new ModuleAction("setUser", "设置账户", ModuleAction.TypeLike.UPDATE))
            .addAction(new ModuleAction("setPassword", "设置密码", ModuleAction.TypeLike.UPDATE))
            .addAction(new ModuleAction("setPasswordSelf", "自助修改账户密码", ModuleAction.TypeLike.UPDATE))
            .addAction(new ModuleAction("disableUser", "禁用账户", ModuleAction.TypeLike.UPDATE))
            .addAction(new ModuleAction("enableUser", "启用账户", ModuleAction.TypeLike.UPDATE))
            .addAction(new ModuleAction("roles", "获取角色", ModuleAction.TypeLike.VIEW))
            .addAction(new ModuleAction("setRoles", "设置角色", ModuleAction.TypeLike.UPDATE));
    }

    private String promptForInput(Scanner scanner, String promptMessage) {
        if (config.isTestMode()) { // 单元测试模式，锁定用户名密码
            return "admin";
        }

        String input;
        do {
            System.out.print(promptMessage);
            input = scanner.nextLine().trim();  // 去掉输入前后的空格

            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            }
        } while (input.isEmpty());
        return input;
    }
}
