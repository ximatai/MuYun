package net.ximatai.muyun.ability;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.database.exception.MyDatabaseException;

import java.util.Map;

public interface IDeleteAbility extends IDatabaseAbility, IMetadataAbility {

    @GET
    @Path("/delete/{id}")
    @Transactional
    default Integer delete(@PathParam("id") String id) {
        Integer num = getDatabase().delete(getDeleteSql(), Map.of("id", id));
        if (num == 0) {
            throw new MyDatabaseException(MyDatabaseException.Type.DATA_NOT_FOUND);
        }

        return num;
    }
}
