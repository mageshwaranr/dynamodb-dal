package com.ttkey.dynamodb.exception;

/**
 * Indicates a scenario when an item/row couldn't be updated. The row might be inserted at all or version checks fails (in case of optimistic lock etc.,)
 * in case of dynamo, operations could fail due to conditional expression supplied in the request
 */
public class CouldNotUpdateException extends DataAccessException {

    public CouldNotUpdateException(String message) {
        super(message);
    }

    public CouldNotUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}


