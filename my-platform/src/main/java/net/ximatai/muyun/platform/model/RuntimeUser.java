package net.ximatai.muyun.platform.model;

import net.ximatai.muyun.model.IRuntimeUser;

public class RuntimeUser implements IRuntimeUser {

    private String id;
    private String name;
    private String username;
    private boolean isAdmin;
    private String organizationId;
    private String departmentId;

    public String getId() {
        return id;
    }

    public RuntimeUser setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public RuntimeUser setName(String name) {
        this.name = name;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public RuntimeUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public RuntimeUser setAdmin(boolean admin) {
        isAdmin = admin;
        return this;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public RuntimeUser setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public RuntimeUser setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
        return this;
    }
}
