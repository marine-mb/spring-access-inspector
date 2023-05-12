package com.theodo.tools.preauthorize.analyzer.impl.utils;

import spoon.reflect.declaration.CtElement;

public class SourceLocation {
    public static String getSourceLocation(CtElement ctElement) {
        return "file://" + ctElement.getPosition().getFile().getAbsolutePath() + ":" + ctElement.getPosition().getLine();
    }
}
