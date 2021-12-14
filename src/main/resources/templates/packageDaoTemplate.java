package ${packageDaoPackageName};

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import it.eg.sloth.db.manager.DataConnectionManager;
import it.eg.sloth.framework.common.exception.FrameworkException;

public class ${packageDaoClassName} {

    // Costanti - Procedure
#foreach( $procedure in $procedureCollection )
    private static final String ${procedure.uniqueName.toUpperCase()} = "${DbUtil.genStatement(${packageName}, ${procedure})}";
#end

    // Costanti - Function
#foreach( $function in $functionCollection )
    private static final String ${function.uniqueName.toUpperCase()} = "${DbUtil.genStatement(${packageName}, ${function})}";
#end

    // Metodi - Procedure
#foreach( $procedure in $procedureCollection )
    public static void ${procedure.name}(${DbUtil.genArgumentList($procedure, true, true)}) throws SQLException {
        try (CallableStatement callableStatement = connection.prepareCall(${procedure.uniqueName.toUpperCase()})) {
            int i = 1;
    #foreach( $argument in $procedure.arguments )
            callableStatement.setObject(i++, ${GenUtil.initLow($argument.name)}, ${DbUtil.getTypes($argument)});
    #end
            callableStatement.execute();
        }
    }

    public static void ${procedure.name}(${DbUtil.genArgumentList($procedure, false, true)}) throws SQLException, FrameworkException {
        try (Connection connection = DataConnectionManager.getInstance().getDataSource().getConnection()) {
            ${procedure.name}(${DbUtil.genArgumentList($procedure, true, false)});
        }
    }

#end

    // Metodi - Function
#foreach( $function in $functionCollection )
    public static ${DbUtil.getJavaClass($function)} ${function.name}(${DbUtil.genArgumentList($function, true, true)}) throws SQLException {
        try (CallableStatement callableStatement = connection.prepareCall(${function.uniqueName.toUpperCase()})) {
            int i = 1;
            callableStatement.registerOutParameter(i++, ${DbUtil.getTypes($function)});
    #foreach( $argument in $function.arguments )
            callableStatement.setObject(i++, ${GenUtil.initLow($argument.name)}, ${DbUtil.getTypes($argument)});
    #end
            callableStatement.execute();

    #if ( ${DbUtil.isOracleBoolean($function.getReturnType())} )
            ${DbUtil.getJavaClass($function)} value = "S".equals(callableStatement.getString(1));
    #else
            ${DbUtil.getJavaClass($function)} value = callableStatement.get${DbUtil.getJavaClass($function)} (1);
    #end
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