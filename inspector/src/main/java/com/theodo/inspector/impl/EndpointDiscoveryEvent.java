package com.theodo.inspector.impl;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

public interface EndpointDiscoveryEvent {
    void foundEndpoint(CtClass<?> ctClass, CtMethod<?> ctMethod, String verb, String path);
}
