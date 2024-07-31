package net.ximatai.muyun.ability;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.util.Map;

public interface ICreateAbility extends IDatabaseAbility, IMetadataAbility {

    @POST
    @Path("/create")
    default String create(Map body) {
        return getDatabase().insert(getInsertSql(body), body);
    }

}
