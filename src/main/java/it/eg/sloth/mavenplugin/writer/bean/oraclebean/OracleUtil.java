package it.eg.sloth.mavenplugin.writer.bean.oraclebean;

import it.eg.sloth.jaxb.dbschema.*;
import it.eg.sloth.mavenplugin.common.GenUtil;

import java.util.*;

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
public class OracleUtil {

    public static boolean isBoolean(String dataType) {
        if (dataType == null)
            return false;

        if (dataType.indexOf("PL/SQL BOOLEAN") == 0)
            return true;
        else
            return false;
    }

    public static String getJavaClass(String dataType) {
        dataType = dataType.toUpperCase();

        if (dataType.startsWith("NUMBER") || dataType.startsWith("DOUBLE") || dataType.startsWith("FLOAT") || dataType.startsWith("BIT") || dataType.startsWith("BIGINT") || dataType.startsWith("INT") || dataType.startsWith("TINYINT") || dataType.startsWith("UNIQUEIDENTIFIER") || dataType.startsWith("DECIMAL")) {    //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
            return "BigDecimal";
        } else if (dataType.startsWith("DATE")) {
            return "Timestamp";
        } else if (dataType.startsWith("TIMESTAMP")) {
            return "Timestamp";
        } else if (dataType.startsWith("VARCHAR") || dataType.startsWith("CHAR") || dataType.startsWith("LONG")) {
            return "String";
        } else if (dataType.startsWith("BLOB")) {
            return "byte[]";
        } else if (dataType.startsWith("CLOB")) {
            return "String";
        } else if (dataType.startsWith("PL/SQL BOOLEAN")) {
            return "boolean";
        } else {
            return null;
        }
    }

    public static String getTypes(String dataType) {
        dataType = dataType.toUpperCase();

        if (dataType.equals("NUMBER(38,0)")) {
            return "INTEGER";
        } else if (dataType.startsWith("NUMBER") || dataType.startsWith("DOUBLE") || dataType.startsWith("FLOAT") || dataType.startsWith("BIT") || dataType.startsWith("BIGINT") || dataType.startsWith("INT") || dataType.startsWith("TINYINT") || dataType.startsWith("UNIQUEIDENTIFIER") || dataType.startsWith("DECIMAL")) {    //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
            return "DECIMAL";
        } else if (dataType.startsWith("DATE")) {
            return "DATE";
        } else if (dataType.startsWith("TIMESTAMP")) {
            return "TIMESTAMP";
        } else if (dataType.startsWith("VARCHAR") || dataType.startsWith("CHAR") || dataType.startsWith("LONG")) {
            return "VARCHAR";
        } else if (dataType.startsWith("BLOB")) {
            return "BLOB";
        } else if (dataType.startsWith("CLOB")) {
            return "CLOB";
        } else if (dataType.startsWith("PL/SQL BOOLEAN")) {
            return "VARCHAR";
        } else {
            return null;
        }

    }


    public static boolean isJavaPortable(Method method) {
        int i = 0;
        for (Argument dbArgument : method.getArguments().getArgument()) {
            if (dbArgument.getType().indexOf("RECORD") >= 0) {
                return false;
            } else if (dbArgument.getType().indexOf("TABLE") >= 0) {
                return false;
            } else if (dbArgument.getInOut().equals("INOUT")) {
                return false;
            } else if (dbArgument.getInOut().equals("OUT") && i > 0) {
                return false;
            }

            i++;
        }

        return true;
    }

    /**
     * Ritorna la lista delle colonne extra rispetto alle colonne previste per i DecodeBean (chiave, DESCRIZIONEBREVE, DESCRIZIONELUNGA, POSIZIONE, FLAGVALIDO)
     *
     * @param columnList
     * @return
     */
    public static List<Column> getFlags(List<? extends Column> columnList) {
        List<Column> result = new ArrayList<>();

        Set<String> excludeList = new HashSet<>();
        excludeList.add(columnList.get(0).getName().toUpperCase());
        excludeList.add("DESCRIZIONEBREVE");
        excludeList.add("DESCRIZIONELUNGA");
        excludeList.add("POSIZIONE");
        excludeList.add("FLAGVALIDO");

        for (Column column : columnList) {
            if (!excludeList.contains(column.getName().toUpperCase())) {
                result.add(column);
            }
        }

        return result;
    }

    public static String getFlagsForDecodeKey(List<? extends Column> columnList, FIELDS_GEN_TYPE genType, boolean nullValues) {
        StringBuilder testo = new StringBuilder("");
        boolean isFirst = true;

        for (Column column : getFlags(columnList)) {
            String field = genType.equals(FIELDS_GEN_TYPE.AS_PARAMETER) ? OracleUtil.getJavaClass(column.getType()) + " " + column.getName() : " " + (nullValues ? "null" : column.getName());
            if (!isFirst) {
                testo.append(", ");
            } else {
                isFirst = false;
            }
            testo.append(field);
        }
        return testo.toString();
    }


    public static boolean isLob(Column column) {
        return isBlob(column) || isClob(column);
    }


    public static boolean isBlob(Column column) {
        return "BLOB".equalsIgnoreCase(column.getType());
    }


    public static boolean isClob(Column column) {
        return "CLOB".equalsIgnoreCase(column.getType());
    }


    public static String getSetterGetter(Column column, String RowBeanName, boolean isTable) {
        StringBuilder testo = new StringBuilder();

        if (!isLob(column)) {
            testo.append("  public " + getJavaClass(column.getType()) + " get" + GenUtil.initCap(column.getName()) + "() {\n");
            testo.append("    return get" + getJavaClass(column.getType()) + "(" + column.getName().toUpperCase() + ");\n");
            testo.append("  }\n");
            testo.append("\n");
            testo.append("  public " + OracleUtil.getJavaClass(column.getType()) + " getOld" + GenUtil.initCap(column.getName()) + "() {\n");
            testo.append("    return getOld" + OracleUtil.getJavaClass(column.getType()) + "(" + column.getName().toUpperCase() + ");\n");
            testo.append("  }\n");
            testo.append("\n");
            testo.append("  public void set" + GenUtil.initCap(column.getName()) + "(" + OracleUtil.getJavaClass(column.getType()) + " " + column.getName().toLowerCase() + ") {\n");
            testo.append("    setObject(" + column.getName().toUpperCase() + ", " + column.getName().toLowerCase() + ");\n");
            testo.append("  }\n");
            testo.append("\n");

        } else if (isBlob(column)) {

            testo.append("  protected void set" + GenUtil.initCap(column.getName()) + "BLobData(BLobData bLobData) {\n");
            testo.append("    super.setObject(" + column.getName().toUpperCase() + ", bLobData);\n");
            testo.append("  }\n");
            testo.append("\n");
            testo.append("  @SneakyThrows\n");
            testo.append("  protected BLobData get" + GenUtil.initCap(column.getName()) + "BLobData() {\n");
            testo.append("    if (super.getObject(" + column.getName().toUpperCase() + ") == null) {\n");
            testo.append("      set" + GenUtil.initCap(column.getName()) + "BLobData(new BLobData(isAutoloadLob(), null));\n");
            testo.append("    }\n");
            testo.append("    return (BLobData) super.getObject(" + column.getName().toUpperCase() + ");\n");
            testo.append("  }\n");
            testo.append("\n");

            if (isTable) {
                testo.append("  public byte[] get" + GenUtil.initCap(column.getName()) + "(Connection connection) throws SQLException, FrameworkException, IOException {\n");
                testo.append("      if (get" + GenUtil.initCap(column.getName()) + "BLobData().getStatus() != BLobData.OFF_LINE) {\n");
                testo.append("        return get" + GenUtil.initCap(column.getName()) + "BLobData().getValue();\n");
                testo.append("      }\n");
                testo.append("\n");
                testo.append("      if (this.getStatus() == RowStatus.INSERTED || this.getStatus() == RowStatus.INCONSISTENT) {\n");
                testo.append("        return null;\n");
                testo.append("      }\n");
                testo.append("\n");
                testo.append("      if (connection == null) {\n");
                testo.append("        try (Connection newConnection = DataConnectionManager.getInstance().getDataSource().getConnection()) {\n");
                testo.append("          return get" + GenUtil.initCap(column.getName()) + "(newConnection);\n");
                testo.append("        }\n");
                testo.append("      } else {\n");
                testo.append("        DataRow row = selectQuery().selectRow(connection);\n");
                testo.append("        BLobData bLobData = new BLobData(true, (Blob) row.getObject(" + column.getName().toUpperCase() + "));\n");
                testo.append("        set" + GenUtil.initCap(column.getName()) + "BLobData(bLobData);\n");
                testo.append("\n");
                testo.append("        return bLobData.getValue();\n");
                testo.append("      }\n");
                testo.append("  }\n");
                testo.append("\n");
                testo.append("  public byte[] get" + GenUtil.initCap(column.getName()) + "() throws SQLException, FrameworkException, IOException {\n");
                testo.append("    return get" + GenUtil.initCap(column.getName()) + "(null);\n");
                testo.append("  }\n");
                testo.append("\n");
            } else {
                testo.append("  public byte[] get" + GenUtil.initCap(column.getName()) + "() throws SQLException, FrameworkException, IOException {\n");
                testo.append("    return get" + GenUtil.initCap(column.getName()) + "BLobData().getValue();\n");
                testo.append("  }\n");
                testo.append("\n");
            }

            testo.append("  public void set" + GenUtil.initCap(column.getName()) + "(byte[] " + column.getName().toLowerCase() + ") {\n");
            testo.append("    get" + GenUtil.initCap(column.getName()) + "BLobData().setValue(" + column.getName().toLowerCase() + ");\n");
            testo.append("  }\n");
            testo.append("\n");

        } else if (isClob(column)) {

            testo.append("  protected void set" + GenUtil.initCap(column.getName()) + "CLobData(CLobData cLobData) {\n");
            testo.append("    super.setObject(" + column.getName().toUpperCase() + ", cLobData);\n");
            testo.append("  }\n");
            testo.append("\n");
            testo.append("  @SneakyThrows\n");
            testo.append("  protected CLobData get" + GenUtil.initCap(column.getName()) + "CLobData() {\n");
            testo.append("    if (super.getObject(" + column.getName().toUpperCase() + ") == null) {\n");
            testo.append("      set" + GenUtil.initCap(column.getName()) + "CLobData(new CLobData(isAutoloadLob(), null));\n");
            testo.append("    }\n");
            testo.append("    return (CLobData) super.getObject(" + column.getName().toUpperCase() + ");\n");
            testo.append("  }\n");
            testo.append("\n");

            if (isTable) {
                testo.append("  public String get" + GenUtil.initCap(column.getName()) + "(Connection connection) throws SQLException, FrameworkException, IOException {\n")
                        .append("    try {\n")
                        .append("      if (get" + GenUtil.initCap(column.getName()) + "CLobData().getStatus() != CLobData.OFF_LINE) {\n")
                        .append("        return get" + GenUtil.initCap(column.getName()) + "CLobData().getValue();\n")
                        .append("      }\n")
                        .append("\n")
                        .append("      if (this.getStatus() == RowStatus.INSERTED || this.getStatus() == RowStatus.INCONSISTENT) {\n")
                        .append("        return null;\n")
                        .append("      }\n")
                        .append("\n")
                        .append("      if (connection == null) {\n")
                        .append("        try (Connection newConnection = DataConnectionManager.getInstance().getDataSource().getConnection()) {\n")
                        .append("          return get" + GenUtil.initCap(column.getName()) + "(newConnection);\n")
                        .append("        }\n")
                        .append("      } else {\n")
                        .append("        DataRow row = selectQuery().selectRow(connection);\n")
                        .append("        CLobData cLobData = new CLobData(true, (Clob) row.getObject(" + column.getName().toUpperCase() + "));\n")
                        .append("        set" + GenUtil.initCap(column.getName()) + "CLobData(cLobData);\n")
                        .append("        return cLobData.getValue();\n")
                        .append("      }\n")
                        .append("    } catch (Throwable t) {\n")
                        .append("      throw new RuntimeException(t);\n")
                        .append("    }\n")
                        .append("  }\n")
                        .append("\n")
                        .append("  public String get" + GenUtil.initCap(column.getName()) + "() throws SQLException, FrameworkException, IOException {\n")
                        .append("    return get" + GenUtil.initCap(column.getName()) + "(null);\n")
                        .append("  }\n")
                        .append("\n");
            } else {
                testo.append("  public String get" + GenUtil.initCap(column.getName()) + "() throws SQLException, FrameworkException, IOException {\n")
                        .append("    return get" + GenUtil.initCap(column.getName()) + "CLobData().getValue();\n")
                        .append("  }\n")
                        .append("\n");
            }

            testo.append("  public void set" + GenUtil.initCap(column.getName()) + "(String " + column.getName().toLowerCase() + ") {\n")
                    .append("    get" + GenUtil.initCap(column.getName()) + "CLobData().setValue(" + column.getName().toLowerCase() + ");\n")
                    .append("  }\n")
                    .append("\n");

        }

        return testo.toString();
    }

    public static String getPrimaryKeyParameters(List<TableColumn> primaryKeyList, FIELDS_GEN_TYPE genType) {
        StringBuilder testo = new StringBuilder("");
        int i = 0;
        for (TableColumn column : primaryKeyList) {
            String field = genType.equals(FIELDS_GEN_TYPE.AS_PARAMETER) ? OracleUtil.getJavaClass(column.getType()) + " " + column.getName() : " " + column.getName();
            testo.append((++i == primaryKeyList.size()) ? field : field + ", ");
        }

        return testo.toString();
    }

    public enum FIELDS_GEN_TYPE {
        AS_PARAMETER, AS_VARIABLE
    }

}
