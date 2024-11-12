package net.ximatai.muyun.platform.model;

import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MuYunMessage {
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
    private final String url;

    public MuYunMessage(String title, String content, String url) {
        this(title, content, url, LocalDateTime.now());
    }

    public MuYunMessage(String title, String content, String url, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.url = url;
        this.createdAt = createdAt;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("title", title);
        json.put("content", content);
        json.put("createdAt", createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return json;
    }
}
