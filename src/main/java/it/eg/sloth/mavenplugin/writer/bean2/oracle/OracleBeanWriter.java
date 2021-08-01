package it.eg.sloth.mavenplugin.writer.bean2.oracle;

import it.eg.sloth.dbmodeler.model.database.DataBaseType;
import it.eg.sloth.dbmodeler.model.schema.sequence.Sequence;
import it.eg.sloth.mavenplugin.writer.bean2.AbstractBeanWriter;
import it.eg.sloth.mavenplugin.writer.bean2.BeanWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class OracleBeanWriter extends AbstractBeanWriter implements BeanWriter {

    public OracleBeanWriter(File outputJavaDirectory, String genPackage, DataBaseType dataBaseType) {
        super(outputJavaDirectory, genPackage, dataBaseType);
    }

}
