package net.ximatai.muyun.model;

public class ApiRequest {

    public static final ApiRequest BLANK = new ApiRequest("");

    private final String path;
    private String module;
    private String action;
    private String dataID;
    private boolean isSkip = false;
    private IRuntimeUser user = IRuntimeUser.WHITE;
    private String moduleID;
    private String moduleName;
    private String actionName;
    private String username;
    private String authCondition;

    private RuntimeException accessException;

    public ApiRequest(String path) {
        this.path = path;

        // /api/platform/user/view/xxxx
        String[] urlBlock = path.split("/");
        // ["", "api", "platform", "user", "view", "xxxx"]

        if (urlBlock.length < 5) { //说明不是平台标化接口
            isSkip = true;
            return;
        }

        if ("wildcard".equals(urlBlock[4])) { //说明是通配接口，需要特殊处理
            // /api/platform/commondoc/wildcard/wangpan/view/xxxx
            module = urlBlock[3] + "/" + urlBlock[4] + "/" + urlBlock[5];
            action = urlBlock[6];
            if (urlBlock.length > 7) {
                dataID = urlBlock[7];
            }
        } else {
            module = urlBlock[3];
            action = urlBlock[4];

            if (urlBlock.length > 5) {
                dataID = urlBlock[5];
            }
        }

    }

    public ApiRequest setUser(IRuntimeUser user) {
        this.user = user;
        return this;
    }

    public void setSkip() {
        isSkip = true;
    }

    public void setAccessException(RuntimeException accessException) {
        this.accessException = accessException;
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

    public RuntimeException getAccessException() {
        return accessException;
    }

    public String getModuleID() {
        return moduleID;
    }

    public ApiRequest setModuleID(String moduleID) {
        this.moduleID = moduleID;
        return this;
    }

    public String getModuleName() {
        return moduleName;
    }

    public ApiRequest setModuleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public String getActionName() {
        return actionName;
    }

    public ApiRequest setActionName(String actionName) {
        this.actionName = actionName;
        return this;
    }

    public String getUsername() {
        if (username != null) { // sso 登录的时候，会手动写入username
            return username;
        }

        return user.getUsername();
    }

    public ApiRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getAuthCondition() {
        return authCondition;
    }

    public void setAuthCondition(String authCondition) {
        this.authCondition = authCondition;
    }

    @Override
    public String toString() {
        return "path:" + path + ", module:" + module + ", action:" + action + ", dataID:" + dataID + ", userID" + getUser().getId() + ", isSkip:" + isSkip;
    }
}
