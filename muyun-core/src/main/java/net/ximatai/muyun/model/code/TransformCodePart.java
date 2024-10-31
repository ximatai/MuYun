package net.ximatai.muyun.model.code;

import java.util.Map;
import java.util.function.Function;

public class TransformCodePart implements ICodePart {
    private final int width;
    private final Function<Map, String> transform;
    private Map data;

    public void setData(Map data) {
        this.data = data;
    }

    public TransformCodePart(int width, Function<Map, String> transform) {
        this.width = width;
        this.transform = transform;
    }

    @Override
    public String varchar() {
        if (data != null) {
            return transform.apply(data);
        } else {
            return "X".repeat(width);
        }
    }
}
