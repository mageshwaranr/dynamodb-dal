package com.tteky.dynamodb.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.lang.model.element.Modifier;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
public class MapperMethodGenerationTemplates {

//    private Map<String, MethodSpec> templates = new HashMap<>();
    private Map<String, Map.Entry<String, String>> supportedScalars = new HashMap<>();

    public MapperMethodGenerationTemplates() {
        this.initialize();
    }

    public void initialize() {
//        templates.put("float", typeToScalar(TypeName.FLOAT));
//        templates.put("int", typeToScalar(TypeName.INT));
//        templates.put("double", typeToScalar(TypeName.DOUBLE));
//        templates.put("long", typeToScalar(TypeName.LONG));
//        templates.put("char", typeToScalar(TypeName.CHAR));
//        templates.put("byte", typeToScalar(TypeName.BYTE));
//        templates.put("short", typeToScalar(TypeName.SHORT));
//        templates.put(Float.class.getCanonicalName(), typeToScalar(TypeName.FLOAT));
//        templates.put(Integer.class.getCanonicalName(), typeToScalar(TypeName.INT));
//        templates.put(Double.class.getCanonicalName(), typeToScalar(TypeName.DOUBLE));
//        templates.put(Long.class.getCanonicalName(), typeToScalar(TypeName.LONG));
//        templates.put(Character.class.getCanonicalName(), typeToScalar(TypeName.CHAR.box()));
//        templates.put(Byte.class.getCanonicalName(), typeToScalar(TypeName.BYTE));
//        templates.put(Short.class.getCanonicalName(), typeToScalar(TypeName.SHORT));
        supportedScalars.put("byte", Map.entry("byteToAttr", "toByte"));
        supportedScalars.put("Byte", Map.entry("byteToAttr", "toByte"));
        supportedScalars.put("short", Map.entry("shortToAttr", "toShort"));
        supportedScalars.put("Short", Map.entry("shortToAttr", "toShort"));
        supportedScalars.put("int", Map.entry("intToAttr", "toInt"));
        supportedScalars.put("Integer", Map.entry("intToAttr", "toInt"));
        supportedScalars.put("long", Map.entry("longToAttr", "toLong"));
        supportedScalars.put("Long", Map.entry("longToAttr", "toLong"));
        supportedScalars.put("double", Map.entry("doubleToAttr", "toDouble"));
        supportedScalars.put("Double", Map.entry("doubleToAttr", "toDouble"));
        supportedScalars.put("float", Map.entry("floatToAttr", "toFloat"));
        supportedScalars.put("Float", Map.entry("floatToAttr", "toFloat"));
        supportedScalars.put("char", Map.entry("charToAttr", "toChar"));
        supportedScalars.put("Character", Map.entry("charToAttr", "toChar"));
        supportedScalars.put("String", Map.entry("stringToAttr", "toString"));
        supportedScalars.put("Object", Map.entry("objectToAttr", "toObject"));
        supportedScalars.put("Enum", Map.entry("enumToAttr", "toEnum"));
        supportedScalars.put("Boolean", Map.entry("booleanToAttr", "toBoolean"));
        supportedScalars.put("boolean", Map.entry("booleanToAttr", "toBoolean"));
        supportedScalars.put("BigDecimal", Map.entry("bigDecimalToAttr", "toBigDecimal"));
        supportedScalars.put("BigInteger", Map.entry("bigIntegerToAttr", "toBigInteger"));
        supportedScalars.put("Date", Map.entry("dateToAttr", "toDate"));
        supportedScalars.put("Instance", Map.entry("instantToAttr", "toInstant"));
        supportedScalars.put("Calendar", Map.entry("calendarToAttr", "toCalendar"));
    }

    Collection<MethodSpec> generateTypeConversionMethods(CodeGenerationContext context) {
        Set<MethodSpec> specs = new HashSet<>();
        context.getDynamoFields().forEach((className, annotatedFields) -> {
            MethodSpec.Builder fromAttributes = MethodSpec.methodBuilder(attrToEntityMethodName(className))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterizedTypeName.get(Map.class, String.class, AttributeValue.class), "attributes")
                    .addStatement("$T entity = new $T()", className, className)// create new entity
                    .returns(className);
            MethodSpec.Builder toAttributes = MethodSpec.methodBuilder(entityToAttrMethodName(className))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(className, "entity")
                    .addStatement("$T<String, $T> fields = new $T<>()", Map.class, AttributeValue.class, HashMap.class) // create new attributes values map
                    .returns(ParameterizedTypeName.get(Map.class, String.class, AttributeValue.class));

            annotatedFields.forEach(annotatedField -> {
                log.info(" Processing Field Name: {}; Simple Field Type: {} ; AnnotatedFieldInfo: {}", annotatedField.getCasedFieldName(), annotatedField.getSimpleFieldType(), annotatedField);
                if (isScalar(annotatedField.getSimpleFieldType())) {
                    var entry = supportedScalars.get(annotatedField.getSimpleFieldType());
                    fromAttributes.addStatement("entity.set$L(super.$L(attributes,$S))", annotatedField.getCasedFieldName(), entry.getValue(), annotatedField.getElementName());
                    toAttributes.addStatement("fields.put($S,$L)", annotatedField.getElementName(), String.format("super.%s(entity.get%s())", entry.getKey(), annotatedField.getCasedFieldName()));
                } else {
                    TypeName typeName = ClassName.get(annotatedField.getElementType());
                    String name = typeName.toString();
                    if (typeName instanceof ParameterizedTypeName) {
                        name = ((ParameterizedTypeName) typeName).rawType.toString();
                    }
                    try {
                        Class<?> type = Class.forName(name);
                        if (Collection.class.isAssignableFrom(type)) {
                            // handle collection
                            Map.Entry<String,String>  itemMapperMethodName = supportedScalars.get("Object") ;
                            if(annotatedField.isFieldParameterized() ) {
                                TypeName itemParameterType = annotatedField.fieldParameterInfo().get(0);
                                itemMapperMethodName =  mapperMethodFor(itemParameterType);
                            }
                            // handle toAttributes part
                            String conditionStmt = String.format("if(entity.get%s() != null) \n \t",annotatedField.getCasedFieldName());
                            String collectionTemplate = "super.listAttrToAttr(entity.get%s().stream().map(this::%s).map(this::mapAttrToAttr).collect($T.toList()))";
                            String stmt =  String.format(collectionTemplate,annotatedField.getCasedFieldName(),itemMapperMethodName.getKey());
                            toAttributes.addStatement(conditionStmt +"fields.put($S,"+stmt + ")", annotatedField.getElementName(),Collectors.class);
                            // handle fromAttributes part
                            collectionTemplate = "super.toListAttr(attributes,$S).stream().map(this::attrToMapAttr).map(this::%s).collect($T.toList())";
                            stmt = String.format(collectionTemplate,itemMapperMethodName.getValue());
                            fromAttributes.addStatement("entity.set$L("+stmt+")", annotatedField.getCasedFieldName(), annotatedField.getElementName(),Collectors.class);
                        } else if (Map.class.isAssignableFrom(type)) {
                            // handle map
                            if(annotatedField.isFieldParameterized() && annotatedField.fieldParameterInfo().size() == 2) {
                                TypeName entryKeyParameterType = annotatedField.fieldParameterInfo().get(0);
                                TypeName entryValueParameterType =  annotatedField.fieldParameterInfo().get(1) ;
                                String entryKeyType = AnnotatedField.simpleTypeOf(entryKeyParameterType);
                                var entry = mapperMethodFor(entryValueParameterType);
                                if(String.class.getSimpleName().equals(entryKeyType)) {
                                    // handle toAttributes part
                                    String stmt;
                                    String simpleValueType = AnnotatedField.simpleTypeOf(entryValueParameterType);
                                    if(isScalar(simpleValueType)) {
                                         stmt = String.format("super.mapOfObjToAttr(entity.get%s(),this::%s)",annotatedField.getCasedFieldName(),entry.getKey());
//                                        stmt= String.format("super.mapAttrToAttr(super.mapOfObjToMapOfAttr(entity.get%s(),this::%s))",annotatedField.getCasedFieldName(),entry.getKey());
                                        toAttributes.addStatement("fields.put($S,"+stmt + ")", annotatedField.getElementName());
                                        stmt = String.format("super.toMap(attributes,$S, this::%s)",entry.getValue());
                                        fromAttributes.addStatement("entity.set$L("+stmt + ")", annotatedField.getCasedFieldName(),annotatedField.getElementName());
                                    } else {
//                                        Function<Info, Map<String,AttributeValue>> infoToAttributes = this::infoToAttributes;
//                                        Function<Info, AttributeValue> infoVFunction = infoToAttributes.andThen(this::mapAttrToAttr);
                                        toAttributes.addStatement("$T<$L,Map<String,AttributeValue>> $L = this::$L",Function.class,simpleValueType,entry.getKey(),entry.getKey());
                                        //%s.andThen(this::mapAttrToAttr);
                                        stmt= String.format("super.mapAttrToAttr(super.mapOfObjToMapOfAttr(entity.get%s(), %s.andThen(this::mapAttrToAttr)))",annotatedField.getCasedFieldName(),entry.getKey());
                                        toAttributes.addStatement("fields.put($S,"+stmt + ")", annotatedField.getElementName());

                                        stmt = String.format("super.toMap(attributes,$S, this.attrToMapOfAttr().andThen(this::%s))",entry.getValue());
                                        fromAttributes.addStatement("entity.set$L("+stmt + ")", annotatedField.getCasedFieldName(),annotatedField.getElementName());
                                    }
                                } else {
                                    context.addWarning(format("Unable to handle field `%s` in class `%s`. Map key should be string.", annotatedField.getElementName(), className));
                                }
                            }
                        } else {
                            context.addWarning(format("Unable to handle field `%s` in class `%s`", annotatedField.getElementName(), className));
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("unable to process field " + annotatedField);
                    }
                }
            });
            fromAttributes.addStatement("return entity");
            toAttributes.addStatement("return fields");
            specs.add(fromAttributes.build());
            specs.add(toAttributes.build());
        });

        return specs;

    }

    private boolean isScalar(String simpleFieldType) {
        return supportedScalars.containsKey(simpleFieldType);
    }

    public Map.Entry<String,String> mapperMethodFor( TypeName type) {
        String entryValueType = AnnotatedField.simpleTypeOf(type);
        Map.Entry<String, String> entry = supportedScalars.get("Object") ;
        if(isScalar(entryValueType)) {
            entry = supportedScalars.get(entryValueType);
        } else if(type instanceof ClassName){
            ClassName itemParameterClass = (ClassName) type;
            entry = Map.entry(entityToAttrMethodName(itemParameterClass),attrToEntityMethodName(itemParameterClass));
        }
        return entry;
    }

    static String attrToEntityMethodName(ClassName className) {
        return format("%sFromAttributes", Utils.firstAsSmall(className.simpleName()));
    }

    static String entityToAttrMethodName(ClassName className) {
        return format("%sToAttributes", Utils.firstAsSmall(className.simpleName()));
    }

//    private MethodSpec typeToScalar(TypeName typeName) {
//        String methodName = format("%sToScalarValue", typeName.toString());
//        return MethodSpec.methodBuilder(methodName)
//                .addModifiers(Modifier.PROTECTED)
//                .addParameter(typeName, "val")
//                .returns(String.class)
//                .addStatement(" return $T.valueOf($L)", String.class, "val")
//                .build();
//    }
}
