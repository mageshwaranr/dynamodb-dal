package com.tteky.dynamodb.processor;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class UtilsTest {


    @ParameterizedTest
    @CsvSource({
            "Integer, integer",
            "DBUtils, dBUtils",
    })
    void firstAsSmall(String someClassName, String expectedValue) {
        assertThat(Utils.firstAsSmall(someClassName), is(equalTo(expectedValue)));
    }

    @ParameterizedTest
    @CsvSource({
            "name, Name",
            "aCat, ACat",
    })
    void firstAsCaps(String fieldName, String camelcaseSuffix) {
        assertThat(Utils.firstAsCaps(fieldName), is(equalTo(camelcaseSuffix)));
    }

    @ParameterizedTest
    @CsvSource({
            "com.tteky.dynamodb.processor,'' , com.tteky.dynamodb.dao",
            "com.tteky.dynamodb.processor, com.tteky.dao , com.tteky.dao",
            "orm, '' ,dao",
    })
    void extractPackageName(String fallback, String preferred, String expected) {
        assertThat(Utils.extractPackageName(preferred,fallback), is(equalTo(expected)));
    }

}
