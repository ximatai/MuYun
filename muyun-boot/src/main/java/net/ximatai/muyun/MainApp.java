package net.ximatai.muyun;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class MainApp implements QuarkusApplication {

    private final Logger logger = LoggerFactory.getLogger(MainApp.class);

    @Inject
    MuYunConfig config;

    @ConfigProperty(name = "quarkus.http.port")
    int port;

    @Override
    public int run(String... args) {
        logger.info("MuYun started successfully! Running on port: {}. Debug mode is {}", port, config.debug() ? "OPEN" : "CLOSE");
        Quarkus.waitForExit();
        return 0;
    }
}
