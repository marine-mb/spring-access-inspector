package com.theodo.tools.metamodel;

import com.theodo.inspector.SpringAccessInspector;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "inspect", defaultPhase = LifecyclePhase.PRE_SITE)
public class InspectorMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    String projectBaseDir;
    @Parameter(readonly = true)
    String htmlOutputFile;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            SpringAccessInspector inspector = new SpringAccessInspector();
            inspector.projectDirectory = projectBaseDir;
            inspector.htmlOutputFile = htmlOutputFile;

            inspector.call();
        } catch (Exception e) {
            throw new MojoExecutionException(String.format("Exception occurred while inspecting project %s", projectBaseDir), e);
        }

    }
}
