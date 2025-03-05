package com.theodo.inspector.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

public abstract class InspectorCommand implements Callable<Integer> {
    @CommandLine.Parameters(paramLabel = "PROJECT_DIR", description = "Root project Directory (project to analyze)", defaultValue = ".", index = "0")
    public String projectDirectory;
    @CommandLine.Option(names = {"-o", "--output"}, paramLabel = "HTML_OUTPUT_FILE", description = "Output HTML file", defaultValue = "table.html")
    public String htmlOutputFile = "table.html";
}