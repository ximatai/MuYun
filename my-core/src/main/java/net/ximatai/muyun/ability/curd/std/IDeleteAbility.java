package net.ximatai.muyun.ability.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IMetadataAbility;

public interface IDeleteAbility extends IDatabaseAbilityStd, IMetadataAbility {

    @GET
    @Path("/delete/{id}")
    @Transactional
    default Integer delete(@PathParam("id") String id) {
        return getDatabase().deleteItem(getMainTable(), id);
    }
}
