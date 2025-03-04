package com.theodo.tools.preauthorize.analyzer;

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeEach;

class UnitTest {
    private final String currentDirectory = System.getProperty("user.dir");
    protected String sampleProjectRootPath;

    @BeforeEach
    void setup() {
        Configurator.initialize("config", TestUtils.getConfigLocation(currentDirectory));
        sampleProjectRootPath = TestUtils.getDirectory(currentDirectory, "sample").getAbsolutePath();
    }
}
