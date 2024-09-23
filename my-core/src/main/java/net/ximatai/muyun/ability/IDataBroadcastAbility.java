package net.ximatai.muyun.ability;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import net.ximatai.muyun.model.DataChangeChannel;

public interface IDataBroadcastAbility extends IMetadataAbility {

    EventBus getEventBus();

    DataChangeChannel getDataChangeChannel();

    default void broadcast(DataChangeChannel.Type type, String id) {
        EventBus eventBus = getEventBus();
        String address = getDataChangeChannel().getAddress();
        String addressWithType = getDataChangeChannel().getAddressWithType(type);

        eventBus.publish(address, id, new DeliveryOptions().addHeader("type", type.name()));
        eventBus.publish(addressWithType, id);
    }
}
