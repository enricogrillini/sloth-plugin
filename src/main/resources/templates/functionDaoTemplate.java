package ${functionsDaoPackageName};

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import it.eg.sloth.db.manager.DataConnectionManager;
import it.eg.sloth.framework.common.exception.FrameworkException;

public class FunctionsDao {
    // Costanti
#foreach( $function in $functionCollection )
    private static final String ${function.name.toUpperCase()} = "{ ? = call ${function.name}(${DbUtil.genArgumentParamList($function)}) }";
#end

#foreach( $function in $functionCollection )
    public static ${DbUtil.getJavaClass($function)} ${function.name}(${DbUtil.genArgumentList($function, true, true)}) throws SQLException {
        try (CallableStatement callableStatement = connection.prepareCall(${function.name.toUpperCase()})) {
            int i = 1;
            callableStatement.registerOutParameter(i++, ${DbUtil.getTypes($function)});
    #foreach( $argument in $function.arguments )
            callableStatement.setObject(i++, ${GenUtil.initLow($argument.name)}, ${DbUtil.getTypes($argument)});
    #end
            callableStatement.execute();

            ${DbUtil.getJavaClass($function)}  value = callableStatement.get${DbUtil.getJavaClass($function)} (1);
            return value;
        }
    }

    public static ${DbUtil.getJavaClass($function)} ${function.name}(${DbUtil.genArgumentList($function, false, true)}) throws SQLException, FrameworkException {
        try (Connection connection = DataConnectionManager.getInstance().getDataSource().getConnection()) {
            return ${function.name}(${DbUtil.genArgumentList($function, true, false)});
        }
    }

#end

}