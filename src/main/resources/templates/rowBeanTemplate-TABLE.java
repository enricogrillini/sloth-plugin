package ${tableBeanPackageName};

import it.eg.sloth.db.datasource.DataRow;
import it.eg.sloth.db.datasource.RowStatus;
import it.eg.sloth.db.datasource.row.DbRow;
import it.eg.sloth.db.datasource.row.TransactionalRow;
import it.eg.sloth.db.datasource.row.column.Column;
import it.eg.sloth.db.datasource.row.lob.BLobData;
import it.eg.sloth.db.datasource.row.lob.CLobData;
import it.eg.sloth.db.manager.DataConnectionManager;
import it.eg.sloth.db.query.SelectQueryInterface;
import it.eg.sloth.db.query.query.Query;
import it.eg.sloth.framework.common.exception.FrameworkException;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.*;

import org.apache.commons.io.IOUtils;

/**
 * RowBean per la tabella ${tableName}
 */
public class ${rowBeanClassName} extends DbRow {

#foreach( $tableColumn in $table.tableColumnCollection )
    public static final String $tableColumn.name.toUpperCase() = "$tableColumn.name.toLowerCase()";
#end

    public static final Column[] columns = {
#foreach( $tableColumn in $table.tableColumnCollection )
        ${DbUtil.genColumn($tableColumn)}#if( $foreach.hasNext ),#end
#end
    };

    private static String SQL_SELECT =
        ${DbUtil.genSelect($table)};

    private static String SQL_INSERT =
        ${DbUtil.genInsert($table)};

    private static String SQL_DELETE =
        ${DbUtil.genDelete($table)};

    private static String SQL_UPDATE =
        ${DbUtil.genUpdate($table)};
		
#foreach( $tableColumn in $table.lobColumnCollection )
    private static String SQL_UPDATE_${tableColumn.name.toUpperCase()} =
        ${DbUtil.genUdateLob($table, $tableColumn)};
#end

    public ${rowBeanClassName}() {
        super();
    }

    @Override
    public TransactionalRow setObject(String name, Object value) {
#foreach( $tableColumn in $table.blobColumnCollection )
        if (${tableColumn.name.toUpperCase()}.equalsIgnoreCase(name) && value instanceof byte[]) {
            set${tableColumn.name}((byte[]) value);
            return this;
        }
#end
#foreach( $tableColumn in $table.clobColumnCollection )
        if (${tableColumn.name.toUpperCase()}.equalsIgnoreCase(name) && value instanceof String) {
            set${tableColumn.name}((String) value);
            return this;
        }
#end
        return super.setObject(name, value);
       }

#if ( $table.getPrimaryKeyCollection().size() > 0)
    public ${rowBeanClassName}(${DbUtil.genPrimaryKeyList($table, true)}) throws SQLException, IOException, FrameworkException {
        this(null, ${DbUtil.genPrimaryKeyList($table, false)});
    }

    public ${rowBeanClassName}(Connection connection, ${DbUtil.genPrimaryKeyList($table, true)}) throws SQLException, IOException, FrameworkException {
        this();
#foreach( $tableColumn in $table.primaryKeyCollection )
        set${tableColumn.name}(${GenUtil.initLow($tableColumn.name)});
#end
        select(connection);
    }
#end

    @Override
    public Column[] getColumns() {
        return columns;
    }

    @Override
    public String getSelect() {
        return SQL_SELECT;
    }

    @Override
    public String getInsert() {
        return SQL_INSERT;
    }

    @Override
    public String getDelete() {
        return SQL_DELETE;
    }

    @Override
    public String getUpdate() {
        return SQL_UPDATE;
    }

    // Setter/Getter
#foreach( $tableColumn in $table.plainColumnCollection )
    public $DbUtil.getJavaClass($tableColumn) get${tableColumn.name}() {
        return (${DbUtil.getJavaClass($tableColumn)})getObject($tableColumn.name.toUpperCase());
    }

    public $DbUtil.getJavaClass($tableColumn) getOld$tableColumn.name () {
        return (${DbUtil.getJavaClass($tableColumn)})getOldObject($tableColumn.name.toUpperCase());
    }

    public void set$tableColumn.name ($DbUtil.getJavaClass($tableColumn) $GenUtil.initLow($tableColumn.name)) {
        setObject($tableColumn.name.toUpperCase(), $GenUtil.initLow($tableColumn.name));
    }

#end
#foreach( $tableColumn in $table.blobColumnCollection )
    protected void set${tableColumn.name}BLobData(BLobData bLobData) {
        super.setObject(${tableColumn.name.toUpperCase()}, bLobData);
    }

    @SneakyThrows
    protected BLobData get${tableColumn.name}BLobData() {
        if (super.getObject(${tableColumn.name.toUpperCase()}) == null) {
            set${tableColumn.name}BLobData(new BLobData(isAutoloadLob(), null));
        }
        return (BLobData) super.getObject(${tableColumn.name.toUpperCase()});
    }

    public byte[] get${tableColumn.name}(Connection connection) throws SQLException, FrameworkException, IOException {
        if (get${tableColumn.name}BLobData().getStatus() != BLobData.OFF_LINE) {
              return get${tableColumn.name}BLobData().getValue();
        }

        if (this.getStatus() == RowStatus.INSERTED || this.getStatus() == RowStatus.INCONSISTENT) {
            return null;
        }

        if (connection == null) {
            try (Connection newConnection = DataConnectionManager.getInstance().getDataSource().getConnection()) {
                return get${tableColumn.name}(newConnection);
            }
        } else {
            DataRow row = selectQuery().selectRow(connection);
            BLobData bLobData = new BLobData(true, (Blob) row.getObject(${tableColumn.name.toUpperCase()}));
            set${tableColumn.name}BLobData(bLobData);

            return bLobData.getValue();
        }
    }

    public byte[] get${tableColumn.name}() throws SQLException, FrameworkException, IOException {
        return get${tableColumn.name}(null);
    }

    public void set${tableColumn.name}(byte[] ${GenUtil.initLow($tableColumn.name)}) {
        get${tableColumn.name}BLobData().setValue(${GenUtil.initLow($tableColumn.name)});
    }

#end
#foreach( $tableColumn in $table.clobColumnCollection )
    protected void set${tableColumn.name}CLobData(CLobData cLobData) {
        super.setObject(${tableColumn.name.toUpperCase()}, cLobData);
    }

    @SneakyThrows
    protected CLobData get${tableColumn.name}CLobData() {
        if (super.getObject(${tableColumn.name.toUpperCase()}) == null) {
            set${tableColumn.name}CLobData(new CLobData(isAutoloadLob(), null));
        }
        return (CLobData) super.getObject(${tableColumn.name.toUpperCase()});
    }

    public String get${tableColumn.name}(Connection connection) throws SQLException, FrameworkException, IOException {
        if (get${tableColumn.name}CLobData().getStatus() != CLobData.OFF_LINE) {
              return get${tableColumn.name}CLobData().getValue();
        }

        if (this.getStatus() == RowStatus.INSERTED || this.getStatus() == RowStatus.INCONSISTENT) {
            return null;
        }

        if (connection == null) {
            try (Connection newConnection = DataConnectionManager.getInstance().getDataSource().getConnection()) {
                return get${tableColumn.name}(newConnection);
            }
        } else {
            DataRow row = selectQuery().selectRow(connection);
            CLobData cLobData = new CLobData(true, (Clob) row.getObject(${tableColumn.name.toUpperCase()}));
            set${tableColumn.name}CLobData(cLobData);

            return cLobData.getValue();
        }
    }

    public String get${tableColumn.name}() throws SQLException, FrameworkException, IOException {
        return get${tableColumn.name}(null);
    }

    public void set${tableColumn.name}(String ${GenUtil.initLow($tableColumn.name)}) {
        get${tableColumn.name}CLobData().setValue(${GenUtil.initLow($tableColumn.name)});
    }

#end
    private Query selectQuery() {
        Query query = new Query (SQL_SELECT);
#foreach( $tableColumn in $table.primaryKeyCollection )
        query.addParameter(${DbUtil.getTypes($tableColumn)}, get${tableColumn.name}());
#end
        return query;
    }

    public boolean select(Connection connection) throws SQLException, IOException, FrameworkException {
        return loadFromQuery(selectQuery(),connection);
    }

    private void updateLob(Connection connection) {

#foreach( $tableColumn in $table.blobColumnCollection )		
		try {
			if (get${tableColumn.name}BLobData().getStatus() == BLobData.CHANGED) {
				Query query = new Query(SQL_UPDATE_${tableColumn.name.toUpperCase()});
    #foreach( $tableColumn in $table.primaryKeyCollection )
				query.addParameter(${DbUtil.getTypes($tableColumn)}, get${tableColumn.name}());
    #end
                query.execute(connection);

				if (get${tableColumn.name}BLobData().getValue() != null) {
					DataRow row = selectQuery().selectRow(connection);
					Blob blob = (Blob) row.getObject(${tableColumn.name.toUpperCase()});
					try (InputStream inputStream = new ByteArrayInputStream(get${tableColumn.name}BLobData().getValue());
            OutputStream outputStream = blob.setBinaryStream(0)) {
						IOUtils.copy(inputStream, outputStream);
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
#end

#foreach( $tableColumn in $table.clobColumnCollection )		
		try {
			if (get${tableColumn.name}CLobData().getStatus() == CLobData.CHANGED) {
				Query query = new Query(SQL_UPDATE_${tableColumn.name.toUpperCase()});
    #foreach( $tableColumn in $table.primaryKeyCollection )
				query.addParameter(${DbUtil.getTypes($tableColumn)}, get${tableColumn.name}());
    #end
                query.execute(connection);

				if (get${tableColumn.name}CLobData().getValue() != null) {
					DataRow row = selectQuery().selectRow(connection);
					Clob clob = (Clob) row.getObject(${tableColumn.name.toUpperCase()});
					try (Reader reader = new StringReader(get${tableColumn.name}CLobData().getValue());
						Writer outputStream = clob.setCharacterStream(0)) {
						IOUtils.copy(reader, outputStream);
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
#end
    }

    public void update(Connection connection) {
        try {
            int i = 1;
            PreparedStatement preparedStatement = connection.prepareStatement(getUpdate());
#foreach( $tableColumn in $table.plainColumnCollection )
            preparedStatement.setObject(i++, get${tableColumn.name}(), ${DbUtil.getTypes($tableColumn)});
#end
#foreach( $tableColumn in $table.primaryKeyCollection )
            preparedStatement.setObject(i++, getOld${tableColumn.name}(), ${DbUtil.getTypes($tableColumn)});
#end

            preparedStatement.executeUpdate();
            preparedStatement.close();

            updateLob(connection);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(Connection connection) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(getInsert());
            int i = 1;
#foreach( $tableColumn in $table.plainColumnCollection )
            preparedStatement.setObject(i++, get${tableColumn.name}(), ${DbUtil.getTypes($tableColumn)});
#end

            preparedStatement.executeUpdate();
            preparedStatement.close();

            updateLob(connection);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Connection connection) {
        try {
            Query query = new Query(SQL_DELETE);
#foreach( $tableColumn in $table.primaryKeyCollection )
            query.addParameter(${DbUtil.getTypes($tableColumn)}, getOld${tableColumn.name}());
#end
            query.execute(connection);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static class Factory {
        public static ${rowBeanClassName} load(SelectQueryInterface query) throws SQLException, IOException, FrameworkException {
            ${rowBeanClassName} rowBean = new ${rowBeanClassName}();
            rowBean.loadFromQuery(query);
            return rowBean;
        }

    }
}
