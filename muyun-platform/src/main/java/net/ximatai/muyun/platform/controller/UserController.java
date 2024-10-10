package net.ximatai.muyun.platform.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.ISecurityAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.security.AbstractEncryptor;
import net.ximatai.muyun.core.security.SMEncryptor;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import java.util.List;

@ApplicationScoped
public class UserController extends ScaffoldForPlatform implements IQueryAbility, ISecurityAbility, IReferableAbility, IChildrenAbility {

    @Override
    public String getMainTable() {
        return "auth_user";
    }

    @Inject
    SMEncryptor smEncryptor;

    @Inject
    UserRoleController userRoleController;

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn("v_username")
            .addColumn("v_password")
            .addColumn("t_create")
            .addColumn("t_update")
            .addColumn("t_last_login")
            .addColumn("t_this_login")
            .addColumn(Column.of("b_enabled").setDefaultValue(true))
            .addIndex("v_username", true);
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("v_username"),
            QueryItem.of("b_enabled")
        );
    }

    @Override
    public List<String> getColumnsForSigning() {
        return List.of();
    }

    @Override
    public List<String> getColumnsForEncryption() {
        return List.of("v_password");
    }

    @Override
    public AbstractEncryptor getAEncryptor() {
        return smEncryptor;
    }

    @Override
    public String getLabelColumn() {
        return "v_username";
    }

    @Override
    public List<ChildTableInfo> getChildren() {
        return List.of(
            userRoleController.toChildTable("id_at_auth_user").setAutoDelete()
        );
    }

}
