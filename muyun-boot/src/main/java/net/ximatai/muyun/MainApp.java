package net.ximatai.muyun;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class MainApp implements QuarkusApplication {

    private final Logger logger = LoggerFactory.getLogger(MainApp.class);

    @Inject
    MuYunConfig config;

    @Override
    public int run(String... args) {
        logger.info("PROFILE ON {}", config.profile());
        Quarkus.waitForExit();
        return 0;
    }
}
