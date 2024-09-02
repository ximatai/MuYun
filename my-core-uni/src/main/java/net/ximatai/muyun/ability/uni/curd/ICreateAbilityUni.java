package net.ximatai.muyun.ability.uni.curd;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.uni.IDatabaseUniAbility;

import java.util.Map;

public interface ICreateAbilityUni extends IDatabaseUniAbility, IMetadataAbility {

    @POST
    @Path("/create")
    default Uni<String> create(Map body) {
        return getDatabase().insertItem(getMainTable(), body);
    }

}
