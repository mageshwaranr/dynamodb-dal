package com.tteky.dynamodb.dao;


import com.tteky.dynamodb.domain.Info;
import com.tteky.dynamodb.domain.Movie;
import com.tteky.dynamodb.domain.Review;
import com.ttkey.dynamodb.exception.CouldNotInsertException;
import com.ttkey.dynamodb.exception.TableExistsException;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MovieDaoTest {

    private Logger log = LoggerFactory.getLogger(ReviewDaoTest.class);

    @Container
    private static GenericContainer dynamoDb = new GenericContainer<>("amazon/dynamodb-local:latest").withExposedPorts(8000);

    private static MovieDao dao;
    private static MovieMapper mapper;
    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setupDb() {
        Integer mappedPort = dynamoDb.getMappedPort(8000);
        var credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create("AWS_ACCESS_KEY_ID", "AWS_SECRET_KEY"));
        var dbClient = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://127.0.0.1:" + mappedPort))
                .credentialsProvider(credentialsProvider)
                .region(Region.US_EAST_1)
                .build();

        dao = new MovieDao(dbClient);
        mapper = new MovieMapper();
    }

    @Test
    @Order(1)
    void createTable(){

        CreateTableRequest.Builder builder = CreateTableRequest.builder()
                .keySchema(List.of(
                        KeySchemaElement.builder().keyType(KeyType.HASH).attributeName("yr").build(),
                        KeySchemaElement.builder().keyType(KeyType.RANGE).attributeName("title").build()))
                .attributeDefinitions(List.of(
                        AttributeDefinition.builder().attributeType("N").attributeName("yr").build(),
                        AttributeDefinition.builder().attributeType("S").attributeName("title").build()))
                .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build());

        dao.createTable(builder);
        log.info("Table created");

        assertThrows(TableExistsException.class, () -> dao.createTable(builder));

    }

    @Test
    @Order(2)
    void crud() {
        Movie movie = new Movie();
        movie.setYear(1990);
        movie.setTitle("Big Hit Movie");
        movie.setReviews(List.of(newReview(3.8f, "Comment 1"), newReview(3.9f, "Comment 2")));
        movie.setCreatedOn(Instant.now().toEpochMilli());
        movie.setInfo(Map.of("mapInfoKey1", "mapInfoValue1","mapInfoKey2","mapInfoValue2"));
        movie.setLastUpdatedOn(Instant.now().toEpochMilli());
        movie.setObjInfo(Map.of("mapObjInfoKey1",newInfo(1),"mapObjInfoKey2",newInfo(2)));
        movie.setPrimInfo(Map.of("mapPrimInfoKey1", "mapInfoValue1","mapPrimInfoKey2","mapInfoValue2"));

        dao.insertEntity(movie);

        Movie withId = new Movie();
        withId.setYear(movie.getYear());
        withId.setTitle(movie.getTitle());
        Movie getItem = dao.getEntity(withId);
        assertNotNull(getItem);
        var expected = objectMapper.convertValue(movie, Map.class);
        var actual = objectMapper.convertValue(getItem, Map.class);
        assertEquals(expected, actual);

        assertThrows(CouldNotInsertException.class, () -> dao.insertEntity(movie));

    }

    private Info newInfo(int i) {
        Info info = new Info();
        info.setComments("Info Comments "+i);
        info.setReviewer("Some Reviewer "+i);
        return info;
    }

    private Review newReview(Float rating, String comment){
        Review r = new Review();
        r.setId(UUID.randomUUID().toString());
        r.setReviewer("SomeReviewer");
        r.setRating(rating);
        r.setComments(comment);
        return r;
    }
}
