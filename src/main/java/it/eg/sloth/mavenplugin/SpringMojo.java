package it.eg.sloth.mavenplugin;

import it.eg.sloth.mavenplugin.writer.spring.SpringControllerWriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Goal che crea le classi di configurazione spring
 */
@Mojo(name = "spring",
        threadSafe = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class SpringMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", property = "project", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${project.basedir}/src/main/java", property = "javaDirectory", required = true)
    private File javaDirectory;

    @Parameter(defaultValue = "${project.basedir}/src/main/webapp", property = "webappDirectory", required = true)
    private File webappDirectory;

    @Parameter(property = "controllerPackage", required = true)
    private String controllerPackage;

    @Parameter(property = "systemXml", required = true)
    private File systemXml;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/sloth", property = "outputJavaDirectory", required = true)
    private File outputJavaDirectory;

    @Parameter(property = "genPackage", required = true)
    private String genPackage;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("------------------------------------------------");
        getLog().info("Sloth: Spring goal");
        getLog().info("  project: " + project);
        getLog().info("  javaDirectory: " + javaDirectory);
        getLog().info("  webappDirectory: " + webappDirectory);
        getLog().info("  controllerPackage: " + genPackage);
        getLog().info("  systemXml: " + systemXml);
        getLog().info("  outputJavaDirectory: " + outputJavaDirectory);
        getLog().info("  genPackage: " + genPackage);
        getLog().info("------------------------------------------------");

        if (!outputJavaDirectory.exists() && !this.outputJavaDirectory.mkdirs()) {
            getLog().error("Could not create source directory!");
        } else {

            try {
                project.addCompileSourceRoot(outputJavaDirectory.getAbsolutePath());

                new SpringControllerWriter(javaDirectory, webappDirectory, controllerPackage, systemXml, outputJavaDirectory, genPackage, project, getLog()).write();

            } catch (Exception e) {
                throw new MojoExecutionException("Could not generate Java source code!", e);
            }
        }
    }
}
