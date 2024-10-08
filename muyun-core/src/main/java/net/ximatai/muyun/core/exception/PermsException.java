package net.ximatai.muyun.core.exception;

public class PermsException extends RuntimeException {
    private String module;
    private String action;
    private String dataID;

    public PermsException(String module, String action) {
        this.module = module;
        this.action = action;
    }

    public PermsException setDataID(String dataID) {
        this.dataID = dataID;
        return this;
    }
}
