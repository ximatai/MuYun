package net.ximatai.muyun.core;

import io.vertx.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IDatabaseAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.database.IDatabaseOperations;

public abstract class Scaffold implements IDatabaseAbility {

    private IDatabaseOperations databaseOperations;
    private EventBus eventBus;

    @Inject
    public void setDatabaseOperations(IDatabaseOperations databaseOperations) {
        this.databaseOperations = databaseOperations;
    }

    @Inject
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    @PostConstruct
    void init() {
        if (this instanceof ITableCreateAbility ability) {
            ability.create(getDatabaseOperations());
        }
    }

}
