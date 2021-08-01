package it.eg.sloth.mavenplugin;

import it.eg.sloth.dbmodeler.model.DataBase;
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
 * Goal che effettua il refresh db
 *
 * @author Enrico Grillini
 */
@Mojo(name = "refreshdb2",
        threadSafe = true,
        defaultPhase = LifecyclePhase.NONE,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class RefreshDb2Mojo extends AbstractMojo {


    @Parameter(defaultValue = "${project}", property = "project", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${project.basedir}/db/dbSchema.json", property = "dbSchema", required = true)
    private File dbSchema;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/sloth", property = "outputJavaDirectory", required = true)
    private File outputJavaDirectory;

    @Parameter(property = "genPackage", required = true)
    private String genPackage;

    @Override
    public void execute() throws MojoExecutionException {
        Instant start = Instant.now();

        ////////////////
        // REFRESH DB //
        ////////////////
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Sloth: goal refreshdb2 Experimental");
        getLog().info("  project: " + project);
        getLog().info("  dbSchema: " + dbSchema);
        getLog().info("  outputJavaDirectory: " + outputJavaDirectory);
        getLog().info("  genPackage: " + genPackage);
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Aggiornamento schema Start");

        try {
            DataBase dataBase = new DataBase();
            dataBase.readJson(dbSchema);
            getLog().info("  Schema type:" + dataBase.getDbConnection().getDataBaseType());
            dataBase.refreshSchema();
            dataBase.writeJson(dbSchema);

        } catch (Exception e) {
            throw new MojoExecutionException("Could not generate Java source code!", e);
        }

        getLog().info("Aggiornamento schema End: " + ChronoUnit.MILLIS.between(start, Instant.now()));
    }
}
