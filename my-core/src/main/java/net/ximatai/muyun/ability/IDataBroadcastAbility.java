package net.ximatai.muyun.ability;

import io.vertx.core.eventbus.EventBus;
import net.ximatai.muyun.model.DataChangeChannel;

public interface IDataBroadcastAbility extends IMetadataAbility {

    EventBus getEventBus();

    default DataChangeChannel getDataChangeChannel() {
        // 使用 volatile 和双重检查锁确保线程安全的延迟初始化
        DataChangeChannel localChannel = DataChangeChannelHolder.INSTANCE;
        if (localChannel == null) {
            synchronized (IDataBroadcastAbility.class) {
                if (DataChangeChannelHolder.INSTANCE == null) {
                    DataChangeChannelHolder.INSTANCE = new DataChangeChannel(this);
                }
                localChannel = DataChangeChannelHolder.INSTANCE;
            }
        }
        return localChannel;
    }

    // 静态内部类用于延迟初始化
    class DataChangeChannelHolder {
        static volatile DataChangeChannel INSTANCE = null;
    }

    default void broadcast(DataChangeChannel.Type type, String id) {
        EventBus eventBus = getEventBus();
        String address = getDataChangeChannel().getAddress();
        String addressWithType = getDataChangeChannel().getAddressWithType(type);

        eventBus.publish(address, id);
        eventBus.publish(addressWithType, id);
    }
}
