package net.ximatai.muyun.platform.model;

import java.time.LocalDateTime;
import java.util.List;

public class OnlineUser {
    private String id;
    private String name;
    private String username;
    private String organizationName;
    private String organizationId;
    private String departmentName;
    private String departmentId;

    private LocalDateTime loginTime;

    private List<OnlineDevice> deviceList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public List<OnlineDevice> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<OnlineDevice> deviceList) {
        this.deviceList = deviceList;
    }
}
