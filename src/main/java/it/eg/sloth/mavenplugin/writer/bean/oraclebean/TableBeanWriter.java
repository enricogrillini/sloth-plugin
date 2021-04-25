package it.eg.sloth.mavenplugin.writer.bean.oraclebean;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.dbschema.Column;
import it.eg.sloth.jaxb.dbschema.Constant;
import it.eg.sloth.jaxb.dbschema.Table;
import it.eg.sloth.jaxb.dbschema.TableColumn;
import it.eg.sloth.mavenplugin.common.GenUtil;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
public class TableBeanWriter {
    private static final String DECODE_BEAN = ".bean.decode";
    private static final String TABLE_BEAN = ".bean.tablebean";

    public static final String COLUMN = "    new Column ({0}, {1}, {2}, {3}, {4}, Types.{5})";

    private Table table;

    String tableBeanClassName;
    String tableBeanFullClassName;
    String tableBeanPackageName;
    File tableBeanClassFile;

    String rowBeanClassName;
    String rowBeanFullClassName;
    String rowBeanPackageName;
    File rowBeanClassFile;

    String decodeBeanClassName;
    String decodeBeanFullClassName;
    String decodeBeanPackageName;
    File decodeBeanClassFile;

    List<TableColumn> primaryKeyColumnList;
    List<TableColumn> lobColumnList;

    public TableBeanWriter(File outputJavaDirectory, String genPackage, Table table) {
        this.table = table;

        // TableBean properties
        tableBeanClassName = GenUtil.initCap(table.getName()) + "TableBean";
        tableBeanFullClassName = genPackage + TABLE_BEAN + "." + tableBeanClassName;
        tableBeanPackageName = genPackage + TABLE_BEAN;
        tableBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, tableBeanPackageName, tableBeanClassName);

        rowBeanClassName = GenUtil.initCap(table.getName()) + "RowBean";
        rowBeanFullClassName = genPackage + TABLE_BEAN + "." + rowBeanClassName;
        rowBeanPackageName = genPackage + TABLE_BEAN;
        rowBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, rowBeanPackageName, rowBeanClassName);

        decodeBeanClassName = GenUtil.initCap(table.getName()) + "DecodeBean";
        decodeBeanFullClassName = genPackage + DECODE_BEAN + "." + rowBeanClassName;
        decodeBeanPackageName = genPackage + DECODE_BEAN;
        decodeBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, decodeBeanPackageName, decodeBeanClassName);

        // primaryKeyColumnList
        primaryKeyColumnList = new ArrayList<>();
        for (TableColumn column : table.getColumns().getColumn()) {
            if (column.isPrimaryKey()) {
                primaryKeyColumnList.add(column);
            }
        }

        // lobColumnList
        lobColumnList = new ArrayList<>();
        for (TableColumn dbTableColumn : table.getColumns().getColumn()) {
            if (OracleUtil.isLob(dbTableColumn))
                lobColumnList.add(dbTableColumn);
        }
    }

    public StringBuilder getTableBean() {
        return new StringBuilder()
                .append("package " + tableBeanPackageName + ";\n")
                .append("\n")
                .append("import it.eg.sloth.db.query.SelectQueryInterface;\n")
                .append("import it.eg.sloth.framework.common.exception.FrameworkException;\n")
                .append("import it.eg.sloth.db.datasource.table.DbTable;\n")
                .append("import java.io.IOException;\n")
                .append("import java.sql.Connection;\n")
                .append("import java.sql.SQLException;\n")
                .append("\n")
                .append("/**\n")
                .append(" * TableBean per la tabella " + table.getName() + "\n")
                .append(" *\n")
                .append(" */\n")
                .append("public class " + tableBeanClassName + " extends DbTable<" + rowBeanClassName + "> {\n")
                .append("\n")
                .append("  \n")
                .append("\n")
                .append("  public static final String SELECT = \"Select * from " + table.getName() + " /*W*/\";\n")
                .append("  public static final String TABLE_NAME = \"" + table.getName().toUpperCase() + "\";\n")
                .append("\n")
                .append("  @Override\n")
                .append("  protected " + rowBeanClassName + " createRow () {\n")
                .append("    " + rowBeanClassName + " rowBean = new " + rowBeanClassName + "();\n")
                .append("    rowBean.setAutoloadLob(isAutoloadLob());\n")
                .append("    return rowBean;\n")
                .append("  }\n")
                .append("\n")
                .append("  @Override\n")
                .append("  protected " + tableBeanClassName + " newTable() {\n")
                .append("    return new " + tableBeanClassName + "();\n")
                .append("  }\n")
                .append("\n")
                .append("  public void load(" + rowBeanClassName + " " + GenUtil.initLow(rowBeanClassName) + ") throws SQLException, IOException, FrameworkException {\n")
                .append("    load(SELECT, " + rowBeanClassName + ".columns, " + GenUtil.initLow(rowBeanClassName) + ");\n")
                .append("  }\n")
                .append("\n")
                .append("  public void load(" + rowBeanClassName + " " + GenUtil.initLow(rowBeanClassName) + ", Connection connection) throws SQLException, IOException, FrameworkException {\n")
                .append("    load(SELECT, " + rowBeanClassName + ".columns, " + GenUtil.initLow(rowBeanClassName) + ", connection);\n")
                .append("  }\n")
                .append("\n")
                .append("  public static class Factory {\n")
                .append("\n")
                .append("    public static " + tableBeanClassName + " load(" + rowBeanClassName + " rowBean, int pageSize, Connection connection) throws SQLException, IOException, FrameworkException {\n")
                .append("      " + tableBeanClassName + " tableBean = new " + tableBeanClassName + "();\n")
                .append("      tableBean.load(rowBean, connection);\n")
                .append("      tableBean.setPageSize(pageSize);\n")
                .append("      return tableBean;\n")
                .append("    }\n")
                .append("\n")
                .append("    public static " + tableBeanClassName + " load(" + rowBeanClassName + " rowBean, int pageSize)throws SQLException, IOException, FrameworkException {\n")
                .append("      return load(rowBean, pageSize, null);\n")
                .append("    }\n")
                .append("\n")
                .append("    public static " + tableBeanClassName + " load(" + rowBeanClassName + " rowBean) throws SQLException, IOException, FrameworkException {\n")
                .append("      return load(rowBean, -1);\n")
                .append("    }\n")
                .append("\n")
                .append("    public static " + tableBeanClassName + " loadFromQuery(SelectQueryInterface query, int pageSize) throws SQLException, IOException, FrameworkException {\n")
                .append("      " + tableBeanClassName + " tableBean = new " + tableBeanClassName + "();\n")
                .append("      tableBean.loadFromQuery(query);\n")
                .append("      tableBean.setPageSize(pageSize);\n")
                .append("      return tableBean;\n")
                .append("    }\n")
                .append("\n")
                .append("    public static " + tableBeanClassName + " loadFromQuery(SelectQueryInterface query) throws SQLException, IOException, FrameworkException {\n")
                .append("      return loadFromQuery (query, -1);\n")
                .append("    }\n")
                .append("  }\n")
                .append("\n")
                .append("}");
    }

    private String getSelect() {
        String result = "Select *\n" + "From " + table.getName() + "\n";

        int i = 0;
        for (TableColumn column : primaryKeyColumnList) {
            if (!OracleUtil.isLob(column)) {
                result += i++ == 0 ? "Where " : " And\n       ";
                result += column.getName() + " = ?";
            }
        }

        return result;
    }

    private String getInsert() {
        String result = "Insert into " + table.getName() + "\n";

        int i = 0;
        for (TableColumn dbTableColumn : table.getColumns().getColumn()) {
            if (!OracleUtil.isLob(dbTableColumn)) {
                result += i++ == 0 ? "      (" : ",\n       ";
                result += dbTableColumn.getName();
            }
        }
        result += ")\n";

        i = 0;
        for (TableColumn column : table.getColumns().getColumn()) {
            if (!OracleUtil.isLob(column)) {
                result += i++ == 0 ? "Values (" : ",\n        ";
                result += "?";
            }
        }
        result += ")";

        return result;
    }

    private String getDelete() {
        String result = "Delete " + table.getName() + "\n";

        int i = 0;
        for (TableColumn column : primaryKeyColumnList) {
            if (!OracleUtil.isLob(column)) {
                result += i++ == 0 ? "Where " : " And\n       ";
                result += column.getName() + " = ?";
            }
        }

        return result;
    }

    private String getUpdate() {
        String result = "Update " + table.getName() + "\n";

        int i = 0;
        for (TableColumn column : table.getColumns().getColumn()) {
            if (!OracleUtil.isLob(column)) {
                result += i++ == 0 ? "Set " : ",\n    ";
                result += column.getName() + " = ?";
            }
        }
        result += "\n";

        i = 0;
        for (TableColumn column : primaryKeyColumnList) {
            if (!OracleUtil.isLob(column)) {
                result += i++ == 0 ? "Where " : " And\n       ";
                result += column.getName() + " = ?";
            }
        }

        return result;
    }

    private String getUpdateLob(TableColumn lobColumn) {
        String result = "Update " + table.getName() + "\n" + "Set " + lobColumn.getName() + " = empty_" + lobColumn.getType() + "()\n"; //$NON-NLS-5$

        int i = 0;
        for (TableColumn column : primaryKeyColumnList) {
            if (!OracleUtil.isLob(column)) {
                result += i++ == 0 ? "Where " : " And\n       ";
                result += column.getName() + " = ?";
            }
        }

        return result;
    }

    /**
     * Scrive le costanti
     *
     * @param testo
     */
    private void writeConstant(StringBuilder testo) {
        for (TableColumn dbTableColumn : table.getColumns().getColumn()) {
            testo.append("  public static final String " + dbTableColumn.getName().toUpperCase() + " = \"" + dbTableColumn.getName().toLowerCase() + "\";\n");
        }

        testo.append("\n");
        testo.append("  public static final Column[] columns = {");

        int i = 0;
        for (TableColumn dbTableColumn : table.getColumns().getColumn()) {
            testo.append((i++ == 0 ? "\n" : ",\n"));
            testo.append(
                    MessageFormat.format(
                            COLUMN,
                            dbTableColumn.getName().toUpperCase(),
                            StringUtil.toJavaStringParameter(dbTableColumn.getDescription()),
                            dbTableColumn.isPrimaryKey(),
                            dbTableColumn.isNullable(),
                            dbTableColumn.getDataPrecision(),
                            OracleUtil.getTypes(dbTableColumn.getType())
                    ));
        }

        testo.append("\n");
        testo.append("  };\n");
        testo.append("\n");

        testo.append("  private static String strSelect = \"" + getSelect().replaceAll("\\n", "\\\\n\" +\n                                    \"") + "\";\n\n");
        testo.append("  private static String strInsert = \"" + getInsert().replaceAll("\\n", "\\\\n\" +\n                                    \"") + "\";\n\n");

        if (primaryKeyColumnList.size() > 0) {
            testo.append("  private static String strDelete = \"" + getDelete().replaceAll("\\n", "\\\\n\" +\n                                    \"") + "\";\n\n");
            testo.append("  private static String strUpdate = \"" + getUpdate().replaceAll("\\n", "\\\\n\" +\n                                    \"") + "\";\n\n");
        } else {
            testo.append("  private static String strDelete = null;\n");
            testo.append("  private static String strUpdate = null;\n");
        }

        for (TableColumn dbTableColumn : lobColumnList) {
            testo.append("  private static String strUpdate" + GenUtil.initCap(dbTableColumn.getName()) + " = \"" + getUpdateLob(dbTableColumn).replaceAll("\\n", "\\\\n\" +\n                                    \"") + "\";\n\n"); //$NON-NLS-5$
        }
    }

    /**
     * Scrive il costruttore
     *
     * @param testo
     */
    private void writeConstructor(StringBuilder testo) {
        testo.append("  public " + rowBeanClassName + " () {\n");
        testo.append("    super();\n");
        testo.append("  }\n");
        testo.append("\n");

        // Costrutture su primary key
        if (primaryKeyColumnList.size() > 0) {
            testo.append("  public " + rowBeanClassName + " (");
            testo.append(OracleUtil.getPrimaryKeyParameters(primaryKeyColumnList, OracleUtil.FIELDS_GEN_TYPE.AS_PARAMETER));
            testo.append(") throws SQLException, IOException, FrameworkException {\n");
            testo.append("    this(null," + OracleUtil.getPrimaryKeyParameters(primaryKeyColumnList, OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE) + ");\n");
            testo.append("  }\n");
            testo.append("\n");
            testo.append("  public " + rowBeanClassName + " (Connection connection, ");
            testo.append(OracleUtil.getPrimaryKeyParameters(primaryKeyColumnList, OracleUtil.FIELDS_GEN_TYPE.AS_PARAMETER));
            testo.append(") throws SQLException, IOException, FrameworkException {\n");
            testo.append("    this();\n");

            for (TableColumn dbTableColumn : primaryKeyColumnList) {
                testo.append("    set" + GenUtil.initCap(dbTableColumn.getName()) + "(" + dbTableColumn.getName() + ");\n");
            }

            testo.append("    select(connection);\n");
            testo.append("  }\n");
            testo.append("\n");
        }

    }

    /**
     * Scrive i getter/setter
     *
     * @param testo
     */
    private void writeSetterGetter(StringBuilder testo) {

        testo.append("  @Override");
        testo.append("   public Column[] getColumns() {");
        testo.append("    return columns;");
        testo.append("   }");
        testo.append("\n");

        testo.append("  @Override");
        testo.append("  public String getSelect() {\n");
        testo.append("     return strSelect;\n");
        testo.append("  }\n");
        testo.append("\n");

        testo.append("  @Override");
        testo.append("  public String getInsert() {\n");
        testo.append("     return strInsert;\n");
        testo.append("  }\n");
        testo.append("\n");

        testo.append("  @Override");
        testo.append("  public String getDelete() {\n");
        testo.append("     return strDelete;\n");
        testo.append("  }\n");
        testo.append("\n");

        testo.append("  @Override");
        testo.append("  public String getUpdate() {\n");
        testo.append("     return strUpdate;\n");
        testo.append("  }\n");
        testo.append("\n");

        if (lobColumnList.size() > 0) {
            testo.append("  @Override\n");
            testo.append("  public void setObject(String name, Object value) {\n");

            for (TableColumn dbTableColumn : lobColumnList) {
                if (OracleUtil.isClob(dbTableColumn)) {
                    testo.append("    if (" + dbTableColumn.getName().toUpperCase() + ".equalsIgnoreCase(name) && value instanceof String) {\n");
                    testo.append("      set" + GenUtil.initCap(dbTableColumn.getName()) + "((String) value);\n");
                    testo.append("      return;\n");
                    testo.append("    }\n");
                } else if (OracleUtil.isBlob(dbTableColumn)) {
                    testo.append("    if (" + dbTableColumn.getName().toUpperCase() + ".equalsIgnoreCase(name) && value instanceof byte[]) {\n");
                    testo.append("      set" + GenUtil.initCap(dbTableColumn.getName()) + "((byte[]) value);\n");
                    testo.append("      return;\n");
                    testo.append("    }\n");
                }
            }

            testo.append("    super.setObject(name, value);\n");
            testo.append("  }\n");
        }

        // Scrivo i setter e i getter per le colonne
        for (TableColumn dbTableColumn : table.getColumns().getColumn()) {
            testo.append(OracleUtil.getSetterGetter(dbTableColumn, rowBeanClassName, true));
        }
    }

    /**
     * Scrive i metodi per la gestione delle query
     *
     * @param testo
     */
    private void writeQuery(StringBuilder testo) {

        // SelectQuery
        testo.append("  private Query selectQuery() {\n");
        testo.append("    Query query = new Query (getSelect());\n");

        for (TableColumn dbTableColumn : primaryKeyColumnList) {
            testo.append("    query.addParameter(Types." + OracleUtil.getTypes(dbTableColumn.getType()) + ", get" + GenUtil.initCap(dbTableColumn.getName()) + "());\n");
        }

        testo.append("    return query;\n");
        testo.append("  }\n");

        // Select
        testo.append("  public boolean select(Connection connection) throws SQLException, IOException, FrameworkException {\n")
                .append("    return loadFromQuery(selectQuery(), connection);\n")
                .append("  }\n")
                .append("\n");

        // UpdateLob
        testo.append("  private void updateLob(Connection connection) {\n");

        for (TableColumn dbTableColumn : table.getColumns().getColumn()) {
            if (OracleUtil.isBlob(dbTableColumn)) {
                testo.append("    try {\n");
                testo.append("      if (get" + GenUtil.initCap(dbTableColumn.getName()) + "BLobData().getStatus() == BLobData.CHANGED) {\n");
                testo.append("        Query query = new Query(strUpdate" + GenUtil.initCap(dbTableColumn.getName()) + ");\n");

                for (TableColumn primaryKey : primaryKeyColumnList) {
                    testo.append("        query.addParameter(Types." + OracleUtil.getTypes(primaryKey.getType()) + ", get" + GenUtil.initCap(primaryKey.getName()) + "());\n");
                }

                testo.append("        query.execute(connection);\n");
                testo.append("\n");
                testo.append("        if (get" + GenUtil.initCap(dbTableColumn.getName()) + "BLobData().getValue() != null) {\n");
                testo.append("          DataRow row = selectQuery().selectRow(connection);\n");
                testo.append("          Blob blob = (Blob) row.getObject(" + dbTableColumn.getName().toUpperCase() + ");\n");
                testo.append("          InputStream inputStream = new ByteArrayInputStream(get" + GenUtil.initCap(dbTableColumn.getName()) + "BLobData().getValue());\n");
                testo.append("          OutputStream outputStream = blob.setBinaryStream(0);\n");
                testo.append("\n");
                testo.append("          IOUtils.copy(inputStream, outputStream);\n");
                testo.append("\n");
                testo.append("          outputStream.close();\n");
                testo.append("          inputStream.close();\n");
                testo.append("        }\n");
                testo.append("      }\n");
                testo.append("    } catch (Throwable e) {\n");
                testo.append("      throw new RuntimeException(e);\n");
                testo.append("    }\n");
                testo.append("\n");
            } else if (OracleUtil.isClob(dbTableColumn)) {
                testo.append("    try {\n");
                testo.append("      if (get" + GenUtil.initCap(dbTableColumn.getName()) + "CLobData().getStatus() == CLobData.CHANGED) {\n");
                testo.append("        Query query = new Query(strUpdate" + GenUtil.initCap(dbTableColumn.getName()) + ");\n");

                for (TableColumn primaryKey : primaryKeyColumnList) {
                    testo.append("        query.addParameter(Types." + OracleUtil.getTypes(primaryKey.getType()) + ", get" + GenUtil.initCap(primaryKey.getName()) + "());\n");
                }

                testo.append("        query.execute(connection);\n");
                testo.append("\n");
                testo.append("        if (get" + GenUtil.initCap(dbTableColumn.getName()) + "CLobData().getValue() != null) {\n");
                testo.append("          DataRow row = selectQuery().selectRow(connection);\n");
                testo.append("          Clob clob = (Clob) row.getObject(" + dbTableColumn.getName().toUpperCase() + ");\n");
                testo.append("          try (Reader reader = new StringReader(get" + GenUtil.initCap(dbTableColumn.getName()) + "CLobData().getValue());\n");
                testo.append("            Writer outputStream = clob.setCharacterStream(0)) {\n");
                testo.append("            IOUtils.copy(reader, outputStream);\n");
                testo.append("          }\n");
                testo.append("        }\n");
                testo.append("      }\n");
                testo.append("    } catch (Throwable e) {\n");
                testo.append("      throw new RuntimeException(e);\n");
                testo.append("    }\n");
                testo.append("\n");
            }
        }
        testo.append("  }\n");
        testo.append("\n");

        // Update
        testo.append("  public void update(Connection connection) {\n");
        testo.append("    try {\n");
        testo.append("      PreparedStatement preparedStatement = connection.prepareStatement(getUpdate());\n");

        int j = 1;
        for (TableColumn dbTableColumn : table.getColumns().getColumn()) {
            if (!OracleUtil.isLob(dbTableColumn))
                testo.append("      preparedStatement.setObject (" + j++ + ", get" + GenUtil.initCap(dbTableColumn.getName()) + "(), Types." + OracleUtil.getTypes(dbTableColumn.getType()) + ");\n");
        }

        for (TableColumn dbTableColumn : primaryKeyColumnList) {
            if (!OracleUtil.isLob(dbTableColumn))
                testo.append("      preparedStatement.setObject (" + j++ + ", getOld" + GenUtil.initCap(dbTableColumn.getName()) + "(), Types." + OracleUtil.getTypes(dbTableColumn.getType()) + ");\n");
        }

        testo.append("\n");
        testo.append("      preparedStatement.executeUpdate();\n");
        testo.append("      preparedStatement.close();\n");
        testo.append("\n");
        testo.append("      updateLob (connection);\n");
        testo.append("    } catch (Throwable e) {\n");
        testo.append("      throw new RuntimeException (e);\n");
        testo.append("    }\n");
        testo.append("  }\n");
        testo.append("\n");

        // Insert
        testo.append("  public void insert(Connection connection) {\n");
        testo.append("    try {\n");
        testo.append("      PreparedStatement preparedStatement = connection.prepareStatement(getInsert());\n");

        j = 1;
        for (TableColumn dbTableColumn : table.getColumns().getColumn()) {
            if (!OracleUtil.isLob(dbTableColumn))
                testo.append("      preparedStatement.setObject (" + j++ + ", get" + GenUtil.initCap(dbTableColumn.getName()) + "(), Types." + OracleUtil.getTypes(dbTableColumn.getType()) + ");\n");
        }

        testo.append("\n");
        testo.append("      preparedStatement.executeUpdate();\n");
        testo.append("      preparedStatement.close() ;\n");
        testo.append("\n");
        testo.append("      updateLob (connection);\n");
        testo.append("    } catch (Throwable e) {\n");
        testo.append("      throw new RuntimeException (e);\n");
        testo.append("    }\n");
        testo.append("  }\n");
        testo.append("\n");

        // Delete
        testo.append("  public void delete(Connection connection) {\n");
        testo.append("    try {\n");
        testo.append("      PreparedStatement preparedStatement = connection.prepareStatement(getDelete());\n");

        j = 1;
        for (TableColumn dbTableColumn : primaryKeyColumnList) {
            if (!OracleUtil.isLob(dbTableColumn))
                testo.append("      preparedStatement.setObject (" + j++ + ", getOld" + GenUtil.initCap(dbTableColumn.getName()) + "(), Types." + OracleUtil.getTypes(dbTableColumn.getType()) + ");\n");
        }

        testo.append("\n");
        testo.append("      preparedStatement.executeUpdate();\n");
        testo.append("      preparedStatement.close() ;\n");
        testo.append("    } catch (Throwable e) {\n");
        testo.append("      throw new RuntimeException (e);\n");
        testo.append("    }\n");
        testo.append("  }\n");
    }

    /**
     * Scrive i metodi per la gestione delle query
     *
     * @param testo
     */
    private void writeFactory(StringBuilder testo) {
        testo.append("  public static class Factory {\n")
                .append("\n")
                .append("    public static " + rowBeanClassName + " load(SelectQueryInterface selectQueryInterface) throws SQLException, IOException, FrameworkException {\n")
                .append("      " + rowBeanClassName + " " + GenUtil.initLow(rowBeanClassName) + " = new " + rowBeanClassName + "();\n")
                .append("      " + GenUtil.initLow(rowBeanClassName) + ".loadFromQuery(selectQueryInterface);\n")
                .append("      return " + GenUtil.initLow(rowBeanClassName) + ";\n")
                .append("    }\n")
                .append("\n")
                .append("  }\n");
    }

    public StringBuilder getRowBean() {
        StringBuilder testo = new StringBuilder()
                .append("package " + rowBeanPackageName + ";\n")
                .append("\n")
                .append("import it.eg.sloth.db.query.SelectQueryInterface;\n")
                .append("import it.eg.sloth.db.query.query.Query;\n")
                .append("import it.eg.sloth.framework.common.exception.FrameworkException;\n")
                .append("import it.eg.sloth.db.datasource.row.DbRow;\n")
                .append("import it.eg.sloth.db.datasource.DataRow;\n")
                .append("import it.eg.sloth.db.datasource.RowStatus;\n")
                .append("import it.eg.sloth.db.datasource.row.lob.BLobData;\n")
                .append("import it.eg.sloth.db.datasource.row.lob.CLobData;\n")
                .append("import it.eg.sloth.db.datasource.row.column.Column;\n")
                .append("import org.apache.commons.io.IOUtils;\n")
                .append("import it.eg.sloth.db.manager.DataConnectionManager;\n")
                .append("import java.io.ByteArrayInputStream;\n")
                .append("import java.io.IOException;\n")
                .append("import java.io.InputStream;\n")
                .append("import java.io.OutputStream;\n")
                .append("\n")
                .append("import java.math.BigDecimal;\n")
                .append("import java.sql.Clob;\n")
                .append("import java.sql.Blob;\n")
                .append("import java.sql.Connection;\n")
                .append("import java.sql.PreparedStatement;\n")
                .append("import java.sql.ResultSet;\n")
                .append("import java.sql.SQLException;\n")
                .append("  import java.io.Reader;\n")
                .append("import java.io.StringReader;\n")
                .append("import java.io.Writer;\n")
                .append("import java.sql.Timestamp;\n")
                .append("import java.sql.Types;\n")
                .append("import lombok.SneakyThrows;\n")
                .append("\n")
                .append("/**\n")
                .append(" * RowBean per la tabella " + table.getName() + "\n")
                .append(" *\n")
                .append(" */\n")
                .append("public class " + rowBeanClassName + " extends DbRow {\n")
                .append("\n")
                .append("  \n")
                .append("\n");

        writeConstant(testo);
        writeConstructor(testo);
        writeSetterGetter(testo);
        writeQuery(testo);
        writeFactory(testo);

        testo.append("}\n");

        return testo;

    }

    public StringBuilder getDecodeBean() {
        StringBuilder testo = new StringBuilder();

        TableColumn firstColumn = table.getColumns().getColumn().get(0);

        String firstColumnJavaClass = OracleUtil.getJavaClass(firstColumn.getType());
        String firstColumnName = table.getColumns().getColumn().get(0).getName().toUpperCase();

        testo.append("package " + decodeBeanPackageName + ";\n")
                .append("\n")
                .append("import it.eg.sloth.framework.common.cache.CacheSingleton;\n")
                .append("import it.eg.sloth.db.decodemap.map.TableDecodeMap;\n")
                .append(" import it.eg.sloth.framework.common.exception.FrameworkException;\n")
                .append("import " + tableBeanPackageName + "." + tableBeanClassName + ";\n")
                .append("import " + rowBeanPackageName + "." + rowBeanClassName + ";\n")
                .append("\n")
                .append("import java.math.BigDecimal;\n")
                .append("import java.sql.Timestamp;\n")
                .append("import java.io.IOException;\n")
                .append("import java.sql.SQLException;\n")
                .append("\n")
                .append("public class " + decodeBeanClassName + " extends TableDecodeMap<" + firstColumnJavaClass + ", " + rowBeanClassName + "> {\n")
                .append("  \n")
                .append("\n");

        // Costanti
        for (Constant constant : table.getConstants().getConstant()) {
            testo.append("  public static final String " + constant.getName() + " = \"" + constant.getValue() + "\";\n");
        }

        // Metodi
        testo.append("\n");
        String comma = OracleUtil.getFlags(table.getColumns().getColumn()).size() > 0 ? ", " : "";
        testo.append("  public " + decodeBeanClassName + "(boolean onlyValid, boolean fullDescription " + comma + OracleUtil.getFlagsForDecodeKey(table.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_PARAMETER, false) + ") throws SQLException, IOException, FrameworkException {\n");
        testo.append("    super();\n"); // $NON-NLS-5$
        testo.append("\n");
        testo.append("    " + rowBeanClassName + " row = new " + rowBeanClassName + "();\n");
        testo.append("    row.setFlagvalido(onlyValid ? \"S\" : null);\n");

        for (Column column : OracleUtil.getFlags(table.getColumns().getColumn())) {
            testo.append("    row.set" + column.getName() + "(" + column.getName() + ");\n");
        }

        testo.append("    " + tableBeanClassName + " table = " + tableBeanClassName + ".Factory.load(row);\n");
        testo.append("    table.addSortingRule(" + rowBeanClassName + ".POSIZIONE);\n");
        testo.append("    table.addSortingRule(fullDescription ? " + rowBeanClassName + ".DESCRIZIONELUNGA : " + rowBeanClassName + ".DESCRIZIONEBREVE);\n");
        testo.append("    table.applySort();\n");
        testo.append("\n");
        testo.append("    load(table, " + rowBeanClassName + "." + firstColumnName + ",  fullDescription ? " + rowBeanClassName + ".DESCRIZIONELUNGA : " + rowBeanClassName + ".DESCRIZIONEBREVE);\n"); //$NON-NLS-5$
        testo.append("  }\n");
        testo.append("\n");

        // Factory
        testo.append("  public static class Factory {\n");
        testo.append("\n");
        testo.append("    public static " + decodeBeanClassName + " getFromDb() throws SQLException, IOException, FrameworkException {\n");
        testo.append("      return new " + decodeBeanClassName + "(false, true " + comma + OracleUtil.getFlagsForDecodeKey(table.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, true) + ");\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static " + decodeBeanClassName + " getFromDb(boolean onlyValid, boolean fullDescription) throws SQLException, IOException, FrameworkException {\n");
        testo.append("      return new " + decodeBeanClassName + "(onlyValid, fullDescription " + comma + OracleUtil.getFlagsForDecodeKey(table.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, true) + ");\n");
        testo.append("    }\n");
        testo.append("\n");
        if (OracleUtil.getFlags(table.getColumns().getColumn()).size() > 0) {
            testo.append("    public static " + decodeBeanClassName + " getFromDb(" + OracleUtil.getFlagsForDecodeKey(table.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_PARAMETER, false) + ") throws SQLException, IOException, FrameworkException {\n");
            testo.append("      return new " + decodeBeanClassName + "(false, true, " + OracleUtil.getFlagsForDecodeKey(table.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, false) + ");\n");
            testo.append("    }\n");
            testo.append("\n");
            testo.append("    public static " + decodeBeanClassName + " getFromDb(boolean onlyValid, boolean fullDescription " + comma + OracleUtil.getFlagsForDecodeKey(table.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_PARAMETER, false) + ") throws SQLException, IOException, FrameworkException {\n");
            testo.append("      return new " + decodeBeanClassName + "(onlyValid, fullDescription, " + OracleUtil.getFlagsForDecodeKey(table.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, false) + ");\n");
            testo.append("    }\n");
            testo.append("\n");
        }
        testo.append("    public static synchronized " + decodeBeanClassName + " getFromCache() throws SQLException, IOException, FrameworkException {\n");
        testo.append("      " + decodeBeanClassName + " decodeBean = (" + decodeBeanClassName + ") CacheSingleton.getInstance().get(" + decodeBeanClassName + ".class.getName());\n");
        testo.append("      if (decodeBean == null) {\n");
        testo.append("        decodeBean = getFromDb();\n");
        testo.append("        CacheSingleton.getInstance().put(" + decodeBeanClassName + ".class.getName(), decodeBean);\n");
        testo.append("      }\n");
        testo.append("\n");
        testo.append("      return decodeBean;\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static String decode (String code) throws SQLException, IOException, FrameworkException {\n");
        testo.append("      return getFromDb().decode(code);\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static  " + rowBeanClassName + " getRowBean (String code) throws SQLException, IOException, FrameworkException {\n");
        testo.append("      return getFromDb().getRowBean(code);\n");
        testo.append("    }\n");
        testo.append("  }\n");
        testo.append("}\n");

        return testo;
    }

    public void write() throws IOException {
        // Row Bean
        GenUtil.writeFile(rowBeanClassFile, getRowBean().toString());

        // Decode Bean
        if (table.getName().toUpperCase().contains("DEC_")) {
            GenUtil.writeFile(decodeBeanClassFile, getDecodeBean().toString());
        }
    }

}
