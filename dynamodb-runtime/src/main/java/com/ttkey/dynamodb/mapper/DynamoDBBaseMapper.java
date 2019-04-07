package com.ttkey.dynamodb.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DynamoDBBaseMapper {

    private static ObjectMapper mapper = new ObjectMapper();

    public AttributeValue byteToAttr(Byte value) {
        return value == null ? null : AttributeValue.builder().n(String.valueOf(value)).build();
    }

    public AttributeValue shortToAttr(Short value) {
        return value == null ? null : AttributeValue.builder().n(String.valueOf(value)).build();
    }

    public AttributeValue intToAttr(Integer value) {
        return value == null ? null : AttributeValue.builder().n(String.valueOf(value)).build();
    }

    public AttributeValue longToAttr(Long value) {
        return value == null ? null : AttributeValue.builder().n(String.valueOf(value)).build();
    }

    public AttributeValue doubleToAttr(Double value) {
        return value == null ? null : AttributeValue.builder().n(String.valueOf(value)).build();
    }

    public AttributeValue floatToAttr(Float value) {
        return value == null ? null : AttributeValue.builder().n(String.valueOf(value)).build();
    }

    public AttributeValue charToAttr(Character value) {
        return value == null ? null : AttributeValue.builder().s(String.valueOf(value)).build();
    }

    public AttributeValue stringToAttr(String value) {
        return value == null ? null : AttributeValue.builder().s(value).build();
    }

    public AttributeValue objectToAttr(Object value) {
        try {
            return value == null ? null : AttributeValue.builder().s(getMapper().writeValueAsString(value)).build();
        } catch (JsonProcessingException e) {
            return AttributeValue.builder().s(String.valueOf(value)).build();
        }
    }

    public AttributeValue booleanToAttr(Boolean value) {
        return value == null ? null : AttributeValue.builder().bool(value).build();
    }

    public AttributeValue enumToAttr(Enum value) {
        return value == null ? null : AttributeValue.builder().s(value.name()).build();
    }


    public AttributeValue bigDecimalToAttr(BigDecimal value) {
        return value == null ? null : AttributeValue.builder().n(value.toString()).build();
    }

    public AttributeValue bigIntegerToAttr(BigInteger value) {
        return value == null ? null : AttributeValue.builder().n(value.toString()).build();
    }

    public AttributeValue dateToAttr(Date value) {
        return value == null ? null : instantToAttr(value.toInstant());
    }

    public AttributeValue calendarToAttr(Calendar value) {
        return value == null ? null : instantToAttr(value.toInstant());
    }

    public AttributeValue instantToAttr(Instant value) {
        return value == null ? null : longToAttr(value.toEpochMilli());
    }


    public Byte toByte(Map<String, AttributeValue> attributes, String fieldName) {
        return toByte(attributes.get(fieldName));
    }

    public Byte toByte(AttributeValue val) {
        return val == null ? null : Byte.valueOf(val.n());
    }

    public Short toShort(Map<String, AttributeValue> attributes, String fieldName) {
        return toShort(attributes.get(fieldName));
    }

    public Short toShort(AttributeValue val) {
        return val == null ? null : Short.valueOf(val.n());
    }

    public Integer toInt(Map<String, AttributeValue> attributes, String fieldName) {
        return toInt(attributes.get(fieldName));
    }

    public Integer toInt(AttributeValue val) {
        return val == null ? null : Integer.valueOf(val.n());
    }

    public Long toLong(Map<String, AttributeValue> attributes, String fieldName) {
        return toLong(attributes.get(fieldName));
    }

    public Long toLong(AttributeValue val) {
        return val == null ? null : Long.valueOf(val.n());
    }


    public Double toDouble(Map<String, AttributeValue> attributes, String fieldName) {
        return toDouble(attributes.get(fieldName));
    }

    public Double toDouble(AttributeValue val) {
        return val == null ? null : Double.valueOf(val.n());
    }

    public Float toFloat(Map<String, AttributeValue> attributes, String fieldName) {
        return toFloat(attributes.get(fieldName));
    }

    public Float toFloat(AttributeValue val) {
        return val == null ? null : Float.valueOf(val.n());
    }

    public Character toChar(Map<String, AttributeValue> attributes, String fieldName) {
        return toChar(attributes.get(fieldName));
    }

    public Character toChar(AttributeValue val) {
        return val == null ? null : val.s().charAt(0);
    }

    public String toString(Map<String, AttributeValue> attributes, String fieldName) {
        return toString(attributes.get(fieldName));
    }

    public String toString(AttributeValue val) {
        return val == null ? null : val.s();
    }

    public Object toObject(Map<String, AttributeValue> attributes, String fieldName) {
        return toObject(attributes.get(fieldName));
    }

    public Object toObject(AttributeValue val) {
        if (val != null) {
            String strValue = val.s();
            try {
                return getMapper().readValue(strValue, Object.class);
            } catch (IOException e) {
                return strValue;
            }
        } else
            return null;
    }

    public <T extends Enum<T>> T toEnum(Map<String, AttributeValue> attributes, String fieldName, Class<T> enumType) {
        return toEnum(attributes.get(fieldName),enumType);
    }

    public <T extends Enum<T>> T toEnum(AttributeValue val, Class<T> enumType) {
        return val == null ? null : Enum.valueOf(enumType, val.s());
    }

    public Boolean toBoolean(Map<String, AttributeValue> attributes, String fieldName) {
        return toBoolean(attributes.get(fieldName));
    }

    public Boolean toBoolean( AttributeValue val) {
        return val == null ? null : val.bool();
    }

    public BigDecimal toBigDecimal(Map<String, AttributeValue> attributes, String fieldName) {
        return toBigDecimal(attributes.get(fieldName));
    }

    public BigDecimal toBigDecimal( AttributeValue val) {
        return val == null ? null : new BigDecimal(val.n());
    }

    public BigInteger toBigInteger(Map<String, AttributeValue> attributes, String fieldName) {
        return toBigInteger(attributes.get(fieldName));
    }

    public BigInteger toBigInteger( AttributeValue val) {
        return val == null ? null : new BigInteger(val.n());
    }

    public Date toDate(Map<String, AttributeValue> attributes, String fieldName) {
        return toDate(attributes.get(fieldName));
    }

    public Date toDate( AttributeValue val) {
        return val == null ? null : new Date(toLong(val));
    }

    public Instant toInstant(Map<String, AttributeValue> attributes, String fieldName) {
        return toInstant(attributes.get(fieldName));
    }

    public Instant toInstant( AttributeValue val) {
        return val == null ? null : Instant.ofEpochMilli(toLong(val));
    }

    public Calendar toCalendar( AttributeValue val) {
        return Optional.ofNullable(val).map(v -> {
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(toLong(v));
            return instance;
        }).orElse(null);
    }

    public Calendar toCalendar(Map<String, AttributeValue> attributes, String fieldName) {
        return toCalendar(attributes.get(fieldName));
    }

    // pojo as map to attr
    public AttributeValue mapAttrToAttr(Map<String, AttributeValue> dataFields) {
        return AttributeValue.builder().m(dataFields).build();
    }

    public Map<String, AttributeValue> attrToMapAttr(AttributeValue attributeValue) {
        return attributeValue == null ? Map.of() : attributeValue.m();
    }

    public AttributeValue listAttrToAttr(List<AttributeValue> dataFields) {
        return AttributeValue.builder().l(dataFields).build();
    }

    public List<AttributeValue> toListAttr(Map<String, AttributeValue> attributes, String fieldName) {
        return attributes.containsKey(fieldName) ? attributes.get(fieldName).l() : List.of();
    }

    //  java.util.Map<String, SimpleTypes> to Map<String,AttributeValue>
    public <T> Map<String, AttributeValue> mapOfObjToMapOfAttr(Map<String, T> value, Function<T, AttributeValue> objToAttr) {
        return value == null ? null : value.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, e -> objToAttr.apply(e.getValue())));
    }

    public <T> AttributeValue mapOfObjToAttr(Map<String, T> value, Function<T, AttributeValue> objToAttr) {
        return value == null ? null : AttributeValue.builder().m(value.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, e -> objToAttr.apply(e.getValue())))).build();
    }

    public Function<AttributeValue,Map<String,AttributeValue>> attrToMapOfAttr() {
        return AttributeValue::m;
    }

    public <T> Map<String, T> toMap(Map<String, AttributeValue> attributes, String fieldName, Function<AttributeValue, T> objToAttr) {
        if (attributes.containsKey(fieldName)) {
            return attributes.get(fieldName).m().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> objToAttr.apply(e.getValue())));
        } else
            return Map.of();
    }


    public ObjectMapper getMapper() {
        return mapper;
    }
}
