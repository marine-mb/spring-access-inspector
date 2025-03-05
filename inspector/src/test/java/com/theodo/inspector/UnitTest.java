package com.theodo.inspector;

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;

class UnitTest {
    private static final String currentDirectory = System.getProperty("user.dir");
    protected static String sampleProjectRootPath;

    @BeforeAll
    static void setup() {
        Configurator.initialize("config", TestUtils.getConfigLocation(currentDirectory));
        sampleProjectRootPath = TestUtils.getDirectory(currentDirectory, "sample/microservice-example")
                .getAbsolutePath();
    }
}
