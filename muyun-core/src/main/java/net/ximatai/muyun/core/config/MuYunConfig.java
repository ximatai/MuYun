package net.ximatai.muyun.core.config;

import io.smallrye.config.ConfigMapping;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Objects;

@ConfigMapping(prefix = "muyun")
public interface MuYunConfig {

    default ProfileMode profile() {
        Config config = ConfigProvider.getConfig();
        String upperCase = config.getValue("quarkus.profile", String.class).toUpperCase();
        return ProfileMode.valueOf(upperCase);
    }

    default boolean isTestMode() {
        return profile().equals(ProfileMode.TEST);
    }

    default boolean isProdMode() {
        return profile().equals(ProfileMode.PROD);
    }

    default boolean isDevMode() {
        return profile().equals(ProfileMode.DEV);
    }

    String superUserId();

    default boolean isSuperUser(String userID) {
        Objects.requireNonNull(userID, "请提供测试用户ID");
        return userID.equals(superUserId());
    }
}
