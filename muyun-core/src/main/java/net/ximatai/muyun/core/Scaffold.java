package net.ximatai.muyun.core;

import jakarta.annotation.PostConstruct;
import net.ximatai.muyun.ability.IDatabaseAbility;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Scaffold implements IDatabaseAbility, IRuntimeAbility {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    protected void init() {
        if (this instanceof ITableCreateAbility ability) {
            ability.create(getDatabaseOperations());
        }
        afterInit();
        logger.info("{} initialized", this.getClass().getSimpleName());
    }

    protected void afterInit() {

    }

}
