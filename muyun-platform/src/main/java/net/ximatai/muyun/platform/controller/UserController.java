package net.ximatai.muyun.platform.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.ISecurityAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.core.exception.MuYunException;
import net.ximatai.muyun.core.security.AbstractEncryptor;
import net.ximatai.muyun.core.security.SMEncryptor;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.util.StringUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class UserController extends ScaffoldForPlatform implements IQueryAbility, ISecurityAbility, IReferableAbility, IChildrenAbility {

    @Override
    public String getMainTable() {
        return "auth_user";
    }

    @Inject
    MuYunConfig config;

    @Inject
    SMEncryptor smEncryptor;

    @Inject
    UserRoleController userRoleController;

    @Inject
    DictCategoryController dictCategoryController;

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
            .addColumn("d_invalid", "账号失效时间")
            .addColumn("d_password_invalid", "密码失效时间")
            .addColumn("av_used_password", "已经使用的密码")
            .addColumn(Column.of("b_enabled").setDefaultValue(true))
            .addIndex("v_username", true);
    }

    @Override
    public void fitOutDefaultValue(Map body) {
        super.fitOutDefaultValue(body);

        if (config.userValidateDays() > 0) {
            body.put("d_invalid", LocalDate.now().plusDays(config.userValidateDays()));
        }

    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("v_username"),
            QueryItem.of("b_enabled")
        );
    }

    /**
     * 登录成功标记  t_this_login \ t_last_login
     */
    public void checkIn(String id) {
        getDB().update("""
            update platform.auth_user set t_last_login = t_this_login,t_this_login = now() where id = ?
            """, id);
    }

    @Override
    public List<String> getColumnsForSigning() {
        return List.of();
    }

    @Override
    public List<String> getColumnsForEncryption() {
        return List.of("v_password", "av_used_password");
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

    public Integer updatePassword(String id, String password) {
        checkPasswordComplexity(password);

        Map<String, Object> user = view(id);

        List<String> avUsedPassword = user.get("av_used_password") != null ? new ArrayList<>(Arrays.asList(((String[]) user.get("av_used_password")))) : new ArrayList<>();

        if (config.userPasswordCheckReuse() && avUsedPassword.contains(password)) {
            throw new MuYunException("新设置的密码曾经使用过，不允许再次使用");
        }

        String old = (String) user.get("v_password");

        if (old != null) {
            avUsedPassword.add(old);
        }

        if (config.userPasswordValidateDays() > 0) {
            return update(id, Map.of("v_password", password, "av_used_password", avUsedPassword, "d_password_invalid", LocalDate.now().plusDays(config.userPasswordValidateDays())));
        } else {
            return update(id, Map.of("v_password", password, "av_used_password", avUsedPassword));
        }

    }

    private void checkPasswordComplexity(String password) {
        List<Map> childTableList = dictCategoryController.getChildTableList("password_complexity", "app_dict", null);
        StringBuilder msgBuilder = new StringBuilder();
        childTableList.forEach(map -> {
            String regex = (String) map.get("v_value");
            if (StringUtil.isNotBlank(regex) && !password.matches(regex)) {
                String remark = (String) map.get("v_remark");
                String name = (String) map.get("v_name");
                msgBuilder.append("，").append(StringUtil.isNotBlank(remark) ? remark : name);
            }
        });
        if (!msgBuilder.isEmpty()) {
            String error = msgBuilder.substring(1);
            logger.error("bad password :%s,%s".formatted(password, error));
            throw new MuYunException(error);
        }
    }

}
