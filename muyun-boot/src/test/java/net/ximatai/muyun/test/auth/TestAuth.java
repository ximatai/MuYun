package net.ximatai.muyun.test.auth;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.authorization.AuthorizationService;
import net.ximatai.muyun.database.IDatabaseOperationsStd;
import net.ximatai.muyun.model.AuthorizedResource;
import net.ximatai.muyun.platform.controller.ModuleController;
import net.ximatai.muyun.platform.controller.RoleActionController;
import net.ximatai.muyun.platform.controller.RoleController;
import net.ximatai.muyun.platform.controller.UserInfoController;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestAuth {

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

    @BeforeAll
    void setUp() {
        userID = db.insertItem("platform", "auth_user", Map.of(
            "v_username", "test"
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
            "v_alias", "module1"
        ));

        module2 = moduleController.create(Map.of(
            "v_name", "module2",
            "v_alias", "module2"
        ));

        List<Map> actionList1 = moduleController.getChildTableList(module1, "app_module_action", null);

        List<Map> actionList2 = moduleController.getChildTableList(module2, "app_module_action", null);

        roleActionController.create(Map.of(
            "id_at_auth_role", role1,
            "id_at_app_module_action", actionList1.get(0).get("id")
        ));

        roleActionController.create(Map.of(
            "id_at_auth_role", role1,
            "id_at_app_module_action", actionList1.get(1).get("id")
        ));

        roleActionController.create(Map.of(
            "id_at_auth_role", role1,
            "id_at_app_module_action", actionList1.get(2).get("id")
        ));

        roleActionController.create(Map.of(
            "id_at_auth_role", role1,
            "id_at_app_module_action", actionList2.get(0).get("id")
        ));

        roleActionController.create(Map.of(
            "id_at_auth_role", role2,
            "id_at_app_module_action", actionList1.get(0).get("id")
        ));
    }

    @Test
    void testGetUserAvailableRoles() {
        List<String> roles = authService.getUserAvailableRoles(userID);
        assertEquals(2, roles.size());
        assertTrue(roles.contains(role1));
        assertTrue(roles.contains(role2));
    }

    @Test
    void testGetAllowedActions() {
        List<String> actions = authService.getAllowedActions(userID, "module1");
        assertEquals(3, actions.size());

        List<String> actions2 = authService.getAllowedActions(userID, "module2");
        assertEquals(1, actions2.size());
    }

    @Test
    void testGetAllowedActionsForSuper() {
        List<String> actions = authService.getAllowedActions("1", "module1");
        assertEquals(ModuleController.ACTIONS.size(), actions.size());

        List<String> actions2 = authService.getAllowedActions("1", "module2");
        assertEquals(ModuleController.ACTIONS.size(), actions2.size());
    }

    @Test
    void testGetAuthorizedResources() {
        Set<AuthorizedResource> resources = authService.getAuthorizedResources(userID);
        assertEquals(4, resources.size());
    }

    @Test
    void testGetAuthorizedResourcesForSuper() {
        Set<AuthorizedResource> resources = authService.getAuthorizedResources("1");
        assertTrue(resources.size() > 11);
    }

    @Test
    void testIsAuthorized() {
        assertTrue(authService.isAuthorized(userID, "module1", "view"));
        assertTrue(authService.isAuthorized(userID, "module1", "menu"));
        assertTrue(authService.isAuthorized(userID, "module1", "export"));
        assertTrue(authService.isAuthorized(userID, "module2", "menu"));
        assertFalse(authService.isAuthorized(userID, "module2", "view"));
    }

    @Test
    void testIsAuthorizedForSuper() {
        assertTrue(authService.isAuthorized("1", "module1", "view"));
        assertTrue(authService.isAuthorized("1", "module1", "menu"));
        assertTrue(authService.isAuthorized("1", "module1", "export"));
        assertTrue(authService.isAuthorized("1", "module2", "menu"));
        assertTrue(authService.isAuthorized("1", "module2", "view"));
    }

}
