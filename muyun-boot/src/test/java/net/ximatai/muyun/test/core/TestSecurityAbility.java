package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ISecurityAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.core.security.AbstractEncryptor;
import net.ximatai.muyun.core.security.SMEncryptor;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestSecurityAbility {

    private String path = "/TestSecurityAbility";

    @Inject
    SMEncryptor smEncryptor;

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    TestSecurityAbilityController testController;

    @Test
    void test() {
        String id = testController.create(Map.of(
            "v_for_sign", "test",
            "v_for_encrypt", "test2"
        ));
        Map<String, ?> response = testController.view(id);
        assertEquals("test", response.get("v_for_sign"));
        assertEquals("test2", response.get("v_for_encrypt"));

        Map row = (Map) databaseOperations.row("select * from test.testsecurityability where id = ?", id);
        assertEquals("test2", smEncryptor.decrypt((String) row.get("v_for_encrypt")));
        assertEquals(smEncryptor.sign("test"), row.get(testController.column2SignColumn("v_for_sign")));
    }

    @Test
    @DisplayName("对同一数据加密两次的输出应该不同")
    void testSmEncryptor() {
        String test1 = smEncryptor.encrypt("test");
        String test2 = smEncryptor.encrypt("test");
        assertNotEquals(test1, test2);
    }

}

@Path("/TestSecurityAbility")
class TestSecurityAbilityController extends Scaffold implements ICURDAbility, ITableCreateAbility, ISecurityAbility {

    @Inject
    SMEncryptor smEncryptor;

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testsecurityability";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("v_for_sign").setType("varchar"))
            .addColumn(Column.of("v_for_encrypt").setType("varchar"))
            .addColumn(Column.of("pid").setType("varchar"))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));
    }

    @Override
    public List<String> getColumnsForSigning() {
        return List.of("v_for_sign");
    }

    @Override
    public List<String> getColumnsForEncryption() {
        return List.of("v_for_encrypt");
    }

    @Override
    public AbstractEncryptor getAEncryptor() {
        return smEncryptor;
    }
}
