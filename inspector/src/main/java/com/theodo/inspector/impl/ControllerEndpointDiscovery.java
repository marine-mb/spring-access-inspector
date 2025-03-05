package com.theodo.inspector.impl;

import com.theodo.inspector.impl.utils.LiteralExtraction;
import com.theodo.inspector.impl.utils.UriNormalizer;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.theodo.inspector.impl.utils.AnnotationHelpers.getAllAnnotationsForMethod;

public class ControllerEndpointDiscovery {
    private static final Set<String> POSSIBLE_CLASS_CONTEXT =
            Set.of("RestController", "RequestMapping"); // Annotation for Controller
    private static final Set<String> POSSIBLE_MAPPINGS =
            Set.of("DeleteMapping", "GetMapping", "PostMapping", "PutMapping", "PatchMapping"); // Annotations for methods

    public static void analyzeControllers(CtPackage rootPackage, EndpointDiscoveryEvent discoveryEvent) {
        List<CtClass<?>> ctClasses = rootPackage.getElements(new TypeFilter<>(CtClass.class));
        for (CtClass<?> ctClass : ctClasses) {
            List<CtAnnotation<? extends Annotation>> annotations = ctClass.getAnnotations();
            List<CtAnnotation<? extends Annotation>> matchingAnnotations = filterControllerAnnotations(annotations);

            if (matchingAnnotations.isEmpty()) continue; // Not a controller

            Collection<String> controllerContext = getControllerUriBaseContext(matchingAnnotations);
            analyzeMethodsMappings(ctClass, controllerContext, discoveryEvent);
        }
    }

    private static List<CtAnnotation<? extends Annotation>> filterControllerAnnotations(List<CtAnnotation<? extends Annotation>> annotations) {
        return annotations.stream().filter(ctAnnotation ->
                POSSIBLE_CLASS_CONTEXT.contains(ctAnnotation.getName())).toList();
    }

    private static Collection<String> getControllerUriBaseContext(List<CtAnnotation<? extends Annotation>> matchingAnnotations) {
        List<String> contexts = matchingAnnotations.stream()
                .flatMap(annotation -> getAnnotationContextValues(annotation).stream()).toList();

        List<String> nonEmptyContexts = contexts.stream().filter(s -> !s.isBlank()).toList();
        if (nonEmptyContexts.isEmpty()) {
            return List.of("");
        }
        return nonEmptyContexts;
    }

    private static Collection<String> getAnnotationContextValues(CtAnnotation<? extends Annotation> ctAnnotation) {

        AnnotationVisitor visitor = new AnnotationVisitor();
        CtExpression<?> controllerContextExpression = ctAnnotation.getValue("value");
        controllerContextExpression.accept(visitor);

        if (ctAnnotation.getValues().containsKey("path")) {
            CtExpression<?> controllerPathExpression = ctAnnotation.getValue("path");
            controllerPathExpression.accept(visitor);
        }
        return visitor.literals;
    }

    private static void analyzeMethodsMappings(CtClass<?> ctClass, Collection<String> controllerContext, EndpointDiscoveryEvent discoveryEvent) {
        ctClass.accept(new CtScanner() {
            @Override
            public <T> void visitCtMethod(CtMethod<T> ctMethod) {
                List<CtAnnotation<? extends Annotation>> methodAnnotations = getAllAnnotationsForMethod(ctMethod);

                methodAnnotations.forEach(methodAnnotation -> {
                    String name = methodAnnotation.getName();
                    if (POSSIBLE_MAPPINGS.contains(name)) {
                        foundEndpoint(ctMethod, methodAnnotation, name);
                    }
                    if ("RequestMapping".equals(name)) {
                        foundEndpoint(ctMethod, methodAnnotation, methodAnnotation.getName());
                    }
                });
            }

            private <T> void foundEndpoint(CtMethod<T> ctMethod, CtAnnotation<? extends Annotation> methodAnnotation, String verb) {
                Collection<String> methodContext = getAnnotationContextValues(methodAnnotation);
                if (methodContext.isEmpty()) {
                    methodContext.add("");
                }
                for (String context : methodContext) {
                    for (String controller : controllerContext) {
                        String exposedEndpoint = controller + UriNormalizer.normalizeUri(context);
                        if (exposedEndpoint.isBlank()) continue;

                        String path = UriNormalizer.normalizeUri(exposedEndpoint);
                        discoveryEvent.foundEndpoint(ctClass, ctMethod, verb, path);
                    }
                }
            }
        });
    }


    private static class AnnotationVisitor extends CtScanner {
        private final Set<String> literals = new HashSet<>();

        @Override
        public <T> void visitCtLiteral(CtLiteral<T> literal) {
            String extract = LiteralExtraction.extract(literal, true);
            if (extract != null) literals.add(extract);
        }

        @Override
        public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
            String extract = LiteralExtraction.extract(fieldRead, true);
            if (extract != null) literals.add(extract);
        }

        @Override
        public <T> void visitCtVariableRead(CtVariableRead<T> variableRead) {
            String extract = LiteralExtraction.extract(variableRead, true);
            if (extract != null) literals.add(extract);
        }

        @Override
        public <T> void visitCtNewArray(CtNewArray<T> newArray) {
            List<CtExpression<?>> elements = newArray.getElements();
            for (CtExpression<?> element : elements) {
                String extract = LiteralExtraction.extract(element, true);
                if (extract != null) literals.add(extract);
            }
        }
    }
}
