package net.ximatai.muyun.core.config;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "web")
public interface WebConfig {
    List<Redirect> redirects();
}
