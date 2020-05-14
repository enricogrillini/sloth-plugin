package it.eg.sloth.mavenplugin.writer.spring.model;


import it.eg.sloth.mavenplugin.common.GenUtil;
import lombok.Getter;
import lombok.ToString;

import java.io.File;

@Getter
@ToString
public class ControllerProperties {

    private String controllerGenPackageName;

    File inputFile;

    String controllerClassName;
    String controllerFullClassName;
    String controllerRelativePackage;
    String controllerPackageName;
    File controllerClassFile;

    public ControllerProperties(File basePath, File outputJavaDirectory, String genPackage, File inputFile) {
        this.inputFile = inputFile;

        // Calcolo il relative package
        controllerRelativePackage = "";
        for (File file = inputFile.getParentFile(); !basePath.equals(file); file = file.getParentFile()) {
            controllerRelativePackage = "." + file.getName() + controllerRelativePackage;
        }

        controllerGenPackageName = genPackage + ".controller";

        // Controller
        controllerClassName = GenUtil.removeExension(inputFile);
        controllerFullClassName = controllerGenPackageName + controllerRelativePackage + "." + controllerClassName;
        controllerPackageName = controllerGenPackageName + controllerRelativePackage;
        controllerClassFile = GenUtil.getClassFile(outputJavaDirectory, controllerPackageName, controllerClassName);
    }


}
