package net.ximatai.muyun.platform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.LocalDateTime;

public class OnlineDevice {
    private String id;
    private String os;
    private String browser;
    private boolean isActive = true;

    private LocalDateTime checkInTime;
    private LocalDateTime lastActiveTime;
    private OnlineUser onlineUser;

    public String getId() {
        return id;
    }

    public OnlineDevice setId(String id) {
        this.id = id;
        return this;
    }

    public String getOs() {
        return os;
    }

    public OnlineDevice setOs(String os) {
        this.os = os;
        return this;
    }

    public String getBrowser() {
        return browser;
    }

    public OnlineDevice setBrowser(String browser) {
        this.browser = browser;
        return this;
    }

    public boolean isActive() {
        return isActive;
    }

    public OnlineDevice setActive(boolean active) {
        isActive = active;
        return this;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public OnlineDevice setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
        return this;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    public OnlineDevice setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
        return this;
    }

    public Duration getDuration() {
        return Duration.between(checkInTime, lastActiveTime);
    }

    @JsonIgnore
    public OnlineUser getOnlineUser() {
        return onlineUser;
    }

    public OnlineDevice setOnlineUser(OnlineUser onlineUser) {
        this.onlineUser = onlineUser;
        return this;
    }
}
