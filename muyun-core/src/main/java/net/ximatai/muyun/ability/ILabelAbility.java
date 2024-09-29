package net.ximatai.muyun.ability;

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
