package net.ximatai.muyun.platform.controller;

import jakarta.ws.rs.Path;
import net.ximatai.muyun.core.exception.MyException;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Tag(name = "站内信收件箱")
@Path(BASE_PATH + "/inbox")
public class InboxController extends MessageController {
    @Override
    public String getAuthCondition() {
        return """
               and id in (
               select id_at_app_message from app_message_person where b_delete = false and id_at_auth_user__to = '%s'
               )
            """.formatted(getUser().getId());
    }

    @Override
    public Map<String, Object> view(String id) {
        Map<String, Object> view = super.view(id);
        getDB().update("update %s.app_message_person set b_read = true,t_read = now() where id_at_app_message = ? and id_at_auth_user__to = ?"
            .formatted(getSchemaName()), id, getUser().getId());
        return view;
    }

    @Override
    public Integer delete(String id) {
        return getDB().update("update %s.app_message_person set b_delete = true where id_at_app_message = ? and id_at_auth_user__to = ?"
            .formatted(getSchemaName()), id, getUser().getId());
    }

    @Override
    public Integer update(String id, Map body) {
        throw new MyException("收件箱内容不可修改");
    }
}
