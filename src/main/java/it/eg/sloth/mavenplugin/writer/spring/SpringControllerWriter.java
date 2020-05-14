package it.eg.sloth.mavenplugin.writer.spring;


import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.framework.common.base.TimeStampUtil;
import it.eg.sloth.framework.common.casting.DataTypes;
import it.eg.sloth.framework.common.exception.BusinessException;
import it.eg.sloth.jaxb.config.Configuration;
import it.eg.sloth.jaxb.config.Group;
import it.eg.sloth.jaxb.config.Parameter;
import it.eg.sloth.mavenplugin.common.GenUtil;
import it.eg.sloth.mavenplugin.common.files.DirectoryFilter;
import it.eg.sloth.mavenplugin.common.files.ExtensionFilter;
import it.eg.sloth.mavenplugin.writer.spring.model.ControllerProperties;
import it.eg.sloth.mavenplugin.writer.spring.model.JspProperties;


public class SpringControllerWriter {

    File javaDirectory;
    File webappDirectory;
    String controllerPackage;
    File systemXml;
    File outputJavaDirectory;
    String genPackage;

    MavenProject project;
    Log log;

    File appConfig;
    File constant;

    public SpringControllerWriter(File javaDirectory, File webappDirectory, String controllerPackage, File systemXml, File outputJavaDirectory, String genPackage, MavenProject project, Log log) {
        this.javaDirectory = javaDirectory;
        this.webappDirectory = webappDirectory;
        this.controllerPackage = controllerPackage;
        this.systemXml = systemXml;
        this.outputJavaDirectory = outputJavaDirectory;
        this.genPackage = genPackage;
        this.log = log;
        this.project = project;

        appConfig = new File(outputJavaDirectory.getAbsolutePath() + GenUtil.UNIX_PATH_DELIMITER + genPackage.replace('.', GenUtil.UNIX_PATH_DELIMITER) + "/AppConfig.java");
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
            log.info("  " + properties.getControllerClassName());
            writeController(properties);
        }

        // AppConfig
        log.info("AppConfig");
        writeAppConfig(controllerPropertiesList);

        // Constant
        log.info("Constant");
        writeConstant(controllerPropertiesList, jspPropertiesList);


    }

    public void writeController(ControllerProperties properties) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("package " + properties.getControllerPackageName() + ";\n");
        stringBuilder.append("\n");
        stringBuilder.append("import javax.servlet.http.HttpServletRequest;\n");
        stringBuilder.append("import javax.servlet.http.HttpServletResponse;\n");
        stringBuilder.append("\n");
        stringBuilder.append("import org.springframework.web.servlet.ModelAndView;\n");
        stringBuilder.append("import org.springframework.web.servlet.mvc.Controller;\n");
        stringBuilder.append("\n");
        stringBuilder.append("public class " + properties.getControllerClassName() + " implements Controller {\n");
        stringBuilder.append("  \n");
        stringBuilder.append("  @Override\n");
        stringBuilder.append("  public ModelAndView handleRequest(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {\n");
        stringBuilder.append("    return new " + controllerPackage + properties.getControllerRelativePackage() + "." + properties.getControllerClassName() + "().handleRequest(arg0, arg1);\n");
        stringBuilder.append("  }\n");
        stringBuilder.append("\n");
        stringBuilder.append("}\n");

        GenUtil.writeFile(properties.getControllerClassFile(), stringBuilder.toString());
    }

    public void writeAppConfig(List<ControllerProperties> controllerPropertiesList) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("package " + genPackage + ";\n");
        stringBuilder.append("\n");
        stringBuilder.append("import java.util.HashMap;\n");
        stringBuilder.append("import java.util.Map;\n");
        stringBuilder.append("import org.springframework.context.annotation.Bean;\n");
        stringBuilder.append("import org.springframework.context.annotation.Configuration;\n");
        stringBuilder.append("import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;\n");
        stringBuilder.append("\n");
        stringBuilder.append("@Configuration\n");
        stringBuilder.append("public class AppConfig {\n");

        stringBuilder.append("  @Bean\n");
        stringBuilder.append("  public org.springframework.web.servlet.handler.SimpleUrlHandlerMapping urlHandler () {\n");
        stringBuilder.append("    SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();\n");
        stringBuilder.append("    Map<String, Object> urlMap = new HashMap<String, Object>();\n");
        for (ControllerProperties properties : controllerPropertiesList) {
            String objectName = StringUtil.toJavaObjectName(properties.getControllerClassName());
            stringBuilder.append("    urlMap.put(\"html/" + properties.getControllerClassName() + ".html\", " + objectName + "());\n");
            stringBuilder.append("    urlMap.put(\"" + properties.getControllerClassName() + ".html\", " + objectName + "());\n");
            stringBuilder.append("\n");
        }

        stringBuilder.append("    mapping.setUrlMap(urlMap);\n");
        stringBuilder.append("    return mapping;\n");
        stringBuilder.append("  }\n");


        for (ControllerProperties properties : controllerPropertiesList) {
            String objectName = StringUtil.toJavaObjectName(properties.getControllerClassName());

//            stringBuilder.append("  @Bean(id = \"" + properties.getControllerClassName() +  "\" name = \"/html/" + properties.getControllerClassName() + ".html\")\n");
            stringBuilder.append("  @Bean\n");
            stringBuilder.append("  public " + properties.getControllerFullClassName() + " " + objectName + "() {\n");
            stringBuilder.append("    return new " + properties.getControllerFullClassName() + "();\n");
            stringBuilder.append("  }\n");
            stringBuilder.append("\n");
        }

        stringBuilder.append("  @Bean()\n");
        stringBuilder.append("  public  org.springframework.web.servlet.view.InternalResourceViewResolver viewResolver () {\n");
        stringBuilder.append("      org.springframework.web.servlet.view.InternalResourceViewResolver  internalResourceViewResolver = new org.springframework.web.servlet.view.InternalResourceViewResolver();\n");
        stringBuilder.append("\n");
        stringBuilder.append("      internalResourceViewResolver.setPrefix(\"/jsp/\");\n");
        stringBuilder.append("      internalResourceViewResolver.setSuffix(\".jsp\");\n");
        stringBuilder.append("\n");
        stringBuilder.append("      return internalResourceViewResolver;\n");
        stringBuilder.append("  }\n");

        stringBuilder.append("}\n");

        GenUtil.writeFile(appConfig, stringBuilder.toString());
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
            String className = properties.getControllerClassName();

            if (className.endsWith("Json")) {
                stringBuilder.append("    public static final String " + StringUtil.toJavaConstantName(className) + " = \"" + className + ".json\";\n");
            } else {
                stringBuilder.append("    public static final String " + StringUtil.toJavaConstantName(className) + " = \"" + className + ".html\";\n");
            }
        }
        stringBuilder.append("  }\n");

        // Configuration
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Configuration configuration = (Configuration) jaxbUnmarshaller.unmarshal(systemXml);

        if (configuration.getParameters() != null) {
            stringBuilder.append("  public class Parameters {\n");
            for (Group group : configuration.getParameters().getGroup()) {
                stringBuilder.append("    public static final String " + StringUtil.toJavaConstantName(group.getName()) + " = \"" + group.getName() + "\";\n");
            }
            stringBuilder.append("\n");

            for (Group group : configuration.getParameters().getGroup()) {
                stringBuilder.append("    public class " + StringUtil.toJavaClassName(group.getName()) + " {\n");
                for (Parameter parameter : group.getParameter()) {
                    stringBuilder.append("      public static final String " + StringUtil.toJavaConstantName(parameter.getName()) + " = \"" + parameter.getName() + "\";\n");
                }
                stringBuilder.append("    }\n");
            }

            stringBuilder.append("  }\n");
        }

        stringBuilder.append("\n");
        stringBuilder.append("}\n");

        GenUtil.writeFile(constant, stringBuilder.toString());
    }
}
