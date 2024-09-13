package net.ximatai.muyun.model;

import java.util.List;

public class PageResult<T> {
    private List<T> list;
    private long total;
    private long size;
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
