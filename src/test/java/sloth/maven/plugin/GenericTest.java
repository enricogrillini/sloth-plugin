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

            try (OutputStream os = new FileOutputStream("d:/prova.xml")) {
                jaxbMarshaller.marshal(dbToolProject, os);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
