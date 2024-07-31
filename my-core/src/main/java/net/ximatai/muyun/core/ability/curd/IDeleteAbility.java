package net.ximatai.muyun.core.ability.curd;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import net.ximatai.muyun.core.ability.IDatabaseAbility;
import net.ximatai.muyun.core.ability.IMetadataAbility;

import java.util.Map;

public interface IDeleteAbility extends IDatabaseAbility, IMetadataAbility {

    @GET
    @Path("/delete/{id}")
    @Transactional
    default Integer delete(@PathParam("id") String id) {
        Integer num = getDatabase().delete(getDeleteSql(), Map.of("id", id));
        assert num == 1;
        return num;
    }
}
