package net.ximatai.muyun.core.config;

import io.smallrye.config.ConfigMapping;

import java.util.Objects;

@ConfigMapping(prefix = "muyun")
public interface MuYunConfig {
    Boolean debug();

    String superUserId();

    default boolean isSuperUser(String userID) {
        Objects.requireNonNull(userID, "请提供测试用户ID");
        return userID.equals(superUserId());
    }
}
