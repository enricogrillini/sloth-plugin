package it.eg.sloth.mavenplugin.writer.bean2.h2;

import it.eg.sloth.dbmodeler.model.database.DataBaseType;
import it.eg.sloth.mavenplugin.writer.bean2.AbstractBeanWriter;
import it.eg.sloth.mavenplugin.writer.bean2.BeanWriter;

import java.io.File;

public class H2BeanWriter extends AbstractBeanWriter implements BeanWriter {

    public H2BeanWriter(File outputJavaDirectory, String genPackage, DataBaseType dataBaseType) {
        super(outputJavaDirectory, genPackage, dataBaseType);
    }
}
