package net.ximatai.muyun.platform;

public class BusChannel {

    public static final String ROLE_CHANGED = "role_changed";

    public static String channelForUser(String userID) {
        return "web.user.%s".formatted(userID);
    }

}
