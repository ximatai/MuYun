package net.ximatai.muyun.model;

import java.util.List;
import java.util.Map;

/**
 * 导出上下文，封装导出所需的所有信息
 */
public class ExportContext {
    private List<ExportColumn> columns;     // 导出列配置
    private List<Map> items;     // 查询结果（包含数据）
    private int batchSize;                   // 批次大小（用于分批处理，减少内存压力）
    private String fileName;                 // 文件名（不含扩展名）

    public ExportContext() {
        this.batchSize = 1000; // 默认每批处理1000条
    }

    public ExportContext(List<ExportColumn> columns, List<Map> items) {
        this.columns = columns;
        this.items = items;
        this.batchSize = 1000;
    }

    public ExportContext(List<ExportColumn> columns, List<Map> items, int batchSize) {
        this.columns = columns;
        this.items = items;
        this.batchSize = batchSize;
    }

    public static ExportContext of(List<ExportColumn> columns, List<Map> items) {
        return new ExportContext(columns, items);
    }

    public List<ExportColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<ExportColumn> columns) {
        this.columns = columns;
    }

    public List<Map> getItems() {
        return items;
    }

    public void setItems(List<Map> items) {
        this.items = items;
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

