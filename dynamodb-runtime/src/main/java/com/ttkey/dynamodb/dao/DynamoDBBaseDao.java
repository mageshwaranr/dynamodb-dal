package com.ttkey.dynamodb.dao;

import com.ttkey.dynamodb.exception.*;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ttkey.dynamodb.utils.Conditionals.isNullOrEmpty;
import static java.lang.String.format;

public abstract class DynamoDBBaseDao<T> {
    private DynamoDbClient dynamoDb;
    private String tableName;

    public DynamoDBBaseDao(DynamoDbClient dynamoDb, String tableName) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
    }

    /**
     * Creates a new table
     *
     * @param request
     * @return creation response
     * @throws TableExistsException
     */
    public CreateTableResponse createTable(CreateTableRequest.Builder request) throws TableExistsException {
        try {
            return dynamoDb.createTable(request.tableName(tableName).build());
        } catch (RuntimeException e) {
            throw exceptionMapper("Create Table").apply(e);
        }
    }

    /**
     * Get entity using its key
     *
     * @param entityWithKeyFieldsPopulated
     * @return
     */
    public T getEntity(T entityWithKeyFieldsPopulated) {
        Map<String, AttributeValue> keys = entityToKeyAttributes(entityWithKeyFieldsPopulated);
        return getItem(GetItemRequest.builder()
                .key(keys));
    }

    private Map<String, AttributeValue> entityToKeyAttributes(T entityWithKeyFieldsPopulated) {
        var allFields = this.convert(entityWithKeyFieldsPopulated);
        Set<String> keyFieldNames = Set.of(getKeyFieldNames());
        return allFields.entrySet().stream()
                .filter(entry -> keyFieldNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Get item using partition key & sort key. Sort key can be null if the table has no sort key.
     *
     * @return
     */
    public T getItem(GetItemRequest.Builder builder) {
        return Optional.ofNullable(
                dynamoDb.getItem(builder
                        .tableName(tableName)
                        .build()))
                .map(GetItemResponse::item)
                .map(this::convert)
                .orElseThrow(() -> new
                        ItemDoesNotExistException(format("Item does not exist in '%s' table", tableName)));
    }


    /**
     * @param scanBuilder  containing required filter expression and its values.
     * @param pageSize     page size.
     * @param previousPage if not null, uses to fetch next page of data.
     * @return
     */
    public PagedQueryResults<T> scanItemsByPage(ScanRequest.Builder scanBuilder, int pageSize, PagedQueryResults<T> previousPage) throws TableDoesNotExistException {
        final ScanResponse result;
        scanBuilder.tableName(tableName)
                .limit(pageSize);
        if (previousPage != null) {
            scanBuilder.exclusiveStartKey(previousPage.getLastEvaluatedKey());
        }
        try {
            result = dynamoDb.scan(scanBuilder.build());
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException(format(" `%s` table does not exist", tableName));
        } catch (RuntimeException e) {
            throw exceptionMapper("Scan Item").apply(e);
        }
        final List<T> items = result.items().stream()
                .map(this::convert)
                .collect(Collectors.toList());

        return new PagedQueryResults<>(items, result.lastEvaluatedKey());
    }

    /**
     * Returns all the matching results in a single shot. Internally might fire multiple API's to perform this operation.
     *
     * @param scanBuilder scan request with all where clauses
     * @return list of all entities matching the criteria
     * @throws TableDoesNotExistException
     */
    public List<T> scanAllItems(ScanRequest.Builder scanBuilder) throws TableDoesNotExistException {
        var req = scanBuilder.tableName(tableName).build();
        try {
            return dynamoDb.scanPaginator(req)
                    .stream()
                    .flatMap(scanResponse -> scanResponse.items()
                            .stream()
                            .map(this::convert))
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException(format(" `%s` table does not exist", tableName));
        } catch (RuntimeException e) {
            throw exceptionMapper("Scan Item").apply(e);
        }
    }

    /**
     * Inserts new entity
     *
     * @param entity
     * @return
     */
    public void insertEntity(T entity) {
        var builder = PutItemRequest.builder()
                .item(convert(entity));
        this.insertItem(builder, getKeyFieldNames());
    }


    /**
     * Creates new Item.
     * //TODO: Based on options, handle audit here.
     *
     * @param putItemBuilder Builder containing the details of the item that needs to be created.
     * @param keyFieldNames  if present, will be considered as keys and attribute_not_exists check will be added. This can be null if and only if additional checks are already constructed in builder
     * @throws CouldNotInsertException    if item creation failed due to conditional checks like attribute_not_exists
     * @throws TableDoesNotExistException if the referred table doesn't exists.
     */
    public PutItemResponse insertItem(final PutItemRequest.Builder putItemBuilder, String... keyFieldNames) throws CouldNotInsertException, TableDoesNotExistException {
        Objects.requireNonNull(putItemBuilder);
        if (!isNullOrEmpty(keyFieldNames)) {
            putItemBuilder.conditionExpression(attributeNotExists(keyFieldNames));
        }
        return putItem(putItemBuilder);
    }

    private PutItemResponse putItem(PutItemRequest.Builder putItemBuilder) {
        var putItemRequest = putItemBuilder.tableName(tableName).build();
        try {
            return dynamoDb.putItem(putItemRequest);
        } catch (ConditionalCheckFailedException e) {
            var format = format(" Create Item failed in '%s' table due to conditional checks '%s'", tableName, e.getMessage());
            throw new CouldNotInsertException(format);
        } catch (RuntimeException e) {
            throw exceptionMapper("Create Item").apply(e);
        }
    }

    /**
     * Replace the current entity with newer values
     *
     * @param newerValue
     * @return
     */
    public T replaceEntity(T newerValue) {
        return convert(this.replaceItem(PutItemRequest.builder()
                .item(convert(newerValue)), this.getKeyFieldNames()).attributes());
    }

    /**
     * Replaces an existing item using put item command.
     *
     * @param putItemBuilder Request containing items with new values.
     * @param keyFieldNames  if present, adds attribute exists exception. Should be null if you want to use legacy `attribute_update_values`.
     * @return
     * @throws CouldNotInsertException    if new row can't be inserted/replaced
     * @throws TableDoesNotExistException if the referred doesn't exists.
     */
    public PutItemResponse replaceItem(final PutItemRequest.Builder putItemBuilder, String... keyFieldNames) throws CouldNotInsertException, TableDoesNotExistException {
        Objects.requireNonNull(putItemBuilder);
        if (!isNullOrEmpty(keyFieldNames)) {
            putItemBuilder.conditionExpression(attributeExists(keyFieldNames));
        }
        return putItem(putItemBuilder);
    }

    /**
     * //TODO: handle audit here.
     *
     * @param builder       containing necessary values that needs to be updated.
     * @param keyFieldNames should be null if builder has attributeValues populated (deprecated). If used with, expression attributes, validates for presence of keys
     * @return Entity with updated values
     * @throws CouldNotUpdateException    if item update fails due to conditional checks like attribute_not_exists
     * @throws TableDoesNotExistException if the referred table doesn't exists.
     */
    public T updateItem(UpdateItemRequest.Builder builder, String... keyFieldNames) throws CouldNotUpdateException, TableDoesNotExistException {
        builder.returnValues(ReturnValue.ALL_NEW);
        builder.tableName(tableName);
        if (!isNullOrEmpty(keyFieldNames)) {
            builder.conditionExpression(attributeExists(keyFieldNames));
        }
        var req = builder.build();
        try {
            var updateResponse = dynamoDb.updateItem(req);
            return convert(updateResponse.attributes());
        } catch (ConditionalCheckFailedException e) {
            var format = format(" Update Item failed in '%s' table due to conditional checks '%s'", tableName, e.getMessage());
            throw new CouldNotUpdateException(format);
        } catch (RuntimeException e) {
            throw exceptionMapper("Update Item").apply(e);
        }
    }

    public T deleteEntity(T entityWithKeyFields) {
        Map<String, AttributeValue> convert = this.entityToKeyAttributes(entityWithKeyFields);
        return this.deleteItem(DeleteItemRequest.builder()
                        .key(convert)
                , this.getKeyFieldNames());
    }

    /**
     * Deletes the entry using the builder provided.
     * //TODO: handle audit
     *
     * @param keyFieldNames If provided, constructs checks to ensure an item is deleted
     * @param builder       delete request builder
     * @return
     */
    public T deleteItem(DeleteItemRequest.Builder builder, String... keyFieldNames) throws CouldNotDeleteException, TableDoesNotExistException {
        builder.returnValues(ReturnValue.ALL_OLD).tableName(tableName);
        if (!isNullOrEmpty(keyFieldNames)) {
            builder.conditionExpression(attributeExists(keyFieldNames));
        }
        var req = builder.build();
        try {
            var response = dynamoDb.deleteItem(req);
            return convert(response.attributes());
        } catch (ConditionalCheckFailedException e) {
            var format = format(" Delete Item failed in '%s' table due to conditional checks '%s'", tableName, e.getMessage());
            throw new CouldNotDeleteException(format);
        } catch (RuntimeException e) {
            throw exceptionMapper("Update Item").apply(e);
        }
    }

    protected Function<RuntimeException, ? extends DataAccessException> exceptionMapper(String operation) {
        return e -> {
            if (e instanceof ResourceNotFoundException || e instanceof TableNotFoundException) {
                return new TableDoesNotExistException(format(" `%s` table does not exist. '%s'", tableName, e.getMessage()));
            } else if (e instanceof ResourceInUseException || e instanceof TableAlreadyExistsException) {
                return new TableExistsException(format(" `%s` table already exist. '%s'", tableName, e.getMessage()));
            } else if (e instanceof DynamoDbException) {
                var format = format("'%s' failed in '%s' table due to %s", operation, tableName, e.getMessage());
                return new DataAccessException(format, e);
            } else if (e instanceof SdkException) {
                var format = format("'%s' failed in '%s' table due to infrastructure issue %s", operation, tableName, e.getMessage());
                return new DataAccessException(format, e);
            } else {
                var format = format("'%s' failed in '%s' table due to unexpected exception. Error Message is: %s", operation, tableName, e.getMessage());
                return new DataAccessException(format, e);
            }
        };
    }

    protected abstract T convert(Map<String, AttributeValue> item);

    protected abstract Map<String, AttributeValue> convert(T entity);

    protected abstract String[] getKeyFieldNames();

    protected String attributeNotExists(String... fieldNames) {
        return Stream.of(fieldNames)
                .map(name -> format("attribute_not_exists( %s)", name))
                .collect(Collectors.joining(" AND "));
    }

    protected String attributeExists(String... fieldNames) {
        return Stream.of(fieldNames)
                .map(name -> format("attribute_exists( %s)", name))
                .collect(Collectors.joining(" AND "));
    }
}
