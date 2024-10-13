package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.ability.curd.std.IDataCheckAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.model.DictCategory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Startup
@Path(BASE_PATH + "/dict")
public class DictCategoryController extends ScaffoldForPlatform implements ITreeAbility, IChildrenAbility, IDataCheckAbility, IQueryAbility {

    @Inject
    DictController dictController;

    @Override
    public String getMainTable() {
        return "app_dictcategory";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey("id")
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("v_name")
            .addColumn("v_remark");
    }

    @Override
    public List<ChildTableInfo> getChildren() {
        return List.of(
            dictController.toChildTable("id_at_app_dictcategory")
        );
    }

    @GET
    @Path("/tree/{id}")
    public List<TreeNode> tree(@PathParam("id") String id) {
        return dictController.tree(id, false, null, null);
    }

    @GET
    @Path("/translate/{category}")
    public String translate(@PathParam("category") String category, @QueryParam("source") String source) {

        TreeNode node = tree(category).stream().filter(treeNode -> treeNode.getData().get("v_value").equals(source)).findFirst().orElse(null);
        if (node == null) {
            throw new MyException("字典值 %s 在 %s 类型中不存在".formatted(source, category));
        }

        return node.getData().get("v_name").toString();
    }

    @Override
    public Integer sort(String id, String prevId, String nextId, String parentId) {

        Map<String, ?> category = this.view(id);
        Map<String, ?> dict = dictController.view(id);
        if (category != null) { // 说明是给类目排序
            return ITreeAbility.super.sort(id, prevId, nextId, parentId);
        } else if (dict != null) { // 说明是给字典排序
            if (parentId == null) {
                parentId = dict.get("id_at_app_dictcategory").toString();
            }
            return dictController.sort(id, prevId, nextId, parentId);
        }

        return 0;
    }

    @Override
    public void check(Map body, boolean isUpdate) {
        String id = (String) Objects.requireNonNull(body.get("id"), "数据字典类目必须提供编码");
        String name = (String) Objects.requireNonNull(body.get("v_name"), "数据字典类目必须提供名称");

        if (!id.equals(id.toLowerCase())) {
            throw new MyException("数据字典类目编码不能包含大写字母");
        }

        if (!isUpdate) {
            if (this.query(Map.of("id", id)).getTotal() > 0) {
                throw new MyException("存在重复的数据字典类目编码");
            }
            if (this.query(Map.of("v_name", name)).getTotal() > 0) {
                throw new MyException("存在重复的数据字典类目名称");
            }
        }
    }

    @Override
    public Integer update(String id, Map body) {
        this.check(body, true);
        String newID = body.get("id").toString();
        if (!id.equals(newID)) { // 说明id被更改了
            getDB().update("update %s.%s set id = ? where id = ?".formatted(getSchemaName(), getMainTable()), newID, id);
        }
        return super.update(newID, body);
    }

    @Override
    public void onTableCreated(boolean isFirst) {
        List.of(
            new DictCategory("muyun_dir", "平台自用", 0),
            new DictCategory("platform_dir", "平台业务", 1)
        ).forEach(dictCategory -> {
            this.putDictCategory(dictCategory, true);
        });
    }

    public void putDictCategory(DictCategory dictCategory, boolean isLock) {
        Map<String, ?> category = this.view(dictCategory.getId());
        if (category == null) {
            this.create(dictCategory.toMap());
        } else if (isLock) {
            this.putChildTableList(dictCategory.getId(), "app_dict", List.of());
            this.update(dictCategory.getId(), dictCategory.toMap());
        }
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id"),
            QueryItem.of("v_name")
        );
    }
}
