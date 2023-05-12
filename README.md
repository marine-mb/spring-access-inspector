How to build: 

```mvn clean install``` in project directory

How to run (using maven exec plugin):

```mvn exec:java -Dexec.mainClass=com.theodo.tools.preauthorize.analyzer.PreAuthorizeAnalysis  -Dexec.args="/the_path/where/poms/are"```

How to run (using java):
```
mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt
export CLASSPATH=`cat classpath.txt`
java -cp java -cp $CLASSPATH:./target/preauthorize-analysis-1.0.0.jar com.theodo.tools.preauthorize.analyzer.PreAuthorizeAnalysis /the_path/where/poms/are"
````
