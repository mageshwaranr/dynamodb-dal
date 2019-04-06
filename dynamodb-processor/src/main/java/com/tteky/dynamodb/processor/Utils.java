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
        String packageName = entityTypeElement.getAnnotation(DynamoDBEntity.class).packageName();
        String s = entityClassName.packageName();
        if(packageName.length() == 0) {
            if(s.contains(".")) {
                packageName = s.substring(0, s.lastIndexOf(".")) + ".dao";
            } else {
                packageName = "dao";
            }
        }
        return packageName;
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
