package com.tteky.dynamodb.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.JavaSourcesSubject.assertThat;

class DynamoDBProcessorTest {

    @Test
    public void dynamoDbCodeGen() throws Exception {
        assertThat(
                JavaFileObjects.forResource("test/Movie.java"),
                JavaFileObjects.forResource("test/Review.java"),
                JavaFileObjects.forResource("test/Info.java"))
                .processedWith(new DynamoDBProcessor())
                .compilesWithoutError()
        ;

    }



}