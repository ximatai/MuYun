package net.ximatai.muyun.fileserver;

import io.vertx.core.json.JsonObject;

public class FileInfoEntity {
    String name;
    long size;
    String suffix;
    String id;
    String time;

    public FileInfoEntity() {
    }

    public FileInfoEntity(String name, long size, String suffix, String id, String time) {
        this.name = name;
        this.size = size;
        this.suffix = suffix;
        this.id = id;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("name", name)
            .put("size", size)
            .put("suffix", suffix)
            .put("id", id)
            .put("time", time);
        return jsonObject;
    }
}
