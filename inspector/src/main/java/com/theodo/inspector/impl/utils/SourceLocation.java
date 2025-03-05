package com.theodo.inspector.impl.utils;

import spoon.reflect.declaration.CtElement;

public class SourceLocation {
    public static String getSourceLocation(CtElement ctElement) {
        return "file: " + ctElement.getPosition().getFile().getPath(); //  + ":" + ctElement.getPosition().getLine();
    }
}
