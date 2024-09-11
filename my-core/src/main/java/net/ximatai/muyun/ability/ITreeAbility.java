package net.ximatai.muyun.ability;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.util.TreeBuilder;

import java.util.List;
import java.util.Objects;

public interface ITreeAbility extends ISelectAbility, IMetadataAbility {

    default String getParentKeyColumn() {
        return "pid";
    }

    default String getLabelColumn() {
        if (getDBTable().contains("v_name")) {
            return "v_name";
        }
        return null;
    }

    @GET
    @Path("/tree")
    default List<TreeNode> tree(@QueryParam("rootID") String rootID,
                                @QueryParam("showMe") Boolean showMe,
                                @QueryParam("labelColumn") String labelColumn,
                                @QueryParam("maxLevel") Integer maxLevel
    ) {
        if (showMe == null) {
            showMe = true;
        }
        if (maxLevel == null) {
            maxLevel = Integer.MAX_VALUE;
        }
        if (labelColumn == null) {
            labelColumn = getLabelColumn();
        }
        Objects.requireNonNull(labelColumn);

        List list = this.view(null, null, true, null).getList();

        return TreeBuilder.build(getPK(), getParentKeyColumn(), list, rootID, showMe, labelColumn, maxLevel);
    }

}
