package net.ximatai.muyun.ability.curd.std;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IDataBroadcastAbility;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.ability.ISecurityAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.core.exception.MuYunException;
import net.ximatai.muyun.database.core.exception.MuYunDatabaseException;
import net.ximatai.muyun.model.DataChangeChannel;
import net.ximatai.muyun.model.TreeNode;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 修改数据的能力
 */
public interface IUpdateAbility extends IDatabaseAbilityStd, IMetadataAbility {

    /**
     * @deprecated 请使用带 Map 参数的 beforeUpdate 方法，这个方法将在未来的版本中移除
     */
    @Deprecated(forRemoval = true)
    default void beforeUpdate(String id) {

    }

    default void beforeUpdate(String id, Optional<Map> body) {

    }

    default void afterUpdate(String id) {

    }

    @POST
    @Path("/update/{id}")
    @Transactional
    @Operation(summary = "修改数据", description = "返回被修改数据的数量，正常为1")
    default Integer update(@PathParam("id") String id, Map body) {
        beforeUpdate(id);
        beforeUpdate(id, Optional.of(body));
        HashMap map = new HashMap(body);
        map.put(getPK(), id);
        map.put("t_update", LocalDateTime.now());

        if (this instanceof IRuntimeAbility runtimeAbility) {
            String userID = runtimeAbility.getUser().getId();
            map.put("id_at_auth_user__update", userID);
        }

        if (this instanceof ITreeAbility treeAbility) {
            if (id.equals(body.get(treeAbility.getParentKeyColumn().getName()))) {
                throw new MuYunException("树结构的父节点不能是它自身");
            }

            String pid = (String) body.get(treeAbility.getParentKeyColumn().getName());
            if (pid != null) {
                List<TreeNode> tree = treeAbility.tree(id, true, null, null);
                if (isIdInTree(tree, pid)) {
                    throw new MuYunException("不能编辑该节点的父节点为其子孙节点");
                }
            }

        }

        if (this instanceof IDataCheckAbility dataCheckAbility) {
            dataCheckAbility.check(map, true);
            dataCheckAbility.checkWhenUpdate(id, body);
        }

        if (this instanceof ISecurityAbility securityAbility) {
            securityAbility.signAndEncrypt(map);
        }

        if (this instanceof IChildrenAbility childrenAbility) {
            childrenAbility.getChildren().forEach(childTableInfo -> {
                String childAlias = childTableInfo.getChildAlias();
                if (body.containsKey(childAlias) && body.get(childAlias) instanceof List<?> list) {
                    childrenAbility.putChildTableList(id, childAlias, list);
                }
            });
        }

        int result = getDB().updateItem(getSchemaName(), getMainTable(), map);

        if (result == 0) {
            throw new MuYunDatabaseException(MuYunDatabaseException.Type.DATA_NOT_FOUND);
        }

        if (this instanceof IDataBroadcastAbility dataBroadcastAbility) {
            dataBroadcastAbility.broadcast(DataChangeChannel.Type.UPDATE, id);
        }

        afterUpdate(id);

        return result;
    }

    private boolean isIdInTree(List<? extends TreeNode> tree, String id) {
        for (TreeNode node : tree) {
            if (node.getId().equals(id)) {
                return true;
            } else if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                if (isIdInTree(node.getChildren(), id)) {
                    return true;
                }
            }
        }
        return false;
    }

}
