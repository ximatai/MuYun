package net.ximatai.muyun.core.ability.curd;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.core.ability.IDatabaseAbility;
import net.ximatai.muyun.core.ability.IMetadataAbility;

import java.util.Map;

public interface IUpdateAbility extends IDatabaseAbility, IMetadataAbility {

    @POST
    @Path("/update/{id}")
    @Transactional
    default Integer update(@PathParam("id") String id, Map body) {
        body.put("id", id);
        Integer num = getDatabase().update(getUpdateSql(body), body);
        assert num == 1;
        return num;
    }

}
