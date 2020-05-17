package it.eg.sloth.mavenplugin.writer.bean.oraclebean;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.dbschema.*;
import it.eg.sloth.mavenplugin.common.GenUtil;

import java.io.File;
import java.io.IOException;

/**
 * Project: sloth-plugin
 * Copyright (C) 2019-2020 Enrico Grillini
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Classe per la generazione della classe Sequence Dao
 *
 * @author Enrico Grillini
 *
 */
public class SequenceDaoWriter {
    private static final String DAO = ".dao";

    private Sequences sequences;

    String sequencesDaoClassName;
    String sequencesDaoFullClassName;
    String sequencesDaoPackageName;
    File sequencesDaoClassFile;

    public SequenceDaoWriter(File outputJavaDirectory, String genPackage, Sequences sequences) {
        this.sequences = sequences;

        // TableBean properties
        sequencesDaoClassName = "SequencesDao";
        sequencesDaoFullClassName = genPackage + DAO + "." + sequencesDaoClassName;
        sequencesDaoPackageName = genPackage + DAO;
        sequencesDaoClassFile = GenUtil.getClassFile(outputJavaDirectory, sequencesDaoPackageName, sequencesDaoClassName);
    }

    /**
     * Entry point per la generazione della classe SequencesDao
     *
     * @return
     */
    public StringBuilder getSequencesDao() {
        StringBuilder testo = new StringBuilder()
                .append("package " + sequencesDaoPackageName + ";\n")
                .append("\n")
                .append("import java.io.IOException;\n")
                .append("import java.math.BigDecimal;\n")
                .append("import java.sql.SQLException;\n")
                .append("\n")
                .append("import it.eg.sloth.db.query.query.Query;\n")
                .append("\n")
                .append("public class SequencesDao {\n")
                .append("  private static final String COLUMN = \"nextval\";\n")
                .append("\n");

        // Costanti
        for (Sequence sequence : sequences.getSequence()) {
            testo.append("  private static final String " + sequence.getName().toUpperCase() + " = \"select " + sequence.getName() + ".nextval nextval from dual\";");
        }

        testo.append("\n");

        // Metodi
        for (Sequence sequence : sequences.getSequence()) {
            testo
                    .append("  public static BigDecimal " + StringUtil.initCap(sequence.getName()) + "() throws SQLException, IOException {\n")
                    .append("    return new Query(" + sequence.getName().toUpperCase() + ").selectRow().getBigDecimal(COLUMN);\n")
                    .append("  }\n")
                    .append("\n");
        }


        testo.append("}");

        return testo;
    }


    /**
     * Scrive il SequenceDao
     *
     * @throws IOException
     */
    public void write() throws IOException {
        GenUtil.writeFile(sequencesDaoClassFile, getSequencesDao().toString());

    }

}
