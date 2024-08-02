package net.ximatai.muyun.ability.curd.std;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IDatabaseAbility;
import net.ximatai.muyun.ability.IMetadataAbility;

import java.util.Map;

public interface ISelectAbility extends IDatabaseAbility, IMetadataAbility {

    @GET
    @Path("/view/{id}")
    default Map<String, Object> view(@PathParam("id") String id) {
        return getDatabase().row(getSelectOneRowSql(), Map.of("id", id));
    }
}
