package com.tteky.dynamodb.domain;

import com.tteky.dynamodb.DynamoDBEntity;
import com.tteky.dynamodb.DynamoField;
import com.tteky.dynamodb.DynamoHashKey;

/**
 * An example entity with just hash key.
 * For illustration purpose, this is also used as embedded pojo in Movie entity
 * Duplicated from usage module
 */
@DynamoDBEntity
public class Review {

    @DynamoField
    private String reviewer, comments;

    @DynamoField
    @DynamoHashKey
    private float rating;

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
