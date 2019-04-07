package com.ttkey.dynamodb.utils;

import java.util.Map;

public class Conditionals {

    public static boolean isNullOrEmpty(final String val) {
        return val == null || val.isEmpty();
    }

    public static boolean isNullOrEmpty(final String[] val){
        return val == null || val.length == 0;
    }

    public static boolean isNullOrEmpty(final Map<?,?> val){
        return val == null || val.isEmpty();
    }

}
