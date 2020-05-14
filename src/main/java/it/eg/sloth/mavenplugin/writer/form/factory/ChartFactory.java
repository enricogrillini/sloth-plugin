package it.eg.sloth.mavenplugin.writer.form.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.eg.sloth.framework.common.base.BaseFunction;
import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.SimpleChart;

public class ChartFactory {

    private ChartFactory() {
        // nothing
    }

    public static void write(StringBuilder stringBuilder, List<Element> elements) throws IOException {
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
        FieldFactory.writeFieldsCostanti(stringBuilder, elementList);
        FieldFactory.writeFieldsCostanti2(stringBuilder, elementList);
        FieldFactory.writeFieldsVariabili(stringBuilder, elementList);

        // Costruttore
        String title = simpleChart.getTitle() == null ? "null" : "\"" + simpleChart.getTitle() + "\"";

        stringBuilder.append("\n");
        stringBuilder.append("    public " + className + "() {\n");
        stringBuilder.append("      super(NAME, " +
                (simpleChart.getChartType() == null ? null : "ChartType." + simpleChart.getChartType()) + ", " +
                title + "," +
                (simpleChart.getLegendPosition() == null ? null : "LegendPosition." + simpleChart.getLegendPosition()) + ");\n");

        FieldFactory.writeAddFields(stringBuilder, elementList);
        stringBuilder.append("  }\n");

        // Getter/Setter
        FieldFactory.writeFieldsGetter(stringBuilder, elementList);

        stringBuilder.append("  }\n");
        stringBuilder.append("\n");

    }
}
