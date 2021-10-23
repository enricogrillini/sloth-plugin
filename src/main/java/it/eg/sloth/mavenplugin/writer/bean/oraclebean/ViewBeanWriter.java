package it.eg.sloth.mavenplugin.writer.bean.oraclebean;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.dbschema.Column;
import it.eg.sloth.jaxb.dbschema.Constant;
import it.eg.sloth.jaxb.dbschema.View;
import it.eg.sloth.jaxb.dbschema.ViewColumn;
import it.eg.sloth.mavenplugin.common.GenUtil;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
public class ViewBeanWriter {
    private static final String DECODE_BEAN = ".bean.decode";
    private static final String VIEW_BEAN = ".bean.viewbean";

    private View view;

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

    List<ViewColumn> lobColumnList;

    public ViewBeanWriter(File outputJavaDirectory, String genPackage, View view) {
        this.view = view;

        // TableBean properties
        tableBeanClassName = GenUtil.initCap(view.getName()) + "TableBean";
        tableBeanFullClassName = genPackage + VIEW_BEAN + "." + tableBeanClassName;
        tableBeanPackageName = genPackage + VIEW_BEAN;
        tableBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, tableBeanPackageName, tableBeanClassName);

        rowBeanClassName = GenUtil.initCap(view.getName()) + "RowBean";
        rowBeanFullClassName = genPackage + VIEW_BEAN + "." + rowBeanClassName;
        rowBeanPackageName = genPackage + VIEW_BEAN;
        rowBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, rowBeanPackageName, rowBeanClassName);

        decodeBeanClassName = GenUtil.initCap(view.getName()) + "DecodeBean";
        decodeBeanFullClassName = genPackage + DECODE_BEAN + "." + decodeBeanClassName;
        decodeBeanPackageName = genPackage + DECODE_BEAN;
        decodeBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, decodeBeanPackageName, decodeBeanClassName);

        // lobColumnList
        lobColumnList = new ArrayList<>();
        for (ViewColumn column : view.getColumns().getColumn()) {
            if (OracleUtil.isLob(column))
                lobColumnList.add(column);
        }
    }

    /**
     * Entry point per la generazione dei ViewTableBean
     *
     * @return
     */
    public StringBuilder getViewTableBean() {
        return new StringBuilder()
                .append("package " + tableBeanPackageName + ";\n")
                .append("\n")
                .append("import it.eg.sloth.db.query.SelectQueryInterface;\n")
                .append("import it.eg.sloth.db.datasource.DataRow;\n")
                .append("import it.eg.sloth.db.datasource.table.DbTable;\n")
                .append("import it.eg.sloth.framework.common.exception.FrameworkException;\n")
                .append("import java.io.IOException;\n")
                .append("import java.sql.Connection;\n")
                .append("import java.sql.SQLException;\n")
                .append("\n")
                .append("/**\n")
                .append(" * ViewBean per la vista " + view.getName() + "\n")
                .append(" *\n")
                .append(" */\n")
                .append("public class " + tableBeanClassName + " extends DbTable<" + rowBeanClassName + "> {\n")
                .append("\n")
                .append("  \n")
                .append("\n")
                .append("  public static final String SELECT = \"Select * from " + view.getName() + " /*W*/\";\n")
                .append("  public static final String TABLE_NAME = \"" + view.getName().toUpperCase() + "\";\n")
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
                .append("  public " + rowBeanClassName + " get" + rowBeanClassName + "() {\n")
                .append("    return (" + rowBeanClassName + ") getRow();\n")
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
                .append("    public static " + tableBeanClassName + " load(" + rowBeanClassName + " " + GenUtil.initLow(rowBeanClassName) + ", int pageSize) throws SQLException, IOException, FrameworkException {\n")
                .append("      return load(" + GenUtil.initLow(rowBeanClassName) + ", pageSize, null);\n")
                .append("    }\n")
                .append("\n")
                .append("    public static " + tableBeanClassName + " load(" + rowBeanClassName + " " + GenUtil.initLow(rowBeanClassName) + ") throws SQLException, IOException, FrameworkException {\n")
                .append("      return load (" + GenUtil.initLow(rowBeanClassName) + ", -1);\n")
                .append("    }\n")
                .append("\n")
                .append("    public static " + tableBeanClassName + " loadFromQuery(SelectQueryInterface query, int pageSize, Connection connection) throws SQLException, IOException, FrameworkException {\n")
                .append("      " + tableBeanClassName + " tableBean = new " + tableBeanClassName + "();\n")
                .append("      tableBean.loadFromQuery(query, connection);\n")
                .append("      tableBean.setPageSize(pageSize);\n")
                .append("      return tableBean;\n")
                .append("    }\n")
                .append("\n")
                .append("    public static " + tableBeanClassName + " loadFromQuery(SelectQueryInterface query, int pageSize) throws SQLException, IOException, FrameworkException {\n")
                .append("      return loadFromQuery (query, pageSize, null);\n")
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
        return "Select *\n" + "From " + view.getName() + "\n";
    }

    /**
     * Scrive le costanti
     *
     * @param testo
     */
    private void writeConstant(StringBuilder testo) {
        for (ViewColumn dbViewColumn : view.getColumns().getColumn()) {
            testo.append("  public static final String " + dbViewColumn.getName().toUpperCase() + " = \"" + dbViewColumn.getName().toLowerCase() + "\";\n");
        }

        testo.append("\n");
        testo.append("  public static final Column[] columns = {");

        int i = 0;
        for (ViewColumn dbViewColumn : view.getColumns().getColumn()) {
            testo.append((i++ == 0 ? "\n" : ",\n"));
            testo.append(
                    MessageFormat.format(
                            TableBeanWriter.COLUMN,
                            dbViewColumn.getName().toUpperCase(),
                            StringUtil.toJavaStringParameter(dbViewColumn.getDescription()),
                            false,
                            dbViewColumn.isNullable() ,
                            dbViewColumn.getDataPrecision(),
                            OracleUtil.getTypes(dbViewColumn.getType())
                    ));
        }
        testo.append("\n");
        testo.append("  };\n");
        testo.append("\n");

        testo.append("  private static String strSelect = \"" + getSelect().replaceAll("\\n", "\\\\n\" +\n                                    \"") + "\";\n\n");
        testo.append("  private static String strInsert = null;\n");
        testo.append("  private static String strDelete = null;\n");
        testo.append("  private static String strUpdate = null;\n");
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
            testo.append("  public TransactionalRow setObject(String name, Object value) {\n");

            for (ViewColumn dbViewColumn : lobColumnList) {
                if (OracleUtil.isClob(dbViewColumn)) {
                    testo.append("    if (" + dbViewColumn.getName().toUpperCase() + ".equalsIgnoreCase(name) && value instanceof String) {\n");
                    testo.append("      set" + GenUtil.initCap(dbViewColumn.getName()) + "((String) value);\n");
                    testo.append("      return this;\n");
                    testo.append("    }\n");
                } else if (OracleUtil.isBlob(dbViewColumn)) {
                    testo.append("    if (" + dbViewColumn.getName().toUpperCase() + ".equalsIgnoreCase(name) && value instanceof byte[]) {\n");
                    testo.append("      set" + GenUtil.initCap(dbViewColumn.getName()) + "((byte[]) value);\n");
                    testo.append("      return this;\n");
                    testo.append("    }\n");
                }
            }

            testo.append("    return super.setObject(name, value);\n");
            testo.append("  }\n");
        }

        // Scrivo i setter e i getter per le colonne
        for (ViewColumn column : view.getColumns().getColumn()) {
            testo.append(OracleUtil.getSetterGetter(column, rowBeanClassName, false));
        }
    }

    /**
     * Scrive i metodi per la gestione delle query
     *
     * @param testo
     */
    private void writeQuery(StringBuilder testo) {
        // Select
        testo.append("  public boolean select(Connection connection) throws SQLException, IOException, FrameworkException {\n")
                .append("    Query query = new Query (getSelect());\n")
                .append("    return loadFromQuery(query, connection);\n")
                .append("  }\n")
                .append("\n")
                .append("  public void delete(Connection connection) {\n")
                .append("  }\n")
                .append("\n")
                .append("  public void insert(Connection connection) {\n")
                .append("  }\n")
                .append("\n")
                .append("  public void update(Connection connection) {\n")
                .append("  }\n");

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
                .append("  }\n");
    }

    public StringBuilder getViewRowBean() {
        StringBuilder testo = new StringBuilder()
                .append("package " + rowBeanPackageName + ";\n")
                .append("\n")
                .append("import it.eg.sloth.db.datasource.row.lob.CLobData;\n")
                .append("import it.eg.sloth.db.query.SelectQueryInterface;\n")
                .append("import it.eg.sloth.db.query.query.Query;\n")
                .append("import it.eg.sloth.framework.common.exception.FrameworkException;\n")
                .append("import it.eg.sloth.db.datasource.row.DbRow;\n")
                .append("import it.eg.sloth.db.datasource.row.column.Column;\n")
                .append("import it.eg.sloth.db.datasource.row.TransactionalRow;\n")
                .append("import it.eg.sloth.db.datasource.row.lob.BLobData;\n")
                .append("import org.apache.commons.io.IOUtils;\n")
                .append("import it.eg.sloth.db.manager.DataConnectionManager;\n")
                .append("import java.io.ByteArrayInputStream;\n")
                .append("import java.io.IOException;\n")
                .append("import java.io.InputStream;\n")
                .append("import java.io.OutputStream;\n")
                .append("\n")
                .append("import java.math.BigDecimal;\n")
                .append("import java.sql.Connection;\n")
                .append("import java.sql.PreparedStatement;\n")
                .append("import java.sql.ResultSet;\n")
                .append("import java.sql.SQLException;\n")
                .append("import java.sql.Timestamp;\n")
                .append("import java.sql.Types;\n")
                .append("import lombok.SneakyThrows;\n")
                .append("\n")
                .append("/**\n")
                .append(" * RowBean per la tabella " + view.getName() + "\n")
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

    public StringBuilder getViewDecodeBean() {
        String code = view.getColumns().getColumn().get(0).getName().toUpperCase();
        StringBuilder testo = new StringBuilder()
                .append("package " + decodeBeanPackageName + ";\n")
                .append("\n")
                .append("import it.eg.sloth.db.decodemap.map.TableDecodeMap;\n")
                .append("import it.eg.sloth.db.decodemap.value.TableDecodeValue;\n")
                .append("import it.eg.sloth.framework.common.exception.FrameworkException;\n")
                .append("import " + rowBeanPackageName + "." + rowBeanClassName + ";\n")
                .append("import " + tableBeanPackageName + "." + tableBeanClassName + ";\n")
                .append("import java.io.IOException;\n")
                .append("import java.math.BigDecimal;\n")
                .append("import java.sql.Timestamp;\n")
                .append("import java.sql.SQLException;\n")
                .append("\n")
                .append("public class " + decodeBeanClassName + " extends TableDecodeMap {")
                .append("\n");

        // Costanti
        for (Constant constant : view.getConstants().getConstant()) {
            testo.append("  public static final String " + constant.getName() + " = \"" + constant.getValue() + "\";\n");
        }

        // Metodi
        testo.append("\n");
        String comma = OracleUtil.getFlags(view.getColumns().getColumn()).size() > 0 ? ", " : "";
        testo.append("  public " + decodeBeanClassName + "(boolean onlyValid, boolean fullDescription " + comma + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_PARAMETER, false) + ") throws SQLException, IOException, FrameworkException {\n");
        testo.append("    super();\n");     //$NON-NLS-5$
        testo.append("\n");
        testo.append("    " + rowBeanClassName + " row = new " + rowBeanClassName + "();\n");
        testo.append("    row.setFlagvalido(onlyValid ? \"S\" : null);\n");

        for (Column column : OracleUtil.getFlags(view.getColumns().getColumn())) {
            testo.append("    row.set" + column.getName() + "(" + column.getName() + ");\n");
        }

        testo.append("    " + tableBeanClassName + " table = " + tableBeanClassName + ".Factory.load(row);\n");
        testo.append("    table.addSortingRule(" + rowBeanClassName + ".POSIZIONE);\n");
        testo.append("    table.addSortingRule(fullDescription ? " + rowBeanClassName + ".DESCRIZIONELUNGA : " + rowBeanClassName + ".DESCRIZIONEBREVE);\n");
        testo.append("    table.applySort();\n");
        testo.append("\n");
        testo.append("    load(table, "  + rowBeanClassName + "." + code + ",  fullDescription ? " + rowBeanClassName + ".DESCRIZIONELUNGA : " + rowBeanClassName + ".DESCRIZIONEBREVE);\n");
        testo.append("  }\n");
        testo.append("\n");
        testo.append("  public " + rowBeanClassName + " getRowBean(Object code) {\n");
        testo.append("    TableDecodeValue value = (TableDecodeValue) get(code);\n");
        testo.append("    return (" + rowBeanClassName + ") value.getDataRow();\n");
        testo.append("  }\n");
        testo.append("\n");

        // Factory
        testo.append("  public static class Factory {\n");
        testo.append("    private static " + decodeBeanClassName + " decodeBean = null;\n");
        testo.append("\n");
        testo.append("    public static " + decodeBeanClassName + " getFromDb() throws SQLException, IOException, FrameworkException  {\n");
        testo.append("      return new " + decodeBeanClassName + "(false, true " + comma + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, true) + ");\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static " + decodeBeanClassName + " getFromDb(boolean onlyValid, boolean fullDescription) throws SQLException, IOException, FrameworkException  {\n");
        testo.append("      return new " + decodeBeanClassName + "(onlyValid, fullDescription " + comma + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, true) + ");\n");
        testo.append("    }\n");
        testo.append("\n");
        if (OracleUtil.getFlags(view.getColumns().getColumn()).size() > 0) {
            testo.append("    public static " + decodeBeanClassName + " getFromDb(" + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_PARAMETER, false) + ") throws SQLException, IOException, FrameworkException {\n");
            testo.append("      return new " + decodeBeanClassName + "(false, true, " + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, false) + ");\n");
            testo.append("    }\n");
            testo.append("\n");
            testo.append("    public static " + decodeBeanClassName + " getFromDb(boolean onlyValid, boolean fullDescription," + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_PARAMETER, false) + ") throws SQLException, IOException, FrameworkException  {\n");
            testo.append("      return new " + decodeBeanClassName + "(onlyValid, fullDescription, " + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, false) + ");\n");
            testo.append("    }\n");
            testo.append("\n");
        }
        testo.append("    public static synchronized " + decodeBeanClassName + " getFromCache() throws SQLException, IOException, FrameworkException  {\n");
        testo.append("      if (decodeBean == null) {\n");
        testo.append("        return decodeBean = getFromDb();\n");
        testo.append("      } else {\n");
        testo.append("        return decodeBean;\n");
        testo.append("      }\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static String decode (String code) throws SQLException, IOException, FrameworkException  {\n");
        testo.append("      return getFromDb().decode(code);\n");
        testo.append("    }\n");
        testo.append("  }\n");
        testo.append("}\n");

        return testo;
    }

    public void write() throws IOException {
        // Table Bean
        GenUtil.writeFile(tableBeanClassFile, getViewTableBean().toString());

        // Row Bean
        GenUtil.writeFile(rowBeanClassFile, getViewRowBean().toString());

        // Decode Bean
        if (view.getName().toLowerCase().contains("dec_")) {
            GenUtil.writeFile(decodeBeanClassFile, getViewDecodeBean().toString());
        }
    }

}
