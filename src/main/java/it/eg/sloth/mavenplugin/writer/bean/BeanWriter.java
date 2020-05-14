package it.eg.sloth.mavenplugin.writer.bean;


import it.eg.sloth.jaxb.dbschema.*;
import it.eg.sloth.jaxb.dbschema.Package;
import it.eg.sloth.mavenplugin.writer.bean.oraclebean.PackageBeanWriter;
import it.eg.sloth.mavenplugin.writer.bean.oraclebean.SequenceDaoWriter;
import it.eg.sloth.mavenplugin.writer.bean.oraclebean.TableBeanWriter;
import it.eg.sloth.mavenplugin.writer.bean.oraclebean.ViewBeanWriter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;


public class BeanWriter {

    File dbSchemaXml;
    File outputJavaDirectory;
    String genPackage;

    MavenProject project;
    Log log;

    public BeanWriter(File dbSchemaXml, File outputJavaDirectory, String genPackage, MavenProject project, Log log) {
        this.dbSchemaXml = dbSchemaXml;
        this.outputJavaDirectory = outputJavaDirectory;
        this.genPackage = genPackage;
        this.log = log;
        this.project = project;
    }

    public void write() throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(DbToolProject.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        DbToolProject dbToolProject = (DbToolProject) jaxbUnmarshaller.unmarshal(dbSchemaXml);

        log.info("  Package bean");
        for (Package dbPackage : dbToolProject.getDataBase().getPackages().getPackage()) {
            PackageBeanWriter writer = new PackageBeanWriter(outputJavaDirectory, genPackage, dbPackage);
            writer.write();
        }

        log.info("  View bean");
        for (View view : dbToolProject.getDataBase().getViews().getView()) {
            ViewBeanWriter writer = new ViewBeanWriter(outputJavaDirectory, genPackage, view);
            writer.write();
        }

        log.info("  Table bean");
        for (Table table : dbToolProject.getDataBase().getTables().getTable()) {
            TableBeanWriter writer = new TableBeanWriter(outputJavaDirectory, genPackage, table);
            writer.write();
        }

        log.info("  Sequence Dao");
        SequenceDaoWriter writer = new SequenceDaoWriter(outputJavaDirectory, genPackage, dbToolProject.getDataBase().getSequences());
        writer.write();


    }


}
