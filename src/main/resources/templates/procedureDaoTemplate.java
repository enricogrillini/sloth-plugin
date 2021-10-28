package ${proceduresDaoPackageName};

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import it.eg.sloth.db.manager.DataConnectionManager;
import it.eg.sloth.framework.common.exception.FrameworkException;

public class ProceduresDao {
    // Costanti
#foreach( $procedure in $procedureCollection )
    public static final String ${procedure.name.toUpperCase()} = "call ${procedure.name}())";
#end

#foreach( $procedure in $procedureCollection )
    public static void ${procedure.name}(${DbUtil.genArgumentList($procedure, true, true)}) throws SQLException {
        try (CallableStatement callableStatement = connection.prepareCall(${procedure.name.toUpperCase()})) {
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

}