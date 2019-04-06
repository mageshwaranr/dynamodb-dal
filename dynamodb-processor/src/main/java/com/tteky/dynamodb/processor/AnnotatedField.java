package com.tteky.dynamodb.processor;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

@Getter
@Setter
@ToString
public class AnnotatedField {

    Element element;
    Name qualifiedClassName;
    Name simpleClassName;
    Name elementName;
    TypeMirror elementType;
    TypeElement enclosingType;

    public AnnotatedField(Element element) {
        this.element = element;
        elementName = element.getSimpleName();
        simpleClassName = element.getEnclosingElement().getSimpleName();
        enclosingType = ((TypeElement) element.getEnclosingElement());
        qualifiedClassName = enclosingType.getQualifiedName();
        elementType = element.asType();

    }

    public String getCasedFieldName(){
        String input = this.elementName.toString();
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
