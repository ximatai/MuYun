package net.ximatai.muyun.fileserver.exception;

public class FileException extends Exception {

    public FileException(String message) {
        super(message);
    }

    public FileException(Exception e) {
        super(e);
    }

}
