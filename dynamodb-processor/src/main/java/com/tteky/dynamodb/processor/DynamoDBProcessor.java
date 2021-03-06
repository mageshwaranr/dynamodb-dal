package com.tteky.dynamodb.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.tteky.dynamodb.*;
import com.ttkey.dynamodb.dao.DynamoDBBaseDao;
import com.ttkey.dynamodb.mapper.DynamoDBBaseMapper;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import static com.tteky.dynamodb.processor.MapperMethodGenerationTemplates.attrToEntityMethodName;
import static com.tteky.dynamodb.processor.MapperMethodGenerationTemplates.entityToAttrMethodName;
import static com.tteky.dynamodb.processor.Utils.attributeName;
import static com.tteky.dynamodb.processor.Utils.daoPackageName;
import static java.lang.String.format;

@Slf4j
@AutoService(Processor.class)
public class DynamoDBProcessor extends AbstractProcessor {

    private static final List<Class<? extends Annotation>> DYNAMO_TYPES = List.of(DynamoDBEntity.class, DynamoField.class);


    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        log.info("Initialized");
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_11;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : DYNAMO_TYPES) {
            annotations.add(annotation.getCanonicalName());
        }
        return annotations;
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(DynamoDBEntity.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error("Only class can be annotated with DynamoDBEntity", annotatedElement);
                return false;
            }
            TypeElement typeElement = (TypeElement) annotatedElement;
            CodeGenerationContext context = new CodeGenerationContext();
            context.setEntity(typeElement);
            if (!populateHashKeyInfo(roundEnv, context) || !populateRangeKeyInfo(roundEnv, context)) {
                return false;
            }
            if (!generateMapper(roundEnv, typeElement, context)) {
                return false;
            } else if (!buildDao( typeElement, context)) {
                return false;
            }
        }
        return true;
    }

    private boolean buildDao(TypeElement entityTypeElement, CodeGenerationContext context) {
        ClassName entityClassName = ClassName.get(entityTypeElement);
        String packageName = daoPackageName(entityTypeElement, entityClassName);

        //  protected abstract T convert(Map<String, AttributeValue> item);
        ParameterizedTypeName mapOfStringVsAttr = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(AttributeValue.class));
        MethodSpec convertToEntityMethod = MethodSpec.methodBuilder("convert")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapOfStringVsAttr, "attributes")
                .addStatement("return mapper.$L(attributes)", attrToEntityMethodName(entityClassName))
                .returns(entityClassName)
                .build();

//
//        protected abstract Map<String, AttributeValue> convert(T entity);
        MethodSpec convertFromEntityMethod = MethodSpec.methodBuilder("convert")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(entityClassName, "entity")
                .addStatement("return mapper.$L(entity)", entityToAttrMethodName(entityClassName))
                .returns(mapOfStringVsAttr)
                .build();


        MethodSpec keyFieldNamesMethod = MethodSpec.methodBuilder("getKeyFieldNames")
                .addModifiers(Modifier.PUBLIC)
                .addStatement(keyFieldNamesReturnStmt(context))
                .returns(String[].class)
                .build();

//         public DynamoBaseDao(DynamoDbClient dynamoDb, String tableName) {
//            this.dynamoDb = dynamoDb;
//            this.tableName = tableName;
//        }
        String value = entityTypeElement.getAnnotation(DynamoDBEntity.class).value();
        String tableName = value.length() == 0 ? entityClassName.simpleName() : value;

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(DynamoDbClient.class, "dynamoDb")
                .addStatement("super(dynamoDb,$S)", tableName)
                .build();

        FieldSpec build = FieldSpec.builder(ClassName.get(packageName, mapperClassName(entityClassName)), "mapper", Modifier.PRIVATE)
                .initializer("new $L()", mapperClassName(entityClassName))
                .build();
        TypeSpec daoClazz = TypeSpec.classBuilder(format("%sDao", entityClassName.simpleName()))
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(DynamoDBBaseDao.class), entityClassName))
                .addMethods(List.of(convertFromEntityMethod, convertToEntityMethod, keyFieldNamesMethod))
                .addMethod(constructor)
                .addField(build)
                .build();

        generateSrcCode(entityTypeElement, entityClassName, daoClazz);

        return true;
    }

    // implements return statement for //        protected abstract String[] getKeyFieldNames();
    private String keyFieldNamesReturnStmt(CodeGenerationContext ctxt) {
        StringBuilder returnValues = new StringBuilder("return new String[]{");
        returnValues.append("\"").append(attributeName(ctxt.getHashField())).append("\"");
        if (ctxt.getRangeField() != null) {
            returnValues.append(", ").append("\"").append(attributeName(ctxt.getRangeField())).append("\"");
        }
        returnValues.append("}");
        return returnValues.toString();
    }

    private boolean populateHashKeyInfo(RoundEnvironment roundEnv, CodeGenerationContext ctxt) {
        Set<? extends Element> annotatedWith = findAnnotatedElements(roundEnv, DynamoHashKey.class, ctxt);
        if (annotatedWith.size() != 1) {
            String collect = collectFieldNames(annotatedWith);
            error(String.format("Exactly one field should be annotated with DynamoHashKey but found '%s'", collect));
            return true;
        } else {
            var hashField = findFirstAnnotatedField(annotatedWith);
            ctxt.setHashField(hashField);
            return true;
        }
    }

    private boolean populateRangeKeyInfo(RoundEnvironment roundEnv, CodeGenerationContext ctxt) {
        Set<? extends Element> annotatedWith = findAnnotatedElements(roundEnv, DynamoRangeKey.class, ctxt);

        if (annotatedWith.size() > 1) {
            String collect = collectFieldNames(annotatedWith);
            error(String.format("At max one field should be annotated with DynamoRangeKey but found '%s'", collect));
            return false;
        } else if (annotatedWith.size() == 1) {
            var rangeField = findFirstAnnotatedField(annotatedWith);
            ctxt.setRangeField(rangeField);
        }
        return true;
    }

    private Set<? extends Element> findAnnotatedElements(RoundEnvironment roundEnv, Class<? extends Annotation> annotation, CodeGenerationContext ctxt) {
        return roundEnv.getElementsAnnotatedWith(annotation)
                .stream()
                .filter(ele -> ele.getEnclosingElement().equals(ctxt.getEntity()))
                .collect(Collectors.toSet());
    }

    private AnnotatedField findFirstAnnotatedField(Set<? extends Element> annotatedWith) {
        return new AnnotatedField(annotatedWith.stream().findAny().get());
    }

    private String collectFieldNames(Set<? extends Element> annotatedWith) {
        return annotatedWith.stream().map(e -> e.getSimpleName().toString()).collect(Collectors.joining(","));
    }

    private void generateSrcCode(TypeElement entityTypeElement, ClassName entityClassName, TypeSpec mapperClazz) {
        String packageName = daoPackageName(entityTypeElement, entityClassName);
        Utils.generateSrcCode(super.processingEnv, packageName, mapperClazz);
    }

    private boolean generateMapper(RoundEnvironment roundEnv, TypeElement entityTypeElement, CodeGenerationContext context) {
        log.info("entityTypeElement " + entityTypeElement);
        ClassName entityClassName = ClassName.get(entityTypeElement);

        Set<Element> annotatedFields = new HashSet<>();
        annotatedFields.addAll(context.findAnnotatedFields(roundEnv, DynamoField.class));
        annotatedFields.addAll(context.findAnnotatedFields(roundEnv, DynamoHashKey.class));
        annotatedFields.addAll(context.findAnnotatedFields(roundEnv, DynamoRangeKey.class));
        for (Element annotatedElement : annotatedFields) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error("Only field can be annotated with DynamoField", annotatedElement);
                return false;
            }
            VariableElement typeElement = (VariableElement) annotatedElement;

            AnnotatedField annotatedField = new AnnotatedField(typeElement);
            ClassName key = ClassName.get(annotatedField.getEnclosingType());
            context.add(key, annotatedField);
            log.info("VariableElement {}" , annotatedField);
        }


        Collection<MethodSpec> methodSpecs = new MapperMethodGenerationTemplates().generateTypeConversionMethods(context);

        TypeSpec mapperClazz = TypeSpec.classBuilder(mapperClassName(entityClassName))
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(DynamoDBBaseMapper.class))
                .addMethods(methodSpecs)
                .build();

        generateSrcCode(entityTypeElement, entityClassName, mapperClazz);


        return true;
    }

    private String mapperClassName(ClassName entityClassName) {
        return format("%sMapper", entityClassName.simpleName());
    }


    private void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

    private void note(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }
}
