package net.ximatai.muyun.model;

import net.ximatai.muyun.core.exception.MyException;

public class ApiRequest {

    private final String path;
    private String module;
    private String action;
    private String dataID;
    private boolean isSkip = false;
    private final IRuntimeUser user;

    private RuntimeException error;

    public ApiRequest(IRuntimeUser user, String path) {
        this.user = user;
        this.path = path;

        String[] urlBlock = path.split("/");

        if (urlBlock.length < 5) {
            isSkip = true;
            return;
        }

        module = urlBlock[3];
        action = urlBlock[4];

        if (urlBlock.length > 5) {
            dataID = urlBlock[5];
        }
    }

    public void setSkip() {
        isSkip = true;
    }

    public void setError(String moduleName, String actionName) {
        this.error = new MyException("您没有[%s]的[%s]功能权限".formatted(moduleName, actionName));
    }

    public void setError(String moduleName, String actionName, String dataID) {
        this.error = new MyException("您没有[%s]中[%S]数据[%s]的权限".formatted(moduleName, actionName, dataID));
    }

    public String getModule() {
        return module;
    }

    public String getAction() {
        return action;
    }

    public String getDataID() {
        return dataID;
    }

    public IRuntimeUser getUser() {
        return user;
    }

    public boolean isSkip() {
        return isSkip;
    }

    public RuntimeException getError() {
        return error;
    }

    @Override
    public String toString() {
        return "path:" + path + ", module:" + module + ", action:" + action + ", dataID:" + dataID + ", userID" + getUser().getId() + ", isSkip:" + isSkip;
    }
}
