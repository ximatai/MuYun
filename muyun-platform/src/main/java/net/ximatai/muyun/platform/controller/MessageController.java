package net.ximatai.muyun.platform.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.ISoftDeleteAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.model.MuYunMessage;
import net.ximatai.muyun.platform.service.MessageCenter;

import java.util.List;
import java.util.Map;

public class MessageController extends ScaffoldForPlatform implements IChildrenAbility, IReferenceAbility, IQueryAbility, ISoftDeleteAbility {

    @Inject
    MessageForPerson messageForPerson;

    @Inject
    UserInfoController userInfoController;

    @Inject
    MessageCenter messageCenter;

    @Override
    public String getMainTable() {
        return "app_message";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn("id_at_auth_user__create", "创建人/发件人")
            .addColumn("t_create", "创建时间")
            .addColumn("v_title", "标题")
            .addColumn("v_context", "内容")
            .addColumn("files_att", "附件")
            .addColumn("id_at_app_message__root", "消息根id-适用于回帖情况");
    }

    @Override
    public String create(Map body) {
        String result = super.create(body);
        List<Map> personList = (List<Map>) body.get("app_message_person");

        MuYunMessage message = new MuYunMessage(
            "收到新信息啦",
            "%s 刚刚给你发了题为「%s」的信息".formatted(getUser().getName(), body.get("v_title")),
            ""
        );

        personList.forEach(person -> {
            String toUser = (String) person.get("id_at_auth_user__to");
            messageCenter.send(toUser, message);
        });
        return result;
    }

    @Override
    public Map<String, Object> view(String id) {
        Map<String, Object> map = super.view(id);
        List<Map> childTableList = this.getChildTableList(id, "app_message_person", List.of("id_at_auth_user__to"));
        List followUp = this.view(null, null, true, List.of("t_create"), Map.of("id_at_app_message__root", id)).getList();
        map.put("app_message_person", childTableList);
        map.put("follow_up", followUp);
        return map;
    }

    @Override
    public List<ChildTableInfo> getChildren() {
        return List.of(
            messageForPerson.toChildTable("id_at_app_message").setAutoDelete()
        );
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            userInfoController.toReferenceInfo("id_at_auth_user__create").add("v_name", "v_name_at_auth_user__from")
        );
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id_at_app_message__root")
        );
    }
}

@ApplicationScoped
class MessageForPerson extends ScaffoldForPlatform implements IChildAbility, IReferenceAbility, IQueryAbility {

    @Inject
    UserInfoController userInfoController;

    @Override
    public String getMainTable() {
        return "app_message_person";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn("id_at_app_message", "主表id")
            .addColumn("id_at_auth_user__to", "收件人")
            .addColumn("b_read", "是否已读", false)
            .addColumn("t_read", "阅读时间")
            .addColumn("b_delete", "是否删除", false);
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            userInfoController.toReferenceInfo("id_at_auth_user__to").add("v_name", "v_name_at_auth_user__to")
        );
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id_at_app_message"),
            QueryItem.of("b_read")
        );
    }
}
