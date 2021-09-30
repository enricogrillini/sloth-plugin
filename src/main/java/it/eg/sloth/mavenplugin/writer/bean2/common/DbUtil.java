package it.eg.sloth.mavenplugin.writer.bean2.common;

import it.eg.sloth.dbmodeler.model.schema.table.Table;
import it.eg.sloth.dbmodeler.model.schema.table.TableColumn;
import it.eg.sloth.dbmodeler.model.schema.view.ViewColumn;
import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.mavenplugin.common.GenUtil;

import java.text.MessageFormat;

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
public class DbUtil {

    private DbUtil() {
        // NOP
    }

    public static final String COLUMN = "new Column ({0}, {1}, {2}, {3}, {4}, {5})";

    private static String getJavaClass(String type) {
        String dataType = type.toUpperCase();

        if (dataType.startsWith("NUMBER") || dataType.startsWith("DOUBLE") || dataType.startsWith("FLOAT") || dataType.startsWith("BIT") || dataType.startsWith("BIGINT") || dataType.startsWith("INT") || dataType.startsWith("TINYINT") || dataType.startsWith("UNIQUEIDENTIFIER") || dataType.startsWith("DECIMAL")) {
            return "BigDecimal";
        } else if (dataType.startsWith("DATE")) {
            return "Timestamp";
        } else if (dataType.startsWith("TIMESTAMP")) {
            return "Timestamp";
        } else if (dataType.startsWith("VARCHAR") || dataType.startsWith("CHAR") || dataType.startsWith("LONG") || dataType.startsWith("TEXT")) {
            return "String";
        } else if (dataType.startsWith("BLOB") || dataType.startsWith("BYTEA")) {
            return "byte[]";
        } else if (dataType.startsWith("CLOB")) {
            return "String";
        } else if (dataType.startsWith("PL/SQL BOOLEAN")) {
            return "boolean";
        } else {
            return null;
        }
    }

    public static String getJavaClass(ViewColumn tableColumn) {
        return getJavaClass(tableColumn.getType());
    }

    public static String getJavaClass(TableColumn tableColumn) {
        return getJavaClass(tableColumn.getType());
    }

    private static String getTypes(String type) {
        String dataType = type.toUpperCase();

        if (dataType.equals("NUMBER(38,0)")) {
            return "Types.INTEGER";
        } else if (dataType.startsWith("NUMBER") || dataType.startsWith("DOUBLE") || dataType.startsWith("FLOAT") || dataType.startsWith("BIT") || dataType.startsWith("BIGINT") || dataType.startsWith("INT") || dataType.startsWith("TINYINT") || dataType.startsWith("UNIQUEIDENTIFIER") || dataType.startsWith("DECIMAL")) {
            return "Types.DECIMAL";
        } else if (dataType.startsWith("DATE")) {
            return "Types.DATE";
        } else if (dataType.startsWith("TIMESTAMP")) {
            return "Types.TIMESTAMP";
        } else if (dataType.startsWith("VARCHAR") || dataType.startsWith("CHAR") || dataType.startsWith("LONG") || dataType.startsWith("TEXT")) {
            return "Types.VARCHAR";
        } else if (dataType.startsWith("BYTEA")) {
            return "Types.BINARY";
        } else if (dataType.startsWith("BLOB")) {
            return "Types.BLOB";
        } else if (dataType.startsWith("CLOB")) {
            return "Types.CLOB";
        } else if (dataType.startsWith("PL/SQL BOOLEAN")) {
            return "Types.VARCHAR";
        } else {
            return null;
        }
    }

    public static String getTypes(TableColumn tableColumn) {
        return getTypes(tableColumn.getType());
    }

    public static String getTypes(ViewColumn viewColumn) {
        return getTypes(viewColumn.getType());
    }

    public static String genColumn(TableColumn tableColumn) {
        return MessageFormat.format(
                COLUMN,
                tableColumn.getName().toUpperCase(),
                StringUtil.toJavaStringParameter(tableColumn.getDescription()),
                tableColumn.isPrimaryKey(),
                tableColumn.isNullable(),
                tableColumn.getDataPrecision(),
                getTypes(tableColumn.getType())
        );
    }

    public static String genColumn(ViewColumn tableColumn) {
        return MessageFormat.format(
                COLUMN,
                tableColumn.getName().toUpperCase(),
                StringUtil.toJavaStringParameter(tableColumn.getDescription()),
                false,
                true,
                tableColumn.getDataPrecision(),
                getTypes(tableColumn.getType())
        );
    }

    public static String genPrimaryKeyList(Table table, boolean type) {
        StringBuilder result = new StringBuilder();
        for (TableColumn column : table.getPrimaryKeyCollection()) {
            if (result.length() != 0) {
                result.append(", ");
            }

            if (type) {
                result.append(getJavaClass(column) + " ");
            }

            result.append(GenUtil.initLow(column.getName()));
        }

        return result.toString();
    }

    public static String genSelect(Table table) {
        StringBuilder result = new StringBuilder("Select *\n" + "From " + table.getName() + "\n");

        int i = 0;
        for (TableColumn column : table.getPrimaryKeyCollection()) {
            result
                    .append(i++ == 0 ? "Where " : " And\n       ")
                    .append(column.getName() + " = ?");
        }

        return GenUtil.stringToJava(result.toString(), true);
    }

    public static String genInsert(Table table) {
        StringBuilder result = new StringBuilder("Insert into " + table.getName() + "\n");

        int i = 0;
        for (TableColumn column : table.getPlainColumnCollection()) {
            if (!column.isLob()) {
                result
                        .append(i++ == 0 ? "      (" : ",\n       ")
                        .append(column.getName());
            }
        }
        result.append(")\n");

        i = 0;
        for (TableColumn column : table.getPlainColumnCollection()) {
            result
                    .append(i++ == 0 ? "Values (" : ",\n        ")
                    .append("?");
        }
        result.append(")");
        return GenUtil.stringToJava(result.toString(), true);
    }

    public static String genDelete(Table table) {
        StringBuilder result = new StringBuilder("Delete From " + table.getName() + "\n");

        int i = 0;
        for (TableColumn column : table.getPrimaryKeyCollection()) {
            result
                    .append(i++ == 0 ? "Where " : " And\n       ")
                    .append(column.getName() + " = ?");
        }

        return GenUtil.stringToJava(result.toString(), true);
    }


    public static String genUpdate(Table table) {
        StringBuilder result = new StringBuilder("Update " + table.getName() + "\n");

        int i = 0;
        for (TableColumn column : table.getTableColumnCollection()) {
            if (!column.isLob()) {
                result
                        .append(i++ == 0 ? "Set " : ",\n    ")
                        .append(column.getName() + " = ?");
            }
        }
        result.append("\n");

        i = 0;
        for (TableColumn column : table.getPrimaryKeyCollection()) {
            result
                    .append(i++ == 0 ? "Where " : " And\n       ")
                    .append(column.getName() + " = ?");

        }

        return GenUtil.stringToJava(result.toString(), true);
    }

    public static String genUdateLob(Table table, TableColumn column) {
        StringBuilder result = new StringBuilder("Update " + table.getName() + "\n");
        if (column.isClob()) {
            result.append("Set " + column.getName() + " = empty_CLOB()\n");
        } else if (column.isBlob()) {
            result.append("Set " + column.getName() + " = empty_BLOB()\n");
        }

        int i = 0;
        for (TableColumn tableColumn : table.getPrimaryKeyCollection()) {
            result
                    .append(i++ == 0 ? "Where " : " And\n       ")
                    .append(tableColumn.getName() + " = ?");

        }

        return GenUtil.stringToJava(result.toString(), true);
    }
}
