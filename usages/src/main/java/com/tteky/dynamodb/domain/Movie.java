package com.tteky.dynamodb.domain;


import com.tteky.dynamodb.DynamoDBEntity;
import com.tteky.dynamodb.DynamoField;
import com.tteky.dynamodb.DynamoHashKey;
import com.tteky.dynamodb.DynamoRangeKey;

import java.util.List;
import java.util.Map;

@DynamoDBEntity
public class Movie {

    @DynamoField
    @DynamoHashKey
    private Integer year;

    @DynamoField
    private Long lastUpdatedOn, createdOn;

    @DynamoField
    @DynamoRangeKey
    private String title;

    @DynamoField
    private List<Review> reviews;

    @DynamoField
    private Map<String,Object> info;

    @DynamoField
    private Map<String,String> primInfo;

    @DynamoField
    private Map<String,Info> objInfo;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Long getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(Long lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }

    public Map<String, String> getPrimInfo() {
        return primInfo;
    }

    public void setPrimInfo(Map<String, String> primInfo) {
        this.primInfo = primInfo;
    }

    public Map<String, Info> getObjInfo() {
        return objInfo;
    }

    public void setObjInfo(Map<String, Info> objInfo) {
        this.objInfo = objInfo;
    }
}
