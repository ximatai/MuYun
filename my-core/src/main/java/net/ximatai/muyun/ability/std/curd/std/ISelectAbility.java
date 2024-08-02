package net.ximatai.muyun.ability.std.curd.std;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.std.IDatabaseAbility;
import net.ximatai.muyun.ability.std.IMetadataAbility;

import java.util.Map;

public interface ISelectAbility extends IDatabaseAbility, IMetadataAbility {

    @GET
    @Path("/view/{id}")
    default Map<String, Object> view(@PathParam("id") String id) {
        return getDatabase().row(getSelectOneRowSql(), Map.of("id", id));
    }
}
