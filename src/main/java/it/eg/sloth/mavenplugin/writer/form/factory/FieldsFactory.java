package it.eg.sloth.mavenplugin.writer.form.factory;

import java.io.IOException;
import java.util.List;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Fields;

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
public class FieldsFactory {

    private FieldsFactory() {
        // NOP
    }

    public static void write(StringBuilder stringBuilder, List<Element> elements) throws IOException {
        for (Element element : elements) {
            if (element instanceof Fields) {
                write(stringBuilder, (Fields) element);
            }
        }
    }

    private static void write(StringBuilder stringBuilder, Fields fields) {
        String className = StringUtil.toJavaClassName(fields.getName());

        stringBuilder.append("  public static class " + className + " extends Fields<DataSource> {\n");
        stringBuilder.append("    \n");
        stringBuilder.append("    public final static String NAME = \"" + fields.getName() + "\";\n");
        stringBuilder.append("  \n");

        // Costanti e variabili
        ElementFactory.writeFieldsCostanti(stringBuilder, fields.getTextOrInputOrTextArea());
        ElementFactory.writeFieldsCostanti2(stringBuilder, fields.getTextOrInputOrTextArea());
        ElementFactory.writeFieldsVariabili(stringBuilder, fields.getTextOrInputOrTextArea());

        // Costruttore
        String description = fields.getDescription() == null ? "null" : "\"" + fields.getDescription() + "\"";
        String title = fields.getTitle() == null ? "null" : "\"" + fields.getTitle() + "\"";

        stringBuilder.append("\n")
                .append("    public " + className + "() {\n")
                .append("      super(NAME, " + description + ");\n")
                .append("      setTitle (" + title + ");\n");

        FieldFactory.writeAddFields(stringBuilder, fields.getTextOrInputOrTextArea());
        stringBuilder.append("  }\n");

        // Getter/Setter
        ElementFactory.writeGetter(stringBuilder, fields.getTextOrInputOrTextArea());

        stringBuilder.append("  }\n");
        stringBuilder.append("\n");
    }
}
