package net.ximatai.muyun.ability.std.curd.uni;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.std.IDatabaseUniAbility;
import net.ximatai.muyun.ability.std.IMetadataAbility;

import java.util.Map;

public interface ICreateAbilityUni extends IDatabaseUniAbility, IMetadataAbility {

    @POST
    @Path("/create")
    default Uni<String> create(Map body) {
        return getDatabase().insert(getInsertSql(body), body);
    }

}
