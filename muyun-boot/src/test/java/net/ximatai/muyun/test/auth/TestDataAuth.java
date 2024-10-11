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
import net.ximatai.muyun.platform.controller.ModuleController;
import net.ximatai.muyun.platform.controller.RoleActionController;
import net.ximatai.muyun.platform.controller.RoleController;
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
    RoleActionController roleActionController;

    @Inject
    UserInfoController userInfoController;

    @Inject
    RoleController roleController;

    String userID;
    String role1, role2;
    String module1, module2;

    Map view1, view2, update1, delete1, delete2;

    String data1, data2;

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

        data1 = db.insertItem("public", "module1", Map.of(
            "v_name", "test1",
            "id_at_auth_user__perms", userID,
            "id_at_org_department__perms", "1",
            "id_at_org_organization__perms", "1"
        ));

        data2 = db.insertItem("public", "module1", Map.of(
            "v_name", "test2",
            "id_at_auth_user__perms", "2",
            "id_at_org_department__perms", "2",
            "id_at_org_organization__perms", "2"
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

        view1 = actionList1.stream().filter(it -> it.get("v_alias").equals("view")).findFirst().get();
        update1 = actionList1.stream().filter(it -> it.get("v_alias").equals("update")).findFirst().get();
        delete1 = actionList1.stream().filter(it -> it.get("v_alias").equals("delete")).findFirst().get();

        List<Map> actionList2 = moduleController.getChildTableList(module2, "app_module_action", null);
        view2 = actionList2.stream().filter(it -> it.get("v_alias").equals("view")).findFirst().get();
        delete2 = actionList2.stream().filter(it -> it.get("v_alias").equals("delete")).findFirst().get();

    }

    @BeforeEach
    void beforeEach() {
        db.execute("truncate table platform.auth_role_action");
    }

    @Test
    void testIsDataAuthorized() {

        roleActionController.create(Map.of(
            "id_at_auth_role", role1,
            "id_at_app_module_action", view1.get("id"),
            "dict_data_auth", "organization"
        ));

        roleActionController.create(Map.of(
            "id_at_auth_role", role1,
            "id_at_app_module_action", update1.get("id"),
            "dict_data_auth", "department"
        ));

        roleActionController.create(Map.of(
            "id_at_auth_role", role1,
            "id_at_app_module_action", delete1.get("id"),
            "dict_data_auth", "self"
        ));

        roleActionController.create(Map.of(
            "id_at_auth_role", role2,
            "id_at_app_module_action", view1.get("id"),
            "dict_data_auth", "organization"
        ));

        assertTrue(authService.isDataAuthorized(userID, "module1", "view", data1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "update", data1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "delete", data1));

        assertFalse(authService.isDataAuthorized(userID, "module1", "view", data2));
        assertFalse(authService.isDataAuthorized(userID, "module1", "update", data2));
        assertFalse(authService.isDataAuthorized(userID, "module1", "delete", data2));
    }

    @Test
    void testIsDataAuthorized2() {

        roleActionController.create(Map.of(
            "id_at_auth_role", role1,
            "id_at_app_module_action", view1.get("id"),
            "dict_data_auth", "organization_and_subordinates"
        ));

        roleActionController.create(Map.of(
            "id_at_auth_role", role1,
            "id_at_app_module_action", update1.get("id"),
            "dict_data_auth", "department_and_subordinates"
        ));

        roleActionController.create(Map.of(
            "id_at_auth_role", role1,
            "id_at_app_module_action", delete1.get("id"),
            "dict_data_auth", "self"
        ));

        roleActionController.create(Map.of(
            "id_at_auth_role", role2,
            "id_at_app_module_action", view1.get("id"),
            "dict_data_auth", "organization"
        ));

        assertTrue(authService.isDataAuthorized(userID, "module1", "view", data1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "update", data1));
        assertTrue(authService.isDataAuthorized(userID, "module1", "delete", data1));
    }
}
