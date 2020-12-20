package it.eg.sloth.mavenplugin.writer.form;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;

import it.eg.sloth.framework.common.base.BaseFunction;
import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Button;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Fields;
import it.eg.sloth.jaxb.form.Form;
import it.eg.sloth.jaxb.form.Grid;
import it.eg.sloth.jaxb.form.PageType;
import it.eg.sloth.mavenplugin.common.GenUtil;
import it.eg.sloth.mavenplugin.common.files.DirectoryFilter;
import it.eg.sloth.mavenplugin.common.files.ExtensionFilter;
import it.eg.sloth.mavenplugin.writer.form.factory.ChartFactory;
import it.eg.sloth.mavenplugin.writer.form.factory.FieldsFactory;
import it.eg.sloth.mavenplugin.writer.form.factory.GridFactory;
import it.eg.sloth.mavenplugin.writer.form.factory.RollupFactory;
import it.eg.sloth.mavenplugin.writer.form.factory.SkipperFactory;
import it.eg.sloth.mavenplugin.writer.form.factory.TabSheetFactory;
import it.eg.sloth.mavenplugin.writer.form.model.FormProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

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
 */
@AllArgsConstructor
@Data
public class FormWriter {

    File formDirectory;
    File srcDirectory;
    String genPackage;
    Log log;

    /**
     * Scansiona la cartella formDirectory e ritorna la lista delle form
     *
     * @return
     */
    public List<FormProperties> scan() {
        Collection<File> files = FileUtils.listFiles(formDirectory, new ExtensionFilter(".xml"), new DirectoryFilter());
        List<FormProperties> list = new ArrayList<>();
        for (File file : files) {
            list.add(new FormProperties(formDirectory, srcDirectory, genPackage, file));
        }

        return list;
    }

    private List<Grid> getGrids(List<Element> elements) {
        List<Grid> grids = new ArrayList<>();
        for (Element element : elements) {
            if (element instanceof Grid) {
                grids.add((Grid) element);
            }
        }

        return grids;
    }

    /**
     * Scrive le variabili
     *
     * @param stringBuilder
     * @param elements
     * @throws IOException
     */
    private void writeVariabili(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            String className = StringUtil.toJavaClassName(element.getName());
            String objectName = StringUtil.toJavaObjectName(element.getName());

            stringBuilder.append("  private " + className + " " + objectName + ";\n");
        }
    }

    /**
     * Scrive il codice relativo all'aggiunta degli elementi alla form
     *
     * @param stringBuilder
     * @param elements
     * @throws IOException
     */
    private static final void writeAddChild(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            String className = StringUtil.toJavaClassName(element.getName());
            String objectName = StringUtil.toJavaObjectName(element.getName());

            stringBuilder.append("    addChild(" + objectName + " = new " + className + "());\n");
        }
    }

    /**
     * Scrive i Getter e i Setter
     *
     * @param stringBuilder
     * @param elements
     * @throws IOException
     */
    public static final void writeGetter(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            String className = StringUtil.toJavaClassName(element.getName());
            String objectName = StringUtil.toJavaObjectName(element.getName());

            stringBuilder.append("  public " + className + " get" + className + " () {\n");
            stringBuilder.append("    return " + objectName + ";\n");
            stringBuilder.append("  }\n");
            stringBuilder.append("\n");
        }
    }

    public void write() throws JAXBException, IOException {
        getLog().info("Form: ");

        List<FormProperties> formPropertiesList = scan();
        for (FormProperties formProperties : formPropertiesList) {
            getLog().info("  " + formProperties.getFormClassName());

            JAXBContext jaxbContext = JAXBContext.newInstance(Form.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Form form;
            try (InputStream inputStream = new FileInputStream(formProperties.getInputFile()); Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                form = (Form) jaxbUnmarshaller.unmarshal(inputStream);
            }

            writeForm(formProperties, form);
            writeController(formProperties, form);
        }
    }

    public void writeForm(FormProperties formProperties, Form form) throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("package " + formProperties.getFormPackageName() + ";\n")
                .append("\n")
                .append("import java.math.BigDecimal;\n")
                .append("import java.sql.Timestamp;\n")
                .append("import java.util.List;\n")
                .append("\n")
                .append("import " + genPackage + ".bean.tablebean.*;\n")
                .append("import " + genPackage + ".bean.viewbean.*;\n")
                .append("\n")
                .append("import it.eg.sloth.framework.common.casting.DataTypes;\n")
                .append(" import it.eg.sloth.db.datasource.DataSource;\n")
                .append("import it.eg.sloth.db.datasource.DataTable;\n")
                .append("import it.eg.sloth.form.Form;\n")
                .append("import it.eg.sloth.framework.pageinfo.ViewModality;\n")
                .append("import it.eg.sloth.form.fields.Fields;\n")
                .append("import it.eg.sloth.form.fields.field.SimpleField;\n")
                .append("import it.eg.sloth.form.dwh.rollup.Rollup;\n")
                .append("import it.eg.sloth.form.dwh.Attribute;\n")
                .append("import it.eg.sloth.form.dwh.Level;\n")
                .append("import it.eg.sloth.form.dwh.Measure;\n")
                .append("import it.eg.sloth.form.base.Element;\n")
                .append("import it.eg.sloth.form.chart.SimpleChart;\n")
                .append("import it.eg.sloth.form.chart.element.Series;\n")
                .append("import it.eg.sloth.form.chart.element.Labels;\n")
                .append("import it.eg.sloth.form.fields.field.impl.*;\n")
                .append("import it.eg.sloth.jaxb.form.LegendPosition;\n")
                .append("import it.eg.sloth.form.skipper.Skipper;\n")
                .append("import it.eg.sloth.form.tabsheet.Tab;\n")
                .append("import it.eg.sloth.form.tabsheet.TabSheet;\n")
                .append("import it.eg.sloth.form.grid.Grid;\n")
                .append("import it.eg.sloth.jaxb.form.DataType;\n")
                .append("import it.eg.sloth.jaxb.form.ForceCase;\n")
                .append("import it.eg.sloth.jaxb.form.ButtonType;\n")
                .append("import it.eg.sloth.jaxb.form.ChartType;\n")
                .append("\n")
                .append("public class " + formProperties.getFormClassName() + " extends Form {\n")
                .append("\n")
                .append("  public static final String TITLE = " + (form.getTitle() == null ? "null" : "\"" + form.getTitle() + "\"") + ";\n")
                .append("\n");

        // Write variabili
        writeVariabili(stringBuilder, form.getFieldsOrGridOrTabSheet());

        // Write Costruttore
        stringBuilder.append("\n");
        stringBuilder.append("  public " + formProperties.getFormClassName() + "() {\n");
        stringBuilder.append("    this(TITLE);\n");
        stringBuilder.append("  }\n");
        stringBuilder.append("\n");
        stringBuilder.append("  public " + formProperties.getFormClassName() + "(String title) {\n");
        stringBuilder.append("    super(title);\n");

        writeAddChild(stringBuilder, form.getFieldsOrGridOrTabSheet());

        stringBuilder.append("  }\n");
        stringBuilder.append("\n");

        // Getter e Setter
        writeGetter(stringBuilder, form.getFieldsOrGridOrTabSheet());

        // SubClass
        FieldsFactory.write(stringBuilder, form.getFieldsOrGridOrTabSheet());
        GridFactory.write(stringBuilder, form.getFieldsOrGridOrTabSheet());
        RollupFactory.write(stringBuilder, form.getFieldsOrGridOrTabSheet());
        TabSheetFactory.write(stringBuilder, form.getFieldsOrGridOrTabSheet());
        SkipperFactory.write(stringBuilder, form.getFieldsOrGridOrTabSheet());
        ChartFactory.write(stringBuilder, form.getFieldsOrGridOrTabSheet());

        stringBuilder.append("\n");
        stringBuilder.append("}\n");

        GenUtil.writeFile(formProperties.getFormClassFile(), stringBuilder.toString());
    }

    public void writeController(FormProperties formProperties, Form form) throws IOException {
        List<Grid> grids = getGrids(form.getFieldsOrGridOrTabSheet());

        if (PageType.EDITABLE_GRID.equals(form.getPageType()) && grids.isEmpty()) {
            log.error(formProperties.getFormClassName() + ": impossibile trovare una grid!");
            return;
        }

        if (PageType.EDITABLE_MASTER_DETAIL.equals(form.getPageType()) && grids.isEmpty()) {
            log.error(formProperties.getFormClassName() + ": impossibile trovare una grid!");
            return;
        }

        String parentController = "SimplePage<" + formProperties.getFormClassName() + ">";
        if (PageType.PROJECT_PAGE.equals(form.getPageType())) {
            parentController = "ProjectPage<" + formProperties.getFormClassName() + ">";
        } else if (PageType.PROJECT_MASTER_DETAIL_PAGE.equals(form.getPageType())) {
            parentController = "ProjectMasterDetailPage<" + formProperties.getFormClassName() + ", " + formProperties.getFormClassName() + "." + grids.get(0).getName() + ">";
        } else if (PageType.WEB_PAGE.equals(form.getPageType())) {
            parentController = "WebSimplePage<" + formProperties.getFormClassName() + ">";
        } else if (PageType.EDITABLE_PAGE.equals(form.getPageType())) {
            parentController = "EditablePage<" + formProperties.getFormClassName() + ">";
        } else if (PageType.SIMPLE_SEARCH.equals(form.getPageType())) {
            parentController = "SimpleSearchPage<" + formProperties.getFormClassName() + ">";
        } else if (PageType.EDITABLE_GRID.equals(form.getPageType())) {
            parentController = "EditableGridPage<" + formProperties.getFormClassName() + ", " + formProperties.getFormClassName() + "." + grids.get(0).getName() + ">";
        } else if (PageType.MASTER_DETAIL.equals(form.getPageType())) {
            parentController = "MasterDetailPage<" + formProperties.getFormClassName() + ", " + formProperties.getFormClassName() + "." + grids.get(0).getName() + ">";
        } else if (PageType.EDITABLE_MASTER_DETAIL.equals(form.getPageType())) {
            parentController = "EditableMasterDetailPage<" + formProperties.getFormClassName() + ", " + formProperties.getFormClassName() + "." + grids.get(0).getName() + ">";
        } else if (PageType.REPORT.equals(form.getPageType())) {
            parentController = "ReportGridPage<" + formProperties.getFormClassName() + ">";
        } else if (PageType.JSON.equals(form.getPageType())) {
            parentController = "FormJson<" + formProperties.getFormClassName() + ">";
        }

        StringBuilder stringBuilder = new StringBuilder()
                .append("package " + formProperties.getControllerPackageName() + ";\n")
                .append("\n")
                .append("import " + genPackage + ".bean.tablebean.*;\n")
                .append("import " + genPackage + ".bean.viewbean.*;\n")
                .append("\n")
                .append("import it.eg.sloth.form.NavigationConst;\n")
                .append("import it.eg.sloth.webdesktop.controller.page.EditableGridPage;\n")
                .append("import it.eg.sloth.webdesktop.controller.page.ReportGridPage;\n")
                .append("import it.eg.sloth.webdesktop.controller.page.MasterDetailPage;\n")
                .append("import it.eg.sloth.webdesktop.controller.page.SimplePage;\n")
                .append("import it.eg.sloth.webdesktop.controller.page.EditableMasterDetailPage;\n")
                .append("import it.eg.sloth.webdesktop.controller.webpage.WebSimplePage;\n")
                .append("import it.eg.sloth.webdesktop.controller.page.EditablePage;\n")
                .append("import " + formProperties.getFormFullClassName() + ";\n")
                .append("import lombok.extern.slf4j.Slf4j;\n")
                .append("\n")
                .append("@Slf4j\n")
                .append("public abstract class " + formProperties.getControlleClassName() + " extends " + parentController + " {\n")
                .append("\n")
                .append("  @Override\n")
                .append("  public " + formProperties.getFormClassName() + " createForm() {\n")
                .append("    return new " + formProperties.getFormClassName() + "();\n")
                .append("  }\n")
                .append("\n");

        if (!PageType.JSON.equals(form.getPageType())) {
            if (PageType.PROJECT_MASTER_DETAIL_PAGE.equals(form.getPageType()) || PageType.EDITABLE_GRID.equals(form.getPageType()) || PageType.MASTER_DETAIL.equals(form.getPageType()) || PageType.EDITABLE_MASTER_DETAIL.equals(form.getPageType())) {
                stringBuilder.append("  @Override\n");
                stringBuilder.append("  public " + formProperties.getFormClassName() + "." + grids.get(0).getName() + " getGrid() {\n");
                stringBuilder.append("    return getForm().get" + grids.get(0).getName() + "();\n");
                stringBuilder.append("  }\n");
                stringBuilder.append("\n");

                if (!BaseFunction.isBlank(grids.get(0).getTable())) {
                    String tableName = grids.get(0).getTable().substring(0, 1).toUpperCase() + grids.get(0).getTable().substring(1).toLowerCase();

                    stringBuilder.append("  public " + tableName + "RowBean getRowBean() {\n");
                    stringBuilder.append("    return getGrid().getDataSource().getRow();\n");
                    stringBuilder.append("  }\n");
                    stringBuilder.append("\n");
                }
            }

            stringBuilder.append("  @Override\n");
            stringBuilder.append("  public boolean defaultNavigation() throws Exception {\n");
            stringBuilder.append("    if (super.defaultNavigation()) {\n");
            stringBuilder.append("      return true;\n");
            stringBuilder.append("    }\n");
            stringBuilder.append("\n");
            stringBuilder.append("    String navigation[] = getWebRequest().getNavigation();\n");
            stringBuilder.append("    if (navigation.length == 2) {\n");

            for (Element element : form.getFieldsOrGridOrTabSheet()) {
                if (element instanceof Fields) {
                    Fields fields = (Fields) element;

                    for (Element element2 : fields.getTextOrInputOrTextArea()) {
                        if (element2 instanceof Button) {
                            Button button = (Button) element2;
                            String fieldsClassName = StringUtil.toJavaClassName(fields.getName());
                            String btnConstantName = StringUtil.toJavaConstantName(button.getName());
                            String btnObjectName = StringUtil.toJavaObjectName(button.getName());

                            stringBuilder.append("      if (NavigationConst.BUTTON.equals(navigation[0]) && " + formProperties.getFormClassName() + "." + fieldsClassName + "._" + btnConstantName + ".equalsIgnoreCase(navigation[1])) {\n");
                            stringBuilder.append("        log.info(\"PRESS: " + btnObjectName + "\");\n");
                            stringBuilder.append("        " + btnObjectName + "Pressed();\n");
                            stringBuilder.append("        return true;\n");
                            stringBuilder.append("      }\n");
                        }
                    }
                }
            }

            stringBuilder.append("    }\n");
            stringBuilder.append("\n");

            stringBuilder.append("    if (navigation.length == 3) {\n");
            for (Element element : form.getFieldsOrGridOrTabSheet()) {
                if (element instanceof Grid) {
                    Grid grid = (Grid) element;

                    for (Element element2 : grid.getTextOrInputOrTextArea()) {
                        if (element2 instanceof Button) {
                            Button button = (Button) element2;
                            String fieldsClassName = StringUtil.toJavaClassName(grid.getName());
                            String btnConstantName = StringUtil.toJavaConstantName(button.getName());
                            String btnObjectName = StringUtil.toJavaObjectName(button.getName());

                            stringBuilder.append("      if (NavigationConst.BUTTON.equals(navigation[0]) && " + formProperties.getFormClassName() + "." + fieldsClassName + "._" + btnConstantName + ".equalsIgnoreCase(navigation[1])) {\n");
                            stringBuilder.append("        log.info(\"PRESS: " + btnObjectName + "\");\n");
                            stringBuilder.append("        " + btnObjectName + "Pressed(new Integer(navigation[2]));\n");
                            stringBuilder.append("        return true;\n");
                            stringBuilder.append("      }\n");
                        }
                    }
                }
            }

            stringBuilder.append("    }\n");
            stringBuilder.append("\n");

            stringBuilder.append("    return false;\n");
            stringBuilder.append("  }\n");
            stringBuilder.append("\n");

            for (Element element : form.getFieldsOrGridOrTabSheet()) {
                if (element instanceof Fields) {
                    Fields fields = (Fields) element;

                    for (Element element2 : fields.getTextOrInputOrTextArea()) {
                        if (element2 instanceof Button) {
                            Button button = (Button) element2;
                            String btnObjectName = StringUtil.toJavaObjectName(button.getName());

                            stringBuilder.append("  public abstract void " + btnObjectName + "Pressed() throws Exception;\n");
                        }
                    }
                }
            }

            for (Element element : form.getFieldsOrGridOrTabSheet()) {
                if (element instanceof Grid) {
                    Grid grid = (Grid) element;

                    for (Element element2 : grid.getTextOrInputOrTextArea()) {
                        if (element2 instanceof Button) {
                            Button button = (Button) element2;
                            String btnObjectName = StringUtil.toJavaObjectName(button.getName());

                            stringBuilder.append("  public abstract void " + btnObjectName + "Pressed(int rowIndex) throws Exception;\n");
                        }
                    }
                }
            }
        }

        stringBuilder.append("\n");
        stringBuilder.append("}\n");

        GenUtil.writeFile(formProperties.getControllerClassFile(), stringBuilder.toString());
    }
}
