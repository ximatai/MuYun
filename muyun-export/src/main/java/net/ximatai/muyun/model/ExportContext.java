package net.ximatai.muyun.model;

import java.util.List;
import java.util.Map;

/**
 * 导出上下文，封装导出所需的所有信息
 */
public class ExportContext {

    private List<ExportColumn> columns;     // 导出列配置
    private PageResult<Map> pageResult;     // 查询结果（包含数据）
    private int batchSize;                   // 批次大小（用于分批处理，减少内存压力）
    private String fileName;                 // 文件名（不含扩展名）

    public ExportContext() {
        this.batchSize = 1000; // 默认每批处理1000条
    }

    public ExportContext(List<ExportColumn> columns, PageResult<Map> pageResult) {
        this.columns = columns;
        this.pageResult = pageResult;
        this.batchSize = 1000;
    }

    public ExportContext(List<ExportColumn> columns, PageResult<Map> pageResult, int batchSize) {
        this.columns = columns;
        this.pageResult = pageResult;
        this.batchSize = batchSize;
    }

    public static ExportContext of(List<ExportColumn> columns, PageResult<Map> pageResult) {
        return new ExportContext(columns, pageResult);
    }

    public List<ExportColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<ExportColumn> columns) {
        this.columns = columns;
    }

    public PageResult<Map> getPageResult() {
        return pageResult;
    }

    public void setPageResult(PageResult<Map> pageResult) {
        this.pageResult = pageResult;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

