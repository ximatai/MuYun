package net.ximatai.muyun.platform.controller;

import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.model.IRuntimeUser;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Path(BASE_PATH + "/runtime")
public class RuntimeController implements IRuntimeAbility {

    @Inject
    RoutingContext routingContext;

    @GET
    @Path("/whoami")
    public IRuntimeUser whoami() {
        return getUser();
    }

    @Override
    public RoutingContext getRoutingContext() {
        return routingContext;
    }
}
