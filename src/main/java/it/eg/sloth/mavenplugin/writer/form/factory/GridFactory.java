package it.eg.sloth.mavenplugin.writer.form.factory;

import java.io.IOException;
import java.util.List;

import it.eg.sloth.framework.common.base.BaseFunction;
import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Grid;
import it.eg.sloth.jaxb.form.GridType;

public class GridFactory {

  private GridFactory() {
    // nothing
  }

  public static void write(StringBuilder stringBuilder, List<Element> elements) throws IOException {
    for (Element element : elements) {
      if (element instanceof Grid) {
        write(stringBuilder, (Grid) element);
      }
    }
  }

  public static void write(StringBuilder stringBuilder, Grid grid) throws IOException {
    String className = StringUtil.toJavaClassName(grid.getName());

    String dataTable = "<DataTable<?>>";
    if (!BaseFunction.isBlank(grid.getTable())) {
      dataTable = "<" + grid.getTable().substring(0, 1).toUpperCase() + grid.getTable().substring(1).toLowerCase() + "TableBean>";
    }

    if (grid.getGridType() == null || GridType.BASE.equals(grid.getGridType())) {
      stringBuilder.append("  public static class " + className + " extends Grid" + dataTable + " {\n");
    } else if (grid.getGridType() == null || GridType.RADIO.equals(grid.getGridType())) {
      stringBuilder.append("  public static class " + className + " extends RadioGrid" + dataTable + " {\n");
    }

    stringBuilder.append("    \n");
    stringBuilder.append("    public final static String NAME = \"" + grid.getName() + "\";\n");
    stringBuilder.append("\n");

    // Costanti e variabili
    FieldFactory.writeFieldsCostanti(stringBuilder, grid.getTextOrInputOrTextArea());
    FieldFactory.writeFieldsCostanti2(stringBuilder, grid.getTextOrInputOrTextArea());
    FieldFactory.writeFieldsVariabili(stringBuilder, grid.getTextOrInputOrTextArea());

    // Costruttore
    String description = grid.getDescription() == null ? "null" : "\"" + grid.getDescription() + "\"";
    String title = grid.getTitle() == null ? "null" : "\"" + grid.getTitle() + "\"";

    stringBuilder.append("\n");
    stringBuilder.append("    public " + className + "() {\n");
    stringBuilder.append("      super(NAME, " +
                         description + "," +
                         title + "," +
                         grid.isBackButtonHidden() + "," +
                         grid.isSelectButtonHidden() + "," +
                         grid.isFirstButtonHidden() + "," +
                         grid.isPrevPageButtonHidden() + "," +
                         grid.isPrevButtonHidden() + "," +
                         grid.isDetailButtonHidden() + "," +
                         grid.isNextButtonHidden() + "," +
                         grid.isNextPageButtonHidden() + "," +
                         grid.isLastPageButtonHidden() + "," +
                         grid.isInsertButtonHidden() + "," +
                         grid.isDeleteButtonHidden() + "," +
                         grid.isUpdateButtonHidden() + "," +
                         grid.isCommitButtonHidden() + "," +
                         grid.isRollbackButtonHidden() + ");\n");

    FieldFactory.writeAddFields(stringBuilder, grid.getTextOrInputOrTextArea());
    stringBuilder.append("  }\n");

    // Getter/Setter
    FieldFactory.writeFieldsGetter(stringBuilder, grid.getTextOrInputOrTextArea());

    stringBuilder.append("  }\n");
    stringBuilder.append("\n");

  }
}
