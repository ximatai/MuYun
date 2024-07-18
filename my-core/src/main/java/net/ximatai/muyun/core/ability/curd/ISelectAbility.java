package net.ximatai.muyun.core.ability.curd;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import java.util.Map;

public interface ISelectAbility {

    @GET
    @Path("/view/{id}")
    default Map<String, Object> view(@PathParam("id") String id) {
        return Map.of("hello", id);
    }
}
