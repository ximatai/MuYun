package net.ximatai.muyun.platform.controller;

import jakarta.enterprise.context.ApplicationScoped;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.IDataBroadcastAbility;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.ability.curd.std.IDataCheckAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.exception.MuYunException;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//@Path(BASE_PATH + "/dict")
@ApplicationScoped
public class DictController extends ScaffoldForPlatform implements ITreeAbility, IChildAbility, IReferableAbility, IDataCheckAbility, IQueryAbility, IDataBroadcastAbility {

    @Override
    public String getMainTable() {
        return "app_dict";
    }

    @Override
    public String getKeyColumn() {
        return "v_value";
    }

    @Override
    public ReferenceInfo toReferenceInfo(String foreignKey) {
        String category;
        if (foreignKey.startsWith("dict_")) {
            category = foreignKey.substring("dict_".length());
        } else {
            throw new IllegalArgumentException("foreignKey must start with 'dict_'");
        }

        return this.toReferenceInfo(foreignKey, category).add("v_name", "v_name_at_" + foreignKey);
    }

    public ReferenceInfo toReferenceInfo(String foreignKey, String category) {
        category = category.toLowerCase();
        ReferenceInfo referenceInfo = IReferableAbility.super.toReferenceInfo(foreignKey);
        referenceInfo.addOtherCondition("id_at_app_dictcategory", "'%s'".formatted(category));
        return referenceInfo;
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("id_at_app_dictcategory")
            .addColumn("v_value", "字典值")
            .addColumn("v_name", "字典标题")
            .addColumn("v_remark")
            .addIndex(List.of("id_at_app_dictcategory", "v_value"), true);
    }

    @Override
    public String create(Map body) {
        //这样就可以把id_at_app_dictcategory当作ROOT_ID使用了
        Objects.requireNonNull(body.get("id_at_app_dictcategory"));
        String treePidName = PresetColumn.TREE_PID.getName();
        HashMap map = new HashMap<>(body);
        if (map.get(treePidName) == null) {
            map.put(treePidName, body.get("id_at_app_dictcategory"));
        }
        return super.create(map);
    }

    @Override
    public List<TreeNode> tree(String rootID, Boolean showMe, String labelColumn, Integer maxLevel) {
        Objects.requireNonNull(rootID, "必须提供根节点id，正常为类目id");
        if (showMe == null) {
            showMe = false;
        }
        return ITreeAbility.super.tree(rootID, showMe, labelColumn, maxLevel);
    }

    @Override
    public void check(Map body, boolean isUpdate) {
        Objects.requireNonNull(body.get("id_at_app_dictcategory"), "必须提供类目id");
        Objects.requireNonNull(body.get("v_value"), "必须提供字典值");

        PageResult<Map> query = this.query(Map.of(
            "id_at_app_dictcategory", body.get("id_at_app_dictcategory"),
            "v_value", body.get("v_value")
        ));

        if (query.getTotal() > 0) {
            String dictID = (String) query.getList().getFirst().get("id");
            if (!dictID.equals(body.get("id"))) {
                throw new MuYunException("该类目下存在相同的字典值");
            }
        }
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id_at_app_dictcategory"),
            QueryItem.of("v_value")
        );
    }
}
