package com.ttkey.dynamodb.exception;

/**
 * Exception to indicate conditions like creating an item with an primary key which exists already
 */
public class ItemExistException extends DataAccessException {

    public ItemExistException(String message) {
        super(message);
    }

    public ItemExistException(String message, Throwable cause) {
        super(message, cause);
    }

}