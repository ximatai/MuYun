package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IDesensitizationAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.core.desensitization.Desensitizer;
import net.ximatai.muyun.core.desensitization.MaskMiddleAlgorithm;
import net.ximatai.muyun.core.security.SMEncryptor;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestDesensitizationAbility {

    private String path = "/TestSecurityAbility";

    @Inject
    SMEncryptor smEncryptor;

    @Inject
    IDatabaseAccess databaseAccess;

    @Inject
    TestDesensitizationAbilityController testController;

    @Test
    void test() {
        String text = "hello world!";
        String id = testController.create(Map.of(
            "v_name", text
        ));
        Map<String, ?> response = testController.view(id);

        String responseVName = (String) response.get("v_name");
        assertEquals(text.length(), responseVName.length());
        assertNotEquals(text, responseVName);
        assertEquals("h**********!", responseVName);
        assertNull(response.get("v_name2"));
    }

}

@Path("/TestDesensitizationAbility")
class TestDesensitizationAbilityController extends Scaffold implements ICURDAbility, ITableCreateAbility, IDesensitizationAbility {

    @Inject
    SMEncryptor smEncryptor;

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testdesensitizationability";
    }

    @Override
    public TableWrapper fitOutTable() {
        return TableWrapper.withName(getMainTable())
            .setSchema(getSchemaName())
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("v_name").setType("varchar"))
            .addColumn(Column.of("v_name2").setType("varchar"))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));
    }

    @Override
    public Desensitizer getDesensitizer() {
        return new Desensitizer().registerAlgorithm("v_name", new MaskMiddleAlgorithm());
    }
}
