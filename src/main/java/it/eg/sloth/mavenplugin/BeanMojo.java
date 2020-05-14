package it.eg.sloth.mavenplugin;

import it.eg.sloth.mavenplugin.writer.bean.BeanWriter;
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
@Mojo(name = "bean",
        threadSafe = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class BeanMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", property = "project", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${project.basedir}/db/dbSchema.xml", property = "dbSchema", required = true)
    private File dbSchema;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/sloth", property = "outputJavaDirectory", required = true)
    private File outputJavaDirectory;

    @Parameter(property = "genPackage", required = true)
    private String genPackage;


    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Sloth: Bean goal");
        getLog().info("  project: " + project);
        getLog().info("  dbSchema: " + dbSchema);
        getLog().info("  outputJavaDirectory: " + outputJavaDirectory);
        getLog().info("  genPackage: " + genPackage);
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Generazione Bean");

        if (!outputJavaDirectory.exists() && !this.outputJavaDirectory.mkdirs()) {
            getLog().error("Could not create source directory!");
        } else {

            try {
                project.addCompileSourceRoot(outputJavaDirectory.getAbsolutePath());

                new BeanWriter(dbSchema, outputJavaDirectory, genPackage, project, getLog()).write();

            } catch (Exception e) {
                throw new MojoExecutionException("Could not generate Java source code!", e);
            }
        }
    }
}
