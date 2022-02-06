package it.eg.sloth.mavenplugin.writer.spring;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.framework.common.base.TimeStampUtil;
import it.eg.sloth.framework.common.casting.DataTypes;
import it.eg.sloth.framework.common.exception.FrameworkException;
import it.eg.sloth.mavenplugin.common.GenUtil;
import it.eg.sloth.mavenplugin.common.files.DirectoryFilter;
import it.eg.sloth.mavenplugin.common.files.ExtensionFilter;
import it.eg.sloth.mavenplugin.writer.spring.model.ControllerProperties;
import it.eg.sloth.mavenplugin.writer.spring.model.JspProperties;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;

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
public class SpringControllerWriter {

    private static final String START_CONTROLLER = "" +
            "package {0};\n" +
            "\n" +
            "import javax.servlet.http.HttpServletRequest;\n" +
            "import javax.servlet.http.HttpServletResponse;\n" +
            "\n" +
            "import org.springframework.web.bind.annotation.RequestMapping;\n" +
            "import org.springframework.web.servlet.ModelAndView;\n" +
            "import org.springframework.stereotype.Controller;\n" +
            "import {1}.*;\n" +
            "\n" +
            "@Controller\n" +
            "public class {2} '{'\n" +
            "\n";

    private static final String BODY_CONTROLLER = "" +
            "  @RequestMapping(\"/html/{0}.html\")\n" +
            "  public ModelAndView handle{0}(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception '{'\n" +
            "    return new {0}().handleRequest(arg0, arg1);\n" +
            "  '}'\n" +
            "\n";

    private static final String END_CONTROLLER = "}\n";


    File javaDirectory;
    File webappDirectory;
    String controllerPackage;
    File outputJavaDirectory;
    String genPackage;

    MavenProject project;
    Log log;

    File constant;

    public SpringControllerWriter(File javaDirectory, File webappDirectory, String controllerPackage, File outputJavaDirectory, String genPackage, MavenProject project, Log log) {
        this.javaDirectory = javaDirectory;
        this.webappDirectory = webappDirectory;
        this.controllerPackage = controllerPackage;
        this.outputJavaDirectory = outputJavaDirectory;
        this.genPackage = genPackage;
        this.log = log;
        this.project = project;

        constant = new File(outputJavaDirectory.getAbsolutePath() + GenUtil.UNIX_PATH_DELIMITER + genPackage.replace('.', GenUtil.UNIX_PATH_DELIMITER) + "/Constant.java");
    }

    /**
     * Scansiona la cartella formDirectory e ritorna la lista delle form
     *
     * @return
     */
    private List<ControllerProperties> scanController() {
        File pathToScan = new File(javaDirectory.getAbsolutePath() + GenUtil.UNIX_PATH_DELIMITER + controllerPackage.replace('.', GenUtil.UNIX_PATH_DELIMITER));

        Collection<File> files = FileUtils.listFiles(pathToScan, new ExtensionFilter(".java"), new DirectoryFilter());
        List<ControllerProperties> list = new ArrayList<>();
        for (File file : files) {
            list.add(new ControllerProperties(pathToScan, outputJavaDirectory, genPackage, file));
        }

        return list;
    }

    private List<JspProperties> scanJsp() {
        File pathToScan = new File(webappDirectory.getAbsolutePath() + "/WEB-INF/views");

        Collection<File> files = FileUtils.listFiles(pathToScan, new ExtensionFilter(".jsp"), new DirectoryFilter());
        List<JspProperties> list = new ArrayList<>();
        for (File file : files) {
            list.add(new JspProperties(pathToScan, file));
        }

        return list;
    }

    public void write() throws IOException, FrameworkException {
        List<ControllerProperties> controllerPropertiesList = scanController();
        List<JspProperties> jspPropertiesList = scanJsp();

        // Controller
        log.info("  Controller");

        // Costruisco una mappa che aggregi i controller per package di appartenenza
        Map<String, List<ControllerProperties>> controllers = new HashMap<>();
        for (ControllerProperties properties : controllerPropertiesList) {
            if (!controllers.containsKey(properties.getNewOutputClassName())) {
                controllers.put(properties.getNewOutputClassName(), new ArrayList<>());
            }

            controllers.get(properties.getNewOutputClassName()).add(properties);
        }

        // Scrivo i controller
        for (List<ControllerProperties> propertiesList : controllers.values()) {
            writeControllers(propertiesList);
        }

        // Constant
        log.info("  Constant");
        writeConstant(controllerPropertiesList, jspPropertiesList);
    }

    public void writeControllers(List<ControllerProperties> propertiesList) throws IOException {
        ControllerProperties first = propertiesList.get(0);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(MessageFormat.format(START_CONTROLLER, first.getNewOutputPackageName(), controllerPackage + first.getInputRelativePackage(), first.getNewOutputClassName()));

        for (ControllerProperties properties : propertiesList) {
            stringBuilder.append(MessageFormat.format(BODY_CONTROLLER, properties.getInputClassName() ));
        }

        stringBuilder.append(END_CONTROLLER);

        GenUtil.writeFile(first.getNewOutputClassFile(), stringBuilder.toString());
    }

    public void writeConstant(List<ControllerProperties> controllerPropertiesList, List<JspProperties> jspPropertiesList) throws IOException, FrameworkException {
        Timestamp date = TimeStampUtil.sysdate();
        String version = project.getVersion();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("package " + genPackage + ";\n");
        stringBuilder.append("\n");
        stringBuilder.append("import java.sql.Timestamp;\n");
        stringBuilder.append("import it.eg.sloth.framework.common.base.TimeStampUtil;\n");
        stringBuilder.append("import it.eg.sloth.framework.common.casting.DataTypes;\n");
        stringBuilder.append("\n");
        stringBuilder.append("public class Constant {\n");
        stringBuilder.append("\n");
        stringBuilder.append("  public static final String VERSION = \"" + version + "\";\n");
        stringBuilder.append("  public static final String DATE = \"" + DataTypes.DATETIME.formatText(date, Locale.ITALY) + "\";\n");
        stringBuilder.append("\n");

        // Jsp
        stringBuilder.append("  public class Jsp {\n");
        for (JspProperties jspProperties : jspPropertiesList) {
            String jspConstantName = StringUtil.toJavaConstantName(jspProperties.getJspName());

            stringBuilder.append("    public static final String " + jspConstantName + " = \"" + jspProperties.getJspRelativeName() + "\";\n");
        }
        stringBuilder.append("  }\n");
        stringBuilder.append("\n");

        // Page
        stringBuilder.append("  public class Page {\n");
        for (ControllerProperties properties : controllerPropertiesList) {
            String className = properties.getInputClassName();

            if (className.endsWith("Json")) {
                stringBuilder.append("    public static final String " + StringUtil.toJavaConstantName(className) + " = \"" + className + ".json\";\n");
            } else {
                stringBuilder.append("    public static final String " + StringUtil.toJavaConstantName(className) + " = \"" + className + ".html\";\n");
            }
        }
        stringBuilder.append("  }\n");

        stringBuilder.append("}\n");

        GenUtil.writeFile(constant, stringBuilder.toString());
    }
}
