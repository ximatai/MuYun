package net.ximatai.muyun.ability.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IDataBroadcastAbility;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.ability.ISecurityAbility;
import net.ximatai.muyun.model.DataChangeChannel;
import net.ximatai.muyun.model.IRuntimeUser;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ICreateAbility extends IDatabaseAbilityStd, IMetadataAbility {

    default void afterCreate(String id) {

    }

    @POST
    @Path("/create")
    @Transactional
    @Operation(summary = "新增数据", description = "返回新增数据ID")
    default String create(Map body) {
        HashMap map = new HashMap<>(body);
        fitOutDefaultValue(map);

        if (this instanceof IDataCheckAbility dataCheckAbility) {
            dataCheckAbility.check(body, true);
        }

        if (this instanceof ISecurityAbility securityAbility) {
            securityAbility.signAndEncrypt(map);
        }

        String main = getDB().insertItem(getSchemaName(), getMainTable(), map);

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

        afterCreate(main);
        return main;
    }

    default void fitOutDefaultValue(Map body) {
        if (!body.containsKey("t_create")) {
            body.put("t_create", LocalDateTime.now());
        }

        if (this instanceof IRuntimeAbility runtimeAbility) {
            String moduleID = runtimeAbility.getApiRequest().getModuleID();
            IRuntimeUser user = runtimeAbility.getApiRequest().getUser();
            body.put("id_at_auth_user__create", user.getId());
            body.put("id_at_auth_user__perms", user.getId());
            body.put("id_at_org_department__perms", user.getDepartmentId());
            body.put("id_at_org_organization__perms", user.getOrganizationId());
            body.put("id_at_app_module__perms", moduleID);
        }
    }

}
