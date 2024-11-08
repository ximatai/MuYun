package net.ximatai.muyun.fileserver;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "file-server")
public interface FileServerConfig {
    String uploadPath();

    @WithDefault("fileServer")
    String pagePath();
    
}
