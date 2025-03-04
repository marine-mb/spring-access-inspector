package com.theodo.tools.preauthorize.analyzer;

import java.io.File;

public class TestUtils {
    public static String getConfigLocation(String currentDirectory) {
        return getFileInDir(currentDirectory, "analyzer", "log4j.xml");
    }

    public static File getDirectory(String currentDirectory, String targetDir) {
        if (currentDirectory.contains("/analyzer")) {
            File currentDir = new File(currentDirectory);
            File rootDir = currentDir.getParentFile(); // root project directory
            return new File(rootDir, targetDir);
        }
        File rootDir = new File(currentDirectory);
        return new File(rootDir, targetDir);
    }

    private static String getFileInDir(String currentDirectory, String targetDir, String fileName) {
        File sampleDir = getDirectory(currentDirectory, targetDir);
        File file = new File(sampleDir, fileName);
        return file.getAbsolutePath();
    }
}
