package it.eg.sloth.mavenplugin.writer.form.factory;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Modal;
import it.eg.sloth.mavenplugin.common.GenUtil;

import java.util.List;

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
 *
 */
public class ModalFactory {

    private ModalFactory() {
        // nothing
    }

    public static void write(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            if (element instanceof Modal) {
                write(stringBuilder, (Modal) element);
            }
        }
    }

    public static void write(StringBuilder stringBuilder, Modal modal) {
        String className = StringUtil.toJavaClassName(modal.getName());
        String title = GenUtil.stringToJava(modal.getTitle());

        stringBuilder.append("  public class " + className + " extends Modal {\n");
        stringBuilder.append("    \n");
        stringBuilder.append("    public final static String NAME = \"" + modal.getName() + "\";\n");
        stringBuilder.append("\n");
        stringBuilder.append("    public " + className + " () {\n");
        stringBuilder.append("      super(NAME, " + title + ");\n");
        stringBuilder.append("    }\n");
        stringBuilder.append("  }\n");
    }

}
