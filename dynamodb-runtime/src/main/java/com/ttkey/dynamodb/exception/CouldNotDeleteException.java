package com.ttkey.dynamodb.exception;

/**
 * Indicates a scenario when an item/row couldn't be deleted. The row might be already deleted or version checks fails in case of optimistic lock etc.,
 * in case of dynamo, operations could fail due to conditional expression supplied in the request
 */
public class CouldNotDeleteException extends DataAccessException {

    public CouldNotDeleteException(String message) {
        super(message);
    }

    public CouldNotDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
