package it.eg.sloth.mavenplugin;

import it.eg.sloth.db.manager.DataConnectionManager;
import it.eg.sloth.jaxb.dbschema.DataBase;
import it.eg.sloth.jaxb.dbschema.DbToolProject;
import it.eg.sloth.mavenplugin.writer.bean.BeanWriter;
import it.eg.sloth.mavenplugin.writer.refreshdb.DbIFace;
import it.eg.sloth.mavenplugin.writer.refreshdb.oracle.OracleDb;
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
@Mojo(name = "refreshdb",
        threadSafe = true,
        defaultPhase = LifecyclePhase.NONE,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class RefreshDbMojo extends AbstractMojo {


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
        getLog().info("Aggiornamento schema");

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

            try (OutputStream os = new FileOutputStream(dbSchema)) {
                jaxbMarshaller.marshal(dbToolProject, os);
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
    }
}
