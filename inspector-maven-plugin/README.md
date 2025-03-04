# ARCHITECTURE ANALYSIS MAVEN PLUGIN

## How to build the maven plugin

`mvn clean install`

## How to use the maven plugin in another pom.xml project

In your `./xxx-api/pom.xml` file add the plugin in `build/pluginManagement`

```xml
  <build>

    ... Whatever...

    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>com.theodo</groupId>
                <artifactId>inspector-maven-plugin</artifactId>
                <version>1.0.0</version>
                <configuration>
                    <projectBaseDir>${project.basedir}</projectBaseDir>
                    <htmlOutputFile>./table.html</htmlOutputFile>
                </configuration>
            </plugin>
      </plugins>
    </pluginManagement>
  </build>
```

Then in your Shell or CI, launch the analysis:

`mvn inspector:inspect`
