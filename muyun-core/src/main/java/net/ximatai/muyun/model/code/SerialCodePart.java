package net.ximatai.muyun.model.code;

public class SerialCodePart implements ICodePart {
    private final int width;
    private long base;

    public SerialCodePart setBase(long base) {
        this.base = base;
        return this;
    }

    public SerialCodePart(int width) {
        this.width = width;
    }

    @Override
    public String varchar() {
        return String.format("%0" + width + "d", base + 1);
    }
}
