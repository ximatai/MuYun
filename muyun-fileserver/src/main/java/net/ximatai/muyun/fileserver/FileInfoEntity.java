package net.ximatai.muyun.fileserver;

public class FileInfoEntity {
    String name;
    long size;
    String suffix;
    String uid;
    String time;

    public FileInfoEntity() {
    }

    public FileInfoEntity(String name, long size, String suffix, String uid, String time) {
        this.name = name;
        this.size = size;
        this.suffix = suffix;
        this.uid = uid;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
