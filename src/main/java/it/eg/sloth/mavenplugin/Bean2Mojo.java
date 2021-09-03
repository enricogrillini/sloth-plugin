package it.eg.sloth.mavenplugin;

import it.eg.sloth.dbmodeler.model.DataBase;
import it.eg.sloth.mavenplugin.writer.bean2.BeanWriter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
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
 * <p>
 * Goal: che crea le classi di configurazione spring
 *
 * @author Enrico Grillini
 */
@Mojo(name = "bean2",
        threadSafe = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class Bean2Mojo extends SlothMojo {

    @Parameter(defaultValue = "${project.basedir}/db/dbSchema.json", property = "dbSchema", required = true)
    private File dbSchema;

    @Override
    public void execute() throws MojoExecutionException {

        getLog().info("------------------------------------------------------------------------");
        getLog().info("Sloth: Bean2 Experimental");
        getLog().info("  project: " + project);
        getLog().info("  dbSchema: " + dbSchema);
        getLog().info("  outputJavaDirectory: " + outputJavaDirectory);
        getLog().info("  genPackage: " + genPackage);
        getLog().info("------------------------------------------------------------------------");

        generateBean(project, getLog(), dbSchema, outputJavaDirectory, genPackage);
    }

    public static void generateBean(MavenProject project, Log log, File dbSchema, File outputJavaDirectory, String genPackage) throws MojoExecutionException {
        Instant start = Instant.now();
        log.info("Generazione Bean Start");

        if (!outputJavaDirectory.exists() && !outputJavaDirectory.mkdirs()) {
            log.error("Could not create source directory!");
        } else {
            try {
                project.addCompileSourceRoot(outputJavaDirectory.getAbsolutePath());

                DataBase dataBase = new DataBase();
                dataBase.readJson(dbSchema);

                log.info("  Schema type:" + dataBase.getDbConnection().getDataBaseType());
                BeanWriter beanWriter = BeanWriter.Factory.getBeanWriter(outputJavaDirectory, genPackage, dataBase.getDbConnection().getDataBaseType());

                // Table bean
                log.info("  Table bean");
                beanWriter.writeTables(dataBase.getSchema().getTableCollection());

                // View bean
                log.info("  View bean");
                beanWriter.writeViews(dataBase.getSchema().getViewCollection());

                // Sequence Dao
                log.info("  Sequence Dao");
                beanWriter.writeSequence(dataBase.getSchema().getSequenceCollection());

            } catch (Exception e) {
                throw new MojoExecutionException("Could not generate Java source code!", e);
            }
        }

        log.info("Generazione Bean End: " + ChronoUnit.MILLIS.between(start, Instant.now()) + " ms");
    }

}
