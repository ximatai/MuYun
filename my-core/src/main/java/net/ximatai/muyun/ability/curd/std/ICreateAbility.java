package net.ximatai.muyun.ability.curd.std;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IMetadataAbility;

import java.util.Map;

public interface ICreateAbility extends IDatabaseAbilityStd, IMetadataAbility {

    @POST
    @Path("/create")
    default String create(Map body) {
        return getDatabase().insertItem(getSchemaName(), getMainTable(), body);
    }

}
