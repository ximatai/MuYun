package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.platform.controller.MenuController;
import net.ximatai.muyun.platform.controller.MenuSchemaController;
import net.ximatai.muyun.platform.controller.OrganizationController;
import net.ximatai.muyun.platform.controller.RoleController;
import net.ximatai.muyun.platform.controller.UserInfoController;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestUserAndMenu {
    String base = PlatformConst.BASE_PATH;

    @Inject
    MuYunConfig config;

    @Inject
    UserInfoController userInfoController;

    @Inject
    OrganizationController organizationController;

    @Inject
    MenuSchemaController menuSchemaController;

    @Inject
    MenuController menuController;

    @Inject
    RoleController roleController;

    String schemaID;
    String userID;

    @BeforeAll
    void setUp() {
        String roleID = roleController.create(Map.of("v_name", "role1"));
        String orgID = organizationController.create(Map.of("v_name", "organization1"));
        userID = userInfoController.create(Map.of("v_name", "user1", "id_at_org_organization", orgID));

        roleController.putChildTableList(roleID, "auth_user_role", List.of(Map.of(
            "id_at_auth_user", userID
        )));

        schemaID = menuSchemaController.create(Map.of(
            "v_name", "menu_schema",
            "dicts_terminal_type", List.of("web"),
//            "ids_at_auth_role", List.of(roleID),
            "ids_at_orga_organization", List.of(orgID)
        ));

        String root1 = menuController.create(Map.of("v_name", "root", "id_at_app_menu_schema", schemaID));
        menuController.create(Map.of("v_name", "1-1", "id_at_app_menu_schema", schemaID, "pid", root1));
    }

    @Test
    void testIdToName() {
        String name = userInfoController.idToName(userID);
        assertEquals("user1", name);
    }

    @Test
    void testSchemaInit() {
        List<TreeNode> menus = given()
            .header("userID", config.superUserId())
            .get("%s/menuSchema/tree/%s".formatted(base, schemaID))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals(1, menus.size());
    }

    @Test
    void testMenuEmpty() {
        String userID = userInfoController.create(Map.of(
            "v_name", "test"
        ));

        userInfoController.setUser(userID, Map.of(
            "v_username", "test",
            "v_password", "test",
            "v_password2", "test"
        ));

        String roleID = roleController.create(Map.of("v_name", "role1"));

        roleController.putChildTableList(roleID, "auth_user_role", List.of(Map.of(
            "id_at_auth_user", userID
        )));

        String schemaID = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "默认方案",
                "dicts_terminal_type", List.of("web", "app"),
                "ids_at_auth_role", List.of(roleID)
            ))
            .when()
            .post("%s/menuSchema/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertNotNull(schemaID);

        List<TreeNode> menus = given()
            .header("userID", userID)
            .get("%s/runtime/menu".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals(0, menus.size());
    }

    @Test
    void testWhiteUserAccess() {
        given()
            .get("%s/runtime/menu".formatted(base))
            .then()
            .statusCode(401)
            .extract()
            .asString();

        given()
            .get("%s/runtime/whoami".formatted(base))
            .then()
            .statusCode(401)
            .extract()
            .asString();
    }

    @Test
    void testSchemaForUser() {
        String hitSchema = menuSchemaController.schemaForUser(userID, null);
        assertEquals(schemaID, hitSchema);
    }
}
