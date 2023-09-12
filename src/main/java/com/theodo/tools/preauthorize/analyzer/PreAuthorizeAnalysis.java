package com.theodo.tools.preauthorize.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import com.theodo.tools.preauthorize.analyzer.impl.PreAuthorizeAnnotationProcessing;
import com.theodo.tools.preauthorize.analyzer.impl.ast.ASTReader;
import com.theodo.tools.preauthorize.analyzer.impl.utils.AnnotationsDto;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import spoon.reflect.CtModel;

@Slf4j
public class PreAuthorizeAnalysis implements Callable<Integer>, AnnotationEvent {
    @CommandLine.Parameters(paramLabel = "PROJECT_DIR", description = "Root project Directory (project to analyze)", defaultValue = ".", index = "0")
    protected String projectDirectory;

    @Getter
    private int errorCount = 0;

    public static void main(String[] args) {
        Configurator.setLevel("com.theodo.tools", Level.ERROR);
        PreAuthorizeAnalysis analyzer = new PreAuthorizeAnalysis();
        int exitCode = new CommandLine(analyzer).execute(args);
        log.info("Process ended with exit code: {}", exitCode);
    }

    @Override
    public Integer call() throws Exception {
        try (Stream<File> walk = findPoms(projectDirectory)) { // For all maven projects found in directory
            List<AnnotationsDto> annotations = new ArrayList<>();
            walk.forEach(pomFile -> {
                CtModel astModel = ASTReader.readAst(pomFile); // Analyze JAVA AST
                List<AnnotationsDto> temporaryAnnotation = PreAuthorizeAnnotationProcessing
                        .visitAllAnnotations(astModel, this);
                annotations.addAll(temporaryAnnotation);

            });
            displayTable(annotations);
        }
        return 0;
    }

    public static void displayTable(List<AnnotationsDto> annotations) {
        // ANSI escape sequences for colors and formatting
        String reset = "\u001B[0m";
        String bold = "\u001B[1m";
        String cyan = "\u001B[36m";
        String yellow = "\u001B[33m";

        // Calculate the maximum width for each column
        int maxEndpointWidth = getMaxColumnWidth(annotations, AnnotationsDto::endpoint);
        int maxMethodWidth = getMaxColumnWidth(annotations, AnnotationsDto::method);
        int offset = 7;

        // Print the table headers with colors and formatting
        System.out.println("\n\n" + bold + "#######################");
        System.out.printf("%-" + (maxEndpointWidth + offset) + "s| %-" + (maxMethodWidth + offset) + "s| %s%s\n",
                "Endpoint", "Method", "PreAuthorize", reset);

        // Iterate over the ArrayList and print each person's data
        for (AnnotationsDto annotation : annotations) {
            System.out.printf("%-" + (maxEndpointWidth + offset) + "s| %-" + (maxMethodWidth + offset) + "s| %s%s\n",
                    cyan + annotation.endpoint(), annotation.method(), yellow + annotation.preAuthorize(), reset);
        }
    }

    private static int getMaxColumnWidth(List<AnnotationsDto> annotations,
            Function<AnnotationsDto, String> columnExtractor) {
        return annotations.stream()
                .mapToInt(annotation -> columnExtractor.apply(annotation).length())
                .max()
                .orElse(0);
    }

    public static Stream<File> findPoms(String basePath) throws IOException {
        // noinspection resource
        return Files.walk(Paths.get(basePath))
                .filter(path -> path.getFileName().toString().contains("pom.xml"))
                .map(Path::toFile);
    }

    @Override
    public void foundOkAnnotation(String content) {
        log.info("🔍 Found PreAuthorize '{}'", content);
    }

    @Override
    public void foundErroneousAnnotation(String sourceLocation) {
        log.error("\n\t❌ Erroneous security at '{}'", sourceLocation);
        errorCount++;
    }
}
