package net.ximatai.muyun.core;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "muyun")
public interface MuYunConfig {
    Boolean debug();

    String superUserId();
}
