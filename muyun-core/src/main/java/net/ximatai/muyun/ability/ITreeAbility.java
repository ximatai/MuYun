package net.ximatai.muyun.ability;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.util.TreeBuilder;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.List;
import java.util.Objects;

/**
 * 树型数据能力
 */
public interface ITreeAbility extends ISelectAbility, ISortAbility, IMetadataAbility, ILabelAbility {

    default Column getParentKeyColumn() {
        return Column.TREE_PID.setDefaultValue(TreeBuilder.ROOT_PID);
    }

    @GET
    @Path("/tree")
    @Operation(summary = "按树结构获取数据")
    default List<TreeNode> tree(@Parameter(description = "指定根节点id") @QueryParam("rootID") String rootID,
                                @Parameter(description = "是否展示指定的根节点") @QueryParam("showMe") Boolean showMe,
                                @Parameter(description = "指定作为 Label 的列") @QueryParam("labelColumn") String labelColumn,
                                @Parameter(description = "树的最大层级") @QueryParam("maxLevel") Integer maxLevel
    ) {
        if (showMe == null) {
            showMe = true;
        }
        if (rootID == null) {
            showMe = false;
        }
        if (maxLevel == null) {
            maxLevel = Integer.MAX_VALUE;
        }
        if (labelColumn == null) {
            labelColumn = getLabelColumn();
        }
        Objects.requireNonNull(labelColumn);

        List list = this.view(null, null, true, null).getList();

        if(list.isEmpty()) {
            return List.of();
        }

        return TreeBuilder.build(getPK(), getParentKeyColumn().getName(), list, rootID, showMe, labelColumn, maxLevel);
    }

}
