package it.eg.sloth.mavenplugin;

import it.eg.sloth.mavenplugin.writer.form.FormWriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Goal che crea le classi form
 */
@Mojo(name = "form",
        threadSafe = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class FormMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", property = "project", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${project.basedir}/form", property = "formDirectory", required = true)
    private File formDirectory;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/sloth", property = "outputJavaDirectory", required = true)
    private File outputJavaDirectory;

    @Parameter(property = "genPackage", required = true)
    private String genPackage;



    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Sloth: Form goal");
        getLog().info("  project: " + project);
        getLog().info("  formDirectory: " + formDirectory);
        getLog().info("  outputJavaDirectory: " + outputJavaDirectory);
        getLog().info("  genPackage: " + genPackage);
        getLog().info("------------------------------------------------------------------------");

        if (!outputJavaDirectory.exists() && !this.outputJavaDirectory.mkdirs()) {
            getLog().error("Could not create source directory!");
        } else {

            try {
                project.addCompileSourceRoot(outputJavaDirectory.getAbsolutePath());

                new FormWriter(formDirectory, outputJavaDirectory, genPackage, getLog()).write();

            } catch (Exception e) {
                throw new MojoExecutionException("Could not generate Java source code!", e);
            }
        }
    }
}
