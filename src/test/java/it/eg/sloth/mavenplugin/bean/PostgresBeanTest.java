package it.eg.sloth.mavenplugin.bean;

import it.eg.sloth.dbmodeler.model.database.DataBaseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Types;

class PostgresBeanTest extends AbstractBeanTest {

    @BeforeEach
    void init() throws IOException {
        super.init(DataBaseType.POSTGRES);
    }

    @Test
    void beanGenTest() throws IOException {
        getBeanWriter().writeTable(dataBase.getSchema().getTableCollection());

        getBeanWriter().writeSequence(dataBase.getSchema().getSequenceCollection());
    }

}
