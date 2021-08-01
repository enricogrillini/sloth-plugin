package ${sequencesDaoPackageName};

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import it.eg.sloth.db.query.query.Query;
import it.eg.sloth.framework.common.exception.FrameworkException;

public class SequencesDao {
    private static final String COLUMN = "num";

#foreach( $sequence in $sequenceCollection )
    public static final String ${sequence.name.toUpperCase()} = "select nextval ( '${sequence.name}' ) num";
#end

#foreach( $sequence in $sequenceCollection )
    public static BigDecimal ${sequence.name}() throws SQLException, IOException, FrameworkException {
        return new Query(${sequence.name.toUpperCase()}).selectRow().getBigDecimal(COLUMN);
    }
#end

}