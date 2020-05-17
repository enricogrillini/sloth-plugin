package it.eg.sloth.mavenplugin.writer.spring.model;


import it.eg.sloth.mavenplugin.common.GenUtil;
import lombok.Getter;
import lombok.ToString;

import java.io.File;

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
 *
 */
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
