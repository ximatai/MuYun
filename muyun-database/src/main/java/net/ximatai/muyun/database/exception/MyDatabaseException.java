package net.ximatai.muyun.database.exception;

public class MyDatabaseException extends RuntimeException {

    private final Type type;

    public enum Type {
        DEFAULT,
        DATA_NOT_FOUND,
        READ_METADATA_ERROR,
    }

    @Override
    public String getMessage() {

        switch (type) {
            case DATA_NOT_FOUND -> {
                return "操作的数据不存在";
            }
            case READ_METADATA_ERROR -> {
                return "元数据读取失败，" + super.getMessage();
            }
        }

        return super.getMessage();
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
