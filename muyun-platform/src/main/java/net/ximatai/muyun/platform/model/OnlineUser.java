package net.ximatai.muyun.platform.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OnlineUser {
    private String id;
    private String name;
    private String username;
    private String organizationId;
    private String departmentId;
    private String organizationName;
    private String departmentName;

    private List<OnlineDevice> deviceList = new ArrayList<>();

    public String getId() {
        return id;
    }

    public OnlineUser setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public OnlineUser setName(String name) {
        this.name = name;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public OnlineUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public OnlineUser setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public OnlineUser setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
        return this;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public OnlineUser setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
        return this;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public OnlineUser setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
        return this;
    }

    public LocalDateTime getCheckInTime() {
        return deviceList.stream().min(Comparator.comparing(OnlineDevice::getCheckInTime)).get().getCheckInTime();
    }

    public LocalDateTime getLastActiveTime() {
        return deviceList.stream().max(Comparator.comparing(OnlineDevice::getLastActiveTime)).get().getLastActiveTime();
    }

    public List<OnlineDevice> getDeviceList() {
        return deviceList;
    }

    public OnlineUser setDeviceList(List<OnlineDevice> deviceList) {
        this.deviceList = deviceList;
        return this;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OnlineUser user) {
            return id.equals(user.id);
        }
        return false;
    }
}
