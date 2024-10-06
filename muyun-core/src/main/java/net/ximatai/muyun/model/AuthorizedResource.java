package net.ximatai.muyun.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorizedResource that = (AuthorizedResource) o;
        return Objects.equals(module, that.module) && Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, action);
    }
}
