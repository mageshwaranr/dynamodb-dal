package com.ttkey.dynamodb.exception;

/**
 * Exception to indicate operations like deleting a non-existing row/item etc.,
 */
public class ItemDoesNotExistException extends DataAccessException {

    public ItemDoesNotExistException(String message) {
        super(message);
    }

    public ItemDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

}
