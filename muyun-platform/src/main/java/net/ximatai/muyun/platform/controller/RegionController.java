package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.ability.curd.std.IDataCheckAbility;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Startup
@Path(BASE_PATH + "/region")
@Tag(name = "行政区划管理")
public class RegionController extends ScaffoldForPlatform implements ITreeAbility, IReferableAbility, IChildrenAbility, IDataCheckAbility {

    @Inject
    SupervisionRegionController supervisionRegionController;

    @Override
    public String getMainTable() {
        return "app_region";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn("v_name", "区划名称")
            .addColumn("v_shortname", "区划简称");
    }

    @Override
    public List<ChildTableInfo> getChildren() {
        return List.of(
            supervisionRegionController.toChildTable("id_at_app_region").setAutoDelete()
        );
    }

    @Override
    public Integer update(String id, Map body) {
        this.check(body, true);
        String newID = body.get("id").toString();
        if (!id.equals(newID)) { // 说明id被更改了
            Map<String, ?> view = this.view(newID);
            if (view != null) {
                throw new MyException("行政区划代码[%s]重复".formatted(newID));
            }

            getDB().update("update %s.%s set id = ? where id = ?".formatted(getSchemaName(), getMainTable()), newID, id);
        }
        return super.update(newID, body);
    }

    @Override
    public void check(Map body, boolean isUpdate) {
        String id = (String) body.get("id");
        Objects.requireNonNull(id, "必须提供行政区划编码");

        Map row = this.view(id);

        if (!isUpdate && row != null) { // 找到重复行
            throw new MyException("行政区划代码[%s]重复".formatted(id));
        }
    }

}
