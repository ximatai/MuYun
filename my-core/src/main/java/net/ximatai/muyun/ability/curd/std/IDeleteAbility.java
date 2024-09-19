package net.ximatai.muyun.ability.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IDataBroadcastAbility;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.ISoftDeleteAbility;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.DataChangeChannel;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public interface IDeleteAbility extends IDatabaseAbilityStd, IMetadataAbility {

    @GET
    @Path("/delete/{id}")
    @Transactional
    @Operation(summary = "删除数据", description = "返回被删除数据的数量，正常为1")
    default Integer delete(@PathParam("id") String id) {
        int result;

        if (this instanceof IChildrenAbility ability) { // 如果带子表，考虑级联删除
            ability.getChildren().stream().filter(ChildTableInfo::isAutoDelete).forEach(childTableInfo -> {
                ability.putChildTableList(id, childTableInfo.getChildAlias(), List.of()); //对应即可清空子表
            });
        }

        if (this instanceof ISoftDeleteAbility ability) {
            HashMap map = new HashMap();
            map.put(getPK(), id);
            map.put(ability.getSoftDeleteColumn().getName(), true);
            map.put("t_delete", LocalDateTime.now());

            result = getDatabase().updateItem(getSchemaName(), getMainTable(), map);
        } else {
            result = getDatabase().deleteItem(getSchemaName(), getMainTable(), id);
        }

        if (this instanceof IDataBroadcastAbility dataBroadcastAbility) {
            dataBroadcastAbility.broadcast(DataChangeChannel.Type.DELETE, id);
        }

        return result;
    }
}
