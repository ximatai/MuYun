package net.ximatai.muyun.core.exception;

public class PermsException extends RuntimeException implements IToFrontendException {

    public PermsException(String message) {
        super(message);
    }
}
