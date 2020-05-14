package it.eg.sloth.mavenplugin.writer.form.model;


import it.eg.sloth.mavenplugin.common.GenUtil;
import lombok.Getter;
import lombok.ToString;

import java.io.File;

@Getter
@ToString
public class FormProperties {

    String formGenPackageName;
    static final String FORM_SUFFIX = "Form";

    String controllerGenPackageName;
    static final String CONTROLLER_SUFFIX = "AbstractPage";

    File inputFile;

    String formClassName;
    String formFullClassName;
    String formPackageName;
    File formClassFile;

    String controlleClassName;
    String controlleFullClassName;
    String controllerPackageName;
    File controllerClassFile;

    public FormProperties(File formDirectory, File srcDirectory, String genPackage, File inputFile) {
        this.inputFile = inputFile;

        // Calcolo il relative package
        String relativePackage = "";
        for (File file = inputFile.getParentFile(); !formDirectory.equals(file); file = file.getParentFile()) {
            relativePackage = "." + file.getName() + relativePackage;
        }

        formGenPackageName = genPackage + ".form";
        controllerGenPackageName = genPackage + ".controllerBaseLogic";

        // Form
        formClassName = GenUtil.removeExension(inputFile) + FORM_SUFFIX;
        formFullClassName = formGenPackageName + relativePackage + "." + formClassName;
        formPackageName = formGenPackageName + relativePackage;
        formClassFile = GenUtil.getClassFile(srcDirectory, formPackageName, formClassName);

        // Controller
        controlleClassName = GenUtil.removeExension(inputFile) + CONTROLLER_SUFFIX;
        controlleFullClassName = controllerGenPackageName + relativePackage + "." + controlleClassName;
        controllerPackageName = controllerGenPackageName + relativePackage;
        controllerClassFile = GenUtil.getClassFile(srcDirectory, controllerPackageName, controlleClassName);
    }


}
