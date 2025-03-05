package com.theodo.inspector;

import java.io.File;

public class TestUtils {
    public static String getConfigLocation(String currentDirectory) {
        return getFileInDir(currentDirectory, "inspector", "log4j.xml");
    }

    public static File getDirectory(String currentDirectory, String targetDir) {
        if (currentDirectory.contains("/inspector")) {
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
