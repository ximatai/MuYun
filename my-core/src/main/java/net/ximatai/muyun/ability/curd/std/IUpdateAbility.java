package net.ximatai.muyun.ability.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IDatabaseAbility;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.database.exception.MyDatabaseException;

import java.util.Map;

public interface IUpdateAbility extends IDatabaseAbility, IMetadataAbility {

    @POST
    @Path("/update/{id}")
    @Transactional
    default Integer update(@PathParam("id") String id, Map body) {
        body.put("id", id);
        Integer num = getDatabase().update(getUpdateSql(body), body);
        if (num == 0) {
            throw new MyDatabaseException(MyDatabaseException.Type.DATA_NOT_FOUND);
        }
        return num;
    }

}
