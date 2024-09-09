package net.ximatai.muyun.ability.curd.std;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IMetadataAbility;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public interface ICreateAbility extends IDatabaseAbilityStd, IMetadataAbility {

    @POST
    @Path("/create")
    default String create(Map body) {
        HashMap map = new HashMap(body);
        fitOutDefaultValue(map);
        return getDatabase().insertItem(getSchemaName(), getMainTable(), map);
    }

    default void fitOutDefaultValue(Map body) {
        if (!body.containsKey("t_create")) {
            body.put("t_create", LocalDateTime.now());
        }
    }

}
