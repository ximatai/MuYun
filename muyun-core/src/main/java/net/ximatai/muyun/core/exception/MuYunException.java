package net.ximatai.muyun.core.exception;

public class MuYunException extends RuntimeException implements IToFrontendException, IExceptionForLog {
    public MuYunException(String message) {
        super(message);
    }

    @Override
    public String getLogMessage() {
        return this.getMessage();
    }
}
