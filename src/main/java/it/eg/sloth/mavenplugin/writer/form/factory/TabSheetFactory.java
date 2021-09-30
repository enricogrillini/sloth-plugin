package it.eg.sloth.mavenplugin.writer.form.factory;

import java.util.List;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Tab;
import it.eg.sloth.jaxb.form.TabSheet;
import it.eg.sloth.mavenplugin.common.GenUtil;

/**
 * Project: sloth-plugin
 * Copyright (C) 2019-2021 Enrico Grillini
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Enrico Grillini
 */
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
            stringBuilder
                    .append("      " + StringUtil.toJavaObjectName(tab.getName()) + " = Tab.builder()\n")
                    .append("        .name(_" + StringUtil.toJavaConstantName(tab.getName()) + ")\n")
                    .append("        .description(" + GenUtil.stringToJava(tab.getDescription()) + ")\n")
                    .append("        .tooltip(" + GenUtil.stringToJava(tab.getToolTip()) + ")\n")
                    .append("        .hidden(" + tab.isHidden() + ")\n")
                    .append("        .disabled(" + tab.isDisabled() + ")\n")
                    .append("        .badgeHtml(" + GenUtil.stringToJava(tab.getBadgeHtml()) + ")\n")
                    .append("        .badgeType(" + (tab.getBadgeType() == null ? "null" : "BadgeType." + tab.getBadgeType()) + ")\n")
                    .append("        .build();\n")
                    .append("      addChild(" + StringUtil.toJavaObjectName(tab.getName()) + ");\n")
                    .append("\n");
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
