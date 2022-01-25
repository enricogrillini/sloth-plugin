package it.eg.sloth.mavenplugin;

import it.eg.sloth.dbmodeler.model.DataBase;
import it.eg.sloth.mavenplugin.writer.bean.BeanWriter;
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
 * Goal: che crea le classi di configurazione spring
 *
 * @author Enrico Grillini
 */
@Mojo(name = "bean",
        threadSafe = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class BeanMojo extends SlothMojo {

    @Parameter(defaultValue = "${project.basedir}/db/dbSchema.xml", property = "dbSchema", required = true)
    private File dbSchema;

    @Parameter(defaultValue = "${project.basedir}/db/dbSchema.json", property = "dbSchema2", required = true)
    private File dbSchema2;

    @Override
    public void execute() throws MojoExecutionException {
        Instant start = Instant.now();

        getLog().info("------------------------------------------------------------------------");
        getLog().info("Sloth: Bean goal");
        getLog().info("  project: " + project);
        getLog().info("  dbSchema: " + dbSchema);
        getLog().info("  outputJavaDirectory: " + outputJavaDirectory);
        getLog().info("  genPackage: " + genPackage);
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Generazione Bean Start");

        if (!outputJavaDirectory.exists() && !this.outputJavaDirectory.mkdirs()) {
            getLog().error("Could not create source directory!");
        } else {
            try {
                project.addCompileSourceRoot(outputJavaDirectory.getAbsolutePath());

                // Bean 1
                new BeanWriter(dbSchema, outputJavaDirectory, genPackage, project, getLog()).write();

                // Bean 2
                try {
                    project.addCompileSourceRoot(outputJavaDirectory.getAbsolutePath());

                    DataBase dataBase = new DataBase();
                    dataBase.readJson(dbSchema2);

                    getLog().info("  Schema type:" + dataBase.getDbConnection().getDataBaseType());
                    it.eg.sloth.mavenplugin.writer.bean2.BeanWriter beanWriter = it.eg.sloth.mavenplugin.writer.bean2.BeanWriter.Factory.getBeanWriter(outputJavaDirectory, genPackage, dataBase.getDbConnection().getDataBaseType());

//                    // Table bean
//                    getLog().info("  Table bean");
//                    beanWriter.writeTables(dataBase.getSchema().getTableCollection());
//
                    // View bean
                    getLog().info("  View bean");
                    beanWriter.writeViews(dataBase.getSchema().getViewCollection());

                    // Sequence Dao
                    getLog().info("  Sequence Dao");
                    beanWriter.writeSequence(dataBase.getSchema().getSequenceCollection());

                    // Function Dao
                    getLog().info("  Function Dao");
                    beanWriter.writeFunction(dataBase.getSchema().getFunctionCollection());

                    // Procedure Dao
                    getLog().info("  Procedure Dao");
                    beanWriter.writeProcedure(dataBase.getSchema().getProcedureCollection());

                    // Package Dao
                    getLog().info("  Package Dao");
                    beanWriter.writePackages(dataBase.getSchema().getPackageCollection());

                } catch (Exception e) {
                    throw new MojoExecutionException("Could not generate Java source code!", e);
                }


            } catch (Exception e) {
                throw new MojoExecutionException("Could not generate Java source code!", e);
            }
        }

        getLog().info("Generazione Bean End: " + ChronoUnit.MILLIS.between(start, Instant.now()));
    }
}
