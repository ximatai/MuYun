package net.ximatai.muyun.core.exception;

public class MuYunException extends RuntimeException implements IToFrontendException {
    public MuYunException(String message) {
        super(message);
    }
}
