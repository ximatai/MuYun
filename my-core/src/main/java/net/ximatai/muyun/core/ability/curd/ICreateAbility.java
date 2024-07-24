package net.ximatai.muyun.core.ability.curd;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.core.ability.IDatabaseAbility;
import net.ximatai.muyun.core.ability.IMetadataAbility;

import java.util.Map;

public interface ICreateAbility extends IDatabaseAbility, IMetadataAbility {

    @POST
    @Path("/create")
    default String create(Map body) {
        return getDatabase().insert(getInsertSql(body), body);
    }

}
