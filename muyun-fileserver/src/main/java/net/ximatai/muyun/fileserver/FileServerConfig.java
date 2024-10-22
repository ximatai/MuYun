package net.ximatai.muyun.fileserver;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "file-server")
public interface FileServerConfig {
    String uploadPath();
    
    String pagePath();
}
