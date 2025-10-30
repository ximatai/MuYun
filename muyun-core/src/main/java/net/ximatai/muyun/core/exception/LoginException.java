package net.ximatai.muyun.core.exception;

public class LoginException extends MuYunException {

    public LoginException(String message) {
        super(message);
    }

    public LoginException setLogMessage(String logMessage) {
        super.setLogMessage(logMessage);
        return this;
    }
}
