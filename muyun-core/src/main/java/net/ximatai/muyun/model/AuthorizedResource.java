package net.ximatai.muyun.model;

public class AuthorizedResource {
    private String module;
    private String action;

    public AuthorizedResource(String module, String action) {
        this.module = module;
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public String getAction() {
        return action;
    }
}
