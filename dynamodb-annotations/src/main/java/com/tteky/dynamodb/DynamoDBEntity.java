package com.tteky.dynamodb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DynamoDBEntity {

    /**
     * Should hold the table name, if empty uses class name as the table name
     */
    String value() default "";

    /**
     * By default computed as ./../dao
     * ie., for  test.entity.Model type, dao and mapper class will be under test.dao package
     * @return
     */
    String packageName() default "";

}
