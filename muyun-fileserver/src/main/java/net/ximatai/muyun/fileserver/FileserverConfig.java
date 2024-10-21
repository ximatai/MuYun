package net.ximatai.muyun.fileserver;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "fileserver")
public interface FileserverConfig {
    String uploadPath();
    String pagePath();
}
