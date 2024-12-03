package net.ximatai.muyun.ability;

public interface IArchiveWhenDelete extends ITableCreateAbility {

    default String getArchiveTableName() {
        return getMainTable() + "__archive";
    }

}
