package net.ximatai.muyun.adaptor.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;
import net.ximatai.muyun.adaptor.IExportAdaptor;
import net.ximatai.muyun.model.ExportColumn;
import net.ximatai.muyun.model.ExportContext;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 导出适配器
 */
@ApplicationScoped
public class JSONExportAdaptor implements IExportAdaptor {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public String getType() {
        return "json";
    }

    @Override
    public String getFileExtension() {
        return ".json";
    }

    @Override
    public String getMimeType() {
        return "application/json;charset=utf-8";
    }

    @Override
    public StreamingOutput export(ExportContext context) {
        return output -> {
            try (BufferedOutputStream bufferedOutput = new BufferedOutputStream(output)) {

                List<Map> dataList = context.getPageResult().getList();
                List<ExportColumn> columns = context.getColumns();

                // 开始 JSON 数组
                bufferedOutput.write("[".getBytes(StandardCharsets.UTF_8));

                // 如果没有数据，返回空数组
                if (dataList == null || dataList.isEmpty()) {
                    bufferedOutput.write("]".getBytes(StandardCharsets.UTF_8));
                    bufferedOutput.flush();
                    return;
                }

                // 分批处理数据，减少内存压力
                int batchSize = context.getBatchSize();
                int totalSize = dataList.size();
                boolean isFirst = true;

                for (int i = 0; i < totalSize; i += batchSize) {
                    int endIndex = Math.min(i + batchSize, totalSize);
                    List<Map> batch = dataList.subList(i, endIndex);

                    // 写入当前批次的数据
                    for (Map row : batch) {
                        // 添加逗号分隔符（第一个元素除外）
                        if (!isFirst) {
                            bufferedOutput.write(",".getBytes(StandardCharsets.UTF_8));
                        }
                        isFirst = false;

                        // 根据列配置构建输出对象
                        Map<String, Object> outputRow;
                        if (columns != null && !columns.isEmpty()) {
                            // 使用 LinkedHashMap 保持列顺序
                            outputRow = new LinkedHashMap<>();
                            for (ExportColumn column : columns) {
                                Object value = row.get(column.getFieldName());
                                String formattedValue = column.format(value);
                                outputRow.put(column.getFieldName(), formattedValue);
                            }
                        } else {
                            // 没有配置列，导出所有字段
                            outputRow = row;
                        }

                        // 将对象序列化为 JSON 并写入
                        byte[] jsonBytes = objectMapper.writeValueAsBytes(outputRow);
                        bufferedOutput.write(jsonBytes);
                    }

                    // 刷新缓冲区，及时写入输出流
                    bufferedOutput.flush();
                }

                // 结束 JSON 数组
                bufferedOutput.write("]".getBytes(StandardCharsets.UTF_8));
                bufferedOutput.flush();

            } catch (IOException e) {
                throw new RuntimeException("JSON 导出失败", e);
            }
        };
    }
}

