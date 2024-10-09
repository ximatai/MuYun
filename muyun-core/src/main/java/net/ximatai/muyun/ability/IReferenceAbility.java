package net.ximatai.muyun.ability;

import net.ximatai.muyun.model.ReferenceInfo;

import java.util.List;

/**
 * 关联别人的能力（可以自动扩展 view 的查询字段）
 */
public interface IReferenceAbility {

    List<ReferenceInfo> getReferenceList();

}
