package net.ximatai.muyun.ability.std.curd.std;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.std.IDatabaseAbility;
import net.ximatai.muyun.ability.std.IMetadataAbility;

import java.util.Map;

public interface ICreateAbility extends IDatabaseAbility, IMetadataAbility {

    @POST
    @Path("/create")
    default String create(Map body) {
        return getDatabase().insert(getInsertSql(body), body);
    }

}
