package net.ximatai.muyun.domain;

import java.util.List;

public class PageResult<T> {
    private List<T> list;
    private long total;
    private int limit;
    private int page;

    public PageResult() {
    }

    public PageResult(List<T> list, long total, int limit, int page) {
        this.list = list;
        this.total = total;
        this.limit = limit;
        this.page = page;
    }

    public List<T> getList() {
        return list;
    }

    public long getTotal() {
        return total;
    }

    public int getLimit() {
        return limit;
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

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
