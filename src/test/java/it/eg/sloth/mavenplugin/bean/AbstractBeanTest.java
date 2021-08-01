package it.eg.sloth.mavenplugin.bean;

import it.eg.sloth.dbmodeler.model.DataBase;
import it.eg.sloth.dbmodeler.model.database.DataBaseType;
import it.eg.sloth.framework.utility.resource.ResourceUtil;
import it.eg.sloth.mavenplugin.TestFactory;
import it.eg.sloth.mavenplugin.writer.bean2.BeanWriter;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

public class AbstractBeanTest {

    protected DataBase dataBase;

    @Getter
    protected BeanWriter beanWriter;

    void init(DataBaseType dataBaseType) throws IOException {
        dataBase = new DataBase();
        dataBase.readJson(ResourceUtil.resourceFile("dbmodeler/" + dataBaseType + "-db.json"));


        beanWriter = BeanWriter.Factory.getBeanWriter(new File(TestFactory.OUTPUT_DIR), "it.itdistribuzione.gilda.gen", dataBase.getDbConnection().getDataBaseType());
        TestFactory.createOutputDir();
        //TestFactory.createOutputDir("writerData");
    }


}
