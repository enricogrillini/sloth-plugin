package it.eg.sloth.mavenplugin.writer.bean.oraclebean;

import it.eg.sloth.jaxb.dbschema.Table;
import it.eg.sloth.mavenplugin.common.GenUtil;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
public class VelocityTableBeanWriter {
    private static final String DECODE_BEAN = ".bean.decode";
    private static final String TABLE_BEAN = ".bean.tablebean";

    private static final String TABLE_BEAN_TEMPLATE = "/templates/tableBeanTemplate.java";

    File outputJavaDirectory;
    String genPackage;
    Table table;

    VelocityEngine velocityEngine;
    Template tableBeanTemplate;
    VelocityContext velocityContext;

    public VelocityTableBeanWriter(File outputJavaDirectory, String genPackage) {
        this.outputJavaDirectory = outputJavaDirectory;
        this.genPackage = genPackage;

        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        velocityEngine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        tableBeanTemplate = velocityEngine.getTemplate(TABLE_BEAN_TEMPLATE);
    }

    public void write(Table table) throws IOException {
        this.table = table;

        // TableBean properties
        String tableBeanClassName = GenUtil.initCap(table.getName()) + "TableBean";
        String tableBeanFullClassName = genPackage + TABLE_BEAN + "." + tableBeanClassName;
        String tableBeanPackageName = genPackage + TABLE_BEAN;
        File tableBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, tableBeanPackageName, tableBeanClassName);

        String rowBeanClassName = GenUtil.initCap(table.getName()) + "RowBean";
        String rowBeanFullClassName = genPackage + TABLE_BEAN + "." + rowBeanClassName;
        String rowBeanPackageName = genPackage + TABLE_BEAN;
        File rowBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, rowBeanPackageName, rowBeanClassName);

        String decodeBeanClassName = GenUtil.initCap(table.getName()) + "DecodeBean";
        String decodeBeanFullClassName = genPackage + DECODE_BEAN + "." + rowBeanClassName;
        String decodeBeanPackageName = genPackage + DECODE_BEAN;
        File decodeBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, decodeBeanPackageName, decodeBeanClassName);

        velocityContext = new VelocityContext();
        velocityContext.put("tableBeanClassName", tableBeanClassName);
        velocityContext.put("rowBeanClassName", rowBeanClassName);
        velocityContext.put("rowBeanObjectName", GenUtil.initLow(rowBeanClassName));
        velocityContext.put("tableName", table.getName().toUpperCase());
        velocityContext.put("tableBeanPackageName", tableBeanPackageName);

        // Scrivo il table Bean
        writeTableBean(tableBeanClassFile);
    }

    private void writeTableBean(File tableBeanClassFile) throws IOException {
        try (FileWriter fileWriter = new FileWriter(tableBeanClassFile)) {
            tableBeanTemplate.merge(velocityContext, fileWriter);
        }
    }


}
