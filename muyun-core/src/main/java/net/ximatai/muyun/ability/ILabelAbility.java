package net.ximatai.muyun.ability;

/**
 * 数据行标签化的能力（树、被关联自动翻译、日志记录时展示字段）
 */
public interface ILabelAbility extends IMetadataAbility {
    default String getLabelColumn() {
        if (checkColumn("v_name")) {
            return "v_name";
        }

        if (checkColumn("v_label")) {
            return "v_label";
        }
        return null;
    }
}
