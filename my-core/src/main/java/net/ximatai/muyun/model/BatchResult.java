package net.ximatai.muyun.model;

public class BatchResult {
    private int create;
    private int update;
    private int delete;

    public BatchResult() {
    }

    public BatchResult(int create, int update, int delete) {
        this.create = create;
        this.update = update;
        this.delete = delete;
    }

    public int getCreate() {
        return create;
    }

    public void setCreate(int create) {
        this.create = create;
    }

    public int getUpdate() {
        return update;
    }

    public void setUpdate(int update) {
        this.update = update;
    }

    public int getDelete() {
        return delete;
    }

    public void setDelete(int delete) {
        this.delete = delete;
    }
}
