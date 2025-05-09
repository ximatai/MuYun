package net.ximatai.muyun.ability;

import io.quarkus.arc.Arc;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import net.ximatai.muyun.model.DataChangeChannel;

/**
 * 数据变动时通过 EventBus 向外广播的能力
 */
public interface IDataBroadcastAbility extends IMetadataAbility {

    default EventBus getEventBus() {
        return Arc.container().instance(EventBus.class).get();
    }

    default DataChangeChannel getDataChangeChannel() {
        return new DataChangeChannel(this);
    }

    default boolean msgToFrontEnd() {
        return true;
    }

    default void broadcast(DataChangeChannel.Type type, String id) {
        EventBus eventBus = getEventBus();
        DataChangeChannel channel = getDataChangeChannel();
        String address = channel.getAddress();
        String addressWithType = channel.getAddressWithType(type);

        JsonObject body = new JsonObject();
        body.put("type", type.name());
        body.put("id", id);
        if (msgToFrontEnd()) {
            body.put("toFrontEnd", true);
        }

        eventBus.publish(address, body);
        eventBus.publish(addressWithType, body);
    }
}
