package net.ximatai.muyun.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IExportAbility;
import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.model.ExportColumn;
import net.ximatai.muyun.model.QueryItem;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * 导出功能使用示例
 *
 * 使用方式：
 * 1. 实现 IExportAbility 接口
 * 2. 可选：覆盖 getExportColumns() 自定义导出列
 * 3. 可选：覆盖 getExportFileName() 自定义文件名
 * 4. 可选：覆盖 filterExportData() 导出前过滤数据
 *
 * API 调用：
 * - GET  /api/example/exportTypes    获取支持的导出类型
 * - POST /api/example/export/csv     导出为 CSV
 */
@Path("/api/example")
public class ExportExampleController implements IExportAbility {

    @Inject
    IDatabaseOperations databaseOperations;

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @Override
    public String getSchemaName() {
        return "public";
    }

    @Override
    public String getMainTable() {
        return "t_example";
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("v_name").setLabel("名称"),
            QueryItem.of("v_description").setLabel("描述")
        );
    }

    /**
     * 自定义导出列配置
     * 可以指定导出哪些列、列的显示名称、以及值的格式化方式
     */
    @Override
    public List<ExportColumn> getExportColumns(String type) {
        return List.of(
            ExportColumn.of("id", "ID"),
            ExportColumn.of("v_name", "名称"),
            ExportColumn.of("v_description", "描述"),

            // 时间格式化
            ExportColumn.of("t_create", "创建时间", value -> {
                if (value instanceof Timestamp) {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
                }
                return value != null ? value.toString() : "";
            }),

            // 布尔值转换
            ExportColumn.of("b_active", "状态", value -> {
                if (value instanceof Boolean) {
                    return ((Boolean) value) ? "启用" : "禁用";
                }
                return "";
            })
        );
    }

    /**
     * 自定义导出文件名
     */
    @Override
    public String getExportFileName(String type) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return "示例数据_" + sdf.format(new java.util.Date());
    }

    /**
     * 导出前的数据过滤和处理
     * 可以在这里移除敏感字段、添加计算字段等
     */
    @Override
    public void filterExportData(List<Map> data) {
        data.forEach(row -> {
            // 移除不需要导出的字段
            row.remove("v_password");

            // 添加计算字段
            String name = (String) row.get("v_name");
            String desc = (String) row.get("v_description");
            row.put("v_full_info", name + " - " + desc);
        });
    }
}

