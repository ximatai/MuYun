package net.ximatai.muyun.ability;

public interface IArchiveWhenDeleteAbility {

    default String tableNameSuffix() {
        return "_archive";
    }

}
