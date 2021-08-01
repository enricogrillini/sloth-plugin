package it.eg.sloth.mavenplugin.common;

import it.eg.sloth.framework.common.base.StringUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
 * @author Enrico Grillini
 */
public class GenUtil {

    public static final char UNIX_PATH_DELIMITER = '/';
    public static final String NEWLINE = System.getProperty("line.separator");

    private GenUtil() {
        // nothing
    }

    /**
     * Ritorna il file name senza estensione
     *
     * @param file
     * @return
     */
    public static String removeExension(File file) {
        String fileName = file.getName();

        if (fileName.equals("") || !fileName.contains(".") || fileName.endsWith(".")) {
            return null;
        } else {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }
    }


    /**
     * Ritona il File della classe passata
     *
     * @param packageName - si intende il package di riferimento ad es. "it.eg.sloth.gen"
     * @param className   - si intende il nome della classe comprensivo di  path relativo es. "sicurezza/UtentiForm"
     * @return
     */
    public static File getClassFile(File srcDirectory, String packageName, String className) {
        return new File(srcDirectory.getPath() + UNIX_PATH_DELIMITER + packageName.replace('.', UNIX_PATH_DELIMITER) + UNIX_PATH_DELIMITER + className + ".java");
    }

    public static String initCap(String testo) {
        return testo.substring(0, 1).toUpperCase() + testo.substring(1).toLowerCase();
    }

    public static String initLow(String testo) {
        return testo.substring(0, 1).toLowerCase() + testo.substring(1).toLowerCase();
    }


    public static void writeFile(File file, String string) throws IOException {
        // Creo la directory se non esiste
        if (!file.getParentFile().exists()) {
            FileUtils.forceMkdir(file.getParentFile());
        }

        FileUtils.writeStringToFile(file, string, StandardCharsets.UTF_8);
    }


//    public static String cleanDbCode(String string) throws IOException {
//        if (string == null) {
//            return null;
//        }
//
//        StringBuilder stringBuilder = new StringBuilder();
//
//        BufferedReader reader = new BufferedReader(new StringReader(string));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            stringBuilder.append(StringUtil.rtrim(line));
//            stringBuilder.append(NEWLINE);
//        }
//
//        // GG 19-12-2012: rimuovo tutti gli invii a fine stringa (in ambo le possibili forme: \r\n e \n).
//        // NB: preservo l'ULTIMO carattere di invio
//        while ((stringBuilder.length() > 4) &&
//                stringBuilder.substring(stringBuilder.length() - 4).equals("\r\n\r\n")) { //$NON-NLS-1$
//            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
//        }
//        while ((stringBuilder.length() > 2) &&
//                stringBuilder.substring(stringBuilder.length() - 2).equals("\n\n")) { //$NON-NLS-1$
//            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
//        }
//
//        return stringBuilder.toString();
//    }

    public static String stringToJava(String value) {
        return stringToJava(value, false);
    }

    public static String stringToJava(String value, boolean intented) {
        if (value == null) {
            return "null";
        } else {
            if (intented) {
                return "\"" + StringUtil.replace(value, "\"", "\\\"").replace("\n", "\\n\" +\n        \"").replace("\r", "") + "\"";
            } else {
                return "\"" + StringUtil.replace(value, "\"", "\\\"").replace("\n", "\\n\" +\n\"").replace("\r", "") + "\"";
            }
        }
    }

    public static String booleanToJava(Boolean value) {
        if (value == null) {
            return "false";
        } else {
            return "" + value;
        }
    }

}
