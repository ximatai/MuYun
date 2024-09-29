package net.ximatai.muyun.core;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "server")
public interface ServerConfig {
    Boolean debug();
}
