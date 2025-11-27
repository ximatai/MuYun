package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IDesensitizationAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.core.desensitization.Desensitizer;
import net.ximatai.muyun.core.desensitization.MaskMiddleAlgorithm;
import net.ximatai.muyun.core.security.SMEncryptor;
import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class TestDesensitizationAbility {

    private String path = "/TestSecurityAbility";

    @Inject
    SMEncryptor smEncryptor;

    @Inject
    IDatabaseOperations<String> databaseOperations;

    @Inject
    TestDesensitizationAbilityController testController;

    @Test
    @DisplayName("测试创建和查看数据时v_name字段是否被脱敏")
    void test() {
        String text = "hello world!";
        String id = testController.create(Map.of(
            "v_name", text,
            "v_name3", text
        ));
        Map<String, ?> response = testController.view(id);

        String responseVName = (String) response.get("v_name");
        assertEquals(text.length(), responseVName.length());
        assertNotEquals(text, responseVName);
        assertEquals("h**********!", responseVName);
        assertNull(response.get("v_name2"));
        assertEquals(text, response.get("v_name3"));
    }

}

@Path("/TestDesensitizationAbility")
class TestDesensitizationAbilityController extends Scaffold implements ICURDAbility, ITableCreateAbility, IDesensitizationAbility {

    private final Desensitizer desensitizer = new Desensitizer()
        .registerAlgorithm("v_name", MaskMiddleAlgorithm.INSTANCE);

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testdesensitizationability";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID)
            .addColumn(Column.of("v_name"))
            .addColumn(Column.of("v_name2"))
            .addColumn(Column.of("v_name3"))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));
    }

    @Override
    public Desensitizer getDesensitizer() {
        return desensitizer;
    }
}
