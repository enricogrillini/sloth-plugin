package it.eg.sloth.mavenplugin.writer.bean;


import it.eg.sloth.jaxb.dbschema.*;
import it.eg.sloth.jaxb.dbschema.Package;
import it.eg.sloth.mavenplugin.writer.bean.oraclebean.*;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
 *
 * @author Enrico Grillini
 *
 */
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

        DbToolProject dbToolProject = null;
        try (InputStream inStream = new FileInputStream(dbSchemaXml)) {
            dbToolProject = (DbToolProject) jaxbUnmarshaller.unmarshal(inStream);
        }

        log.info("  View bean");
        for (View view : dbToolProject.getDataBase().getViews().getView()) {
            ViewBeanWriter writer = new ViewBeanWriter(outputJavaDirectory, genPackage, view);
            writer.write();
        }

        log.info("  Table bean");
        VelocityTableBeanWriter velocityTableBeanWriter = new VelocityTableBeanWriter(outputJavaDirectory, genPackage);
        for (Table table : dbToolProject.getDataBase().getTables().getTable()) {
            TableBeanWriter writer = new TableBeanWriter(outputJavaDirectory, genPackage, table);
            writer.write();

            velocityTableBeanWriter.write(table);
        }

    }


}
