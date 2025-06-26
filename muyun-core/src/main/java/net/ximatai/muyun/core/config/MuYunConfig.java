package net.ximatai.muyun.core.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Objects;

@ConfigMapping(prefix = "muyun")
public interface MuYunConfig extends IProfile {

    @WithDefault("1")
    String superUserId();

    @WithDefault("24")
    int sessionTimeoutHour();

    // 超过允许用户登录失败的次数后，锁定用户多长时间
    @WithDefault("0")
    int userFailureLockMin();

    // 允许用户登录失败的次数，超过此次数，用户将被锁定
    @WithDefault("0")
    int userFailureMaxCount();

    @WithDefault("false")
    boolean useSession();

    default boolean isSuperUser(String userID) {
        Objects.requireNonNull(userID, "请提供测试用户ID");
        return userID.equals(superUserId());
    }
}
