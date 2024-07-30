package net.ximatai.muyun.core.ability.curd;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.core.ability.IDatabaseAbility;
import net.ximatai.muyun.core.ability.IMetadataAbility;

import java.util.Map;

public interface IDeleteAbility extends IDatabaseAbility, IMetadataAbility {

    @GET
    @Path("/delete/{id}")
    @Transactional
    default Integer update(@PathParam("id") String id) {
        Integer num = getDatabase().delete(getDeleteSql(), Map.of("id", id));
        assert num == 1;
        return num;
    }
}
