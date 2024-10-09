package net.ximatai.muyun.ability.curd.std;

import java.util.Map;

/**
 * 数据新增、修改时进行校验的能力
 */
public interface IDataCheckAbility {

    void check(Map body, boolean isUpdate);

}
