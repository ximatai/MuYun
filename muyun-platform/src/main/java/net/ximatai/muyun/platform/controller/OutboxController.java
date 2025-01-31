package net.ximatai.muyun.platform.controller;

import jakarta.ws.rs.Path;
import net.ximatai.muyun.core.exception.MuYunException;
import net.ximatai.muyun.model.PageResult;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Tag(description = "站内信发件箱")
@Path(BASE_PATH + "/outbox")
public class OutboxController extends MessageController {
    @Override
    public String getAuthCondition() { //发件箱查看只能看自己发的信件(不含回帖)
        return "and id_at_app_message__root isnull and id_at_auth_user__create = '%s'".formatted(getUser().getId());
    }

    @Override
    public void beforeUpdate(String id) {
        PageResult query = this.query(Map.of(
            "id_at_app_message__root", id
        ));
        if (query.getTotal() > 0) {
            throw new MuYunException("该信件已产生了回复，不允许修改或删除");
        }
    }

    @Override
    public void beforeDelete(String id) {
        this.beforeUpdate(id);
    }
}
