package com.ttkey.dynamodb.dao;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

import static com.ttkey.dynamodb.utils.Conditionals.isNullOrEmpty;


/**
 * Container for holding paged results.
 */
public class PagedQueryResults<T> {

    public PagedQueryResults(List<T> results, Map<String, AttributeValue> lastEvaluatedKey) {
        this.results = results;
        this.lastEvaluatedKey = lastEvaluatedKey;
    }

    public PagedQueryResults() {
    }

    private List<T> results;

    private Map<String, AttributeValue> lastEvaluatedKey;

    public boolean hasNext() {
        return !isNullOrEmpty(lastEvaluatedKey);
    }

    public List<T> getResults() {
        return results;
    }

    public Map<String, AttributeValue> getLastEvaluatedKey() {
        return lastEvaluatedKey;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public void setLastEvaluatedKey(Map<String, AttributeValue> lastEvaluatedKey) {
        this.lastEvaluatedKey = lastEvaluatedKey;
    }
}