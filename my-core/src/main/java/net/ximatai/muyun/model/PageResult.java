package net.ximatai.muyun.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "分页结果")
public class PageResult<T> {
    @Schema(description = "数据列表")
    private List<T> list;
    @Schema(description = "总数")
    private long total;
    @Schema(description = "分页大小")
    private long size;
    @Schema(description = "页码")
    private int page;

    public PageResult() {
    }

    public PageResult(List<T> list, long total, long size, int page) {
        this.list = list;
        this.total = total;
        this.size = size;
        this.page = page;
    }

    public List<T> getList() {
        return list;
    }

    public long getTotal() {
        return total;
    }

    public long getSize() {
        return size;
    }

    public int getPage() {
        return page;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
