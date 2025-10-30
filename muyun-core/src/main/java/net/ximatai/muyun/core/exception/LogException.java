package net.ximatai.muyun.core.exception;

public class LogException extends MuYunException {

    String logMessage;

    public LogException(String message) {
        super(message);
    }

    public LogException setLogMessage(String logMessage) {
        this.logMessage = logMessage;
        return this;
    }
}
