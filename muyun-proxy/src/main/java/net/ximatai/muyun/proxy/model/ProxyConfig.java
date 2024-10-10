package net.ximatai.muyun.proxy.model;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "proxy")
public interface ProxyConfig {
    List<UpstreamItem> upstreams();
}
