package net.ximatai.muyun.ability.curd.uni;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IDatabaseUniAbility;
import net.ximatai.muyun.ability.IMetadataAbility;

import java.util.Map;

public interface ICreateAbilityUni extends IDatabaseUniAbility, IMetadataAbility {

    //TODO 与ICreateAbility 函数内容过于雷同，考虑合并
    @POST
    @Path("/create")
    default Uni<String> create(Map body) {
        return getDatabase().create(getInsertSql(body), body, getPK());
    }

}
