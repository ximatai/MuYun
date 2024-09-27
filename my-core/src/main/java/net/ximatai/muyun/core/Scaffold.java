package net.ximatai.muyun.core;

import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IDatabaseAbility;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.database.IDatabaseOperations;

public abstract class Scaffold implements IDatabaseAbility, IRuntimeAbility {

    private IDatabaseOperations databaseOperations;
    private EventBus eventBus;
    private RoutingContext routingContext;

    @Inject
    public void setRoutingContext(RoutingContext routingContext) {
        this.routingContext = routingContext;
    }

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

    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    @PostConstruct
    protected void init() {
        if (this instanceof ITableCreateAbility ability) {
            ability.create(getDatabaseOperations());
        }
        afterInit();
    }

    protected void afterInit() {

    }

}
