package com.ttkey.dynamodb.exception;

/**
 * Create / alter table fails as the table doesn't exists.
 */
public class TableExistsException extends DataAccessException {

    public TableExistsException(String message) {
        super(message);
    }

    public TableExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
