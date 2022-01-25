package it.eg.sloth.mavenplugin.writer.refreshdb.oracle;

import it.eg.sloth.db.datasource.DataRow;
import it.eg.sloth.db.datasource.row.Row;
import it.eg.sloth.db.query.query.Query;
import it.eg.sloth.framework.common.base.BaseFunction;
import it.eg.sloth.framework.common.base.BigDecimalUtil;
import it.eg.sloth.framework.common.exception.FrameworkException;
import it.eg.sloth.jaxb.dbschema.*;
import it.eg.sloth.mavenplugin.writer.refreshdb.AbstractDb;
import lombok.Data;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@Data
public class OracleDb extends AbstractDb {

    private static final String OT_TABLE = "TABLE";
    private static final String OT_VIEW = "VIEW";
    private static final String OT_PACKAGE = "PACKAGE";
    private static final String OT_PACKAGE_BODY = "PACKAGE BODY";
    private static final String OT_PROCEDURE = "PROCEDURE";
    private static final String OT_FUNCTION = "FUNCTION";
    private static final String OT_TYPE = "TYPE";
    private static final String OT_TYPE_BODY = "TYPE BODY";

    private static final String GEN_GENERATED_NAME = "GENERATED NAME";

    private static final String INDX_UNIQUE = "UNIQUE";

    private static final String ARG_IN = "IN";
    private static final String ARG_OUT = "OUT";
    private static final String ARG_INOUT = "IN/OUT";

    private static final String SQL_PACKAGES = "Select InitCap(a.object_name) object_name,\n" +
            "       InitCap(a.package_name) package_name,\n" +
            "       nvl(a.overload, 0) overload,\n" +
            "       InitCap(a.argument_name) argument_name,\n" +
            "       a.position,\n" +
            "       a.data_type,\n" +
            "       a.in_out,\n" +
            "       o.object_type," +
            "       a.type_name\n" +
            "From ALL_arguments a, ###objects o\n" +
            "Where a.owner = o.owner And\n" +
            "      a.package_name = o.object_name And\n" +
            "      a.owner = upper(?) And\n" +
            "      a.package_name is not null And\n" +
            "      a.data_level = 0 And\n" +
            "      o.object_type = 'PACKAGE'\n" +
            "Order By package_name, object_name, overload, sequence";

    private static final String SQL_SOURCE = "Select *\n" +
            "From ###source\n" +
            "Where owner = upper(?) And\n" +
            "      type = ?\n" +
            "Order by type, name, line\n";

    private static final String SQL_VIEW = "Select InitCap(view_name) view_name,\n" +
            "       text\n" +
            "From ###views\n" +
            "Where owner = upper(?) \n" +
            "Order by view_name\n";

    private static final String SQL_TRIGGERS = "Select *\n" +
            "From ###triggers t\n" +
            "Where t.owner = upper(?) and\n" +
            "      t.table_name = nvl(upper(?), t.table_name) And\n" +
            "      t.table_name not like 'BIN%'\n" +
            "Order by trigger_name\n";

    private static final String _sqlLobsFull = "Select l.table_name,\n" +
            "       l.column_name,\n" +
            "       l.chunk,\n" +
            "       l.in_row,\n" +
            "       s.tablespace_name,\n" +
            "       s.initial_extent\n" +
            "From dba_segments s, ###lobs l\n" +
            "Where s.segment_name = l.segment_name And\n" +
            "      s.owner = l.owner And\n" +
            "      l.owner = upper(?) And" +
            "      l.table_name = nvl(upper(?), l.table_name)\n";

    private static final String SQL_LOBS = "Select l.column_name,\n" +
            "       l.table_name,\n" +
            "       l.chunk,\n" +
            "       l.in_row\n" +
            "From ###lobs l\n" +
            "Where l.table_name not like 'BIN%' And\n" +
            "      l.owner = upper(?) And" +
            "      l.table_name = nvl(upper(?), l.table_name)\n";

    private static final String SQL_DB_TABLES = "Select InitCap(t.table_name) table_Name,\n" +
            "       c.comments table_comments,\n" +
            "       t.tablespace_name,\n" +
            "       t.initial_extent,\n" +
            "       t.Temporary,\n" +
            "       t.Duration,\n" +
            "       InitCap(tc.column_name) column_name,\n" +
            "       column_id,\n" +
            "       cc.comments column_comments,\n" +
            "       tc.nullable,\n" +
            "       tc.data_type,\n" +
            "       tc.data_length,\n" +
            "       tc.data_precision,\n" +
            "       tc.data_scale,\n" +
            "       tc.data_default\n" +
            "From ###col_comments cc, ###tab_columns tc, ###tab_comments c, ###tables t\n" +
            "Where t.owner = c.owner (+) And\n" +
            "      t.table_name = c.Table_name (+) And\n" +
            "      t.owner = tc.owner And\n" +
            "      t.table_name = tc.table_name And\n" +
            "      tc.owner = cc.owner (+) And\n" +
            "      tc.table_name = cc.table_name (+) And\n" +
            "      tc.column_name = cc.column_name (+) And\n" +
            "      t.NESTED = 'NO' And\n" +
            "      t.owner = upper(?) And\n" +
            "      t.table_name = nvl(upper(?), t.table_name) And\n" +
            "      c.TABLE_TYPE (+) = 'TABLE' And\n" +
            // Escludo dalle tabelle di sistema per import/export
            "      t.table_name not like 'SYS_EXPORT_SCHEMA%' And\n" +
            "      t.table_name not like 'SYS_IMPORT_FULL%' And\n" +
            // Escludo dalle tabelle le viste materializzate:
            "      Not Exists (Select 1 From ALL_mviews mv Where mv.owner = t.owner And mv.mview_name = t.table_name)\n" +
            "Order by t.table_Name, tc.column_id\n";

    private static final String SQL_DB_VIEWS = "Select InitCap(t.view_name) view_name,\n" +
            "       c.comments table_comments,\n" +
            "       InitCap(tc.column_name) column_name,\n" +
            "       tc.column_id,\n" +
            "       cc.comments column_comments,\n" +
            "       tc.nullable,\n" +
            "       tc.data_type,\n" +
            "       tc.data_length,\n" +
            "       tc.data_precision,\n" +
            "       tc.data_scale\n" +
            "From ###col_comments cc, ###tab_columns tc, ###tab_comments c, ###Views t\n" +
            "Where t.owner = c.owner And\n" +
            "      t.view_name = c.Table_name And\n" +
            "      t.owner = tc.owner And\n" +
            "      t.view_name = tc.table_name And\n" +
            "      tc.owner = cc.owner And\n" +
            "      tc.table_name = cc.table_name And\n" +
            "      tc.column_name = cc.column_name And\n" +
            "      t.owner = upper(?) And\n" +
            "      c.TABLE_TYPE = 'VIEW'\n" +
            "Order by t.view_name, tc.column_id\n";

    private static final String SQL_DB_INDEXES = "Select i.index_name,\n" +
            "       ic.column_position,\n" +
            "       i.owner,\n" +
            "       i.table_name,\n" +
            "       i.uniqueness,\n" +
            "       i.tablespace_name,\n" +
            "       i.initial_extent,\n" +
            "       ic.column_name\n" +
            "From ###ind_columns ic, ###indexes i\n" +
            "Where i.OWNER = ic.index_owner And\n" +
            "      i.INDEX_NAME = ic.index_name And\n" +
            "      i.table_owner = upper(?) And\n" +
            "      i.table_name = nvl(upper(?), i.table_name) And\n" +
            "      i.index_type like '%NORMAL%'\n" +
            "minus\n" +
            "Select i.index_name,\n" +
            "       ic.column_position,\n" +
            "       i.owner,\n" +
            "       i.table_name,\n" +
            "       i.uniqueness,\n" +
            "       i.tablespace_name,\n" +
            "       i.initial_extent,\n" +
            "       ic.column_name\n" +
            "From ###mviews mv, ###ind_columns ic, ###indexes i\n" +
            "Where mv.owner = i.table_owner And\n" +
            "      mv.mview_name = i.table_name And\n" +
            "      i.OWNER = ic.index_owner And\n" +
            "      i.INDEX_NAME = ic.index_name And\n" +
            "      i.table_owner = upper(?) And\n" +
            "      i.table_name = nvl(upper(?), i.table_name) And\n" +
            "      i.index_type like '%NORMAL%'\n" +
            "Order by 1, 2\n";

    // GG 29-09-2014: aggiunta questa query per recuperare le ESPRESSIONI dei
    // campi con FORMULE
    // (ad es. "NVL(campo,'^_^')".) Purtroppo non riesco a metterla in JOIN perché
    // il campo
    // "COLUMN_EXPRESSION" è di tipo LONG e non è leggibile facilmente come
    // stringa.
    private static final String SQL_DB_IDX_COLUMN_EXPR =
            "Select ie.column_expression" +
                    " From ###ind_expressions ie" +
                    " Where ie.index_owner = Upper(?)" +
                    " And   ie.index_name  = Upper(?)" +
                    " And   ie.table_owner = Upper(?)" +
                    " And   ie.table_name  = Upper(?)" +
                    " And   ie.column_position = ?";

    private static final String SQL_DB_CONSTRAINT = "Select c1.table_name,\n" +
            "       decode (c1.constraint_type, 'P', 1, 'R', 2, 'C', 3) Ordinamento,\n" +
            "       c1.constraint_name,\n" +
            "       cc1.position,\n" +
            "       cc1.column_name,\n" +
            "       c1.constraint_type,\n" +
            "       c1.search_condition,\n" +
            "       c1.generated,\n" +
            "       null tabellaReferenziata\n" +
            "From ###cons_columns cc1, ###constraints c1\n" +
            "Where c1.owner = cc1.owner And\n" +
            "      c1.constraint_name = cc1.constraint_name And\n" +
            "      c1.owner = upper(?)  And\n" +
            "      c1.table_name = nvl(upper(?), c1.table_name) And\n" +
            "      c1.constraint_type in ('P', 'U', 'C')\n" +
            "Union All\n" +
            "Select c1.table_name,\n" +
            "       decode (c1.constraint_type, 'P', 1, 'R', 2, 'C', 3) Ordinamento,\n" +
            "       c1.constraint_name,\n" +
            "       cc1.position,\n" +
            "       cc1.column_name,\n" +
            "       c1.constraint_type,\n" +
            "       c1.search_condition,\n" +
            "       c1.generated,\n" +
            "       c2.table_name tabellaReferenziata\n" +
            "From ###cons_columns cc1, ###constraints c2, ###constraints c1\n" +
            "Where c1.owner = cc1.owner And\n" +
            "      c1.constraint_name = cc1.constraint_name And\n" +
            "      c1.owner = upper(?) And\n" +
            "      c1.table_name = nvl(upper(?), c1.table_name) And\n" +
            "      c1.constraint_type in ('R') And\n" +
            "      c1.r_owner = c2.owner And\n" +
            "      c1.r_constraint_name = c2.constraint_name\n" +
            "Order By 1, 2, 3, 4\n";

    private static final String SQL_DB_GRANTS = "Select *\n" +
            "From user_tab_privs\n" +
            "Where owner = upper(?)\n" +
            "Order by Grantee";

    private static final String SQL_DB_SEQUENCES = "Select InitCap(sequence_name) sequence_name\n" +
            "From ###sequences\n" +
            "Where sequence_owner = upper(?)\n" +
            "Order By sequence_name";

    private static final String SQL_INVALID_OBJECT = "Select o.object_name,\n" +
            "       o.object_type,\n" +
            "       e.line,\n" +
            "       e.position,\n" +
            "       e.text\n" +
            "From ###errors e, ###objects o\n" +
            "Where o.object_name = e.name (+) And\n" +
            "      o.object_type = e.type (+) And\n" +
            "      o.owner = e.owner (+) And\n" +
            "      o.STATUS = 'INVALID' And\n" +
            "      o.owner = upper(?)\n" +
            "Order by decode (o.object_type, 'FUNCTION', 1, 'PROCEDURE', 2, 'VIEW', 3, 'PACKAGE', 4, 'PACKAGE BODY', 5, 6), o.object_name, e.line, e.position\n";

    private static final String SQL_TABLE_PARTITION = "select p.table_name,\n" +
            "       pt.partitioning_type,\n" +
            "       P.Partition_Name,\n" +
            "       P.Tablespace_Name,\n" +
            "       P.Initial_Extent,\n" +
            "       C.Column_Name," +
            "       P.High_Value\n" +
            "from ###Part_Tables pt,\n" +
            "     ###tab_PARTITIONS p,\n" +
            "     ###Part_Key_Columns c\n" +
            "where P.Table_Name = c.Name And\n" +
            "      P.Table_Owner = C.Owner And\n" +
            "      P.Table_Name = pt.Table_Name And\n" +
            "      P.Table_Owner = pt.Owner And\n" +
            "      C.Object_Type = 'TABLE' And\n" +
            "      P.Table_Owner = upper(?) And\n" +
            "      P.Table_Name = nvl(upper(?), P.Table_Name) And\n" +
            "      C.Object_Type = 'TABLE'\n" +
            "order by p.table_name, p.Partition_Position\n";

    private static final String SQL_INDEX_PARTITION = "Select P.Index_Name,\n" +
            "       Pt.Partitioning_Type,\n" +
            "       P.Partition_Name,\n" +
            "       P.Tablespace_Name,\n" +
            "       P.Initial_Extent,\n" +
            "       C.Column_Name,\n" +
            "       I.Table_Name,\n" +
            "       P.High_Value\n" +
            "From ###Part_Indexes Pt,\n" +
            "     ###Ind_Partitions P,\n" +
            "     ###Part_Key_Columns C,\n" +
            "     ###indexes i\n" +
            "Where P.Index_Name = C.Name And\n" +
            "      P.Index_Owner = C.Owner And\n" +
            "      P.Index_Name = Pt.Index_Name And\n" +
            "      P.Index_Owner = Pt.Owner And\n" +
            "      P.Index_Name = i.index_name And\n" +
            "      P.Index_Owner = i.Owner And\n" +
            "      C.Object_Type = 'INDEX' And\n" +
            "      i.Table_Owner = upper(?) And\n" +
            "      i.Table_Name = nvl(upper(?), i.Table_Name) And\n" +
            "      C.Object_Type = 'INDEX'\n" +
            "Order By P.Index_Name, P.Partition_Position\n";

    private static final String SQL_RECYCLE_BIN = "select * from user_recyclebin";

    private static final String SQL_PURGE_RECYCLE_BIN = "purge recyclebin";

    private static final String SQL_ATTRIBUTES = "Select *\n" +
            "From ###TYPE_ATTRS\n" +
            "Where owner = upper(?)\n" +
            "Order by type_name, attr_no\n";

    private static final String SQL_LAST_DDL_MODIFIED_DATE = "SELECT MAX(last_ddl_time) AS Last_Ddl_Modified_Date" +
            " FROM USER_OBJECTS";

    private static final String SQL_SELECT_ANY = "select * from dba_objects where rownum < 2";

    // GG 25-06-2013
    private static final String SQL_DEPENDENCIES =
            "Select referenced_owner as OWNER,\n" +
                    "       referenced_name  as NAME,\n" +
                    "       referenced_type  as TYPE\n" +
                    " From ###DEPENDENCIES\n" +
                    " Where owner  =   upper(?)\n" +
                    " And   name   =   upper(?)\n" +
                    " And   type  Like upper(?)||'%'\n" +
                    " Order by owner, name, type, referenced_owner, referenced_name, referenced_type\n";

    private static final String SQL_DEPENDENCIES_REVERSE =
            "Select owner as OWNER,\n" +
                    "       name  as NAME,\n" +
                    "       type  as TYPE\n" +
                    " From ###DEPENDENCIES\n" +
                    " Where referenced_owner  =   upper(?)\n" +
                    " And   referenced_name   =   upper(?)\n" +
                    " And   referenced_type  Like upper(?)||'%'\n" +
                    " Order by referenced_owner, referenced_name, referenced_type, owner, name, type\n";

    private static final String SQL_MATERIALIZED_VIEWS =
            "Select mv.mview_name,\n" +
                    "       mv.query As source\n" +
                    " From ###mviews mv\n" +
                    " Where mv.owner = upper(?)";

    private static final String SQL_MATERIALIZED_VIEWS_COLS =
            "Select InitCap(t.mview_name) mview_name,\n" +
                    "        InitCap(tc.column_name) column_name,\n" +
                    "        column_id,\n" +
                    "        cc.comments column_comments,\n" +
                    "        tc.nullable,\n" +
                    "        tc.data_type,\n" +
                    "        tc.data_length,\n" +
                    "        tc.data_precision,\n" +
                    "        tc.data_scale,\n" +
                    "        tc.data_default\n" +
                    " From ###col_comments cc, ###tab_columns tc, ###mviews t\n" +
                    " Where t.owner = tc.owner\n" +
                    " And   t.mview_name = tc.table_name\n" +
                    " And   tc.owner = cc.owner (+)\n" +
                    " And   tc.table_name = cc.table_name (+)\n" +
                    " And   tc.column_name = cc.column_name (+)\n" +
                    " And   t.owner = upper(?)\n" +
                    " And   t.mview_name = nvl(upper(?), t.mview_name)\n" +
                    " Order by t.mview_name, tc.column_id";

    private Boolean selectAnyDictionary;


    public OracleDb(String owner, Boolean selectAnyDictionary) {
        super(owner);
        this.selectAnyDictionary = selectAnyDictionary;
    }

    private Double fromBigDecimal(BigDecimal bigDecimal) {
        return bigDecimal == null ? null : bigDecimal.doubleValue();
    }

    private String getSqlStatement(String sql) throws SQLException {
        if (getSelectAnyDictionary()) {
            return sql.replace("###", "DBA_");
        } else {
            return sql.replace("###", "ALL_");
        }
    }

    private String getTipoColonna(String dataType, BigDecimal dataPrecision, BigDecimal dataScale, BigDecimal dataLength) {
        dataType = dataType.trim();
        dataPrecision = BaseFunction.nvl(dataPrecision, new BigDecimal(0));
        dataScale = BaseFunction.nvl(dataScale, new BigDecimal(0));
        dataLength = BaseFunction.nvl(dataLength, new BigDecimal(0));

        String result = dataType;
        // "DATE", "LONG", "LONG RAW", "BLOB", "CLOB"
        if ("NUMBER".equals(dataType) && dataPrecision.intValue() == 0) {
            result += "(38," + dataScale + ")";
        } else if ("NUMBER".equals(dataType) && dataPrecision.intValue() != 0) {
            result += "(" + dataPrecision.intValue() + "," + dataScale.intValue() + ")";
        } else if ("FLOAT".equals(dataType)) {
            result += "(" + dataPrecision.intValue() + ")";
        } else if (dataType.indexOf("TIMESTAMP") >= 0) {
            result += "";
        } else if ("DATE".equals(dataType) || "CLOB".equals(dataType) || "BLOB".equals(dataType)) {
            result += "";
        } else {
            result += "(" + dataLength.intValue() + ")";
        }

        return result;
    }

    private ConstraintType getConstraintType(String type) {
        if ("P".equals(type)) {
            return ConstraintType.PRIMARY_KEY;
        } else if ("R".equals(type)) {
            return ConstraintType.FOREIGN_KEY;
        } else {
            return ConstraintType.CHECK;
        }
    }



    public Constants getConstants(String entityName, String keyName) throws SQLException, IOException, FrameworkException {
        final int MAX_CONSTANTS = 100;
        Map<String, Integer> nameCache = new HashMap<>();

        Constants constants = new Constants();
        if (entityName.toUpperCase().contains("DEC_")) {

            // Verifico quanti record sono presenti nella tabella
            Query query = new Query("Select count(*) As Records From " + entityName);
            if (query.selectRow().getBigDecimal("Records").intValue() <= MAX_CONSTANTS) {

                it.eg.sloth.db.datasource.table.Table sqlConstants = new it.eg.sloth.db.datasource.table.Table();

                query = new Query("select * from " + entityName + " Order by NLSSORT(" + keyName + ",'NLS_SORT=BINARY')");
                query.populateDataTable(sqlConstants);

                for (Row row : sqlConstants) {
                    String name = row.getString("DESCRIZIONEBREVE").toUpperCase();
                    String value = row.getString(keyName);

                    // Normalizzo il nome
                    name = name.replaceAll("([^A-Za-z0-9_])", "_");
                    if (name.charAt(0) >= '0' && name.charAt(0) <= '9') {
                        name = "_" + name;
                    }

                    // Gestisco i nomi duplicati
                    if (nameCache.containsKey(name)) {
                        int suff = nameCache.get(name) + 1;
                        nameCache.put(name, suff);

                        name = name + "_" + suff;
                    } else {
                        nameCache.put(name, 0);
                    }

                    Constant constant = new Constant();
                    constant.setName(name);
                    constant.setValue(value);

                    constants.getConstant().add(constant);
                }
            }

        }
        return constants;
    }


    @Override
    public Tables loadTables(String tableName) throws SQLException, IOException, FrameworkException {
        Map<String, Table> tableCache = new HashMap<>();
        Map<String, TableColumn> columnCache = new HashMap<>();

        // Tables
        it.eg.sloth.db.datasource.table.Table sqlTable = new it.eg.sloth.db.datasource.table.Table();

        Query query = new Query(getSqlStatement(SQL_DB_TABLES));
        query.addParameter(Types.VARCHAR, getOwner());
        query.addParameter(Types.VARCHAR, tableName);
        query.populateDataTable(sqlTable);

        Tables tables = new Tables();
        Table dbTable = new Table();
        int j = 0;
        for (DataRow dataRow : sqlTable) {
            String name = dataRow.getString("table_name");
            if (!name.equalsIgnoreCase(dbTable.getName())) {
                // Inizializzo la nuova tabella
                dbTable = new Table();
                dbTable.setColumns(new TableColumns());
                dbTable.setIndexes(new Indexes());
                dbTable.setConstraints(new Constraints());
                dbTable.setTriggers(new Triggers());
                dbTable.setForeignKeyReferences(new ForeignKeyReferences());
                dbTable.setGrants(new Grants());
                dbTable.setPartitions(new Partitions());

                dbTable.setName(name);
                dbTable.setDescription(dataRow.getString("table_comments"));
                dbTable.setTablespace(dataRow.getString("tablespace_name"));
                dbTable.setInitial(fromBigDecimal(dataRow.getBigDecimal("initial_extent")));
                dbTable.setTemporary("Y".equals(dataRow.getString("temporary")));
                dbTable.setDuration(dataRow.getString("duration"));

                // Aggiungo tabella alla lista e alla cache
                tables.getTable().add(dbTable);
                tableCache.put(dbTable.getName().toUpperCase(), dbTable);

                j = 0;
            }

            TableColumn dbTableColumn = new TableColumn();
            dbTableColumn.setName(dataRow.getString("column_name"));
            dbTableColumn.setPrimaryKey(false);
            dbTableColumn.setDescription(dataRow.getString("column_comments"));
            dbTableColumn.setNullable("Y".equals(dataRow.getString("nullable")));
            dbTableColumn.setType(getTipoColonna(dataRow.getString("data_type"), dataRow.getBigDecimal("data_precision"), dataRow.getBigDecimal("data_scale"), dataRow.getBigDecimal("data_length")));
            dbTableColumn.setDataLength(BigDecimalUtil.intValue(dataRow.getBigDecimal("data_length")));
            dbTableColumn.setDataPrecision(BigDecimalUtil.intObject(dataRow.getBigDecimal("data_precision")));
            dbTableColumn.setDefaultValue(dataRow.getString("DATA_DEFAULT"));
            dbTableColumn.setPosition(j++);

            dbTable.getColumns().getColumn().add(dbTableColumn);
            columnCache.put(dbTable.getName().toUpperCase() + "." + dbTableColumn.getName().toUpperCase(), dbTableColumn);
        }


        // Constraints
        it.eg.sloth.db.datasource.table.Table sqlDbConstraint = new it.eg.sloth.db.datasource.table.Table();

        query = new Query(getSqlStatement(SQL_DB_CONSTRAINT));
        query.addParameter(Types.VARCHAR, getOwner());
        query.addParameter(Types.VARCHAR, tableName);
        query.addParameter(Types.VARCHAR, getOwner());
        query.addParameter(Types.VARCHAR, tableName);
        query.populateDataTable(sqlDbConstraint);

        Constraint dbConstraint = new Constraint();
        for (DataRow dataRow : sqlDbConstraint) {
            // Rottura su constraint
            String name = dataRow.getString("constraint_name");
            if (!name.equalsIgnoreCase(dbConstraint.getName())) {
                dbTable = tableCache.get(dataRow.getString("table_name").toUpperCase());

                if (dbTable != null) {
                    dbConstraint = new Constraint();
                    dbConstraint.setColumns(new ConstraintColumns());

                    dbConstraint.setGenerated(GEN_GENERATED_NAME.equals(dataRow.getString("generated")));
                    if (!dbConstraint.isGenerated()) {
                        dbConstraint.setName(dataRow.getString("constraint_name"));
                    }

                    dbConstraint.setType(getConstraintType(dataRow.getString("constraint_type")));
                    dbConstraint.setSearchCondition(dataRow.getString("search_condition"));
                    dbConstraint.setTable(dataRow.getString("tabellaReferenziata"));
                    dbTable.getConstraints().getConstraint().add(dbConstraint);

                    if (ConstraintType.FOREIGN_KEY == dbConstraint.getType()) {
                        Table table = tableCache.get(dbConstraint.getTable());

                        if (table != null) {
                            ForeignKeyReference dbForeignKeyReference = new ForeignKeyReference();
                            dbForeignKeyReference.setTableName(dbTable.getName());

                            table.getForeignKeyReferences().getForeignKeyReference().add(dbForeignKeyReference);
                        }
                    }
                }
            }

            // Colonna
            if (dbTable != null) {
                String columnName = dataRow.getString("column_name");

                if (columnName != null && !"".equals(columnName)) {
                    ConstraintColumn constraintColumn = new ConstraintColumn();
                    constraintColumn.setName(dataRow.getString("column_name"));
                    dbConstraint.getColumns().getConstraintColumn().add(constraintColumn);
                }

                if (dbConstraint.getType() == ConstraintType.PRIMARY_KEY) {
                    columnCache.get(dbTable.getName().toUpperCase() + "." + columnName.toUpperCase()).setPrimaryKey(true);
                }
            }
        }

        // Constants
        for (Table table : tables.getTable()) {
            table.setConstants(getConstants(table.getName(), table.getColumns().getColumn().get(0).getName()));
        }

        return tables;
    }



}
