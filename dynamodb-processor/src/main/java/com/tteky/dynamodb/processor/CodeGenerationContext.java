package com.tteky.dynamodb.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public class CodeGenerationContext {

    private Map<ClassName, List<AnnotatedField>> dynamoFields = new HashMap<>();

    private AnnotatedField hashField, rangeField;

    private List<String> warnings = new ArrayList<>();

    private TypeElement entity;

    public Map<ClassName, List<AnnotatedField>> getDynamoFields() {
        return dynamoFields;
    }

    public void add(ClassName className, AnnotatedField... fields) {
        dynamoFields.putIfAbsent(className, new ArrayList<>());
        dynamoFields.get(className).addAll(Arrays.asList(fields));
    }

    public Set<? extends Element> findAnnotatedFields(RoundEnvironment roundEnv, Class<? extends Annotation> annotation) {
        Set<? extends Element> allFields = roundEnv.getElementsAnnotatedWith(annotation);
        System.out.println("AllFields : "+allFields);
        Set<TypeName> relatedTypes = new HashSet<>();
        allFields.forEach(e -> {
            TypeMirror typeMirror = e.asType();
            TypeName typeName = ClassName.get(typeMirror);
            relatedTypes.add(typeName);
            if(typeName instanceof ParameterizedTypeName) {
                List<TypeName> typeArguments = ((ParameterizedTypeName) typeName).typeArguments;
                relatedTypes.addAll(typeArguments);
            }
        });
        System.out.println("ALl relatedTypes:"+relatedTypes);

        Map<Element, ? extends List<? extends Element>> directFieldsOfEntity = allFields.stream().filter(e -> e.getEnclosingElement().equals(this.getEntity()))
                .collect(Collectors.groupingBy(Element::getEnclosingElement));
        System.out.println("directFieldsOfEntity:"+directFieldsOfEntity);
        Set<? extends Element> collect = allFields
                .stream()
                .filter(ele -> {
                    boolean inScope = ele.getEnclosingElement().equals(this.getEntity());
                    inScope = inScope || relatedTypes.contains(ClassName.get(((TypeElement) ele.getEnclosingElement())));
                    System.out.println("Element "+ele + " isRelated?" +inScope);
                    return inScope;
                })
                .collect(Collectors.toSet());
        System.out.println("All Eligible Fields:"+collect);
        return collect;
    }

    public Set<TypeMirror> fieldTypes() {
        return  dynamoFields.values().stream()
                .flatMap(annotatedFields -> annotatedFields.stream().map(AnnotatedField::getElementType))
                .collect(Collectors.toSet());
    }

    public void addWarning(String warning){
        this.warnings.add(warning);
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public AnnotatedField getHashField() {
        return hashField;
    }

    public void setHashField(AnnotatedField hashField) {
        this.hashField = hashField;
    }

    public AnnotatedField getRangeField() {
        return rangeField;
    }

    public void setRangeField(AnnotatedField rangeField) {
        this.rangeField = rangeField;
    }

    public TypeElement getEntity() {
        return entity;
    }

    public void setEntity(TypeElement entity) {
        this.entity = entity;
    }
}
