package com.theodo.tools.preauthorize.analyzer;

public interface AnnotationEvent {
    void foundOkAnnotation(String content, String sourceLocation);
    void foundErroneousAnnotation(String sourceLocation);
}
