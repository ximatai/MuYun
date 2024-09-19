package net.ximatai.muyun.core;

import io.vertx.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.database.IDatabaseAccess;

public class Scaffold {

    private IDatabaseAccess databaseAccess;
    private EventBus eventBus;

    @Inject
    public void setDatabaseAccess(IDatabaseAccess databaseAccess) {
        this.databaseAccess = databaseAccess;
    }

    @Inject
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public IDatabaseAccess getDatabaseAccess() {
        return databaseAccess;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    @PostConstruct
    void init() {
        if (this instanceof ITableCreateAbility tableCreateAbility) {
            tableCreateAbility.create(getDatabaseAccess());
        }
    }

}
