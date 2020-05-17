package it.eg.sloth.mavenplugin.writer.spring;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.framework.common.base.TimeStampUtil;
import it.eg.sloth.framework.common.casting.DataTypes;
import it.eg.sloth.framework.common.exception.BusinessException;
import it.eg.sloth.mavenplugin.common.GenUtil;
import it.eg.sloth.mavenplugin.common.files.DirectoryFilter;
import it.eg.sloth.mavenplugin.common.files.ExtensionFilter;
import it.eg.sloth.mavenplugin.writer.spring.model.ControllerProperties;
import it.eg.sloth.mavenplugin.writer.spring.model.JspProperties;

/**
 * Project: sloth-framework
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
public class SpringControllerWriter {

    File javaDirectory;
    File webappDirectory;
    String controllerPackage;
    File outputJavaDirectory;
    String genPackage;

    MavenProject project;
    Log log;

    File constant;

    public SpringControllerWriter(File javaDirectory, File webappDirectory, String controllerPackage,  File outputJavaDirectory, String genPackage, MavenProject project, Log log) {
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
        File pathToScan = new File(webappDirectory.getAbsolutePath() + "/jsp");

        Collection<File> files = FileUtils.listFiles(pathToScan, new ExtensionFilter(".jsp"), new DirectoryFilter());
        List<JspProperties> list = new ArrayList<>();
        for (File file : files) {
            list.add(new JspProperties(pathToScan, file));
        }

        return list;
    }

    public void write() throws IOException, JAXBException, BusinessException {
        List<ControllerProperties> controllerPropertiesList = scanController();
        List<JspProperties> jspPropertiesList = scanJsp();

        // Controller
        log.info("Controller: ");
        for (ControllerProperties properties : controllerPropertiesList) {
            log.info("  " + properties.getOutputClassName());
            writeController(properties);
        }

        // Constant
        log.info("Constant");
        writeConstant(controllerPropertiesList, jspPropertiesList);
    }

    public void writeController(ControllerProperties properties) throws IOException {
        StringBuilder stringBuilder = new StringBuilder()
                .append("package " + properties.getOutputPackageName() + ";\n")
                .append("\n")
                .append("import javax.servlet.http.HttpServletRequest;\n")
                .append("import javax.servlet.http.HttpServletResponse;\n")
                .append("\n")
                .append("import org.springframework.web.bind.annotation.RequestMapping;\n")
                .append("import org.springframework.web.servlet.ModelAndView;\n")
                .append("import org.springframework.stereotype.Controller;\n")
                .append("import springfox.documentation.annotations.ApiIgnore;\n")
                .append("import " + controllerPackage + properties.getInputRelativePackage() + "." + properties.getInputClassName() + ";\n")
                .append("\n")
                .append("@Controller\n")
                .append("@ApiIgnore\n")
                .append("public class " + properties.getOutputClassName() + " {\n")
                .append("  \n")
                .append("  @RequestMapping(\"/html/" + properties.getInputClassName() + ".html\")\n")
                .append("  public ModelAndView handleRequest(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {\n")
                .append("    return new " + properties.getInputClassName() + "().handleRequest(arg0, arg1);\n")
                .append("  }\n")
                .append("\n")
                .append("}\n");

        GenUtil.writeFile(properties.getOutputClassFile(), stringBuilder.toString());
    }

    public void writeConstant(List<ControllerProperties> controllerPropertiesList, List<JspProperties> jspPropertiesList) throws IOException, JAXBException, BusinessException {
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