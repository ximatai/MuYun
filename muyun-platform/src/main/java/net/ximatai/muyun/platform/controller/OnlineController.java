package net.ximatai.muyun.platform.controller;

import io.vertx.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.platform.model.OnlineUser;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;
import static net.ximatai.muyun.platform.controller.OnlineController.MODULE_ALIAS;

@Tag(name = "在线用户")
@Path(BASE_PATH + "/" + MODULE_ALIAS)
public class OnlineController {
    public final static String MODULE_ALIAS = "online";

    @Inject
    EventBus eventBus;

    private List<OnlineUser> onlineUsers = new ArrayList<>();

    @PostConstruct
    void init() {

    }

    @GET
    @Path("/view")
    public List<OnlineUser> view() {
        return null;
    }

}

