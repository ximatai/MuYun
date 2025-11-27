package net.ximatai.muyun.model.log;

import java.util.HashMap;
import java.util.Map;

public class LogItem {

    private String uri;
    private String method;
    private Map params;
    private String host;
    private String userAgent;
    private String os;
    private String browser;
    private String userID;
    private String username;

    private String moduleName;
    private String actionName;
    private String dataID;

    private Long costTime;
    private boolean success;
    private int statusCode;
    private String error;

    public String getUri() {
        return uri;
    }

    public LogItem setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public LogItem setMethod(String method) {
        this.method = method;
        return this;
    }

    public Map getParams() {
        return params;
    }

    public LogItem setParams(Map params) {
        this.params = params;
        return this;
    }

    public String getHost() {
        return host;
    }

    public LogItem setHost(String host) {
        this.host = host;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LogItem setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public String getUserID() {
        return userID;
    }

    public LogItem setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public LogItem setUsername(String username) {
        this.username = username;
        return this;
    }

    public Long getCostTime() {
        return costTime;
    }

    public LogItem setCostTime(Long costTime) {
        this.costTime = costTime;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public LogItem setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public LogItem setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String getError() {
        return error;
    }

    public LogItem setError(String error) {
        this.error = error;
        return this;
    }

    public String getOs() {
        return os;
    }

    public LogItem setOs(String os) {
        this.os = os;
        return this;
    }

    public String getBrowser() {
        return browser;
    }

    public LogItem setBrowser(String browser) {
        this.browser = browser;
        return this;
    }

    public String getModuleName() {
        return moduleName;
    }

    public LogItem setModuleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public String getActionName() {
        return actionName;
    }

    public LogItem setActionName(String actionName) {
        this.actionName = actionName;
        return this;
    }

    public String getDataID() {
        return dataID;
    }

    public LogItem setDataID(String dataID) {
        this.dataID = dataID;
        return this;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("v_uri", uri);
        map.put("v_method", method);
        map.put("j_params", params);
        map.put("v_host", host);
        map.put("v_useragent", userAgent);
        map.put("v_os", os);
        map.put("v_browser", browser);
        map.put("id_at_auth_user", userID);
        map.put("v_username", username);
        map.put("v_module", moduleName);
        map.put("v_action", actionName);
        map.put("v_data_id", dataID);
        map.put("i_cost", costTime);
        map.put("b_success", success);
        map.put("i_status_code", statusCode);
        map.put("v_error", error);

        return map;
    }
}
