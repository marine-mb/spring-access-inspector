package com.theodo.tools.preauthorize.analyzer.impl;

import com.theodo.tools.preauthorize.analyzer.AnnotationEvent;
import com.theodo.tools.preauthorize.analyzer.impl.utils.LiteralExtraction;
import com.theodo.tools.preauthorize.analyzer.impl.utils.SourceLocation;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PreAuthorizeAnnotationProcessing {
    public static void visitAllAnnotations(CtModel astModel, AnnotationEvent annotationEvent) {
        ControllerEndpointDiscovery.analyzeControllers(astModel.getRootPackage(),
                (ctClass, ctMethod, verb, path) -> {
                    log.info("🪴 Found '{}' endpoint '{}' in method '{}' in class '{}' at {}", verb, path,
                            ctMethod.getSimpleName(), ctClass.getSimpleName(),
                            SourceLocation.getSourceLocation(ctMethod));

                    analysePreAuthorize(ctClass, ctMethod, annotationEvent);
                });
    }

    private static void analysePreAuthorize(CtClass<?> ctClass, CtMethod<?> ctMethod, AnnotationEvent annotationEvent) {
        List<CtAnnotation<? extends Annotation>> ctPreAuthorizeAnnotations = getPreAuthorizeAnnotations(ctClass, ctMethod);

        if (ctPreAuthorizeAnnotations.isEmpty()) {
            log.warn("🚨 No Preauthorize annotation found");
            annotationEvent.foundErroneousAnnotation(SourceLocation.getSourceLocation(ctMethod));
        } else {
            CtAnnotation<? extends Annotation> ctAnnotation = ctPreAuthorizeAnnotations.get(0);
            if (ctAnnotation.getValues().isEmpty()) {
                log.warn("🚨 No Preauthorize annotation found");
                annotationEvent.foundErroneousAnnotation(SourceLocation.getSourceLocation(ctMethod));
            } else {
                CtExpression<?> preAuthorizeSPEL = ctAnnotation.getValue("value");
                String extract = LiteralExtraction.extract(preAuthorizeSPEL, false);
                annotationEvent.foundOkAnnotation(extract, SourceLocation.getSourceLocation(ctMethod));
            }
        }
    }

    private static List<CtAnnotation<? extends Annotation>> getPreAuthorizeAnnotations(CtClass<?> ctClass, CtMethod<?> ctMethod) {
        List<CtAnnotation<?>> annotations = ctMethod.getElements(new TypeFilter<>(CtAnnotation.class)); // annotations found on methods
        annotations.addAll(ctClass.getElements(new TypeFilter<>(CtAnnotation.class))); // annotations found on class
        return addPreAuthorizedAnnotations(annotations);
    }

    private static List<CtAnnotation<?>> addPreAuthorizedAnnotations(List<CtAnnotation<?>> annotations) {
        List<CtAnnotation<?>> tmp = new ArrayList<>();
        for (CtAnnotation<?> annotation : annotations) {
            if (isPreAuthorizeAnnotation(annotation.getAnnotationType())) tmp.add(annotation);
            else {
                // Annotation can be a homemade annotation that is annotated with PreAuthorize itself (recursive search)
                CtTypeReference<?> annotationType = annotation.getAnnotationType();
                CtType<?> typeDeclaration = annotationType.getTypeDeclaration();
                if (typeDeclaration != null && !typeDeclaration.getAnnotations().isEmpty()) {
                    for (CtAnnotation<?> parentAnnotation : typeDeclaration.getAnnotations()) {
                        if (isPreAuthorizeAnnotation(parentAnnotation.getAnnotationType())) tmp.add(parentAnnotation);
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
