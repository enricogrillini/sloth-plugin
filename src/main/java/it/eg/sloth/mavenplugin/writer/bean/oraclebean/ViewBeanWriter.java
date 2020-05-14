package it.eg.sloth.mavenplugin.writer.bean.oraclebean;

import it.eg.sloth.jaxb.dbschema.*;
import it.eg.sloth.mavenplugin.common.GenUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        decodeBeanFullClassName = genPackage + DECODE_BEAN + "." + rowBeanClassName;
        decodeBeanPackageName = genPackage + DECODE_BEAN;
        decodeBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, decodeBeanFullClassName, decodeBeanClassName);

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
        StringBuilder testo = new StringBuilder();

        testo.append("package " + tableBeanPackageName + ";\n");
        testo.append("\n");
        testo.append("import it.eg.sloth.db.query.SelectQueryInterface;\n");
        testo.append("import it.eg.sloth.db.datasource.DataRow;\n");
        testo.append("import it.eg.sloth.db.datasource.table.DbTable;\n");
        testo.append("\n");
        testo.append("import java.sql.Connection;\n");
        testo.append("\n");
        testo.append("/**\n");
        testo.append(" * ViewBean per la vista " + view.getName() + "\n");
        testo.append(" *\n");
        testo.append(" */\n");
        testo.append("public class " + tableBeanClassName + " extends DbTable<" + rowBeanClassName + "> {\n");
        testo.append("\n");
        testo.append("  \n");
        testo.append("\n");
        testo.append("  public static final String SELECT = \"Select * from " + view.getName() + " /*W*/\";\n");
        testo.append("  public static final String TABLE_NAME = \"" + view.getName().toUpperCase() + "\";\n");
        testo.append("\n");
        testo.append("  @Override\n");
        testo.append("  protected " + rowBeanClassName + " createRow () {\n");
        testo.append("    " + rowBeanClassName + " rowBean = new " + rowBeanClassName + "();\n");
        testo.append("    rowBean.setAutoloadLob(isAutoloadLob());\n");
        testo.append("    return rowBean;\n");
        testo.append("  }\n");
        testo.append("\n");
        testo.append("  @Override\n");
        testo.append("  protected " + tableBeanClassName + " newTable() {\n");
        testo.append("    return new " + tableBeanClassName + "();\n");
        testo.append("  }\n");
        testo.append("\n");
        testo.append("  public " + rowBeanClassName + " get" + rowBeanClassName + "() {\n");
        testo.append("    return (" + rowBeanClassName + ") getRow();\n");
        testo.append("  }\n");
        testo.append("\n");
        testo.append("  public void load(" + rowBeanClassName + " " + GenUtil.initLow(rowBeanClassName) + ") {\n");
        testo.append("    load(SELECT, " + rowBeanClassName + ".columns, " + GenUtil.initLow(rowBeanClassName) + ");\n");
        testo.append("  }\n");
        testo.append("\n");
        testo.append("  public void load(" + rowBeanClassName + " " + GenUtil.initLow(rowBeanClassName) + ", Connection connection) {\n");
        testo.append("    load(SELECT, " + rowBeanClassName + ".columns, " + GenUtil.initLow(rowBeanClassName) + ", connection);\n");
        testo.append("  }\n");
        testo.append("\n");
        testo.append("  public static class Factory {\n");
        testo.append("\n");
        testo.append("    public static " + tableBeanClassName + " load(" + rowBeanClassName + " rowBean, int pageSize, Connection connection) {\n");
        testo.append("      " + tableBeanClassName + " tableBean = new " + tableBeanClassName + "();\n");
        testo.append("      tableBean.load(rowBean, connection);\n");
        testo.append("      tableBean.setPageSize(pageSize);\n");
        testo.append("      return tableBean;\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static " + tableBeanClassName + " load(" + rowBeanClassName + " " + GenUtil.initLow(rowBeanClassName) + ", int pageSize) {\n");
        testo.append("      return load(" + GenUtil.initLow(rowBeanClassName) + ", pageSize, null);\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static " + tableBeanClassName + " load(" + rowBeanClassName + " " + GenUtil.initLow(rowBeanClassName) + ") {\n");
        testo.append("      return load (" + GenUtil.initLow(rowBeanClassName) + ", -1);\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static " + tableBeanClassName + " loadFromQuery(SelectQueryInterface query, int pageSize, Connection connection) {\n");
        testo.append("      " + tableBeanClassName + " tableBean = new " + tableBeanClassName + "();\n");
        testo.append("      tableBean.loadFromQuery(query, connection);\n");
        testo.append("      tableBean.setPageSize(pageSize);\n");
        testo.append("      return tableBean;\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static " + tableBeanClassName + " loadFromQuery(SelectQueryInterface query, int pageSize) {\n");
        testo.append("      return loadFromQuery (query, pageSize, null);\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static " + tableBeanClassName + " loadFromQuery(SelectQueryInterface query) {\n");
        testo.append("      return loadFromQuery (query, -1);\n");
        testo.append("    }\n");
        testo.append("  }\n");
        testo.append("\n");
        testo.append("}");

        return testo;
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
            testo.append("    new Column (" + dbViewColumn.getName().toUpperCase() + ", false, false, Types." + OracleUtil.getTypes(dbViewColumn.getType()) + ")");
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
            testo.append("  public void setObject(String name, Object value) {\n");

            for (ViewColumn dbViewColumn : lobColumnList) {
                if (OracleUtil.isClob(dbViewColumn)) {
                    testo.append("    if (" + dbViewColumn.getName().toUpperCase() + ".equalsIgnoreCase(name) && value instanceof String) {\n");
                    testo.append("      set" + GenUtil.initCap(dbViewColumn.getName()) + "((String) value);\n");
                    testo.append("      return;\n");
                    testo.append("    }\n");
                } else if (OracleUtil.isBlob(dbViewColumn)) {
                    testo.append("    if (" + dbViewColumn.getName().toUpperCase() + ".equalsIgnoreCase(name) && value instanceof byte[]) {\n");
                    testo.append("      set" + GenUtil.initCap(dbViewColumn.getName()) + "((byte[]) value);\n");
                    testo.append("      return;\n");
                    testo.append("    }\n");
                }
            }

            testo.append("    super.setObject(name, value);\n");
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
        testo.append("  public boolean select(Connection connection)  {\n");
        testo.append("    Query query = new Query (getSelect());\n");
        testo.append("    return loadFromQuery(query, connection);\n");
        testo.append("  }\n");
        testo.append("\n");
        testo.append("  public void delete(Connection connection) {\n");
        testo.append("  }\n");
        testo.append("\n");
        testo.append("  public void insert(Connection connection) {\n");
        testo.append("  }\n");
        testo.append("\n");
        testo.append("  public void update(Connection connection) {\n");
        testo.append("  }\n");

    }

    /**
     * Scrive i metodi per la gestione delle query
     *
     * @param testo
     */
    private void writeFactory(StringBuilder testo) {
        testo.append("  public static class Factory {\n");
        testo.append("\n");
        testo.append("    public static " + rowBeanClassName + " load(SelectQueryInterface selectQueryInterface) {\n");
        testo.append("      " + rowBeanClassName + " " + GenUtil.initLow(rowBeanClassName) + " = new " + rowBeanClassName + "();\n");
        testo.append("      " + GenUtil.initLow(rowBeanClassName) + ".loadFromQuery(selectQueryInterface);\n");
        testo.append("      return " + GenUtil.initLow(rowBeanClassName) + ";\n");
        testo.append("    }\n");
        testo.append("  }\n");
    }

    public StringBuilder getViewRowBean() {
        StringBuilder testo = new StringBuilder();

        testo.append("package " + rowBeanPackageName + ";\n");
        testo.append("\n");
        testo.append("import it.eg.sloth.db.query.SelectQueryInterface;\n");
        testo.append("import it.eg.sloth.db.query.query.Query;\n");
        testo.append("import it.eg.sloth.db.datasource.row.DbRow;\n");
        testo.append("import it.eg.sloth.db.datasource.row.column.Column;\n");
        testo.append("import it.eg.sloth.db.datasource.row.lob.BLobData;\n");
        testo.append("import it.eg.sloth.framework.utility.stream.StreamUtil;\n");
        testo.append("import it.eg.sloth.db.manager.DataConnectionManager;\n");
        testo.append("import java.io.ByteArrayInputStream;\n");
        testo.append("\n");
        testo.append("import java.io.InputStream;");
        testo.append("import java.io.OutputStream;");
        testo.append("\n");
        testo.append("import java.math.BigDecimal;\n");
        testo.append("import java.sql.Connection;\n");
        testo.append("import java.sql.PreparedStatement;\n");
        testo.append("import java.sql.ResultSet;\n");
        testo.append("import java.sql.SQLException;\n");
        testo.append("import java.sql.Timestamp;\n");
        testo.append("import java.sql.Types;\n");
        testo.append("\n");
        testo.append("/**\n");
        testo.append(" * RowBean per la tabella " + view.getName() + "\n");
        testo.append(" *\n");
        testo.append(" */\n");
        testo.append("public class " + rowBeanClassName + " extends DbRow {\n");
        testo.append("\n");
        testo.append("  \n");
        testo.append("\n");

        writeConstant(testo);
        writeConstructor(testo);
        writeSetterGetter(testo);
        writeQuery(testo);
        writeFactory(testo);

        testo.append("}\n");

        return testo;

    }

    public StringBuilder getViewDecodeBean() {
        StringBuilder testo = new StringBuilder();
        String code = view.getColumns().getColumn().get(0).getName().toUpperCase();
        testo.append("package " + decodeBeanPackageName + ";\n");
        testo.append("\n");
        testo.append("import it.eg.sloth.db.decodemap.tabledecodeMap.TableDecodeMap;\n");
        testo.append("import it.eg.sloth.db.decodemap.tabledecodeMap.TableDecodeValue;\n");
        testo.append("import " + rowBeanPackageName + "." + rowBeanClassName + ";\n");
        testo.append("import " + tableBeanPackageName + "." + rowBeanClassName + ";\n");
        testo.append("\n");
        testo.append("import java.sql.Timestamp;\n");
        testo.append("import java.sql.SQLException;\n");
        testo.append("\n");
        testo.append("public class " + decodeBeanClassName + " extends TableDecodeMap {");
        testo.append("\n");

        // Costanti
        for (Constant constant : view.getConstants().getConstant()) {
            testo.append("  public static final String " + constant.getName() + " = \"" + constant.getValue() + "\";\n");
        }

        // Metodi
        testo.append("\n");
        String comma = OracleUtil.getFlags(view.getColumns().getColumn()).size() > 0 ? ", " : "";
        testo.append("  public " + decodeBeanClassName + "(boolean onlyValid, boolean fullDescription " + comma + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_PARAMETER, false) + ") {\n");
        testo.append("    super(" + rowBeanClassName + "." + code + ",  fullDescription ? " + rowBeanClassName + ".DESCRIZIONELUNGA : " + rowBeanClassName + ".DESCRIZIONEBREVE);\n");     //$NON-NLS-5$
        testo.append("\n");
        testo.append("    " + rowBeanClassName + " row = new " + rowBeanClassName + "();\n");
        testo.append("    row.setFlagvalido(onlyValid ? \"S\" : null);\n");

        for (Column column : OracleUtil.getFlags(view.getColumns().getColumn())) {
            testo.append("    row.set" + column.getName() + "(" + column.getName() + ");\n");
        }

        testo.append("    " + rowBeanClassName + " table = " + rowBeanClassName + ".Factory.load(row);\n");
        testo.append("    table.addSortingRule(" + rowBeanClassName + ".POSIZIONE);\n");
        testo.append("    table.addSortingRule(fullDescription ? " + rowBeanClassName + ".DESCRIZIONELUNGA : " + rowBeanClassName + ".DESCRIZIONEBREVE);\n");
        testo.append("    table.applySort();\n");
        testo.append("\n");
        testo.append("    load(table);\n");
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
        testo.append("    public static " + decodeBeanClassName + " getFromDb() throws SQLException {\n");
        testo.append("      return new " + decodeBeanClassName + "(false, true " + comma + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, true) + ");\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static " + decodeBeanClassName + " getFromDb(boolean onlyValid, boolean fullDescription) throws SQLException {\n");
        testo.append("      return new " + decodeBeanClassName + "(onlyValid, fullDescription " + comma + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, true) + ");\n");
        testo.append("    }\n");
        testo.append("\n");
        if (OracleUtil.getFlags(view.getColumns().getColumn()).size() > 0) {
            testo.append("    public static " + decodeBeanClassName + " getFromDb(" + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_PARAMETER, false) + ") throws SQLException {\n");
            testo.append("      return new " + decodeBeanClassName + "(false, true, " + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, false) + ");\n");
            testo.append("    }\n");
            testo.append("\n");
            testo.append("    public static " + decodeBeanClassName + " getFromDb(boolean onlyValid, boolean fullDescription," + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_PARAMETER, false) + ") throws SQLException {\n");
            testo.append("      return new " + decodeBeanClassName + "(onlyValid, fullDescription, " + OracleUtil.getFlagsForDecodeKey(view.getColumns().getColumn(), OracleUtil.FIELDS_GEN_TYPE.AS_VARIABLE, false) + ");\n");
            testo.append("    }\n");
            testo.append("\n");
        }
        testo.append("    public static synchronized " + decodeBeanClassName + " getFromCache() throws SQLException {\n");
        testo.append("      if (decodeBean == null) {\n");
        testo.append("        return decodeBean = getFromDb();\n");
        testo.append("      } else {\n");
        testo.append("        return decodeBean;\n");
        testo.append("      }\n");
        testo.append("    }\n");
        testo.append("\n");
        testo.append("    public static String decode (String code) throw SQLException {\n");
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
