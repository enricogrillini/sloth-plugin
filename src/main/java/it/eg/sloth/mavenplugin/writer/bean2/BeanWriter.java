package it.eg.sloth.mavenplugin.writer.bean2;

import it.eg.sloth.dbmodeler.model.database.DataBaseType;
import it.eg.sloth.dbmodeler.model.schema.sequence.Sequence;
import it.eg.sloth.dbmodeler.model.schema.table.Table;
import it.eg.sloth.mavenplugin.writer.bean2.h2.H2BeanWriter;
import it.eg.sloth.mavenplugin.writer.bean2.oracle.OracleBeanWriter;
import it.eg.sloth.mavenplugin.writer.bean2.postgres.PostgresBeanWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface BeanWriter {

    void writeTable(Collection<Table> tableCollection) throws IOException;

    void writeSequence(Collection<Sequence> sequenceCollection) throws IOException;

    class Factory {
        private Factory() {
            // NOP
        }

        public static BeanWriter getBeanWriter(File outputJavaDirectory, String genPackage, DataBaseType dataBaseType) {
            // Imposto il reader corretto
            switch (dataBaseType) {
                case H2:
                    return new H2BeanWriter(outputJavaDirectory, genPackage, dataBaseType);
                case ORACLE:
                    return new OracleBeanWriter(outputJavaDirectory, genPackage, dataBaseType);
                case POSTGRES:
                    return new PostgresBeanWriter(outputJavaDirectory, genPackage, dataBaseType);
                default:
                    // NOP
            }

            return null;
        }
    }

}
