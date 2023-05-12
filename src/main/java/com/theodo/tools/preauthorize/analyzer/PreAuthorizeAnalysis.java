package com.theodo.tools.preauthorize.analyzer;

import com.theodo.tools.preauthorize.analyzer.impl.PreAuthorizeAnnotationProcessing;
import com.theodo.tools.preauthorize.analyzer.impl.ast.ASTReader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine;
import spoon.reflect.CtModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@Slf4j
public class PreAuthorizeAnalysis implements Callable<Integer>, AnnotationEvent {
    @CommandLine.Parameters(paramLabel = "PROJECT_DIR", description = "Root project Directory (project to analyze)", defaultValue = ".", index = "0")
    protected String projectDirectory;

    @Getter
    private int errorCount = 0;

    public static void main(String[] args) {
        Configurator.setLevel("com.theodo.tools", Level.INFO);
        PreAuthorizeAnalysis analyzer = new PreAuthorizeAnalysis();
        int exitCode = new CommandLine(analyzer).execute(args);
        log.info("Process ended with exit code: {}", exitCode);
    }

    @Override
    public Integer call() throws Exception {
        try (Stream<File> walk = findPoms(projectDirectory)) { // For all maven projects found in directory
            walk.forEach(pomFile -> {
                CtModel astModel = ASTReader.readAst(pomFile); // Analyse JAVA AST
                PreAuthorizeAnnotationProcessing.visitAllAnnotations(astModel, this);
            });
        }

        if (errorCount > 0) {
            log.error("❌ Found {} erroneous security(s) in project(s)", errorCount);
            return 1; // ERROR
        }
        return 0;
    }

    public static Stream<File> findPoms(String basePath) throws IOException {
        //noinspection resource
        return Files.walk(Paths.get(basePath))
                .filter(path -> path.getFileName().toString().contains("pom.xml"))
                .map(Path::toFile);
    }

    @Override
    public void foundOkAnnotation(String content, String sourceLocation) {
        log.info("\t\tFound PreAuthorize '{}' at {}", content, sourceLocation);
    }

    @Override
    public void foundErroneousAnnotation(String sourceLocation) {
        log.error("\t\t❌ Erroneous security at '{}'", sourceLocation);
        errorCount++;
    }
}
