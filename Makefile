analyse:
	mvn exec:java -Dexec.mainClass=com.theodo.tools.preauthorize.analyzer.PreAuthorizeAnalysis  -Dexec.args=$(path)