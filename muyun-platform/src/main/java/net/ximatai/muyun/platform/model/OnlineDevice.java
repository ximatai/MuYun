package net.ximatai.muyun.platform.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class OnlineDevice {
    private String id;
    private String os;
    private String browser;
    private Duration onlineDuration;
    private boolean isActive;
    private LocalDateTime lastTouch;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public Duration getOnlineDuration() {
        return onlineDuration;
    }

    public void setOnlineDuration(Duration onlineDuration) {
        this.onlineDuration = onlineDuration;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getLastTouch() {
        return lastTouch;
    }

    public void setLastTouch(LocalDateTime lastTouch) {
        this.lastTouch = lastTouch;
    }
}
