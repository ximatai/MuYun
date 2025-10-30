package net.ximatai.muyun.core.exception;

public class MuYunException extends RuntimeException implements IToFrontendException, IExceptionForLog {

    private String logMessage;

    public MuYunException(String message) {
        super(message);
        this.logMessage = message;
    }

    @Override
    public String getLogMessage() {
        return logMessage;
    }

    public MuYunException setLogMessage(String logMessage) {
        this.logMessage = logMessage;
        return this;
    }
}
