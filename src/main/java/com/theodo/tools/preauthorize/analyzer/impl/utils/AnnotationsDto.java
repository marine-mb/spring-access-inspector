package com.theodo.tools.preauthorize.analyzer.impl.utils;

public class AnnotationsDto {
    String endpoint;
    String method;
    String preAuthorize;

    public AnnotationsDto(String endpoint, String method, String preAuthorize) {
        this.endpoint = endpoint;
        this.method = method;
        this.preAuthorize = preAuthorize;
    }

    public String endpoint() {
        return endpoint;
    }

    public String method() {
        return method;
    }

    public String preAuthorize() {
        return preAuthorize;
    }
}