package net.ximatai.muyun.model.log;

public class LogBaseItem {

    private String uri;
    private String method;
    private String params;
    private String host;
    private String userAgent;
    private String userID;
    private String username;
    private Long costTime;

    private boolean success;
    private int statusCode;
    private String error;

    public String getUri() {
        return uri;
    }

    public LogBaseItem setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public LogBaseItem setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getParams() {
        return params;
    }

    public LogBaseItem setParams(String params) {
        this.params = params;
        return this;
    }

    public String getHost() {
        return host;
    }

    public LogBaseItem setHost(String host) {
        this.host = host;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LogBaseItem setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public String getUserID() {
        return userID;
    }

    public LogBaseItem setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public LogBaseItem setUsername(String username) {
        this.username = username;
        return this;
    }

    public Long getCostTime() {
        return costTime;
    }

    public LogBaseItem setCostTime(Long costTime) {
        this.costTime = costTime;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public LogBaseItem setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public LogBaseItem setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String getError() {
        return error;
    }

    public LogBaseItem setError(String error) {
        this.error = error;
        return this;
    }


}
