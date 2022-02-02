package it.eg.sloth.mavenplugin.writer.form.factory;

import it.eg.sloth.framework.common.base.BaseFunction;
import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.SimpleChart;

import java.util.ArrayList;
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
 */
public class ChartFactory {

    private ChartFactory() {
        // nothing
    }

    public static void write(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            if (element instanceof SimpleChart) {
                write(stringBuilder, (SimpleChart) element);
            }
        }
    }

    public static void write(StringBuilder stringBuilder, SimpleChart simpleChart) {
        String className = StringUtil.toJavaClassName(simpleChart.getName());

        String dataTable = "<DataTable<?>>";
        if (!BaseFunction.isBlank(simpleChart.getTable())) {
            dataTable = "<" + simpleChart.getTable().substring(0, 1).toUpperCase() + simpleChart.getTable().substring(1).toLowerCase() + "TableBean>";
        }

        stringBuilder.append("  public static class " + className + " extends SimpleChart" + dataTable + " {\n");
        stringBuilder.append("\n");
        stringBuilder.append("    public final static String NAME = \"" + simpleChart.getName() + "\";\n");
        stringBuilder.append("\n");

        // Costanti e variabili
        List<Element> elementList = new ArrayList<>(simpleChart.getSeries());
        elementList.add(simpleChart.getLabels());
        ElementFactory.writeFieldsCostanti(stringBuilder, elementList);
        ElementFactory.writeFieldsCostanti2(stringBuilder, elementList);
        ElementFactory.writeFieldsVariabili(stringBuilder, elementList);

        // Costruttore
        String title = simpleChart.getTitle() == null ? "null" : "\"" + simpleChart.getTitle() + "\"";

        stringBuilder.append("\n");
        stringBuilder.append("    public " + className + "() {\n");
        stringBuilder.append("      super(NAME, " +
                (simpleChart.getChartType() == null ? null : "ChartType." + simpleChart.getChartType()) + ", " +
                title + "," +
                (simpleChart.getLegendPosition() == null ? null : "LegendPosition." + simpleChart.getLegendPosition()) + ");\n");

        stringBuilder.append("      setStacked (" + simpleChart.isStacked() + ");\n")
                .append("      setFilled (" + simpleChart.isFilled() + ");\n");


        FieldFactory.writeAddFields(stringBuilder, elementList);
        stringBuilder.append("  }\n");

        // Getter/Setter
        ElementFactory.writeGetter(stringBuilder, elementList);

        stringBuilder.append("  }\n");
        stringBuilder.append("\n");

    }
}
