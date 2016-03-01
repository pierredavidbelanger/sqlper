package ca.pjer.sqlper;

public class SqlperException extends RuntimeException {

    public SqlperException(String message) {
        super(message);
    }

    public SqlperException(String message, Throwable cause) {
        super(message, cause);
    }
}
