package com.ttkey.dynamodb.exception;

/**
 * Indicates when insertion fails. It might fail due to duplicate primary check or
 * in case of dynamo due to conditional expression supplied in the putItem
 */
public class CouldNotInsertException extends DataAccessException {

    public CouldNotInsertException(String message) {
        super(message);
    }

    public CouldNotInsertException(String message, Throwable cause) {
        super(message, cause);
    }
}
