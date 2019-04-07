package com.tteky.dynamodb.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.tteky.dynamodb.DynamoDBEntity;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;

public class Utils {

    public static String firstAsSmall(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static String firstAsCaps(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    static String daoPackageName(TypeElement entityTypeElement, ClassName entityClassName) {
        String preferred = entityTypeElement.getAnnotation(DynamoDBEntity.class).packageName();
        String fallback = entityClassName.packageName();
        return extractPackageName(preferred, fallback);
    }

    static String extractPackageName(String preferred, String fallback) {
        if(preferred.length() == 0) {
            if(fallback.contains(".")) { // at times package name could be just test with out any dots
                preferred = fallback.substring(0, fallback.lastIndexOf(".")) + ".dao";
            } else {
                preferred = "dao";
            }
        }
        return preferred;
    }

    static void generateSrcCode(ProcessingEnvironment environment, String packageName, TypeSpec mapperClazz) {

        JavaFile javaFile = JavaFile.builder(packageName, mapperClazz)
                .build();
        try {
            javaFile.writeTo(System.out);
            javaFile.writeTo(environment.getFiler());
        } catch (IOException e) {
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR,"Couldn't create java file");
        }
    }
}
