package net.ximatai.muyun.ability.curd.std;

/**
 * 自定义SQL查询数据的能力（代替默认的主表查询）
 */
public interface ICustomSelectSqlAbility extends ISelectAbility {

    String getCustomSql();

}
