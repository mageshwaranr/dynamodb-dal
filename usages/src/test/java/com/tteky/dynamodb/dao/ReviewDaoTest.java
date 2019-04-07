package com.tteky.dynamodb.dao;

import com.tteky.dynamodb.domain.Review;
import com.ttkey.dynamodb.exception.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReviewDaoTest {

    Logger log = LoggerFactory.getLogger(ReviewDaoTest.class);

    @Container
    private static GenericContainer dynamoDb = new GenericContainer<>("amazon/dynamodb-local:latest").withExposedPorts(8000);

    private static ReviewDao dao;
    private static ReviewMapper mapper;

    @BeforeAll
    static void setupDb() {
        Integer mappedPort = dynamoDb.getMappedPort(8000);
        var credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create("AWS_ACCESS_KEY_ID", "AWS_SECRET_KEY"));
        var dbClient = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://127.0.0.1:" + mappedPort))
                .credentialsProvider(credentialsProvider)
                .region(Region.US_EAST_1)
                .build();

        dao = new ReviewDao(dbClient);
        mapper = new ReviewMapper();
    }

    @Test
    @Order(1)
    void createTable() {
        CreateTableRequest.Builder builder = CreateTableRequest.builder()
                .keySchema(List.of(
                        KeySchemaElement.builder().keyType(KeyType.HASH).attributeName("id").build()))
                .attributeDefinitions(List.of(
                        AttributeDefinition.builder().attributeType("S").attributeName("id").build()))
                .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build());

        dao.createTable(builder);
        log.info("Table created");

        assertThrows(TableExistsException.class, () -> dao.createTable(builder));
    }

    @Test
    @Order(2)
    void crud() {
        Review r = new Review();
        r.setComments("Some comments");
        r.setRating(3.5f);
        r.setReviewer("M");
        r.setId(UUID.randomUUID().toString());
        dao.insertEntity(r);

        Review withId = new Review();
        withId.setId(r.getId());
        Review item = dao.getEntity(withId);
        assertEquals(r.getComments(), item.getComments());
        assertEquals(r.getReviewer(), item.getReviewer());
        assertEquals(r.getRating(), item.getRating());

        assertThrows(CouldNotInsertException.class, () -> dao.insertEntity(r));

        dao.deleteEntity(withId);
        assertThrows(CouldNotDeleteException.class, () -> dao.deleteEntity(withId));
    }

}
