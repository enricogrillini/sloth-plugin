package it.eg.sloth.mavenplugin.writer.bean2;

import it.eg.sloth.dbmodeler.model.database.DataBaseType;
import it.eg.sloth.dbmodeler.model.schema.sequence.Sequence;
import it.eg.sloth.dbmodeler.model.schema.table.Table;
import it.eg.sloth.mavenplugin.common.GenUtil;
import it.eg.sloth.mavenplugin.writer.bean2.common.DbUtil;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

public class AbstractBeanWriter implements BeanWriter {

    private static final String TABLE_BEAN_TEMPLATE = "/templates/tableBeanTemplate.java";
    private static final String ROW_BEAN_TEMPLATE = "/templates/rowBeanTemplate.java";
    private static final String SEQUENCE_DAO_TEMPLATE = "/templates/{0}-sequenceDaoTemplate.java";

    private static final String DECODE_BEAN = ".bean.decode";
    private static final String TABLE_BEAN = ".bean.tablebean";
    private static final String DAO = ".dao";

    File outputJavaDirectory;
    String genPackage;

    VelocityEngine velocityEngine;
    Template tableBeanTemplate;
    Template rowBeanTemplate;
    Template sequenceDaoTemplate;

    @Getter
    DataBaseType dataBaseType;

    public AbstractBeanWriter(File outputJavaDirectory, String genPackage, DataBaseType dataBaseType) {
        this.outputJavaDirectory = outputJavaDirectory;
        this.genPackage = genPackage;
        this.dataBaseType = dataBaseType;

        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        velocityEngine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        tableBeanTemplate = velocityEngine.getTemplate(TABLE_BEAN_TEMPLATE);
        rowBeanTemplate = velocityEngine.getTemplate(ROW_BEAN_TEMPLATE);
        sequenceDaoTemplate = velocityEngine.getTemplate(MessageFormat.format(SEQUENCE_DAO_TEMPLATE, dataBaseType));
    }

    public void writeTable(Collection<Table> tableCollection) throws IOException {
        for (Table table : tableCollection) {
            write(table);
        }
    }

    public void write(Table table) throws IOException {
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

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("tableBeanClassName", tableBeanClassName);
        velocityContext.put("rowBeanClassName", rowBeanClassName);
        velocityContext.put("rowBeanObjectName", GenUtil.initLow(rowBeanClassName));
        velocityContext.put("tableName", table.getName().toUpperCase());
        velocityContext.put("tableBeanPackageName", tableBeanPackageName);

        velocityContext.put("table", table);

        velocityContext.put("DbUtil", DbUtil.class);
        velocityContext.put("GenUtil", GenUtil.class);

        // Table Bean
        FileUtils.forceMkdir(tableBeanClassFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(tableBeanClassFile)) {
            tableBeanTemplate.merge(velocityContext, fileWriter);
        }

        // Row Bean
        FileUtils.forceMkdir(rowBeanClassFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(rowBeanClassFile)) {
            rowBeanTemplate.merge(velocityContext, fileWriter);
        }
    }


    public void writeSequence(Collection<Sequence> sequenceCollection) throws IOException {
        // SequenceDao properties
        String sequencesDaoClassName = "SequencesDao";
        String sequencesDaoFullClassName = genPackage + DAO + "." + sequencesDaoClassName;
        String sequencesDaoPackageName = genPackage + DAO;
        File sequencesDaoClassFile = GenUtil.getClassFile(outputJavaDirectory, sequencesDaoPackageName, sequencesDaoClassName);


        VelocityContext velocityContext = new VelocityContext();
//        velocityContext.put("tableBeanClassName", tableBeanClassName);
//        velocityContext.put("rowBeanClassName", rowBeanClassName);
//        velocityContext.put("rowBeanObjectName", GenUtil.initLow(rowBeanClassName));
//        velocityContext.put("tableName", table.getName().toUpperCase());
        velocityContext.put("sequencesDaoPackageName", sequencesDaoPackageName);

        velocityContext.put("sequenceCollection", sequenceCollection);
//
//        velocityContext.put("DbUtil", DbUtil.class);
//        velocityContext.put("GenUtil", GenUtil.class);
//
//        // Table Bean
        FileUtils.forceMkdir(sequencesDaoClassFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(sequencesDaoClassFile)) {
            sequenceDaoTemplate.merge(velocityContext, fileWriter);
        }


    }

}
