package net.ximatai.muyun.adaptor.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.StreamingOutput;
import net.ximatai.muyun.adaptor.IExportAdaptor;
import net.ximatai.muyun.model.ExportColumn;
import net.ximatai.muyun.model.ExportContext;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Excel (XSSF) 导出适配器
 */
@ApplicationScoped
public class ExcelExportAdaptor implements IExportAdaptor {
    // 内存中保持的行数，超过后会刷新到磁盘
    // 100 是推荐值：既保证性能，又控制内存
    // 可根据实际情况调整：数据简单可增大，数据复杂可减小
    private static final int WINDOW_SIZE = 100;

    @Override
    public String getName() {
        return "Excel";
    }

    @Override
    public String getType() {
        return "excel";
    }

    @Override
    public String getFileExtension() {
        return ".xlsx";
    }

    @Override
    public String getMimeType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public StreamingOutput export(ExportContext context) {
        return output -> {
            try (SXSSFWorkbook workbook = new SXSSFWorkbook(WINDOW_SIZE)) {
                // 压缩临时文件，进一步减少磁盘占用（使用 GZIP 压缩）
                workbook.setCompressTempFiles(true);

                Sheet sheet = workbook.createSheet("Sheet1");

                List<Map> dataList = context.getItems();
                List<ExportColumn> columns = context.getColumns();

                // 如果没有配置列，则从第一行数据推断
                if (columns == null || columns.isEmpty()) {
                    if (dataList != null && !dataList.isEmpty()) {
                        Map firstRow = dataList.get(0);
                        columns = firstRow.keySet().stream()
                            .map(key -> ExportColumn.of(key.toString(), key.toString()))
                            .toList();
                    } else {
                        // 没有数据，创建空工作簿
                        workbook.write(output);
                        workbook.close();
                        return;
                    }
                }

                // 创建样式
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle dataStyle = createDataStyle(workbook);

                // 写入表头
                writeHeader(sheet, columns, headerStyle);

                // 如果没有数据，只输出表头
                if (dataList == null || dataList.isEmpty()) {
                    workbook.write(output);
                    workbook.close();
                    return;
                }

                // 分批处理数据，减少内存压力
                int batchSize = context.getBatchSize();
                int totalSize = dataList.size();
                int currentRowNum = 1; // 从第1行开始（第0行是表头）

                for (int i = 0; i < totalSize; i += batchSize) {
                    int endIndex = Math.min(i + batchSize, totalSize);
                    List<Map> batch = dataList.subList(i, endIndex);

                    // 写入当前批次的数据
                    for (Map rowData : batch) {
                        writeRow(sheet, currentRowNum++, columns, rowData, dataStyle);
                    }
                }

                // 自动调整列宽（仅针对表头和前100行数据）
                autoSizeColumns(sheet, columns, Math.min(100, currentRowNum));

                // 写入输出流
                workbook.write(output);

                // 清理临时文件
                workbook.close();
            } catch (IOException e) {
                throw new RuntimeException("Excel 导出失败", e);
            }
        };
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置自动换行
        style.setWrapText(false);

        return style;
    }

    /**
     * 写入表头
     */
    private void writeHeader(Sheet sheet, List<ExportColumn> columns, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns.get(i).getDisplayName());
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 写入数据行
     */
    private void writeRow(Sheet sheet, int rowNum, List<ExportColumn> columns, Map rowData, CellStyle dataStyle) {
        Row row = sheet.createRow(rowNum);

        for (int i = 0; i < columns.size(); i++) {
            Cell cell = row.createCell(i);
            ExportColumn column = columns.get(i);
            Object value = rowData.get(column.getFieldName());
            String formattedValue = column.format(value);

            // 尝试将数值型字符串转换为数字
            if (formattedValue != null && !formattedValue.isEmpty()) {
                try {
                    double numValue = Double.parseDouble(formattedValue);
                    cell.setCellValue(numValue);
                } catch (NumberFormatException e) {
                    // 不是数字，作为字符串处理
                    cell.setCellValue(formattedValue);
                }
            } else {
                cell.setCellValue("");
            }

            cell.setCellStyle(dataStyle);
        }
    }

    /**
     * 自动调整列宽
     * 只对前面的行进行计算，避免大数据量时性能问题
     */
    private void autoSizeColumns(Sheet sheet, List<ExportColumn> columns, int maxRows) {
        for (int i = 0; i < columns.size(); i++) {
            try {
                sheet.autoSizeColumn(i);
                // 在自动计算的基础上再增加一点宽度
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 512); // 增加约2个字符的宽度
            } catch (Exception e) {
                // 自动调整失败时使用默认宽度
                sheet.setColumnWidth(i, 20 * 256); // 默认20个字符宽度
            }
        }
    }
}

