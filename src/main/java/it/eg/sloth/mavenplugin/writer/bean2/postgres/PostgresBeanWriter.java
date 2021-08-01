package it.eg.sloth.mavenplugin.writer.bean2.postgres;

import it.eg.sloth.dbmodeler.model.database.DataBaseType;
import it.eg.sloth.mavenplugin.writer.bean2.AbstractBeanWriter;
import it.eg.sloth.mavenplugin.writer.bean2.BeanWriter;

import java.io.File;

public class PostgresBeanWriter extends AbstractBeanWriter implements BeanWriter {

    public PostgresBeanWriter(File outputJavaDirectory, String genPackage, DataBaseType dataBaseType) {
        super(outputJavaDirectory, genPackage, dataBaseType);
    }
}
