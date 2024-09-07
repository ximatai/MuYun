package net.ximatai.muyun.ability.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.ISoftDeleteAbility;

import java.time.LocalDateTime;
import java.util.HashMap;

public interface IDeleteAbility extends IDatabaseAbilityStd, IMetadataAbility {

    @GET
    @Path("/delete/{id}")
    @Transactional
    default Integer delete(@PathParam("id") String id) {
        if (this instanceof ISoftDeleteAbility ability) {
            HashMap map = new HashMap();
            map.put(getPK(), id);
            map.put(ability.getSoftDeleteColumn().getName(), true);
            map.put("t_delete", LocalDateTime.now());

            return getDatabase().updateItem(getSchemaName(), getMainTable(), map);
        } else {
            return getDatabase().deleteItem(getSchemaName(), getMainTable(), id);
        }
    }
}
