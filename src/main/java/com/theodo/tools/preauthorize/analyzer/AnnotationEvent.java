package com.theodo.tools.preauthorize.analyzer;

public interface AnnotationEvent {
    void foundOkAnnotation(String content);

    void foundErroneousAnnotation(String sourceLocation);
}
