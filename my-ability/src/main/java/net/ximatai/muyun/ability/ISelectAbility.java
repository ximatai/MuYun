package net.ximatai.muyun.ability;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import java.util.Map;

public interface ISelectAbility extends IDatabaseAbility, IMetadataAbility {

    @GET
    @Path("/view/{id}")
    default Map<String, Object> view(@PathParam("id") String id) {
        return getDatabase().row(getSelectOneRowSql(), Map.of("id", id));
    }
}
