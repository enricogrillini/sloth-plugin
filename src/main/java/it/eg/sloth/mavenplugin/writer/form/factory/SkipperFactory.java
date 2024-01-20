package it.eg.sloth.mavenplugin.writer.form.factory;

import java.util.List;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Skipper;

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
public class SkipperFactory {

    private SkipperFactory() {
        // nothing
    }

    public static void write(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            if (element instanceof Skipper) {
                write(stringBuilder, (Skipper) element);
            }
        }
    }

    private static void write(StringBuilder stringBuilder, Skipper skipper) {
        String className = StringUtil.toJavaClassName(skipper.getName());

        stringBuilder.append("  public class " + className + " extends Skipper {\n");
        stringBuilder.append("    \n");
        stringBuilder.append("    public final static String NAME = \"" + skipper.getName() + "\";\n");
        stringBuilder.append("\n");
        stringBuilder.append("    public " + className + " () {\n");
        stringBuilder.append("      super(NAME, " + skipper.isSkipBody() + ");\n");
        stringBuilder.append("    }\n");
        stringBuilder.append("  }\n");
    }

}
