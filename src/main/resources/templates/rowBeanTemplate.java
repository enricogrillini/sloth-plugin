package ${tableBeanPackageName};

import it.eg.sloth.db.datasource.DataRow;
import it.eg.sloth.db.datasource.RowStatus;
import it.eg.sloth.db.datasource.row.DbRow;
import it.eg.sloth.db.datasource.row.TransactionalRow;
import it.eg.sloth.db.datasource.row.column.Column;
import it.eg.sloth.db.datasource.row.lob.BLobData;
import it.eg.sloth.db.manager.DataConnectionManager;
import it.eg.sloth.db.query.SelectQueryInterface;
import it.eg.sloth.db.query.query.Query;
import it.eg.sloth.framework.common.exception.FrameworkException;
import lombok.SneakyThrows;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

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
        return super.setObject(name, value);
       }

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
        return get$DbUtil.getJavaClass($tableColumn)($tableColumn.name.toUpperCase());
    }

    public $DbUtil.getJavaClass($tableColumn) getOld$tableColumn.name () {
        return getOld$DbUtil.getJavaClass($tableColumn)($tableColumn.name.toUpperCase());
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

    private Query selectQuery() {
        Query query = new Query (SQL_SELECT);
#foreach( $tableColumn in $table.primaryKeyCollection )
        query.addParameter(${DbUtil.getTypes($tableColumn)}, getOld${tableColumn.name}());
#end
        return query;
    }

    public boolean select(Connection connection) throws SQLException, IOException, FrameworkException {
        return loadFromQuery(selectQuery(),connection);
    }

    private void updateLob(Connection connection) {
    }

    public void update(Connection connection) {
        try {
            int i = 1;
            PreparedStatement preparedStatement = connection.prepareStatement(getUpdate());
#foreach( $tableColumn in $table.tableColumnCollection )
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
#foreach( $tableColumn in $table.tableColumnCollection )
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
