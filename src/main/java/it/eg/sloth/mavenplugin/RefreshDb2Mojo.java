package it.eg.sloth.mavenplugin;

import it.eg.sloth.dbmodeler.model.DataBase;
import it.eg.sloth.dbmodeler.model.schema.Schema;
import it.eg.sloth.dbmodeler.writer.DbSchemaWriter;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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
public class RefreshDb2Mojo extends SlothMojo {

    @Parameter(defaultValue = "${project.basedir}/db/dbSchema.json", property = "dbSchema", required = true)
    private File dbSchema;

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

            DbSchemaWriter dbSchemaWriter = DbSchemaWriter.Factory.getSchemaWriter(dataBase.getDbConnection().getDataBaseType());
            Schema schema = dataBase.getSchema();

            StringBuilder stringBuilder = new StringBuilder()
                    .append(dbSchemaWriter.sqlTables(schema, false, false))
                    .append(dbSchemaWriter.sqlIndexes(schema, false, false))
                    .append(dbSchemaWriter.sqlPrimaryKey(schema))
                    .append(dbSchemaWriter.sqlForeignKey(schema))
                    .append(dbSchemaWriter.sqlSequences(schema))
                    .append(dbSchemaWriter.sqlView(schema))
                    .append(dbSchemaWriter.sqlFunction(schema))
                    .append(dbSchemaWriter.sqlProcedure(schema));

            // Converto il file temporaneo appena creato in un file con i fine linea coerenti con il Sistema operativo per facilitare i confronti con WinMerge
            File ddlFile = new File(dbSchema.getParent(), FilenameUtils.getBaseName(dbSchema.getName()) + "-DDL.sql");
            try (BufferedReader reader = new BufferedReader(new StringReader(stringBuilder.toString()));
                 PrintWriter writer = new PrintWriter(ddlFile, StandardCharsets.UTF_8.name())) {

                String line = null;
                while ((line = reader.readLine()) != null) {
                    writer.println(line);
                }
            }

            getLog().info("  Aggiornati:");
            getLog().info("    Tabelle: " + schema.getTableCollection().size());
            getLog().info("    Sequence: " + schema.getSequenceCollection().size());
            getLog().info("    View: " + schema.getViewCollection().size());
            getLog().info("    Function: " + schema.getFunctionCollection().size());
            getLog().info("    Procedure: " + schema.getProcedureCollection().size());

        } catch (Exception e) {
            throw new MojoExecutionException("Could not generate Java source code!", e);
        }

        getLog().info("Aggiornamento schema End: " + ChronoUnit.MILLIS.between(start, Instant.now()));

        // Generazione Bean
        Bean2Mojo.generateBean(project, getLog(), dbSchema, outputJavaDirectory, genPackage);
    }
}
