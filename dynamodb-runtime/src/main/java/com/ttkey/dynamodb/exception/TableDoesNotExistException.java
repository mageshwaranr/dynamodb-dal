package com.ttkey.dynamodb.exception;

public class TableDoesNotExistException extends DataAccessException {

    public TableDoesNotExistException(String message) {
        super(message);
    }

    public TableDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
