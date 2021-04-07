package it.eg.sloth.mavenplugin.writer.form.factory;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.*;

import java.text.MessageFormat;
import java.util.List;

public class ElementFactory {

    private static final String GETTER = "" +
            "    public {0} get{1}() '{'\n" +
            "      return ({0}) {2};\n" +
            "    '}'\n" +
            "\n";

    private ElementFactory() {
        // NOP
    }

    public static void writeFieldsCostanti(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            stringBuilder.append("    public final static String _" + StringUtil.toJavaConstantName(element.getName()) + " = \"" + element.getName() + "\";\n");
        }
        stringBuilder.append("\n");
    }

    public static void writeFieldsCostanti2(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            stringBuilder.append("    public final static String " + StringUtil.toJavaConstantName(element.getName()) + " = NAME + \".\" + \"" + element.getName() + "\";\n");
        }
        stringBuilder.append("\n");
    }

    public static void writeFieldsVariabili(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            String simpleClassName = element.getClass().getSimpleName();

            if (element instanceof Series || element instanceof TextTotalizer || element instanceof InputTotalizer || element instanceof Semaphore) {
                stringBuilder.append("    private " + simpleClassName + " " + StringUtil.toJavaObjectName(element.getName()) + ";\n");
            } else if (element instanceof CheckButtons) {
                CheckButtons field = (CheckButtons) element;
                stringBuilder.append("    private " + simpleClassName + getListGenerics(field.getDataType()) + " " + StringUtil.toJavaObjectName(element.getName()) + ";\n");
            } else if (element instanceof Field) {
                Field field = (Field) element;
                stringBuilder.append("    private " + simpleClassName + getGenerics(field.getDataType()) + " " + StringUtil.toJavaObjectName(element.getName()) + ";\n");
            } else {
                stringBuilder.append("    private " + simpleClassName + " " + StringUtil.toJavaObjectName(element.getName()) + ";\n");
            }
        }
        stringBuilder.append("\n");
    }

    public static String getListGenerics(DataType dataType) {
        if (dataType == null) {
            return StringUtil.EMPTY;
        }

        if (DataType.DATE.equals(dataType) || DataType.DATETIME.equals(dataType) || DataType.TIME.equals(dataType) || DataType.HOUR.equals(dataType) || DataType.MONTH.equals(dataType)) {
            return "<List<Timestamp>, Timestamp>";
        } else if (DataType.DECIMAL.equals(dataType) || DataType.INTEGER.equals(dataType) || DataType.CURRENCY.equals(dataType) || DataType.PERC.equals(dataType) || DataType.NUMBER.equals(dataType)) {
            return "<List<BigDecimal>, BigDecimal>";
        } else if (DataType.STRING.equals(dataType) || DataType.MAIL.equals(dataType) || DataType.PARTITA_IVA.equals(dataType) || DataType.CODICE_FISCALE.equals(dataType) || DataType.URL.equals(dataType)) {
            return "<List<String>, String>";
        }

        return StringUtil.EMPTY;
    }

    public static String getGenerics(DataType dataType) {
        if (dataType == null) {
            return StringUtil.EMPTY;
        }

        if (DataType.DATE.equals(dataType) || DataType.DATETIME.equals(dataType) || DataType.TIME.equals(dataType) || DataType.HOUR.equals(dataType) || DataType.MONTH.equals(dataType)) {
            return "<Timestamp>";
        } else if (DataType.NUMBER.equals(dataType) || DataType.DECIMAL.equals(dataType) || DataType.INTEGER.equals(dataType) || DataType.CURRENCY.equals(dataType) || DataType.PERC.equals(dataType)) {
            return "<BigDecimal>";
        } else if (DataType.STRING.equals(dataType) || DataType.PASSWORD.equals(dataType) || DataType.MAIL.equals(dataType) || DataType.PARTITA_IVA.equals(dataType) || DataType.CODICE_FISCALE.equals(dataType) || DataType.URL.equals(dataType)) {
            return "<String>";
        }

        return StringUtil.EMPTY;
    }

    public static void writeGetter(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            String returnType = element.getClass().getSimpleName();

            if (element instanceof Series || element instanceof TextTotalizer || element instanceof InputTotalizer || element instanceof Semaphore) {
                // NOP
            } else if (element instanceof CheckButtons) {
                CheckButtons field = (CheckButtons) element;
                returnType = returnType + ElementFactory.getListGenerics(field.getDataType());
            } else if (element instanceof Field) {
                Field field = (Field) element;
                returnType = returnType + ElementFactory.getGenerics(field.getDataType());
            } else {
                // NOP
            }

            String className = StringUtil.toJavaClassName(element.getName());
            String objectName = StringUtil.toJavaObjectName(element.getName());

            stringBuilder.append(MessageFormat.format(GETTER, returnType, className, objectName));
        }

        stringBuilder.append("\n");

    }

}

