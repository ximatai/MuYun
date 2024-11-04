package net.ximatai.muyun.ability;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.model.code.CodeGenerateConfig;
import net.ximatai.muyun.model.code.ICodePart;
import net.ximatai.muyun.model.code.SerialCodePart;
import net.ximatai.muyun.model.code.TransformCodePart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ICodeGenerateAbility extends IMetadataAbility, IDatabaseAbilityStd {

    CodeGenerateConfig getCodeGenerateConfig();

    default String getCodeColumn() {
        return "v_code";
    }

    @GET
    @Path("/generateCode")
    default String generateCode() {
        return generate(null, 1).getFirst();
    }

    @POST
    @Path("/generateCode")
    default String generateCode(Map body) {
        return generate(body, 1).getFirst();
    }

    default List<String> generate(List<Map> list) {
        return generate(list.getFirst(), list.size());
    }

    default List<String> generate(Map data, int size) {
        CodeGenerateConfig config = getCodeGenerateConfig();
        List<ICodePart> codePartList = config.getCodePartList();

        // 设置 TransformCodePart 的数据
        for (ICodePart part : codePartList) {
            if (part instanceof TransformCodePart codePart) {
                codePart.setData(data);
            }
        }

        // 检查是否存在 SerialCodePart
        boolean hasSerial = codePartList.getLast() instanceof SerialCodePart;

        if (!hasSerial) { // 不存在流水号情况
            return List.of(
                codePartList.stream()
                    .map(ICodePart::varchar)
                    .collect(Collectors.joining())
            );
        }

        // 生成前缀
        String prefix = "";
        if (codePartList.size() > 1) { // codePartList 中不止一列，且最后一列是流水号
            prefix = codePartList.subList(0, codePartList.size() - 1).stream()
                .map(ICodePart::varchar)
                .collect(Collectors.joining());
        }

        long nowSerial = 0;

        String codeColumn = getCodeColumn();

        // 查询数据库，获取当前流水号的最大值
        Map<String, Object> row = getDB().row("select %s from %s.%s where %s like ? order by %s desc limit 1 "
            .formatted(codeColumn, getSchemaName(), getMainTable(), codeColumn, codeColumn), prefix + "%");

        if (row != null) {
            String nowCode = (String) row.get(codeColumn);
            nowCode = nowCode.substring(prefix.length());
            nowSerial = Long.parseLong(nowCode);
        }

        // 返回生成的代码
        SerialCodePart serialPart = (SerialCodePart) codePartList.getLast();
        serialPart.setBase(nowSerial);

        List<String> codes = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            codes.add(prefix + serialPart.varchar());
        }

        return codes;
    }

}
