package com.theodo.inspector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import com.theodo.inspector.cli.InspectorCommand;
import com.theodo.inspector.impl.PreAuthorizeAnnotationProcessing;
import com.theodo.inspector.impl.ast.ASTReader;
import com.theodo.inspector.impl.utils.AnnotationsDto;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import spoon.reflect.CtModel;

@Slf4j
public class SpringAccessInspector extends InspectorCommand implements AnnotationEvent {

    @Getter
    private int errorCount = 0;

    public static void main(String[] args) {
        Configurator.setLevel("com.theodo.tools", Level.INFO);

        SpringAccessInspector inspector = new SpringAccessInspector();
        int exitCode = new CommandLine(inspector).execute(args);
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
            generateHtmlTable(annotations);
        }
        return 0;
    }

    public void generateHtmlTable(List<AnnotationsDto> annotations) {
        // Generate the HTML table
        StringBuilder htmlTable = new StringBuilder();
        htmlTable.append("\n<head>\n<style>\n")
                .append("table {\nborder-collapse: collapse;\nwidth:100%;\n}\n")
                .append("th, td {\npadding: 8px;\ntext-align: left;\nborder-bottom: 1px solid #ddd;\n}\n")
                .append("th {\nbackground-color: #f2f2f2;\n}\n")
                .append("</style>\n</head>\n<body>\n")
                .append("<table>\n<tr {}>\n<th>Endpoint</th>\n<th>Method</th>\n<th>PreAuthorize</th>\n</tr>\n");

        // Iterate over the list and generate each row of the table
        for (AnnotationsDto annotation : annotations) {
            htmlTable.append("<tr>\n<td>").append(annotation.endpoint()).append("</td>\n")
                    .append("<td>").append(annotation.method().replace("Mapping", "")).append("</td>\n")
                    .append("<td>").append(annotation.preAuthorize()).append("</td>\n</tr>\n");
        }

        // Close the HTML table and body
        htmlTable.append("</table>\n</body>\n</html>");

        // Write the HTML table to a file
        try (PrintWriter writer = new PrintWriter(this.htmlOutputFile)) {
            writer.println(htmlTable);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Stream<File> findPoms(String basePath) throws IOException {
        // noinspection resource
        return Files.walk(Paths.get(basePath))
                .filter(path -> path.getFileName().toString().contains("pom.xml"))
                .map(Path::toFile);
    }

    @Override
    public void foundErroneousAnnotation(String sourceLocation) {
        errorCount++;
    }
}
