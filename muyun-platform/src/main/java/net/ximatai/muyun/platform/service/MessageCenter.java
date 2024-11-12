package net.ximatai.muyun.platform.service;

import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.platform.model.MuYunMessage;

@ApplicationScoped
public class MessageCenter {

    @Inject
    EventBus eventBus;

    public void send(String userID, MuYunMessage message) {
        eventBus.send(channelForUser(userID), message.toJson());
    }

    private static String channelForUser(String userID) {
        return "web.user.%s".formatted(userID);
    }

}
