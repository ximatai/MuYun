package net.ximatai.muyun.database.exception;

public class MyDatabaseException extends RuntimeException {

    private final Type type;

    public enum Type {
        DEFAULT,
        DATA_NOT_FOUND,
        READ_METADATA_ERROR,
    }

    public MyDatabaseException(Type type) {
        this.type = type;
    }

    public MyDatabaseException(String message) {
        super(message);
        this.type = Type.DEFAULT;
    }

    public MyDatabaseException(String message, Type type) {
        super(message);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
