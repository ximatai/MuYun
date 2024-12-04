package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.platform.controller.AuthorizationController;
import net.ximatai.muyun.platform.controller.ModuleActionController;
import net.ximatai.muyun.platform.controller.ModuleController;
import net.ximatai.muyun.platform.controller.RoleActionController;
import net.ximatai.muyun.platform.controller.RoleController;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
@DisplayName("演示分配权限后，更改模块及功能别名（alias）")
public class TestModuleAliasUpdate {

    @Inject
    ModuleController moduleController;

    @Inject
    RoleController roleController;

    @Inject
    ModuleActionController moduleActionController;

    @Inject
    RoleActionController roleActionController;

    @Inject
    AuthorizationController authorizationController;

    @Test
    @DisplayName("测试模块别名和操作别名更新后授权信息的一致性")
    void test() {
        String moduleID = moduleController.create(Map.of("v_name", "test", "v_alias", "test_alias", "v_table", "test_table"));
        List<Map> actions = moduleController.getChildTableList(moduleID, "app_module_action", null);
        assertTrue(!actions.isEmpty());

        Map viewAction = actions.stream().filter(it -> it.get("v_alias").equals("view")).findFirst().get();

        assertNotNull(viewAction);

        String roleID = roleController.create(Map.of("v_name", "test"));

        String roleActionID = authorizationController.grant(roleID, viewAction.get("id").toString());

        Map<String, ?> roleAction = roleActionController.view(roleActionID);

        assertEquals("test_alias", roleAction.get("v_alias_at_app_module"));
        assertEquals("view", roleAction.get("v_alias_at_app_module_action"));

        moduleController.update(moduleID, Map.of("v_alias", "test_alias2"));

        Map<String, ?> roleAction2 = roleActionController.view(roleActionID);

        assertEquals("test_alias2", roleAction2.get("v_alias_at_app_module"));

        moduleActionController.update(viewAction.get("id").toString(), Map.of("v_alias", "view2"));

        Map<String, ?> roleAction3 = roleActionController.view(roleActionID);

        assertEquals("view2", roleAction3.get("v_alias_at_app_module_action"));
    }

}
