package net.ximatai.muyun.service;

import io.vertx.ext.web.RoutingContext;
import net.ximatai.muyun.model.IRuntimeUser;

public interface IContextFilter {

    void filter(RoutingContext context, IRuntimeUser user);

}
