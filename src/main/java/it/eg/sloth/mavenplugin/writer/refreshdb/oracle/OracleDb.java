package it.eg.sloth.mavenplugin.writer.refreshdb.oracle;

import it.eg.sloth.db.datasource.DataRow;
import it.eg.sloth.db.datasource.row.Row;
import it.eg.sloth.db.query.query.Query;
import it.eg.sloth.framework.common.base.BaseFunction;
import it.eg.sloth.framework.common.base.BigDecimalUtil;
import it.eg.sloth.framework.common.exception.FrameworkException;
import it.eg.sloth.jaxb.dbschema.*;
import it.eg.sloth.jaxb.dbschema.Package;
import it.eg.sloth.mavenplugin.common.GenUtil;
import it.eg.sloth.mavenplugin.writer.refreshdb.AbstractDb;
import lombok.Data;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

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

    private List<Source> loadSourceList(String type) throws SQLException, FrameworkException {
        it.eg.sloth.db.datasource.table.Table sqlSource = new it.eg.sloth.db.datasource.table.Table();

        Query query = new Query(getSqlStatement(SQL_SOURCE));
        query.addParameter(Types.VARCHAR, getOwner());
        query.addParameter(Types.VARCHAR, type);
        query.populateDataTable(sqlSource);

        List<Source> list = new ArrayList<>();
        Source source = new Source("", "");
        for (Row row : sqlSource) {
            String name = row.getString("name");
            if (!source.getName().equals(name)) {
                if (!"".equals(source.getName())) {
                    list.add(source);
                }
                source = new Source(name, type);
            }

            source.append(row.getString("text"));
        }

        if (!"".equals(source.getName()))
            list.add(source);

        return list;
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

//
//        // Triggers
//        i = 1;
//        preparedStatement = getPreparedStatement(_sqlTriggers);
//        preparedStatement.setObject(i++, getOwner(), Types.VARCHAR);
//        preparedStatement.setObject(i++, tableName, Types.VARCHAR);
//        resultSet = preparedStatement.executeQuery();
//
//        while (resultSet.next()) {
//            if (OT_TABLE.equals(resultSet.getString("BASE_OBJECT_TYPE"))) {
//                dbTable = tables.get(resultSet.getString("TABLE_NAME"));
//
//                DbTrigger trigger = dbTable.getTriggers().add(resultSet.getString("TRIGGER_NAME"));
//                trigger.setType(resultSet.getString("TRIGGER_TYPE"));
//                trigger.setEvent(resultSet.getString("TRIGGERING_EVENT"));
//                trigger.setSource(CodeWriter.cleanCode(resultSet.getString("TRIGGER_BODY")));
//            }
//        }
//
//        resultSet.close();
//        preparedStatement.close();
//
//        // Indici
//        i = 1;
//        preparedStatement = getPreparedStatement(_sqlDbIndexes);
//        preparedStatement.setObject(i++, getOwner(), Types.VARCHAR);
//        preparedStatement.setObject(i++, tableName, Types.VARCHAR);
//        preparedStatement.setObject(i++, getOwner(), Types.VARCHAR);
//        preparedStatement.setObject(i++, tableName, Types.VARCHAR);
//        resultSet = preparedStatement.executeQuery();
//
//        PreparedStatement psICE = null;
//        ResultSet rsICE = null;
//        try {
//            DbIndex dbIndex = new DbIndex("");
//            while (resultSet.next()) {
//                String name = resultSet.getString("index_name");
//                if (!dbIndex.getName().equalsIgnoreCase(name)) {
//                    try {
//                        // Cambio indice
//                        dbIndex = tables.get(resultSet.getString("table_name")).getIndexes().add(name);
//                        dbIndex.setInitial(resultSet.getDouble("initial_extent"));
//                        dbIndex.setTablespace(resultSet.getString("tablespace_name"));
//                        dbIndex.setUniqueness(INDX_UNIQUE.equalsIgnoreCase(resultSet.getString("uniqueness")));
//                    } catch (Exception e) {
//                        logger.warn("Error on index", e);
//                    }
//                }
//                String columnName = resultSet.getString("column_name");
//
//                // GG 29-09-2014: se il nome è "di sistema" vuol dire che è un campo con
//                // "ESPRESSIONE"
//                // che devo recuperare da un'altra tabella.
//                if (columnName.startsWith("SYS_") && columnName.endsWith("$")) {
//                    if (psICE == null) {
//                        psICE = getPreparedStatement(_sqlDbIdxColumnExpr);
//                    } else {
//                        psICE.clearParameters();
//                    }
//                    i = 1;
//                    psICE.setObject(i++, resultSet.getString("owner"), Types.VARCHAR);
//                    psICE.setObject(i++, name, Types.VARCHAR);
//                    psICE.setObject(i++, getOwner(), Types.VARCHAR);
//                    psICE.setObject(i++, resultSet.getString("table_name"), Types.VARCHAR);
//                    psICE.setObject(i++, resultSet.getInt("column_position"), Types.INTEGER);
//
//                    rsICE = psICE.executeQuery();
//
//                    if (rsICE.next()) {
//                        columnName = rsICE.getString("column_expression");
//                    }
//
//                    rsICE.close();
//                    rsICE = null;
//                }
//
//                // Aggiungo il nome di colonna o espressione:
//                dbIndex.getColumns().add(columnName);
//
//            } // while
//        } finally {
//            if (rsICE != null)
//                try {
//                    rsICE.close();
//                } catch (Exception ex) {
//                }
//            if (psICE != null)
//                try {
//                    psICE.close();
//                } catch (Exception ex) {
//                }
//        }
//        resultSet.close();
//        preparedStatement.close();
//
//        // Lobs
//        i = 1;
//        preparedStatement = getPreparedStatement(_sqlLobsFull);
//        preparedStatement.setObject(i++, getOwner(), Types.VARCHAR);
//        preparedStatement.setObject(i++, tableName, Types.VARCHAR);
//        try {
//            resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                DbTable table = tables.get(resultSet.getString("table_name"));
//                DbTableColumn tableColumn = table.getColumns().get(resultSet.getString("column_name"));
//
//                DbLob lob = new DbLob();
//                lob.setTablespace(resultSet.getString("tablespace_name"));
//                lob.setInitial(resultSet.getDouble("initial_extent"));
//                lob.setChunk(resultSet.getDouble("chunk"));
//                lob.setInRow("YES".equals(resultSet.getString("in_row")));
//                tableColumn.setLob(lob);
//            }
//
//        } catch (Exception e) {
//            i = 1;
//            preparedStatement = getPreparedStatement(_sqlLobs);
//            preparedStatement.setObject(i++, getOwner(), Types.VARCHAR);
//            preparedStatement.setObject(i++, tableName, Types.VARCHAR);
//            resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                DbTable table = tables.get(resultSet.getString("table_name"));
//                DbTableColumn tableColumn = table.getColumns().get(resultSet.getString("column_name"));
//
//                DbLob lob = new DbLob();
//                lob.setChunk(resultSet.getDouble("chunk"));
//                lob.setInRow("YES".equals(resultSet.getString("in_row")));
//                tableColumn.setLob(lob);
//            }
//        }
//
//        // Partition - Table
//        i = 1;
//        preparedStatement = getPreparedStatement(_sqlTablePartition);
//        preparedStatement.setObject(i++, getOwner(), Types.VARCHAR);
//        preparedStatement.setObject(i++, tableName, Types.VARCHAR);
//        resultSet = preparedStatement.executeQuery();
//
//        while (resultSet.next()) {
//            DbTable table = tables.get(resultSet.getString("table_name"));
//            if (table != null) {
//                // N.B.: la lettura dell' High_Value deve avvenire come prima cosa se no
//                // da errore (e' long)
//                table.getPartitions().setType(resultSet.getString("partitioning_type"));
//                table.getPartitions().setColumnName(resultSet.getString("Column_Name"));
//
//                DbPartition dbPartition = table.getPartitions().addPartition(resultSet.getString("Partition_Name"));
//                dbPartition.setTablespace(resultSet.getString("Tablespace_Name"));
//                dbPartition.setInitial(resultSet.getDouble("Initial_Extent"));
//                dbPartition.setHighValue(resultSet.getString("High_Value"));
//            }
//        }
//
//        // Partition - Index
//        i = 1;
//        preparedStatement = getPreparedStatement(_sqlIndexPartition);
//        preparedStatement.setObject(i++, getOwner(), Types.VARCHAR);
//        preparedStatement.setObject(i++, tableName, Types.VARCHAR);
//        resultSet = preparedStatement.executeQuery();
//
//        while (resultSet.next()) {
//            DbTable table = tables.get(resultSet.getString("table_name"));
//            if (table != null) {
//                DbIndex dbIndex2 = table.getIndexes().get(resultSet.getString("index_name"));
//
//                // N.B.: la lettura dell' High_Value deve avvenire come prima cosa se no
//                // da errore (e' long)
//                dbIndex2.getPartitions().setType(resultSet.getString("partitioning_type"));
//                dbIndex2.getPartitions().setColumnName(resultSet.getString("Column_Name"));
//
//                DbPartition dbPartition = dbIndex2.getPartitions().addPartition(resultSet.getString("Partition_Name"));
//                dbPartition.setTablespace(resultSet.getString("Tablespace_Name"));
//                dbPartition.setInitial(resultSet.getDouble("Initial_Extent"));
//                dbPartition.setHighValue(resultSet.getString("High_Value"));
//            }
//        }
//
        return tables;
    }

    @Override
    public Packages loadPackages() throws SQLException, IOException, FrameworkException {
        Map<String, Package> packageCache = new HashMap<>();

        // Spec
        Packages packages = new Packages();

        List<Source> list = loadSourceList(OT_PACKAGE);
        for (Source source : list) {
            Package dbPackage = new Package();
            dbPackage.setMethods(new Methods());

            dbPackage.setName(source.getName());
            dbPackage.setSourceSpec(source.getSource());

            packages.getPackage().add(dbPackage);
            packageCache.put(dbPackage.getName().toUpperCase(), dbPackage);
        }

        // Boby
        list = loadSourceList(OT_PACKAGE_BODY);
        for (Source source : list) {
            Package dbPackage = packageCache.get(source.getName().toUpperCase());
            if (dbPackage != null) {
                dbPackage.setSourceBody(source.getSource());
            } else {
                throw new RuntimeException("Package specification not found " + source.getName());
            }
        }

        // Method
        it.eg.sloth.db.datasource.table.Table sqlPackages = new it.eg.sloth.db.datasource.table.Table();

        Query query = new Query(getSqlStatement(SQL_PACKAGES));
        query.addParameter(Types.VARCHAR, getOwner());
        query.populateDataTable(sqlPackages);

        Package dbPackage = new Package();
        Method dbMethod = null;

        for (Row row : sqlPackages) {
            String namePck = row.getString("package_name");
            String nameObj = row.getString("object_name");
            if (!namePck.equalsIgnoreCase(dbPackage.getName())) {
                dbPackage = packageCache.get(namePck.toUpperCase());

                dbMethod = new Method();
                dbMethod.setArguments(new Arguments());
                dbMethod.setName(nameObj);
                dbMethod.setOverload(Integer.valueOf(row.getString("overload")));
                dbMethod.setType(row.getBigDecimal("position").intValue() == 0 ? MethodType.FUNCTION : MethodType.PROCEDURE);

                dbPackage.getMethods().getMethod().add(dbMethod);


            } else if (!nameObj.equalsIgnoreCase(dbMethod.getName()) || dbMethod.getOverload() != Integer.valueOf(row.getString("overload"))) {

                dbMethod = new Method();
                dbMethod.setArguments(new Arguments());
                dbMethod.setName(nameObj);
                dbMethod.setOverload(Integer.valueOf(row.getString("overload")));
                dbMethod.setType(row.getBigDecimal("position").intValue() == 0 ? MethodType.FUNCTION : MethodType.PROCEDURE);

                dbPackage.getMethods().getMethod().add(dbMethod);
            }

            if (!(row.getBigDecimal("position").intValue() > 0 && row.getString("argument_name") == null)) {
                Argument argument = new Argument();
                argument.setName(row.getString("argument_name"));
                argument.setPosition(row.getBigDecimal("position").intValue());
                argument.setType(row.getString("data_type"));
                argument.setTypeName(row.getString("type_name"));

                if (ARG_IN.equalsIgnoreCase(row.getString("in_out"))) {
                    argument.setInOut(ArgumentType.IN);
                } else if (ARG_OUT.equalsIgnoreCase(row.getString("in_out"))) {
                    argument.setInOut(ArgumentType.OUT);
                } else if (ARG_INOUT.equalsIgnoreCase(row.getString("in_out"))) {
                    argument.setInOut(ArgumentType.INOUT);
                }

                dbMethod.getArguments().getArgument().add(argument);
            }
        }

        return packages;
    }

    @Override
    public Views loadViews() throws SQLException, IOException, FrameworkException {
        Map<String, View> viewCache = new HashMap<>();

        // View
        it.eg.sloth.db.datasource.table.Table sqlView = new it.eg.sloth.db.datasource.table.Table();

        Query query = new Query(getSqlStatement(SQL_VIEW));
        query.addParameter(Types.VARCHAR, getOwner());
        query.populateDataTable(sqlView);

        Views views = new Views();
        for (Row row : sqlView) {
            View view = new View();
            view.setColumns(new ViewColumns());
            view.setTriggers(new Triggers());

            view.setName(row.getString("view_name"));
            view.setSource(row.getString("text"));

            views.getView().add(view);
            viewCache.put(view.getName().toUpperCase(), view);
        }

        // Colonne
        it.eg.sloth.db.datasource.table.Table sqlViewColumns = new it.eg.sloth.db.datasource.table.Table();

        query = new Query(getSqlStatement(SQL_DB_VIEWS));
        query.addParameter(Types.VARCHAR, getOwner());
        query.populateDataTable(sqlViewColumns);

        View dbView = new View();
        int j = 0;
        for (Row row : sqlViewColumns) {
            String name = row.getString("view_name");

            if (!name.equalsIgnoreCase(dbView.getName())) {
                dbView = viewCache.get(name.toUpperCase());
                j = 0;
            }

            ViewColumn dbViewColumn = new ViewColumn();
            dbViewColumn.setName(row.getString("column_name"));
            dbViewColumn.setDescription(row.getString("column_comments"));
            dbViewColumn.setNullable("Y".equals(row.getString("nullable")));
            dbViewColumn.setType(getTipoColonna(row.getString("data_type"), row.getBigDecimal("data_precision"), row.getBigDecimal("data_scale"), row.getBigDecimal("data_length")));
            dbViewColumn.setDataLength(BigDecimalUtil.intValue(row.getBigDecimal("data_length")));
            dbViewColumn.setDataPrecision(BigDecimalUtil.intObject(row.getBigDecimal("data_precision")));
            dbViewColumn.setPosition(j++);

            dbView.getColumns().getColumn().add(dbViewColumn);
        }

        // Constants
        for (View view : views.getView()) {
            view.setConstants(getConstants(view.getName(), view.getColumns().getColumn().get(0).getName()));
        }


        // Trigger
        it.eg.sloth.db.datasource.table.Table sqlTriggers = new it.eg.sloth.db.datasource.table.Table();

        query = new Query(getSqlStatement(SQL_TRIGGERS));
        query.addParameter(Types.VARCHAR, getOwner());
        query.addParameter(Types.VARCHAR, null);
        query.populateDataTable(sqlTriggers);

        // FIXME
//        for (Row row : sqlTriggers) {
//            if (OT_VIEW.equals(row.getString("BASE_OBJECT_TYPE"))) {
//                View view = viewCache.get(row.getString("TABLE_NAME"));
//
//                Trigger trigger = new Trigger();
//                trigger.setName(row.getString("TRIGGER_NAME"));
//                trigger.setType(row.getString("TRIGGER_TYPE"));
//                trigger.setEvent(row.getString("TRIGGERING_EVENT"));
//                trigger.setSource(GenUtil.cleanDbCode(row.getString("TRIGGER_BODY")));
//
//                view.getTriggers().getTrigger().add(trigger);
//            }
//        }

        return views;
    }

    //
//    @Override
//    public DbProcedures loadProcedures() throws SQLException, IOException {
//        DbProcedures procedures = new DbProcedures(null);
//
//        List<Source> list = loadSourceList(OT_PROCEDURE);
//        for (Source source : list) {
//            procedures.add(source.getName()).setSource(source.getSource());
//        }
//
//        return procedures;
//    }
//
//    @Override
//    public DbFunctions loadFunctions() throws SQLException, IOException {
//        DbFunctions functions = new DbFunctions(null);
//
//        List<Source> list = loadSourceList(OT_FUNCTION);
//        for (Source source : list) {
//            functions.add(source.getName()).setSource(source.getSource());
//        }
//
//        return functions;
//    }
//
//    @Override
//    public DbTypes loadTypes() throws SQLException, IOException {
//        DbTypes types = new DbTypes(null);
//
//        // Spec
//        List<Source> list = loadSourceList(OT_TYPE);
//        for (Source source : list) {
//            types.add(source.getName()).setSourceSpec(source.getSource());
//        }
//
//        // Boby
//        list = loadSourceList(OT_TYPE_BODY);
//        for (Source source : list) {
//            DbType dbType = types.get(source.getName());
//            if (dbType != null) {
//                dbType.setSourceBody(source.getSource());
//            } else {
//                Monitor.addWarn(Messages.format(Messages.getString("OracleDbConnection.message_monitor_object_not_found"), OT_TYPE, source.getName()));
//            }
//        }
//
//        // Attributi
//        int i = 1;
//        PreparedStatement preparedStatement = getPreparedStatement(_sqlAttributes);
//        preparedStatement.setObject(i++, getOwner(), Types.VARCHAR);
//        ResultSet resultSet = preparedStatement.executeQuery();
//
//        while (resultSet.next()) {
//            DbType dbType = types.get(resultSet.getString("type_name"));
//            if (dbType != null) {
//                DbTypeColumn dbTypeColumn = dbType.getColumns().add(resultSet.getString("attr_name"));
//                dbTypeColumn.setType(getTipoColonna(resultSet.getString("ATTR_TYPE_NAME"), resultSet.getBigDecimal("PRECISION"), resultSet.getBigDecimal("SCALE"), resultSet.getBigDecimal("LENGTH")));
//                if (resultSet.getBigDecimal("LENGTH") != null) {
//                    dbTypeColumn.setDataLength(resultSet.getBigDecimal("LENGTH").intValue());
//                } else {
//                    dbTypeColumn.setDataLength(-1);
//                }
//                dbTypeColumn.setPosition(dbType.getColumns().size() - 1);
//            }
//        }
//
//        resultSet.close();
//        preparedStatement.close();
//
//        return types;
//    }
//
    @Override
    public Sequences loadSequences() throws SQLException, IOException, FrameworkException {
        // Sequences
        it.eg.sloth.db.datasource.table.Table sqlDbSequences = new it.eg.sloth.db.datasource.table.Table();

        Query query = new Query(getSqlStatement(SQL_DB_SEQUENCES));
        query.addParameter(Types.VARCHAR, getOwner());
        query.populateDataTable(sqlDbSequences);

        // Lettura sequence
        Sequences sequences = new Sequences();
        for (Row row : sqlDbSequences) {
            Sequence sequence = new Sequence();
            sequence.setName(row.getString("sequence_name"));

            sequences.getSequence().add(sequence);
        }

        return sequences;
    }
//
//    @Override
//    public void loadGrants(DbDataBase dbDataBase) throws SQLException {
//        // Grants
//        int i = 1;
//        PreparedStatement preparedStatement = getPreparedStatement(_sqlDbGrants);
//        preparedStatement.setObject(i++, getOwner(), Types.VARCHAR);
//        ResultSet resultSet = preparedStatement.executeQuery();
//
//        while (resultSet.next()) {
//            String tableName = resultSet.getString("table_name");
//
//            DbTable dbTable = dbDataBase.getDbTables().get(tableName);
//            if (dbTable != null) {
//                DbGrant dbGrant = dbTable.getGrants().addGrant();
//                dbGrant.setPrivilege(resultSet.getString("privilege"));
//                dbGrant.setGrantee(resultSet.getString("grantee"));
//
//                continue;
//            }
//
//            DbView dbView = dbDataBase.getDbViews().get(tableName);
//            if (dbView != null) {
//                DbGrant dbGrant = dbView.getGrants().addGrant();
//                dbGrant.setPrivilege(resultSet.getString("privilege"));
//                dbGrant.setGrantee(resultSet.getString("grantee"));
//                dbGrant.setGrantable("YES".equalsIgnoreCase(resultSet.getString("grantable")));
//
//                continue;
//            }
//
//            DbPackage dbPackage = dbDataBase.getDbPackages().get(tableName);
//            if (dbPackage != null) {
//                DbGrant dbGrant = dbPackage.getGrants().addGrant();
//                dbGrant.setPrivilege(resultSet.getString("privilege"));
//                dbGrant.setGrantee(resultSet.getString("grantee"));
//
//                continue;
//            }
//
//            // GG 30-09-2014:
//            dbView = dbDataBase.getDbMaterializedViews().get(tableName);
//            if (dbView != null) {
//                DbGrant dbGrant = dbView.getGrants().addGrant();
//                dbGrant.setPrivilege(resultSet.getString("privilege"));
//                dbGrant.setGrantee(resultSet.getString("grantee"));
//                dbGrant.setGrantable("YES".equalsIgnoreCase(resultSet.getString("grantable")));
//
//                continue;
//            }
//        }
//
//        resultSet.close();
//        preparedStatement.close();
//    }
//
//    /**
//     * GG 30-09-2014
//     */
//    @Override
//    public DbMaterializedViews loadMaterializedViews() throws SQLException, IOException {
//        // View
//        int i = 1;
//        PreparedStatement preparedStatement = getPreparedStatement(_sqlMaterializedViews);
//        preparedStatement.setObject(i++, getOwner(), Types.VARCHAR);
//        ResultSet resultSet = preparedStatement.executeQuery();
//
//        DbMaterializedViews materializedViews = new DbMaterializedViews(null);
//        while (resultSet.next()) {
//            DbView view = materializedViews.add(resultSet.getString("mview_name"));
//            view.setSource(resultSet.getString("source"));
//        }
//
//        resultSet.close();
//        preparedStatement.close();
//
//        // Colonne della vista materializzata:
//        preparedStatement = getPreparedStatement(_sqlMaterializedViews_Cols);
//        i = 1;
//        preparedStatement.setObject(i++, getOwner(), Types.VARCHAR);
//        preparedStatement.setObject(i++, null, Types.NULL);
//        resultSet = preparedStatement.executeQuery();
//
//        DbView dbView = new DbView("");
//        int j = 0;
//        while (resultSet.next()) {
//            String name = resultSet.getString("mview_name");
//            if (!dbView.getName().equalsIgnoreCase(name)) {
//                dbView = materializedViews.get(name);
//                j = 0;
//            }
//
//            DbViewColumn dbViewColumn = dbView.getColumns().add(resultSet.getString("column_name"));
//            dbViewColumn.setDescription(resultSet.getString("column_comments"));
//            dbViewColumn.setNullable("Y".equals(resultSet.getString("nullable")));
//            dbViewColumn.setType(getTipoColonna(resultSet.getString("data_type"), resultSet.getBigDecimal("data_precision"), resultSet.getBigDecimal("data_scale"), resultSet.getBigDecimal("data_length")));
//            dbViewColumn.setDataLength(resultSet.getBigDecimal("data_length").intValue());
//            dbViewColumn.setPosition(j++);
//        }
//
//        resultSet.close();
//        preparedStatement.close();
//
//        return materializedViews;
//    }
//
//    @Override
//    public List<InvalidObject> getInvalidObjects() throws SQLException {
//        PreparedStatement preparedStatement = getPreparedStatement(_sqlInvalidObject);
//        preparedStatement.setObject(1, getOwner(), Types.VARCHAR);
//        ResultSet rs = preparedStatement.executeQuery();
//
//        // Lettura sequence
//        List<InvalidObject> list = new ArrayList<InvalidObject>();
//        InvalidObject invalidObject = new InvalidObject("", "");
//
//        while (rs.next()) {
//            if (!rs.getString("OBJECT_TYPE").equals(invalidObject.getType()) || !rs.getString("OBJECT_NAME").equals(invalidObject.getName())) {
//                invalidObject = new InvalidObject(rs.getString("OBJECT_TYPE"), rs.getString("OBJECT_NAME"));
//                list.add(invalidObject);
//            }
//
//            invalidObject.addError(new Error(rs.getInt("line"), rs.getInt("position"), rs.getString("text")));
//        }
//
//        rs.close();
//        preparedStatement.close();
//
//        return list;
//    }
//
//    @Override
//    public void compileInvalidObject(InvalidObject invalidObject) throws SQLException {
//        String sql = "Alter " + invalidObject.getType() + " " + invalidObject.getName() + " compile";
//        if (invalidObject.getType().equalsIgnoreCase("PACKAGE BODY")) {
//            sql = "Alter Package " + invalidObject.getName() + " compile body";
//        }
//
//        executeStatement(sql);
//    }
//
//    @Override
//    public List<RecycleBinObject> getRecycleBin() throws SQLException {
//        PreparedStatement preparedStatement = getPreparedStatement(_sqlRecycleBin);
//        ResultSet rs = preparedStatement.executeQuery();
//
//        // Lettura sequence
//        List<RecycleBinObject> list = new ArrayList<RecycleBinObject>();
//
//        while (rs.next()) {
//            RecycleBinObject recycleBinObject = new RecycleBinObject();
//
//            recycleBinObject.setObjectName(rs.getString("OBJECT_NAME"));
//            recycleBinObject.setOriginaName(rs.getString("ORIGINAL_NAME"));
//            recycleBinObject.setOperation(rs.getString("OPERATION"));
//            recycleBinObject.setType(rs.getString("TYPE"));
//
//            list.add(recycleBinObject);
//        }
//
//        rs.close();
//        preparedStatement.close();
//
//        return list;
//    }
//
//    @Override
//    public void purgeRecycleBin() throws SQLException {
//        executeStatement(_sqlPurgeRecycleBin);
//    }
//
//    @Override
//    public void restoreFromRecycleBin(String droppedName, String newName) throws SQLException {
//        executeStatement("flashback table " + droppedName + " to before drop rename to " + newName);
//    }
//
//    /**
//     * GG 25-06-2013: recupero delle dipendenze di un oggetto(non-Javadoc)
//     */
//    @Override
//    public List<Dependence> getDependencies(String owner, String name, String type, boolean reverse) throws SQLException {
//
//        PreparedStatement preparedStatement = getPreparedStatement(!reverse
//                ? _sqlDependencies : _sqlDependenciesReverse);
//        if ((owner == null) || (owner.length() == 0))
//            owner = getOwner();
//        int index = 1;
//        preparedStatement.setObject(index++, owner, Types.VARCHAR);
//        preparedStatement.setObject(index++, name, Types.VARCHAR);
//        preparedStatement.setObject(index++, type, Types.VARCHAR);
//
//        ResultSet rs = preparedStatement.executeQuery();
//
//        // Lettura sequence
//        List<Dependence> list = new ArrayList<Dependence>();
//        while (rs.next()) {
//            Dependence dependence = new Dependence(rs.getString("OWNER"), rs.getString("NAME"), rs.getString("TYPE"));
//            list.add(dependence);
//        }
//
//        rs.close();
//        preparedStatement.close();
//
//        return list;
//    }
//
//    @Override
//    public DbLoaderInterface getdDbLoader() throws SQLException {
//        return new OracleLoader(this);
//    }
//
//    @Override
//    public DDLGeneratorInterface getDDLGeneratorInterface(DbDataBase dbDataBase) {
//        return new OracleDDLGenerator(dbDataBase);
//    }
//
//    @Override
//    public BeanGenInterface getBeanGenInterface(BeanType.Enum beanType, String javaSrcDirectory, String javaBeanPackage) {
//        if (BeanType.ORACLE_BASE.equals(beanType)) {
//            return new OracleBeanGen(this, javaSrcDirectory, javaBeanPackage);
//        } else if (BeanType.ORACLE_4_SL.equals(beanType)) {
//            return new Oracle4SLBeanGen(this, javaSrcDirectory, javaBeanPackage);
//        } else if (BeanType.ORACLE_4_JPA_SERVER.equals(beanType)) {
//            return new Oracle4JPAServerBeanGen(this, javaSrcDirectory, javaBeanPackage);
//        } else if (BeanType.ORACLE_4_JPA_CLIENT.equals(beanType)) {
//            return new Oracle4JPAClientBeanGen(this, javaSrcDirectory, javaBeanPackage);
//        } else if (BeanType.ORACLE_4_ACI.equals(beanType)) {
//            return new Oracle4ACIBeanGen(this, javaSrcDirectory, javaBeanPackage);
//        }
//        return null;
//    }
//
//    @Override
//    public DaoGenInterface getDaoGenInterface(DbDataBase dbDataBase, String owner) {
//        return new OracleDaoGen(dbDataBase, owner);
//    }
//
//    @Override
//    public TdeGenInterface getTdeGenInterface(DbDataBase dbDataBase) {
//        return new OracleTdeGen(dbDataBase);
//    }
//
//    private class Source {
//
//        String name;
//        StringBuilder source;
//
//        public Source(String name, String type) {
//            this.name = name;
//            this.source = new StringBuilder();
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void append(String text) {
//            source.append(text);
//        }
//
//        public String getSource() {
//            return source.toString();
//        }
//    }
//
//    /**
//     * Rende la data di ultima modifica di un oggetto utente (DDL). Lancia
//     * eccezione se non riesce a recuperare la data. (aggiunta da GG il
//     * 12/12/2012)
//     */
//    @Override
//    public Timestamp getLastDdlModifiedDate() throws SQLException {
//        PreparedStatement preparedStatement = getPreparedStatement(_sqlLastDdlModifiedDate);
//        try {
//            ResultSet resultSet = preparedStatement.executeQuery();
//            try {
//                if (resultSet.next()) {
//                    Timestamp date = resultSet.getTimestamp("Last_Ddl_Modified_Date");
//                    // NB: se uso il "gatDate()" mi dà solo la data con l'orario a 0:00:00
//                    // :(
//                    if (date != null)
//                        return date;
//                }
//            } finally {
//                resultSet.close();
//            }
//        } finally {
//            preparedStatement.close();
//        }
//        // In caso di errore o di irreperibilità del dato:
//        throw new SQLException(Messages.getString("OracleDbConnection.error_retriving_last_modified_date"));
//    }
//}

}
