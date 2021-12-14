package it.eg.sloth.mavenplugin.writer.bean2;

import it.eg.sloth.dbmodeler.model.database.DataBaseType;
import it.eg.sloth.dbmodeler.model.schema.code.Function;
import it.eg.sloth.dbmodeler.model.schema.code.Package;
import it.eg.sloth.dbmodeler.model.schema.code.Procedure;
import it.eg.sloth.dbmodeler.model.schema.sequence.Sequence;
import it.eg.sloth.dbmodeler.model.schema.table.Table;
import it.eg.sloth.dbmodeler.model.schema.view.View;
import it.eg.sloth.mavenplugin.writer.bean2.h2.H2BeanWriter;
import it.eg.sloth.mavenplugin.writer.bean2.oracle.OracleBeanWriter;
import it.eg.sloth.mavenplugin.writer.bean2.postgres.PostgresBeanWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

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
 */
public interface BeanWriter {

    void writeTables(Collection<Table> tableCollection) throws IOException;

    void writeViews(Collection<View> viewCollection) throws IOException;

    void writeSequence(Collection<Sequence> sequenceCollection) throws IOException;

    void writeFunction(Collection<Function> functionCollection) throws IOException;

    void writeProcedure(Collection<Procedure> procedureCollection) throws IOException;

    void writePackages(Collection<Package> packageCollection) throws IOException;

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
