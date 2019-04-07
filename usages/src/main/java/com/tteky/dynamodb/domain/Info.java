package com.tteky.dynamodb.domain;

import com.tteky.dynamodb.DynamoField;

/**
 * POJO embedded in other entity types
 */
public class Info {
    @DynamoField
    private String reviewer, comments;

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
}
