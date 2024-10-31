package net.ximatai.muyun.model.code;

public class SimpleCodePart implements ICodePart {
    private final String prefix;

    public SimpleCodePart(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String varchar() {
        return prefix;
    }
}
