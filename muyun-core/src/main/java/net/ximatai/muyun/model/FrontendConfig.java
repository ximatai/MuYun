package net.ximatai.muyun.model;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "frontend")
public interface FrontendConfig {
    List<FrontendItem> resources();
}

