package it.eg.sloth.mavenplugin.writer.form.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Rollup;

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
 *
 * @author Enrico Grillini
 *
 */
public class RollupFactory {

  private RollupFactory() {
    // nothing
  }

  public static void write(StringBuilder stringBuilder, List<Element> elements) throws IOException {
    for (Element element : elements) {
      if (element instanceof Rollup) {
        write(stringBuilder, (Rollup) element);
      }
    }
  }

  public static void write(StringBuilder stringBuilder, Rollup grid) throws IOException {
    String className = StringUtil.toJavaClassName(grid.getName());

    stringBuilder.append("  public static class " + className + " extends Rollup {\n");
    stringBuilder.append("    \n");
    stringBuilder.append("    public final static String NAME = \"" + grid.getName() + "\";\n");
    stringBuilder.append("\n");

    // Costanti e variabili
    List<Element> elementList = new ArrayList<>(grid.getLevelOrMeasureOrAttribute());
    FieldFactory.writeFieldsCostanti(stringBuilder, elementList);
    FieldFactory.writeFieldsCostanti2(stringBuilder, elementList);
    FieldFactory.writeFieldsVariabili(stringBuilder, elementList);

    // Costruttore
    String description = grid.getDescription() == null ? "null" : "\"" + grid.getDescription() + "\"";

    stringBuilder.append("\n");
    stringBuilder.append("    public " + className + "() {\n");
    stringBuilder.append("      super(NAME, " + description + ");\n");

    FieldFactory.writeAddFields(stringBuilder, elementList);
    stringBuilder.append("  }\n");

    // Getter/Setter
    FieldFactory.writeFieldsGetter(stringBuilder, elementList);

    stringBuilder.append("  }\n");
    stringBuilder.append("\n");

  }

}
