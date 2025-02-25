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
        eventBus.publish(channelForUser(userID), message.toJson());
    }

    public void channelChanged(String channel) {
        eventBus.publish("web.channel.%s".formatted(channel), true);
    }

    private static String channelForUser(String userID) {
        return "web.user.%s".formatted(userID);
    }

}
