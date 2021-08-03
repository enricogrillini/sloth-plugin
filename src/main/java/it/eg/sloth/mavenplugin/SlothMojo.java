package it.eg.sloth.mavenplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

public abstract class SlothMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", property = "project", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/sloth", property = "outputJavaDirectory", required = true)
    protected File outputJavaDirectory;

    @Parameter(property = "genPackage", required = true)
    protected String genPackage;


}
