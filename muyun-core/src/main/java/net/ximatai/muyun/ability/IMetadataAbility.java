package net.ximatai.muyun.ability;

import jakarta.ws.rs.Path;
import net.ximatai.muyun.database.builder.TableBase;
import net.ximatai.muyun.database.metadata.DBTable;

/**
 * 元数据信息能力
 */
public interface IMetadataAbility extends IDatabaseAbility {

    String getMainTable();

    default String getSchemaName() {
        return "public";
    }

    default TableBase getTableBase() {
        return new TableBase(getSchemaName(), getMainTable());
    }

    default String getPK() {
        return "id";
    }

    default DBTable getDBTable() {
        return getDatabaseOperations().getDBInfo().getSchema(getSchemaName()).getTable(getMainTable());
    }

    default boolean checkColumn(String column) {
        return getDBTable().contains(column);
    }

    default String getPath() {
        Path annotation = this.getClass().getAnnotation(Path.class);
        if (annotation != null) {
            return annotation.value();
        }
        return null;
    }

    default boolean isModuleMatchingPath(String module) {
        String path = getPath();
        if (path == null) { // 说明本class不是HTTP Controller
            return false;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.equals(module) || path.endsWith(module)) {
            return true;
        } else if (path.contains("wildcard") && module.contains("wildcard")) { //说明是通配的controller
            String moduleAtPath = path.split("/")[0];
            return module.startsWith(moduleAtPath);
        } else {
            return false;
        }
    }
}
