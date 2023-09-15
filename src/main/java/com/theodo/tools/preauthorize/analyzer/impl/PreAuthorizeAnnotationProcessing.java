package com.theodo.tools.preauthorize.analyzer.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.theodo.tools.preauthorize.analyzer.AnnotationEvent;
import com.theodo.tools.preauthorize.analyzer.impl.utils.AnnotationsDto;
import com.theodo.tools.preauthorize.analyzer.impl.utils.LiteralExtraction;
import com.theodo.tools.preauthorize.analyzer.impl.utils.SourceLocation;

import lombok.extern.slf4j.Slf4j;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.FilteringOperator;
import spoon.reflect.visitor.filter.TypeFilter;

@Slf4j
public class PreAuthorizeAnnotationProcessing {
    public static List<AnnotationsDto> visitAllAnnotations(CtModel astModel, AnnotationEvent annotationEvent) {
        List<AnnotationsDto> annotations = new ArrayList<>();

        ControllerEndpointDiscovery.analyzeControllers(astModel.getRootPackage(),
                (ctClass, ctMethod, verb, path) -> {
                    String preAuthorize = analyzePreAuthorize(ctClass, ctMethod, annotationEvent);
                    AnnotationsDto annotation = new AnnotationsDto(path, verb, preAuthorize);
                    annotations.add(annotation);

                    log.info("\n\n\n🪴 Found '{}' endpoint '{}' in method '{}' in class '{}' at {}.\nPreAuthorize: {}",
                            verb, path,
                            ctMethod.getSimpleName(),
                            ctClass.getSimpleName(),
                            SourceLocation.getSourceLocation(ctMethod),
                            preAuthorize);

                });
        return annotations;
    }

    private static String analyzePreAuthorize(CtClass<?> ctClass, CtMethod<?> ctMethod,
            AnnotationEvent annotationEvent) {
        List<CtAnnotation<? extends Annotation>> ctPreAuthorizeAnnotations = getPreAuthorizeAnnotations(ctClass,
                ctMethod);
        String noPreauthorizeMessage = "🚨🚨🚨 No Preauthorize annotation found 🚨🚨🚨";
        String preAuthorizeAnnotation = noPreauthorizeMessage;

        if (ctPreAuthorizeAnnotations.isEmpty()) {
            log.warn(noPreauthorizeMessage);
            annotationEvent.foundErroneousAnnotation(SourceLocation.getSourceLocation(ctMethod));
        } else {
            CtAnnotation<? extends Annotation> ctAnnotation = ctPreAuthorizeAnnotations.get(0);
            if (ctAnnotation.getValues().isEmpty()) {
                log.warn("🚨 No Preauthorize annotation found");
                annotationEvent.foundErroneousAnnotation(SourceLocation.getSourceLocation(ctMethod));
            } else {
                CtExpression<?> preAuthorizeSPEL = ctAnnotation.getValue("value");
                preAuthorizeAnnotation = LiteralExtraction.extract(preAuthorizeSPEL, false);
            }
        }
        return preAuthorizeAnnotation;
    }

    private static List<CtAnnotation<? extends Annotation>> getPreAuthorizeAnnotations(CtClass<?> ctClass,
            CtMethod<?> ctMethod) {
        List<CtAnnotation<?>> annotations = ctMethod.getElements(new TypeFilter<>(CtAnnotation.class)); // annotations
                                                                                                        // found on
                                                                                                        // methods
        annotations.addAll(ctClass.getElements(getFilter())); // annotations found on class
        return addPreAuthorizedAnnotations(annotations);
    }

    private static Filter<CtAnnotation<?>> getFilter() {
        return new CompositeFilter<>(FilteringOperator.INTERSECTION, new TypeFilter<>(CtAnnotation.class),
                annotation -> annotation.getParent(CtMethod.class) == null);
    }

    private static List<CtAnnotation<?>> addPreAuthorizedAnnotations(List<CtAnnotation<?>> annotations) {
        List<CtAnnotation<?>> tmp = new ArrayList<>();
        for (CtAnnotation<?> annotation : annotations) {
            if (isPreAuthorizeAnnotation(annotation.getAnnotationType()))
                tmp.add(annotation);
            else {
                // Annotation can be a homemade annotation that is annotated with PreAuthorize
                // itself (recursive search)
                CtTypeReference<?> annotationType = annotation.getAnnotationType();
                CtType<?> typeDeclaration = annotationType.getTypeDeclaration();
                if (typeDeclaration != null && !typeDeclaration.getAnnotations().isEmpty()) {
                    for (CtAnnotation<?> parentAnnotation : typeDeclaration.getAnnotations()) {
                        if (isPreAuthorizeAnnotation(parentAnnotation.getAnnotationType()))
                            tmp.add(parentAnnotation);
                    }
                }
            }
        }
        return tmp;
    }

    private static boolean isPreAuthorizeAnnotation(CtTypeReference<? extends Annotation> annotationType) {
        return annotationType.getQualifiedName().contains("PreAuthorize") &&
                annotationType.getQualifiedName().contains("org.springframework.security");
    }

}
