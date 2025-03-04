package com.theodo.inspector.impl.ast;

import java.io.File;

import lombok.extern.slf4j.Slf4j;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;

@Slf4j
public class ASTReader {
    public static CtModel readAst(File pomFile) {
        try {
            log.info("🌱 Analyzing Module directory '{}' with POM {}", pomFile.getParentFile().getName(), "file://" + pomFile.getAbsolutePath());
            MavenLauncher launcher = new MavenLauncher(pomFile.toString(), MavenLauncher.SOURCE_TYPE.ALL_SOURCE, true);
            launcher.getEnvironment().setNoClasspath(true);
            launcher.getEnvironment().setIgnoreDuplicateDeclarations(true);
            launcher.getEnvironment().setComplianceLevel(21);
            return launcher.buildModel();
        } finally {
            log.info("🌱 Module has been read and Analyzed");
        }
    }
}
