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
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Project: sloth-plugin
 * Copyright (C) 2019-2020 Enrico Grillini
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Goal: che crea le classi form
 *
 * @author Enrico Grillini
 *
 */
@Mojo(name = "form",
        threadSafe = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class FormMojo extends SlothMojo {

    @Parameter(defaultValue = "${project.basedir}/form", property = "formDirectory", required = true)
    private File formDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Instant start = Instant.now();

        getLog().info("------------------------------------------------------------------------");
        getLog().info("Sloth: Form goal");
        getLog().info("  project: " + project);
        getLog().info("  formDirectory: " + formDirectory);
        getLog().info("  outputJavaDirectory: " + outputJavaDirectory);
        getLog().info("  genPackage: " + genPackage);
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Generazione Form/Page Start");

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

        getLog().info("Generazione Form End: " + ChronoUnit.MILLIS.between(start, Instant.now()));
    }
}
