package com.tteky.dynamodb.processor;

import com.google.testing.compile.JavaFileObjects;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.ttkey.dynamodb.mapper.DynamoDBBaseMapper;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.google.testing.compile.JavaSourcesSubject.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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