package net.ximatai.muyun.adaptor;

import jakarta.ws.rs.core.StreamingOutput;
import net.ximatai.muyun.model.ExportContext;

public interface IExportAdaptor {
    /**
     * 获取导出类型的显示名称
     */
    String getName();

    /**
     * 获取导出类型标识
     */
    String getType();

    /**
     * 获取文件扩展名（包含点号，如 ".csv"）
     */
    String getFileExtension();

    /**
     * 获取 MIME 类型
     */
    String getMimeType();

    /**
     * 执行导出操作
     *
     * @param context 导出上下文
     * @return StreamingOutput 流式输出
     */
    StreamingOutput export(ExportContext context);
}
