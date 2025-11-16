package net.ximatai.muyun.ability;

import io.quarkus.arc.Arc;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.adaptor.IExportAdaptor;
import net.ximatai.muyun.core.exception.MuYunException;
import net.ximatai.muyun.model.ExportColumn;
import net.ximatai.muyun.model.ExportContext;
import net.ximatai.muyun.model.ExportTypeItem;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IExportAbility extends IQueryAbility {
    default Instance<IExportAdaptor> getExportAdaptors() {
        return Arc.container().select(IExportAdaptor.class);
    }

    @Path("/exportTypes")
    @GET
    @Operation(summary = "获取支持的导出类型列表")
    default List<ExportTypeItem> getExportTypes() {
        return getExportAdaptors()
            .stream()
            .map(adaptor -> new ExportTypeItem(adaptor.getName(), adaptor.getType()))
            .toList();
    }

    /**
     * 获取导出列配置
     * 子类可以覆盖此方法以自定义导出列
     *
     * @param type 导出类型
     * @return 导出列配置列表
     */
    default List<ExportColumn> getExportColumns(String type) {
        // 默认返回 null，表示导出所有字段
        return null;
    }

    /**
     * 获取导出文件名（不含扩展名）
     * 子类可以覆盖此方法以自定义文件名
     *
     * @param type 导出类型
     * @return 文件名
     */
    default String getExportFileName(String type) {
        return "export_" + System.currentTimeMillis();
    }

    /**
     * 导出前的数据过滤处理
     * 子类可以覆盖此方法以对数据进行预处理
     *
     * @param items 导出数据列表
     */
    default void filterExportData(List<Map> items) {
    }

    @POST
    @Path("/export/{type}")
    @Operation(summary = "导出数据")
    default Response export(
        @Parameter(description = "导出类型", required = true) @PathParam("type") String type,
        @Parameter(description = "排序", example = "t_create,desc") @QueryParam("sort") List<String> sort,
        @RequestBody(description = "查询条件信息") Map<String, Object> queryBody
    ) {
        // 查找对应的导出适配器
        IExportAdaptor adaptor = getExportAdaptors()
            .stream()
            .filter(a -> a.getType().equals(type))
            .findFirst()
            .orElseThrow(() -> new MuYunException("不支持的导出类型: " + type));

        // 查询数据（导出时通常需要所有数据，所以设置 noPage = true）
        List<Map> items = new ArrayList<>(this.view(null, null, true, sort, queryBody).getList());

        // 数据过滤处理
        filterExportData(items);

        // 构建导出上下文
        ExportContext context = new ExportContext();
        context.setColumns(getExportColumns(type));
        context.setItems(items);
        context.setFileName(getExportFileName(type));

        // 执行导出
        StreamingOutput streamingOutput = adaptor.export(context);

        // 构建文件名
        String fileName = context.getFileName() + adaptor.getFileExtension();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        // 返回响应
        return Response.ok(streamingOutput)
            .header("Content-Type", adaptor.getMimeType())
            .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName)
            .build();
    }
}
