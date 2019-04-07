package com.tteky.dynamodb.processor;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.tteky.dynamodb.DynamoField;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;


@ToString
public class AnnotatedField {

    @Getter
    private Element element;
    private Name qualifiedClassName;
    private Name simpleClassName;
    @Getter
    private TypeMirror elementType;
    @Getter
    private TypeElement enclosingType;

    public AnnotatedField(Element element) {
        this.element = element;
        simpleClassName = element.getEnclosingElement().getSimpleName();
        enclosingType = ((TypeElement) element.getEnclosingElement());
        qualifiedClassName = enclosingType.getQualifiedName();
        elementType = element.asType();
    }

    public String getElementName() {
        return Utils.attributeName(this);
    }

    public String getCasedFieldName(){
        String input = this.element.getSimpleName().toString();
        return  Utils.firstAsCaps(input);
    }


    public String getSimpleFieldType(){
        TypeName typeName = ClassName.get(elementType);
        return simpleTypeOf(typeName);
    }

    public static String simpleTypeOf(TypeName typeName) {
        if(typeName instanceof ClassName) {
            return ((ClassName) typeName).simpleName();
        } else if (typeName instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) typeName).rawType.simpleName();
        }
        return typeName.toString();
    }

    public boolean isFieldParameterized() {
        return ClassName.get(elementType) instanceof ParameterizedTypeName;
    }

    public List<TypeName> fieldParameterInfo() {
        return ((ParameterizedTypeName) ClassName.get(elementType)).typeArguments;
    }
}
