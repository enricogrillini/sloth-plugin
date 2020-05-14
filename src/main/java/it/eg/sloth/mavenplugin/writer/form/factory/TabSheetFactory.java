package it.eg.sloth.mavenplugin.writer.form.factory;

import java.util.List;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Tab;
import it.eg.sloth.jaxb.form.TabSheet;

public class TabSheetFactory {
    private TabSheetFactory() {
        // nothing
    }

    public static void write(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            if (element instanceof TabSheet) {
                write(stringBuilder, (TabSheet) element);
            }
        }
    }

    public static void write(StringBuilder stringBuilder, TabSheet tabSheet) {
        String className = StringUtil.toJavaClassName(tabSheet.getName());

        stringBuilder.append("  public class " + className + " extends TabSheet {\n");
        stringBuilder.append("    \n");
        stringBuilder.append("    public final static String NAME = \"" + tabSheet.getName() + "\";\n");
        stringBuilder.append("\n");

        // Costanti e variabili
        List<Tab> tabs = tabSheet.getTab();
        for (Tab tab : tabs) {
            stringBuilder.append("    public final static String _" + StringUtil.toJavaConstantName(tab.getName()) + " = \"" + StringUtil.toJavaObjectName(tab.getName()) + "\";\n");
        }
        stringBuilder.append("\n");

        for (Tab tab : tabs) {
            stringBuilder.append("    public final static String " + StringUtil.toJavaConstantName(tab.getName()) + " = NAME + \".\" + \"" + StringUtil.toJavaObjectName(tab.getName()) + "\";\n");
        }
        stringBuilder.append("\n");

        for (Tab tab : tabs) {
            stringBuilder.append("    private Tab " + StringUtil.toJavaObjectName(tab.getName()) + ";\n");
        }
        stringBuilder.append("\n");

        // Costruttore
        stringBuilder.append("\n");
        stringBuilder.append("    public " + className + "() {\n");
        stringBuilder.append("      super(NAME);\n");

        for (Tab tab : tabs) {
            stringBuilder.append("      addChild(" + StringUtil.toJavaObjectName(tab.getName()) + " = new Tab(");
            stringBuilder.append("_" + StringUtil.toJavaConstantName(tab.getName()) + ", ");
            stringBuilder.append((tab.getDescription() == null ? "null" : " \"" + tab.getDescription() + "\"") + ", ");
            stringBuilder.append(tab.isHidden() + ", ");
            stringBuilder.append(tab.isHidden() + "));\n");
        }

        stringBuilder.append("    }\n");

        // Getter
        for (Tab tab : tabs) {
            stringBuilder.append("    public Tab get" + StringUtil.toJavaClassName(tab.getName()) + "() {\n");
            stringBuilder.append("      return " + StringUtil.toJavaObjectName(tab.getName()) + ";\n");
            stringBuilder.append("    }\n");
            stringBuilder.append("  \n");
        }

        stringBuilder.append("  }\n");
        stringBuilder.append("  \n");

    }

}
