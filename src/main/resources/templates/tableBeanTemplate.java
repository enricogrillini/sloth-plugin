package ${tableBeanPackageName};

import it.eg.sloth.db.query.SelectQueryInterface;
import it.eg.sloth.framework.common.exception.FrameworkException;
import it.eg.sloth.db.datasource.table.DbTable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * TableBean per la tabella/vista ${tableName}
 */
public class ${tableBeanClassName} extends DbTable<${rowBeanClassName}> {

  public static final String SELECT = "Select * from ${tableName} /*W*/";
  public static final String TABLE_NAME = "${tableName}";

  @Override
  protected ${rowBeanClassName} createRow() {
    ${rowBeanClassName} rowBean = new ${rowBeanClassName}();
    rowBean.setAutoloadLob(isAutoloadLob());
    return rowBean;
  }

  @Override
  protected ${tableBeanClassName} newTable() {
    return new ${tableBeanClassName}();
  }

  public void load(${rowBeanClassName} $rowBeanObjectName) throws SQLException, IOException, FrameworkException {
    load(SELECT, ${rowBeanClassName}.columns, $rowBeanObjectName);
  }

  public void load(${rowBeanClassName} $rowBeanObjectName, Connection connection) throws SQLException, IOException, FrameworkException {
    load(SELECT, ${rowBeanClassName}.columns, $rowBeanObjectName, connection);
  }

  public static class Factory {

    public static ${tableBeanClassName} load(${rowBeanClassName} rowBean, int pageSize, Connection connection) throws SQLException, IOException, FrameworkException {
      ${tableBeanClassName} tableBean = new ${tableBeanClassName}();
      tableBean.load(rowBean, connection);
      tableBean.setPageSize(pageSize);
      return tableBean;
    }

    public static ${tableBeanClassName} load(${rowBeanClassName} rowBean, int pageSize) throws SQLException, IOException, FrameworkException {
      return load(rowBean, pageSize, null);
    }

    public static ${tableBeanClassName} load(${rowBeanClassName} rowBean) throws SQLException, IOException, FrameworkException {
      return load(rowBean, -1);
    }

    public static ${tableBeanClassName} loadFromQuery(SelectQueryInterface query, int pageSize) throws SQLException, IOException, FrameworkException {
      ${tableBeanClassName} tableBean = new ${tableBeanClassName}();
      tableBean.loadFromQuery(query);
      tableBean.setPageSize(pageSize);
      return tableBean;
    }

    public static ${tableBeanClassName} loadFromQuery(SelectQueryInterface query) throws SQLException, IOException, FrameworkException {
      return loadFromQuery(query, -1);
    }
  }

}