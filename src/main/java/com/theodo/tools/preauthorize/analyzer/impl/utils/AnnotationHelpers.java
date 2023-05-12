package com.theodo.tools.preauthorize.analyzer.impl.utils;

import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnnotationHelpers {
    // Search all annotations of a method, even if annotation are set on a super-class or on an interface
    public static List<CtAnnotation<? extends Annotation>> getAllAnnotationsForMethod(CtMethod<?> ctMethod) {
        List<CtAnnotation<? extends Annotation>> methodAnnotations = new ArrayList<>(ctMethod.getAnnotations());
        CtType<?> declaringType = ctMethod.getDeclaringType();
        if (declaringType == null) return methodAnnotations;

        // Add all method annotations found in possible interfaces and super class
        Set<CtTypeReference<?>> superInterfaces = new HashSet<>(declaringType.getSuperInterfaces());
        CtTypeReference<?> superclass = declaringType.getSuperclass();
        if(superclass != null) {
            superInterfaces.add(superclass);
        }

        // Add all method annotations found in possible interfaces and super class
        for (CtTypeReference<?> superInterface : superInterfaces) {
            CtType<?> declaration = superInterface.getTypeDeclaration();
            if (declaration != null) {
                for (CtMethod<?> superMethod : declaration.getAllMethods()) {
                    if (ctMethod.isOverriding(superMethod)) {
                        methodAnnotations.addAll(superMethod.getAnnotations());
                    }
                }
            }
        }
        return methodAnnotations;
    }

}
