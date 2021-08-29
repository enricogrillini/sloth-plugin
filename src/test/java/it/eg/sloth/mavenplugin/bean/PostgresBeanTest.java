package it.eg.sloth.mavenplugin.bean;

import it.eg.sloth.dbmodeler.model.database.DataBaseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PostgresBeanTest extends AbstractBeanTest {

    @BeforeEach
    void init() throws IOException {
        super.init(DataBaseType.POSTGRES);
    }

    @Test
    void beanGenTest() throws IOException {
        getBeanWriter().writeTables(dataBase.getSchema().getTableCollection());

        getBeanWriter().writeViews(dataBase.getSchema().getViewCollection());

        getBeanWriter().writeSequence(dataBase.getSchema().getSequenceCollection());
    }

}
