package net.ximatai.muyun.config;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@ApplicationScoped
public class ConfigDemo {

    private final Logger logger = LoggerFactory.getLogger(ConfigDemo.class);

    @Inject
    MuYunConfig config;

    @PostConstruct
    void init() {
        logger.info("ConfigDemo init");
        logger.info("debug mode :{}", config.debug());
    }
}
