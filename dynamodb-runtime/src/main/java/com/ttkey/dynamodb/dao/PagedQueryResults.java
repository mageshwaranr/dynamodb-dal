package com.ttkey.dynamodb.dao;

import lombok.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

import static com.ttkey.dynamodb.utils.Conditionals.isNullOrEmpty;


/**
 * Container for holding paged results.
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
public class PagedQueryResults<T> {

    @Getter
    private List<T> results;

    @Getter
    private Map<String, AttributeValue> lastEvaluatedKey;

    public boolean hasNext() {
        return !isNullOrEmpty(lastEvaluatedKey);
    }

}