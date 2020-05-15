package sloth.maven.plugin;

import it.eg.sloth.db.manager.DataConnectionManager;
import it.eg.sloth.jaxb.dbschema.DataBase;
import it.eg.sloth.jaxb.dbschema.DbToolProject;
import it.eg.sloth.mavenplugin.writer.refreshdb.DbIFace;
import it.eg.sloth.mavenplugin.writer.refreshdb.oracle.OracleDb;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Project: sloth-framework
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
 * Goal : che crea le classi di configurazione spring
 *
 * @author Enrico Grillini
 */
public class GenericTest {

    @Test
    public void formScannerTest() throws Exception {
        try {

            DataConnectionManager.getInstance().registerDataSource("defaultDB", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@localhost:1521:XE", "gilda", "gilda");

            DbIFace dbIFace = new OracleDb("gilda", true);

            DbToolProject dbToolProject = new DbToolProject();
            dbToolProject.setDataBase(new DataBase());
            dbToolProject.getDataBase().setTables(dbIFace.loadTables(null));
            dbToolProject.getDataBase().setViews(dbIFace.loadViews());
            dbToolProject.getDataBase().setPackages(dbIFace.loadPackages());
            dbToolProject.getDataBase().setSequences(dbIFace.loadSequences());

            JAXBContext jaxbContext = JAXBContext.newInstance(DbToolProject.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            try (OutputStream os = new FileOutputStream("target/prova.xml")) {
                jaxbMarshaller.marshal(dbToolProject, os);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
