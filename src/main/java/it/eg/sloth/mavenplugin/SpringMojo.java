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
import java.time.Instant;
import java.time.temporal.ChronoUnit;


/**
 * Project: sloth-plugin
 * Copyright (C) 2019-2021 Enrico Grillini
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Goal che crea le classi di configurazione spring
 *
 * @author Enrico Grillini
 */
@Mojo(name = "spring",
        threadSafe = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class SpringMojo extends SlothMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/java", property = "javaDirectory", required = true)
    private File javaDirectory;

    @Parameter(defaultValue = "${project.basedir}/src/main/webapp", property = "webappDirectory", required = true)
    private File webappDirectory;

    @Parameter(property = "controllerPackage", required = true)
    private String controllerPackage;

    @Override
    public void execute() throws MojoExecutionException {
        Instant start = Instant.now();

        getLog().info("------------------------------------------------");
        getLog().info("Sloth: Spring goal");
        getLog().info("  project: " + project);
        getLog().info("  javaDirectory: " + javaDirectory);
        getLog().info("  webappDirectory: " + webappDirectory);
        getLog().info("  controllerPackage: " + genPackage);
        getLog().info("  outputJavaDirectory: " + outputJavaDirectory);
        getLog().info("  genPackage: " + genPackage);
        getLog().info("------------------------------------------------");
        getLog().info("Aggiornamento Spring Start");

        if (!outputJavaDirectory.exists() && !this.outputJavaDirectory.mkdirs()) {
            getLog().error("Could not create source directory!");
        } else {

            try {
                project.addCompileSourceRoot(outputJavaDirectory.getAbsolutePath());

                new SpringControllerWriter(javaDirectory, webappDirectory, controllerPackage, outputJavaDirectory, genPackage, project, getLog()).write();

            } catch (Exception e) {
                throw new MojoExecutionException("Could not generate Java source code!", e);
            }
        }

        getLog().info("Aggiornamento Spring End: " + ChronoUnit.MILLIS.between(start, Instant.now()));
    }
}
