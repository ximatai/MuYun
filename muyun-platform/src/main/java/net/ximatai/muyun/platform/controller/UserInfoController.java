package net.ximatai.muyun.platform.controller;

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
import net.ximatai.muyun.core.MuYunConfig;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.model.Dict;
import net.ximatai.muyun.platform.model.DictCategory;
import net.ximatai.muyun.service.IAuthorizationService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Path(BASE_PATH + "/userinfo")
public class UserInfoController extends ScaffoldForPlatform implements IReferableAbility, IReferenceAbility, ISoftDeleteAbility, IQueryAbility {

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
    MuYunConfig config;

    @Override
    public String getMainTable() {
        return "auth_userinfo";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper.setPrimaryKey(Column.ID_POSTGRES).setInherit(BaseBusinessTable.TABLE).addColumn("v_name", "姓名").addColumn("v_work_code", "工号").addColumn("d_birth", "出生日期").addColumn("v_phone", "手机号").addColumn("v_email", "邮箱").addColumn("v_address", "通讯地址").addColumn("id_at_org_department").addColumn("id_at_org_organization").addColumn("file_photo", "头像").addColumn(Column.of("b_user").setDefaultValue(false).setComment("是否用户")).addColumn("dict_user_gender", "性别").addColumn("j_conf", "用户个性化配置");
    }

    @Override
    protected void afterInit() {
        dictCategoryController.putDictCategory(new DictCategory("user_gender", "platform_dir", "人员性别", 1).setDictList(new Dict("0", "未知"), new Dict("1", "男"), new Dict("2", "女")), false);

        String superUserId = config.superUserId();

        Map<String, ?> superUser = this.view(superUserId);
        if (superUser == null) {
            this.create(Map.of("id", superUserId, "v_name", "超级管理员"));
            this.setUser(superUserId, Map.of("v_username", "admin", "v_password", "admin@bsy", "v_password2", "admin@bsy"));
        }
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(organizationController.toReferenceInfo("id_at_org_organization"), departmentController.toReferenceInfo("id_at_org_department"), dictController.toReferenceInfo("dict_user_gender"), userController.toReferenceInfo("id").add("b_enabled").add("v_username", "v_username"));
    }

    @POST
    @Path("/setUser/{id}")
    @Transactional
    @Operation(summary = "设置为可登录用户")
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
            this.roles(id, roles);
        }

        return id;
    }

    @POST
    @Path("/setPassword/{id}")
    @Transactional
    @Operation(summary = "设置用户密码")
    public int setPassword(@PathParam("id") String id, Map<String, Object> params) {
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
    @Operation(summary = "禁用用户")
    public String disableUser(@PathParam("id") String id) {
        userController.update(id, Map.of("b_enabled", false));
        return id;
    }

    @GET
    @Path("/enableUser/{id}")
    @Operation(summary = "启用用户")
    public String enableUser(@PathParam("id") String id) {
        userController.update(id, Map.of("b_enabled", true));
        return id;
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(QueryItem.of("v_name").setSymbolType(QueryItem.SymbolType.LIKE), QueryItem.of("v_phone").setSymbolType(QueryItem.SymbolType.LIKE), QueryItem.of("v_username").setSymbolType(QueryItem.SymbolType.LIKE));
    }

    @GET
    @Path("/roles/{userID}")
    @Operation(summary = "获取用户拥有的角色")
    public List<String> roles(@PathParam("userID") String userID) {
        return authorizationService.getUserAvailableRoles(userID);
    }

    @POST
    @Path("/roles/{userID}")
    @Operation(summary = "设置用户拥有的角色")
    public Integer roles(@PathParam("userID") String userID, List<String> roles) {
        userController.putChildTableList(userID, "auth_user_role", List.of());
        return userController.putChildTableList(userID, "auth_user_role", roles.stream().map(it -> Map.of("id_at_auth_role", it)).toList()).getCreate();
    }
}
