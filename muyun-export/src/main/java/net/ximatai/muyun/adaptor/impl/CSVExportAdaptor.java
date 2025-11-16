package net.ximatai.muyun.adaptor.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.StreamingOutput;
import net.ximatai.muyun.adaptor.IExportAdaptor;
import net.ximatai.muyun.model.ExportColumn;
import net.ximatai.muyun.model.ExportContext;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * CSV 导出适配器
 */
@ApplicationScoped
public class CSVExportAdaptor implements IExportAdaptor {

    @Override
    public String getName() {
        return "CSV";
    }

    @Override
    public String getType() {
        return "csv";
    }

    @Override
    public String getFileExtension() {
        return ".csv";
    }

    @Override
    public String getMimeType() {
        return "text/csv;charset=utf-8";
    }

    @Override
    public StreamingOutput export(ExportContext context) {
        return output -> {
            // 使用 BufferedWriter 提高写入效率
            try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(output, StandardCharsets.UTF_8))) {

                // 写入 UTF-8 BOM，确保 Excel 能正确识别中文
                output.write(0xEF);
                output.write(0xBB);
                output.write(0xBF);

                List<Map> dataList = context.getPageResult().getList();
                List<ExportColumn> columns = context.getColumns();

                // 如果没有配置列，则从第一行数据推断
                if (columns == null || columns.isEmpty()) {
                    if (dataList != null && !dataList.isEmpty()) {
                        Map firstRow = dataList.get(0);
                        columns = firstRow.keySet().stream()
                            .map(key -> ExportColumn.of(key.toString(), key.toString()))
                            .toList();
                    } else {
                        // 没有数据，只写入空文件
                        writer.flush();
                        return;
                    }
                }

                // 写入表头
                writeHeader(writer, columns);

                // 如果没有数据，只输出表头
                if (dataList == null || dataList.isEmpty()) {
                    writer.flush();
                    return;
                }

                // 分批处理数据，减少内存压力
                int batchSize = context.getBatchSize();
                int totalSize = dataList.size();

                for (int i = 0; i < totalSize; i += batchSize) {
                    int endIndex = Math.min(i + batchSize, totalSize);
                    List<Map> batch = dataList.subList(i, endIndex);

                    // 写入当前批次的数据
                    for (Map row : batch) {
                        writeRow(writer, columns, row);
                    }

                    // 刷新缓冲区，及时写入输出流
                    writer.flush();

                    // 提示：这里 batch 引用会在下次循环时被覆盖，有助于 GC
                }

                writer.flush();
            }
        };
    }

    /**
     * 写入 CSV 表头
     */
    private void writeHeader(BufferedWriter writer, List<ExportColumn> columns) throws IOException {
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                writer.write(",");
            }
            String displayName = columns.get(i).getDisplayName();
            writer.write(escapeCsvValue(displayName));
        }
        writer.write("\r\n");
    }

    /**
     * 写入 CSV 数据行
     */
    private void writeRow(BufferedWriter writer, List<ExportColumn> columns, Map row) throws IOException {
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                writer.write(",");
            }

            ExportColumn column = columns.get(i);
            Object value = row.get(column.getFieldName());
            String formattedValue = column.format(value);

            writer.write(escapeCsvValue(formattedValue));
        }
        writer.write("\r\n");
    }

    /**
     * 转义 CSV 值
     * 如果值包含逗号、引号或换行符，需要用引号包裹
     * 如果值包含引号，需要将引号转义为两个引号
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }

        // 检查是否需要转义
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");

        if (needsQuotes) {
            // 将引号转义为两个引号
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }

        return value;
    }
}

