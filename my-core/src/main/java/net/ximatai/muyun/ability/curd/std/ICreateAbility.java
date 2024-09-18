package net.ximatai.muyun.ability.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IDataBroadcastAbility;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.ISecurityAbility;
import net.ximatai.muyun.model.DataChangeChannel;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ICreateAbility extends IDatabaseAbilityStd, IMetadataAbility {

    @POST
    @Path("/create")
    @Transactional
    default String create(Map body) {
        HashMap map = new HashMap<>(body);
        fitOutDefaultValue(map);

        if (this instanceof IDataCheckAbility dataCheckAbility) {
            dataCheckAbility.check(body, true);
        }

        if (this instanceof ISecurityAbility securityAbility) {
            securityAbility.signAndEncrypt(map);
        }

        String main = getDatabase().insertItem(getSchemaName(), getMainTable(), map);

        if (this instanceof IChildrenAbility childrenAbility) {
            childrenAbility.getChildren().forEach(childTableInfo -> {
                String childAlias = childTableInfo.getChildAlias();
                if (body.containsKey(childAlias) && body.get(childAlias) instanceof List<?> list) {
                    childrenAbility.putChildTableList(main, childAlias, list);
                }
            });
        }

        if (this instanceof IDataBroadcastAbility dataBroadcastAbility) {
            dataBroadcastAbility.broadcast(DataChangeChannel.Type.CREATE, main);
        }

        return main;
    }

    default void fitOutDefaultValue(Map body) {
        if (!body.containsKey("t_create")) {
            body.put("t_create", LocalDateTime.now());
        }
    }

}
