package net.ximatai.muyun.core;

import io.smallrye.config.ConfigMapping;

import java.util.Objects;

@ConfigMapping(prefix = "muyun")
public interface MuYunConfig {
    Boolean debug();

    String superUserId();

    default boolean isSuperUser(String userID) {
        Objects.requireNonNull(userID);
        return userID.equals(superUserId());
    }
}
