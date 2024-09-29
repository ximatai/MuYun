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
import net.ximatai.muyun.core.database.MyTableWrapper;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.model.Dict;
import net.ximatai.muyun.platform.model.DictCategory;
import net.ximatai.muyun.service.IAuthorizationService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Path(BASE_PATH + "/userinfo")
public class UserInfoController extends ScaffoldForPlatform implements IReferableAbility, IReferenceAbility, ISoftDeleteAbility, IQueryAbility {

    @Inject
    BaseBusinessTable base;

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
    UserRoleController userRoleController;

    @Override
    public String getMainTable() {
        return "auth_userinfo";
    }

    @Override
    public TableWrapper getTableWrapper() {
        return new MyTableWrapper(this)
            .setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(base.getTableWrapper())
            .addColumn("v_name")
            .addColumn("v_work_code")
            .addColumn("d_birth")
            .addColumn("v_phone")
            .addColumn("v_email")
            .addColumn("v_address")
            .addColumn("id_at_org_department")
            .addColumn("id_at_org_organization")
            .addColumn("file_photo")
            .addColumn(Column.of("b_user").setDefaultValue(false))
            .addColumn("dict_user_gender")
            .addColumn("j_conf", "用户个性化配置");
    }

    @Override
    protected void afterInit() {
        dictCategoryController.putDictCategory(
            new DictCategory("user_gender", "platform_dir", "人员性别", 1).setDictList(
                new Dict("0", "未知"),
                new Dict("1", "男"),
                new Dict("2", "女")
            ), false);
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            organizationController.toReferenceInfo("id_at_org_organization"),
            departmentController.toReferenceInfo("id_at_org_department"),
            dictController.toReferenceInfo("dict_user_gender"),
            userController.toReferenceInfo("id").add("b_enabled").add("v_username", "v_username")
        );
    }

    @POST
    @Path("/setUser/{id}")
    @Transactional
    public String setUser(@PathParam("id") String id, Map<String, Object> params) {
        String username = (String) params.get("v_username");
        String password = (String) params.get("v_password");
        String password2 = (String) params.get("v_password2");
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        Objects.requireNonNull(password2);

        if (!password.equals(password2)) {
            throw new MyException("两次输入的密码不一致");
        }

        Map<String, ?> userInfo = this.view(id);
        if ((boolean) userInfo.get("b_user")) {
            throw new MyException("已经设置用户信息，无法再次设置");
        }
        this.update(id, Map.of(
            "id", id,
            "b_user", true
        ));

        userController.create(Map.of(
            "id", id,
            "v_username", username,
            "v_password", password
        ));

        return id;
    }

    @Override
    @Transactional
    public Integer delete(String id) {
        Integer deleted = super.delete(id);
        userController.delete(id);
        return deleted;
    }

    @GET
    @Path("/disableUser/{id}")
    public String disableUser(@PathParam("id") String id) {
        userController.update(id, Map.of(
            "b_enabled", false
        ));

        return id;
    }

    @GET
    @Path("/enableUser/{id}")
    public String enableUser(@PathParam("id") String id) {
        userController.update(id, Map.of(
            "b_enabled", true
        ));

        return id;
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("v_name").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("v_phone").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("v_username").setSymbolType(QueryItem.SymbolType.LIKE)
        );
    }

    @GET
    @Path("/roles/{userID}")
    public List<String> roles(@PathParam("userID") String userID) {
        return authorizationService.getUserAvailableRoles(userID);
    }

    @POST
    @Path("/roles/{userID}")
    public Integer roles(@PathParam("userID") String userID, List<String> roles) {
        userController.putChildTableList(userID, "auth_user_role", List.of());
        return userController.putChildTableList(userID, "auth_user_role", roles.stream().map(it -> Map.of(
            "id_at_auth_role", it
        )).toList()).getCreate();
    }
}
