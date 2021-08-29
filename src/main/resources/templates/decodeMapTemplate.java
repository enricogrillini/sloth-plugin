package ${decodeMapPackageName};

import ${tableBeanPackageName}.${rowBeanClassName};
import ${tableBeanPackageName}.${tableBeanClassName};
import it.eg.sloth.db.decodemap.DecodeMapManger;
import it.eg.sloth.db.decodemap.map.BaseDecodeMap;
import it.eg.sloth.framework.common.exception.FrameworkException;

import java.io.IOException;
import java.sql.SQLException;

import static it.eg.sloth.db.decodemap.DecodeMapManger.loadFromCache;
import static it.eg.sloth.db.decodemap.DecodeMapManger.loadFromDb;

/**
 * DecodeMap per la tabella ${tableName}
 */
public class ${decodeMapClassName} {

#foreach( $tableConstant in $table.constantCollection )
  public static final String ${tableConstant.name.toUpperCase()} = "${tableConstant.value}";
#end

  public static BaseDecodeMap<String> getFromCache() throws SQLException, IOException, FrameworkException {
    return loadFromCache(${tableBeanClassName}.TABLE_NAME, ${rowBeanClassName}.${table.getTableColumn(0).name.toUpperCase()}, ${rowBeanClassName}.DESCRIZIONEBREVE, ${rowBeanClassName}.FLAGVALIDO);
  }

  public static void removeFromCache() throws SQLException, IOException, FrameworkException {
    DecodeMapManger.removeFromCache(${tableBeanClassName}.TABLE_NAME);
  }

  public static BaseDecodeMap<String> getFromDb() throws SQLException, IOException, FrameworkException {
    return loadFromDb(${tableBeanClassName}.TABLE_NAME, ${rowBeanClassName}.${table.getTableColumn(0).name.toUpperCase()}, ${rowBeanClassName}.DESCRIZIONEBREVE, ${rowBeanClassName}.FLAGVALIDO);
  }

}