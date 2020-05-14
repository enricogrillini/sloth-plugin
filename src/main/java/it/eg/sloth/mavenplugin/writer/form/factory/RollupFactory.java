package it.eg.sloth.mavenplugin.writer.form.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Rollup;

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
