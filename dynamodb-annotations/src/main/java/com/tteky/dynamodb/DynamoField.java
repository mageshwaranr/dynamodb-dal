package com.tteky.dynamodb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface DynamoField {

    /**
     * Should hold the field name, if empty its inferred from member variable
     */
    String value() default "";

    /**
     * Should hold the type of the field, if empty its inferred from Java type
     */
    String type() default "";


}
