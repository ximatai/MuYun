package net.ximatai.muyun.ability.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IDataBroadcastAbility;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.ability.ISecurityAbility;
import net.ximatai.muyun.model.DataChangeChannel;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IUpdateAbility extends IDatabaseAbilityStd, IMetadataAbility {

    default void afterUpdate(String id) {

    }

    @POST
    @Path("/update/{id}")
    @Transactional
    @Operation(summary = "修改数据", description = "返回被修改数据的数量，正常为1")
    default Integer update(@PathParam("id") String id, Map body) {
        HashMap map = new HashMap(body);
        map.put(getPK(), id);
        map.put("t_update", LocalDateTime.now());

        if (this instanceof IRuntimeAbility runtimeAbility) {
            String userID = runtimeAbility.getUser().getId();
            map.put("id_at_auth_user__update", userID);
        }

        if (this instanceof IDataCheckAbility dataCheckAbility) {
            dataCheckAbility.check(map, true);
        }

        if (this instanceof ISecurityAbility securityAbility) {
            securityAbility.signAndEncrypt(map);
        }

        if (this instanceof IChildrenAbility childrenAbility) {
            childrenAbility.getChildren().forEach(childTableInfo -> {
                String childAlias = childTableInfo.getChildAlias();
                if (body.containsKey(childAlias) && body.get(childAlias) instanceof List<?> list) {
                    childrenAbility.putChildTableList(id, childAlias, list);
                }
            });
        }

        int result = getDB().updateItem(getSchemaName(), getMainTable(), map);

        if (this instanceof IDataBroadcastAbility dataBroadcastAbility) {
            dataBroadcastAbility.broadcast(DataChangeChannel.Type.UPDATE, id);
        }

        afterUpdate(id);

        return result;
    }

}
