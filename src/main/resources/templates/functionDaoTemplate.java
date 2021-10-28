package ${functionsDaoPackageName};

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import it.eg.sloth.db.manager.DataConnectionManager;
import it.eg.sloth.framework.common.exception.FrameworkException;

public class FunctionsDao {
    // Costanti
#foreach( $function in $functionCollection )
    public static final String ${function.name.toUpperCase()} = "call ${function.name}())";
#end

#foreach( $function in $functionCollection )
    public static void ${function.name}(${DbUtil.genArgumentList($function, true, true)}) throws SQLException {
        try (CallableStatement callableStatement = connection.prepareCall(${function.name.toUpperCase()})) {
            int i = 1;
    #foreach( $argument in $function.arguments )
            callableStatement.setObject(i++, ${GenUtil.initLow($argument.name)}, ${DbUtil.getTypes($argument)});
    #end
            callableStatement.execute();
        }
    }

    public static void ${function.name}(${DbUtil.genArgumentList($function, false, true)}) throws SQLException, FrameworkException {
        try (Connection connection = DataConnectionManager.getInstance().getDataSource().getConnection()) {
            ${function.name}(${DbUtil.genArgumentList($function, true, false)});
        }
    }

#end

}