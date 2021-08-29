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
 * RowBean per la vista ${tableName}
 */
public class ${rowBeanClassName} extends DbRow {

#foreach( $viewColumn in $table.viewColumnCollection )
    public static final String $viewColumn.name.toUpperCase() = "$viewColumn.name.toLowerCase()";
#end

    public static final Column[] columns = {
#foreach( $viewColumn in $table.viewColumnCollection )
        ${DbUtil.genColumn($viewColumn)}#if( $foreach.hasNext ),#end
#end
    };

    private static String SQL_SELECT = "Select * from ${table.name}";
    private static String SQL_INSERT = null;
    private static String SQL_DELETE = null;
    private static String SQL_UPDATE = null;

    public ${rowBeanClassName}() {
        super();
    }

    @Override
    public TransactionalRow setObject(String name, Object value) {
#foreach( $viewColumn in $table.blobColumnCollection )
        if (${viewColumn.name.toUpperCase()}.equalsIgnoreCase(name) && value instanceof byte[]) {
            set${viewColumn.name}((byte[]) value);
            return this;
        }
#end
        return super.setObject(name, value);
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
#foreach( $viewColumn in $table.plainColumnCollection )
    public $DbUtil.getJavaClass($viewColumn) get${viewColumn.name}() {
        return (${DbUtil.getJavaClass($viewColumn)})getObject($viewColumn.name.toUpperCase());
    }

    public $DbUtil.getJavaClass($viewColumn) getOld$viewColumn.name () {
        return (${DbUtil.getJavaClass($viewColumn)})getOldObject($viewColumn.name.toUpperCase());
    }

    public void set$viewColumn.name ($DbUtil.getJavaClass($viewColumn) $GenUtil.initLow($viewColumn.name)) {
        setObject($viewColumn.name.toUpperCase(), $GenUtil.initLow($viewColumn.name));
    }

#end
#foreach( $viewColumn in $table.blobColumnCollection )
    protected void set${viewColumn.name}BLobData(BLobData bLobData) {
        super.setObject(${viewColumn.name.toUpperCase()}, bLobData);
    }

    @SneakyThrows
    protected BLobData get${viewColumn.name}BLobData() {
        if (super.getObject(${viewColumn.name.toUpperCase()}) == null) {
            set${viewColumn.name}BLobData(new BLobData(isAutoloadLob(), null));
        }
        return (BLobData) super.getObject(${viewColumn.name.toUpperCase()});
    }

    public byte[] get${viewColumn.name}(Connection connection) throws SQLException, FrameworkException, IOException {
        if (get${viewColumn.name}BLobData().getStatus() != BLobData.OFF_LINE) {
              return get${viewColumn.name}BLobData().getValue();
        }

        if (this.getStatus() == RowStatus.INSERTED || this.getStatus() == RowStatus.INCONSISTENT) {
            return null;
        }

        if (connection == null) {
            try (Connection newConnection = DataConnectionManager.getInstance().getDataSource().getConnection()) {
                return get${viewColumn.name}(newConnection);
            }
        } else {
            DataRow row = selectQuery().selectRow(connection);
            BLobData bLobData = new BLobData(true, (Blob) row.getObject(${viewColumn.name.toUpperCase()}));
            set${viewColumn.name}BLobData(bLobData);

            return bLobData.getValue();
        }
    }

    public byte[] get${viewColumn.name}() throws SQLException, FrameworkException, IOException {
        return get${viewColumn.name}(null);
    }

    public void set${viewColumn.name}(byte[] ${GenUtil.initLow($viewColumn.name)}) {
        get${viewColumn.name}BLobData().setValue(${GenUtil.initLow($viewColumn.name)});
    }

#end

    private Query selectQuery() {
        return new Query (SQL_SELECT);
    }

    public boolean select(Connection connection) throws SQLException, IOException, FrameworkException {
        return loadFromQuery(selectQuery(),connection);
    }

    private void updateLob(Connection connection) {
    }

    public void update(Connection connection) {
        // NOP
    }

    public void insert(Connection connection) {
        // NOP
    }

    public void delete(Connection connection) {
        // NOP
    }

    public static class Factory {
        public static ${rowBeanClassName} load(SelectQueryInterface query) throws SQLException, IOException, FrameworkException {
            ${rowBeanClassName} rowBean = new ${rowBeanClassName}();
            rowBean.loadFromQuery(query);
            return rowBean;
        }

    }
}
