package it.eg.sloth.mavenplugin.writer.form.factory;

import java.io.IOException;
import java.util.List;

import it.eg.sloth.framework.common.base.BaseFunction;
import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Grid;
import it.eg.sloth.jaxb.form.GridType;

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

    private static void write(StringBuilder stringBuilder, Grid grid) throws IOException {
        String className = StringUtil.toJavaClassName(grid.getName());

        String dataTable = "<DataTable<?>>";
        if (!BaseFunction.isBlank(grid.getTable())) {
            dataTable = "<" + grid.getTable().substring(0, 1).toUpperCase() + grid.getTable().substring(1).toLowerCase() + "TableBean>";
        }

        if (grid.getGridType() == null || GridType.BASE.equals(grid.getGridType())) {
            stringBuilder.append("  public static class " + className + " extends Grid" + dataTable + " {\n");
        }

        stringBuilder.append("    \n");
        stringBuilder.append("    public final static String NAME = \"" + grid.getName() + "\";\n");
        stringBuilder.append("\n");

        // Costanti e variabili
        ElementFactory.writeFieldsCostanti(stringBuilder, grid.getTextOrInputOrTextArea());
        ElementFactory.writeFieldsCostanti2(stringBuilder, grid.getTextOrInputOrTextArea());
        ElementFactory.writeFieldsVariabili(stringBuilder, grid.getTextOrInputOrTextArea());

        // Costruttore
        String description = grid.getDescription() == null ? "null" : "\"" + grid.getDescription() + "\"";
        String title = grid.getTitle() == null ? "null" : "\"" + grid.getTitle() + "\"";

        stringBuilder.append("\n")
                .append("    public " + className + "() {\n")
                .append("      super(NAME, " + description + ");\n")
                .append("      setTitle (" + title + ");\n")
                .append("      setBackButtonHidden (" + grid.isBackButtonHidden() + ");\n")
                .append("      setSelectButtonHidden (" + grid.isSelectButtonHidden() + ");\n")

                .append("      setFirstButtonHidden (" + grid.isFirstButtonHidden() + ");\n")
                .append("      setPrevPageButtonHidden (" + grid.isPrevPageButtonHidden() + ");\n")
                .append("      setPrevButtonHidden (" + grid.isPrevButtonHidden() + ");\n")
                .append("      setDetailButtonHidden (" + grid.isDetailButtonHidden() + ");\n")
                .append("      setNextButtonHidden (" + grid.isNextButtonHidden() + ");\n")
                .append("      setNextPageButtonHidden (" + grid.isNextPageButtonHidden() + ");\n")
                .append("      setLastButtonHidden (" + grid.isLastPageButtonHidden() + ");\n")

                .append("      setInsertButtonHidden (" + grid.isInsertButtonHidden() + ");\n")
                .append("      setDeleteButtonHidden (" + grid.isDeleteButtonHidden() + ");\n")
                .append("      setUpdateButtonHidden (" + grid.isUpdateButtonHidden() + ");\n")
                .append("      setCommitButtonHidden (" + grid.isCommitButtonHidden() + ");\n")
                .append("      setRollbackButtonHidden (" + grid.isRollbackButtonHidden() + ");\n");


        FieldFactory.writeAddFields(stringBuilder, grid.getTextOrInputOrTextArea());

        PivotFactory.writeAddPivots(stringBuilder, grid.getTextOrInputOrTextArea());
        stringBuilder.append("  }\n");

        // Getter/Setter
        ElementFactory.writeGetter(stringBuilder, grid.getTextOrInputOrTextArea());

        stringBuilder.append("  }\n");
        stringBuilder.append("\n");

    }
}
