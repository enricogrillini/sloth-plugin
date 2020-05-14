package it.eg.sloth.mavenplugin.writer.spring.model;


import it.eg.sloth.mavenplugin.common.GenUtil;
import lombok.Getter;
import lombok.ToString;

import java.io.File;

@Getter
@ToString
public class JspProperties {

    public static final String CONTROLLER_GEN_PACKAGE_NAME = "it.eg.sloth.gen.controller";

    File inputFile;

    String jspName;
    String jspRelativeName;

    public JspProperties(File basePath, File inputFile) {
        this.inputFile = inputFile;

        // Calcolo il relative package
        jspRelativeName = "";
        for (File file = inputFile.getParentFile(); !basePath.equals(file); file = file.getParentFile()) {
            jspRelativeName = file.getName() + "/" + jspRelativeName;
        }
        jspRelativeName += GenUtil.removeExension(inputFile);

        jspName = GenUtil.removeExension(inputFile);
    }

}
