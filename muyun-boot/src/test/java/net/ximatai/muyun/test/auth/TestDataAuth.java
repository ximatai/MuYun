package net.ximatai.muyun.test.auth;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.authorization.AuthorizationService;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.database.IDatabaseOperationsStd;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableBuilder;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.platform.controller.AuthorizationController;
import net.ximatai.muyun.platform.controller.ModuleController;
import net.ximatai.muyun.platform.controller.RegionController;
import net.ximatai.muyun.platform.controller.RoleController;
import net.ximatai.muyun.platform.controller.SupervisionRegionController;
import net.ximatai.muyun.platform.controller.UserInfoController;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestDataAuth {
    String base = PlatformConst.BASE_PATH;

    @Inject
    IDatabaseOperationsStd db;

    @Inject
    AuthorizationService authService;

    @Inject
    ModuleController moduleController;

    @Inject
    AuthorizationController authorizationController;

    @Inject
    UserInfoController userInfoController;

    @Inject
    RoleController roleController;

    @Inject
    RegionController regionController;

    @Inject
    SupervisionRegionController supervisionRegionController;

    String userID;
    String role1, role2;
    String module1, module2;

    String view1, view2, update1, delete1, delete2;

    @BeforeAll
    void setUp() {

        new TableBuilder(db).build(
            TableWrapper.withName("module1")
                .setSchema("public")
                .setPrimaryKey(Column.ID_POSTGRES)
                .setInherit(BaseBusinessTable.TABLE)
                .addColumn("v_name")
        );

        new TableBuilder(db).build(
            TableWrapper.withName("module2")
                .setSchema("public")
                .setPrimaryKey(Column.ID_POSTGRES)
                .setInherit(BaseBusinessTable.TABLE)
                .addColumn("v_name")
        );

        userID = db.insertItem("platform", "auth_user", Map.of(
            "v_username", "test"
        ));

        db.insertItem("platform", "auth_userinfo", Map.of(
            "id", userID,
            "id_at_org_department", "1",
            "id_at_org_organization", "1"
        ));

        role1 = db.insertItem("platform", "auth_role", Map.of(
            "v_name", "role1"
        ));
        role2 = db.insertItem("platform", "auth_role", Map.of(
            "v_name", "role2"
        ));

        db.insertItem("platform", "auth_user_role", Map.of(
            "id_at_auth_user", userID,
            "id_at_auth_role", role1
        ));

        db.insertItem("platform", "auth_user_role", Map.of(
            "id_at_auth_user", userID,
            "id_at_auth_role", role2
        ));

        module1 = moduleController.create(Map.of(
            "v_name", "module1",
            "v_alias", "module1",
            "v_table", "module1"
        ));

        module2 = moduleController.create(Map.of(
            "v_name", "module2",
            "v_alias", "module2",
            "v_table", "module2"
        ));

        List<Map> actionList1 = moduleController.getChildTableList(module1, "app_module_action", null);

        view1 = (String) actionList1.stream().filter(it -> it.get("v_alias").equals("view")).findFirst().get().get("id");
        update1 = (String) actionList1.stream().filter(it -> it.get("v_alias").equals("update")).findFirst().get().get("id");
        delete1 = (String) actionList1.stream().filter(it -> it.get("v_alias").equals("delete")).findFirst().get().get("id");

        List<Map> actionList2 = moduleController.getChildTableList(module2, "app_module_action", null);
        view2 = (String) actionList2.stream().filter(it -> it.get("v_alias").equals("view")).findFirst().get().get("id");
        delete2 = (String) actionList2.stream().filter(it -> it.get("v_alias").equals("delete")).findFirst().get().get("id");

    }

    @BeforeEach
    void beforeEach() {
        db.execute("truncate table platform.auth_role_action");
        db.execute("truncate table platform.org_department");
    }

    @Test
    void testIsDataAuthorized1() {
        String data1 = db.insertItem("public", "module1", Map.of(
            "v_name", "test1",
            "id_at_auth_user__perms", userID,
            "id_at_org_department__perms", "1",
            "id_at_org_organization__perms", "1"
        ));

        accredit(role1, view1, "organization_and_subordinates");
        accredit(role1, update1, "department_and_subordinates");
        accredit(role1, delete1, "self");

        accredit(role2, view1, "organization");

        assertTrue(authService.isDataAuthorized(userID, "module1", "view", data1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "update", data1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "delete", data1));
    }

    @Test
    void testIsDataAuthorized2() {

        String d1 = db.insertItem("public", "module1", Map.of(
            "v_name", "test1",
            "id_at_auth_user__perms", userID,
            "id_at_org_department__perms", "1",
            "id_at_org_organization__perms", "1"
        ));

        String d2 = db.insertItem("public", "module1", Map.of(
            "v_name", "test2",
            "id_at_auth_user__perms", "xxx",
            "id_at_org_department__perms", "2",
            "id_at_org_organization__perms", "1"
        ));

        accredit(role1, view1, "organization_and_subordinates");
        accredit(role1, update1, "department_and_subordinates");
        accredit(role1, delete1, "self");

        accredit(role2, view1, "organization");

        assertTrue(authService.isDataAuthorized(userID, "module1", "view", d1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "update", d1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "delete", d1));

        assertTrue(authService.isDataAuthorized(userID, "module1", "view", d2));
        assertFalse(authService.isDataAuthorized(userID, "module1", "update", d2));
        assertFalse(authService.isDataAuthorized(userID, "module1", "delete", d2));
    }

    @Test
    void testIsDataAuthorized3() {

        String d1 = db.insertItem("public", "module1", Map.of(
            "v_name", "test1",
            "id_at_auth_user__perms", userID,
            "id_at_org_department__perms", "1",
            "id_at_org_organization__perms", "1"
        ));

        String d2 = db.insertItem("public", "module1", Map.of(
            "v_name", "test2",
            "id_at_auth_user__perms", "xxx",
            "id_at_org_department__perms", "2",
            "id_at_org_organization__perms", "1"
        ));

        accredit(role1, view1, "department");
        accredit(role1, update1, "department");
        accredit(role1, delete1, "open"); // 即使 delete 给了更大范围权限，但是仍然不可以突破前面的 view 和 update 的范围。

        accredit(role2, view1, "organization");

        assertTrue(authService.isDataAuthorized(userID, "module1", "view", d1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "update", d1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "delete", d1));

        assertTrue(authService.isDataAuthorized(userID, "module1", "view", d2));
        assertFalse(authService.isDataAuthorized(userID, "module1", "update", d2));
        assertFalse(authService.isDataAuthorized(userID, "module1", "delete", d2));
    }

    @Test
    void testIsDataAuthorized4() {
        db.insertItem("platform", "org_department", Map.of(
            "id", "1",
            "v_name", "root",
            "id_at_org_organization", "1"
        ));

        db.insertItem("platform", "org_department", Map.of(
            "id", "2",
            "pid", "1",
            "v_name", "root",
            "id_at_org_organization", "1"
        ));

        String d1 = db.insertItem("public", "module1", Map.of(
            "v_name", "test1",
            "id_at_auth_user__perms", userID,
            "id_at_org_department__perms", "1",
            "id_at_org_organization__perms", "1"
        ));

        String d2 = db.insertItem("public", "module1", Map.of(
            "v_name", "test2",
            "id_at_auth_user__perms", "xxx",
            "id_at_org_department__perms", "2",
            "id_at_org_organization__perms", "1"
        ));

        authService.invalidateAll();

        accredit(role1, view1, "department_and_subordinates");
        accredit(role1, update1, "department");
        accredit(role1, delete1, "open"); // 即使 delete 给了更大范围权限，但是仍然不可以突破前面的 view 和 update 的范围。

        accredit(role2, view1, "self");

        assertTrue(authService.isDataAuthorized(userID, "module1", "view", d1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "update", d1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "delete", d1));

        assertTrue(authService.isDataAuthorized(userID, "module1", "view", d2));
        assertFalse(authService.isDataAuthorized(userID, "module1", "update", d2));
        assertFalse(authService.isDataAuthorized(userID, "module1", "delete", d2));
    }

    @Test
    void testIsDataAuthorizedRegion() {
        String regionID = regionController.create(Map.of(
            "v_name", "test",
            "v_code", "test"
        ));

        String d1 = db.insertItem("public", "module1", Map.of(
            "v_name", "test1",
            "id_at_auth_user__perms", userID,
            "id_at_org_department__perms", "1",
            "id_at_app_region", regionID
        ));

        authService.invalidateAll();

        accredit(role1, view1, "supervision_region");

        assertFalse(authService.isDataAuthorized(userID, "module1", "view", d1));

        supervisionRegionController.create(Map.of(
            "id_at_org_organization", "1",
            "id_at_app_region", regionID
        ));

        authService.invalidateAll();

        assertTrue(authService.isDataAuthorized(userID, "module1", "view", d1));
    }

    private void accredit(String role, String action, String dataAuth) {
        String grant = authorizationController.grant(role, action);
        authorizationController.setDataAuth(grant, Map.of(
            "dict_data_auth", dataAuth
        ));
    }
}
