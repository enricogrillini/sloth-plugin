package it.eg.sloth.mavenplugin.writer.bean.oraclebean;

import it.eg.sloth.jaxb.dbschema.Package;
import it.eg.sloth.jaxb.dbschema.*;
import it.eg.sloth.mavenplugin.common.GenUtil;
import lombok.Data;

import java.io.File;
import java.io.IOException;

/**
 * Project: sloth-plugin
 * Copyright (C) 2019-2020 Enrico Grillini
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Classe per la generazione dei Package bean
 *
 * @author Enrico Grillini
 */
@Data
public class PackageBeanWriter {
    private static final String PACKAGE_BEAN = ".bean.packagebean";

    Package dbPackage;

    String packageBeanClassName;
    String packageBeanFullClassName;
    String packageBeanPackageName;
    File packageBeanClassFile;


    public PackageBeanWriter(File outputJavaDirectory, String genPackage, Package dbPackage) {
        this.dbPackage = dbPackage;

        // Bean properties
        packageBeanClassName = GenUtil.initCap(dbPackage.getName()) + "Bean";
        packageBeanFullClassName = genPackage + PACKAGE_BEAN + "." + packageBeanClassName;
        packageBeanPackageName = genPackage + PACKAGE_BEAN;
        packageBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, packageBeanPackageName, packageBeanClassName);
    }

    public String getProceduresParameterList(Method dbMethod, boolean writeType) {
        String result = "";

        for (Argument dbArgument : dbMethod.getArguments().getArgument()) {
            if (dbArgument.getPosition().intValue() == 0 || dbArgument.getName() == null)
                continue;

            result += (dbArgument.getPosition().intValue() == 1) ? "" : ", ";
            result += (writeType) ? OracleUtil.getJavaClass(dbArgument.getType()) + " " : "";
            result += dbArgument.getName().toLowerCase();
        }

        return result;
    }

    public void writeProceduresStatement(StringBuilder stringBuilder) {

        for (Method dbMethod : dbPackage.getMethods().getMethod()) {
            if (!OracleUtil.isJavaPortable(dbMethod))
                continue;

            boolean isFunction = dbMethod.getType() == MethodType.FUNCTION;
            boolean isBoolean = false;

            if (isFunction) {
                isBoolean = OracleUtil.isBoolean(dbMethod.getArguments().getArgument().get(0).getType());

                if (isBoolean)
                    stringBuilder.append("  private static String " + dbMethod.getName() + "Statement" + dbMethod.getOverload() + " = \"{ ? = call lib.booleanToChar(" + dbPackage.getName() + "." + dbMethod.getName() + "(");    //$NON-NLS-5$
                else
                    stringBuilder.append("  private static String " + dbMethod.getName() + "Statement" + dbMethod.getOverload() + " = \"{ ? = call " + dbPackage.getName() + "." + dbMethod.getName() + "(");    //$NON-NLS-5$
            } else {
                stringBuilder.append("  private static String " + dbMethod.getName() + "Statement" + dbMethod.getOverload() + " = \"{ call " + dbPackage.getName() + "." + dbMethod.getName() + "(");    //$NON-NLS-5$
            }

            for (Argument dbArgument : dbMethod.getArguments().getArgument()) {
                if (dbArgument.getPosition().intValue() == 0 || dbArgument.getName() == null)
                    continue;

                stringBuilder.append(dbArgument.getPosition() == 1 ? "?" : ", ?");
            }

            if (isFunction && isBoolean)
                stringBuilder.append(")) }\";\n");
            else
                stringBuilder.append(") }\";\n");
        }

    }

    public void writeProcedures(StringBuilder stringBuilder) {

        for (Method dbMethod : dbPackage.getMethods().getMethod()) {
            if (!OracleUtil.isJavaPortable(dbMethod))
                continue;

            // UNUSED: String parameterList1 = getProceduresParameterList(dbMethod, false);
            String parameterList2 = getProceduresParameterList(dbMethod, true);

            // Metodo con connection
            if (dbMethod.getType() == MethodType.FUNCTION) {
                Argument dbArgument = dbMethod.getArguments().getArgument().get(0);

                stringBuilder.append("  public static " + OracleUtil.getJavaClass(dbArgument.getType()) + " " + dbMethod.getName() + " (Connection connection");
                stringBuilder.append(parameterList2 == "" ? "" : ", " + parameterList2);
                stringBuilder.append(") {\n");
            } else {
                stringBuilder.append("  public static void " + dbMethod.getName() + " (Connection connection");
                stringBuilder.append(parameterList2 == "" ? "" : ", " + parameterList2);
                stringBuilder.append(") {\n");
            }

            stringBuilder.append("    CallableStatement callableStatement = null;\n");
            stringBuilder.append("    try {\n");
            stringBuilder.append("      callableStatement = connection.prepareCall(" + dbMethod.getName() + "Statement" + dbMethod.getOverload() + ");\n");
            int j = 0;
            for (Argument dbArgument : dbMethod.getArguments().getArgument()) {
                if (dbArgument.getName() == null && dbArgument.getPosition() > 0)
                    continue;

                if (dbArgument.getInOut() == ArgumentType.IN)
                    stringBuilder.append("      callableStatement.setObject(" + (j + 1) + ", " + dbArgument.getName().toLowerCase() + ", Types." + OracleUtil.getTypes(dbArgument.getType()) + ");\n");

                if (dbArgument.getInOut() == ArgumentType.OUT)
                    stringBuilder.append("      callableStatement.registerOutParameter(" + (j + 1) + ", Types." + OracleUtil.getTypes(dbArgument.getType()) + ");\n");

                j++;
            }

            stringBuilder.append("      callableStatement.execute();\n");
            stringBuilder.append("\n");

            if (dbMethod.getType() == MethodType.FUNCTION) {
                Argument dbArgument = dbMethod.getArguments().getArgument().get(0);
                if (OracleUtil.isBoolean(dbArgument.getType())) {
                    stringBuilder.append("      boolean value = \"S\".equals(callableStatement.getString(1));\n");
                } else {
                    stringBuilder.append("      " + OracleUtil.getJavaClass(dbArgument.getType()) + " value = callableStatement.get" + OracleUtil.getJavaClass(dbArgument.getType()) + "(1);\n");
                }

                stringBuilder.append("      return value;\n");
            } else {
            }
            stringBuilder.append("    } catch (SQLException e) {\n");
            stringBuilder.append("      throw new RuntimeException(e);\n");
            stringBuilder.append("    } finally {\n");
            stringBuilder.append("      DataConnectionManager.release(callableStatement);\n");
            stringBuilder.append("    }\n");
            stringBuilder.append("  }\n");
            stringBuilder.append("\n");

            // Metodo senza connection
            if (dbMethod.getType() == MethodType.FUNCTION) {
                Argument dbArgument = dbMethod.getArguments().getArgument().get(0);

                stringBuilder.append("  public static " + OracleUtil.getJavaClass(dbArgument.getType()) + " " + dbMethod.getName() + " (" + parameterList2 + ") throws SQLException, FrameworkException {\n");
            } else {
                stringBuilder.append("  public static void " + dbMethod.getName() + " (" + parameterList2 + ") throws SQLException, FrameworkException {\n");
            }

            stringBuilder.append("    try (Connection connection = DataConnectionManager.getInstance().getDataSource().getConnection()) {\n");
            stringBuilder.append(dbMethod.getType() == MethodType.FUNCTION ? "      return " + dbMethod.getName() + "(connection" : "      " + dbMethod.getName() + "(connection");

            for (Argument dbArgument : dbMethod.getArguments().getArgument()) {
                if (dbArgument.getName() == null && dbArgument.getPosition() > 0) {
                    continue;
                }

                if (dbArgument.getInOut() == ArgumentType.IN) {
                    stringBuilder.append(", " + dbArgument.getName().toLowerCase());
                }
            }
            stringBuilder.append(");\n");
            stringBuilder.append("    }\n");
            stringBuilder.append("  }\n");
            stringBuilder.append("\n");

        }
    }

    public void write() throws IOException {
        StringBuilder testo = new StringBuilder()
                .append("package " + packageBeanPackageName + ";\n")
                .append("\n")
                .append("import it.eg.sloth.db.manager.DataConnectionManager;\n")
                .append("import it.eg.sloth.framework.common.exception.FrameworkException;\n")
                .append("\n")
                .append("import java.math.BigDecimal;\n")
                .append("import java.sql.CallableStatement;\n")
                .append("import java.sql.Connection;\n")
                .append("import java.sql.SQLException;\n")
                .append("import java.sql.Timestamp;\n")
                .append("import java.sql.Types;\n")
                .append("\n")
                .append("public class " + packageBeanClassName + " {\n");

        writeProceduresStatement(testo);
        writeProcedures(testo);

        testo.append("\n");
        testo.append("}\n");

        GenUtil.writeFile(packageBeanClassFile, testo.toString());
    }


}

 