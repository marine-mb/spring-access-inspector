package com.theodo.tools.preauthorize.analyzer;

public interface AnnotationEvent {
    void foundErroneousAnnotation(String sourceLocation);
}
