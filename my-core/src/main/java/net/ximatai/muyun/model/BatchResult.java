package net.ximatai.muyun.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "数据变更结果")
public class BatchResult {
    @Schema(description = "新增数量")
    private int create;
    @Schema(description = "修改数量")
    private int update;
    @Schema(description = "删除数量")
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
