package net.ximatai.muyun.ability.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.ISecurityAbility;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IUpdateAbility extends IDatabaseAbilityStd, IMetadataAbility {

    @POST
    @Path("/update/{id}")
    @Transactional
    default Integer update(@PathParam("id") String id, Map body) {
        HashMap map = new HashMap(body);
        map.put(getPK(), id);
        map.put("t_update", LocalDateTime.now());
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

        return getDatabase().updateItem(getSchemaName(), getMainTable(), map);
    }

}
