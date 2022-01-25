package it.eg.sloth.mavenplugin;

import it.eg.sloth.db.manager.DataConnectionManager;
import it.eg.sloth.dbmodeler.model.schema.Schema;
import it.eg.sloth.dbmodeler.writer.DbSchemaWriter;
import it.eg.sloth.jaxb.dbschema.DataBase;
import it.eg.sloth.jaxb.dbschema.DbToolProject;
import it.eg.sloth.mavenplugin.writer.bean.BeanWriter;
import it.eg.sloth.mavenplugin.writer.refreshdb.DbIFace;
import it.eg.sloth.mavenplugin.writer.refreshdb.oracle.OracleDb;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * Goal che effettua il refresh db
 *
 * @author Enrico Grillini
 */
@Mojo(name = "refreshdb",
        threadSafe = true,
        defaultPhase = LifecyclePhase.NONE,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class RefreshDbMojo extends SlothMojo {

    @Parameter(defaultValue = "${project.basedir}/db/dbSchema.xml", property = "dbSchema", required = true)
    private File dbSchema;

    @Parameter(defaultValue = "${project.basedir}/db/dbSchema.json", property = "dbSchema2", required = true)
    private File dbSchema2;

    @Override
    public void execute() throws MojoExecutionException {
        execute1();
        execute2();
    }


    public void execute1() throws MojoExecutionException {
        Instant start = Instant.now();

        ////////////////
        // REFRESH DB //
        ////////////////
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Sloth: goal refreshdb");
        getLog().info("  project: " + project);
        getLog().info("  dbSchema: " + dbSchema);
        getLog().info("  outputJavaDirectory: " + outputJavaDirectory);
        getLog().info("  genPackage: " + genPackage);
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Aggiornamento schema Start");


        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DbToolProject.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            DbToolProject dbToolProject = null;
            try (InputStream inStream = new FileInputStream(dbSchema)) {
                dbToolProject = (DbToolProject) jaxbUnmarshaller.unmarshal(inStream);
            }

            DataConnectionManager.getInstance().registerDataSource(
                    "defaultDB",
                    dbToolProject.getConnection().getDriverName(),
                    dbToolProject.getConnection().getJdbcUrl(),
                    dbToolProject.getConnection().getUser(),
                    dbToolProject.getConnection().getPassword());

            DbIFace dbIFace = new OracleDb(dbToolProject.getConnection().getOwner(), true);

            dbToolProject.setDataBase(new DataBase());

            getLog().info("  loadTables");
            dbToolProject.getDataBase().setTables(dbIFace.loadTables(null));

            getLog().info("  loadViews");
            dbToolProject.getDataBase().setViews(dbIFace.loadViews());

            getLog().info("  loadPackages");
            dbToolProject.getDataBase().setPackages(dbIFace.loadPackages());

            getLog().info("  loadSequences");
            dbToolProject.getDataBase().setSequences(dbIFace.loadSequences());

            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());

            // Salvo il dbSchema in un file temporaneo
            Path tempFile = Files.createTempFile(null, null);
            try (OutputStream outputStream = new FileOutputStream(tempFile.toFile())) {
                jaxbMarshaller.marshal(dbToolProject, outputStream);
            }

            // Converto il file temporaneo appena creato in un file con i fine linea coerenti con il Sistema operativo per facilitare i confronti con WinMerge
            try (BufferedReader reader = Files.newBufferedReader(tempFile, StandardCharsets.UTF_8); PrintWriter writer = new PrintWriter(dbSchema, StandardCharsets.UTF_8.name())) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    writer.println(line);
                }
            }

        } catch (Exception e) {
            throw new MojoExecutionException("Could not generate Java source code!", e);
        }

        ///////////////////
        // GENERATE BEAN //
        ///////////////////
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

        getLog().info("Aggiornamento schema End: " + ChronoUnit.MILLIS.between(start, Instant.now()));
    }


    public void execute2() throws MojoExecutionException {
        Instant start = Instant.now();

        ////////////////
        // REFRESH DB //
        ////////////////
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Sloth: goal refreshdb2");
        getLog().info("  project: " + project);
        getLog().info("  dbSchema: " + dbSchema2);
        getLog().info("  outputJavaDirectory: " + outputJavaDirectory);
        getLog().info("  genPackage: " + genPackage);
        getLog().info("------------------------------------------------------------------------");
        getLog().info("Aggiornamento schema Start");

        try {
            it.eg.sloth.dbmodeler.model.DataBase dataBase = new it.eg.sloth.dbmodeler.model.DataBase();
            dataBase.readJson(dbSchema2);
            getLog().info("  Schema type:" + dataBase.getDbConnection().getDataBaseType());
            dataBase.refreshSchema();
            dataBase.writeJson(dbSchema2);

            DbSchemaWriter dbSchemaWriter = DbSchemaWriter.Factory.getSchemaWriter(dataBase.getDbConnection().getDataBaseType());
            Schema schema = dataBase.getSchema();

            StringBuilder stringBuilder = new StringBuilder()
                    .append(dbSchemaWriter.sqlTables(schema, false, false))
                    .append(dbSchemaWriter.sqlIndexes(schema, false, false))
                    .append(dbSchemaWriter.sqlPrimaryKeys(schema))
                    .append(dbSchemaWriter.sqlForeignKeys(schema))
                    .append(dbSchemaWriter.sqlSequences(schema))
                    .append(dbSchemaWriter.sqlView(schema))
                    .append(dbSchemaWriter.sqlFunctions(schema))
                    .append(dbSchemaWriter.sqlProcedures(schema))
                    .append(dbSchemaWriter.sqlPackages(schema));

            // Converto il file temporaneo appena creato in un file con i fine linea coerenti con il Sistema operativo per facilitare i confronti con WinMerge
            File ddlFile = new File(dbSchema2.getParent(), FilenameUtils.getBaseName(dbSchema2.getName()) + "-DDL.sql");
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
            getLog().info("    Package: " + schema.getPackageCollection().size());

        } catch (Exception e) {
            throw new MojoExecutionException("Could not generate Java source code!", e);
        }

        getLog().info("Aggiornamento schema End: " + ChronoUnit.MILLIS.between(start, Instant.now()) + " ms");

        // Generazione Bean
        Bean2Mojo.generateBean(project, getLog(), dbSchema2, outputJavaDirectory, genPackage);
    }
}
