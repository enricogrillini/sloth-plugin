package it.eg.sloth.mavenplugin.writer.bean2;

import it.eg.sloth.dbmodeler.model.database.DataBaseType;
import it.eg.sloth.dbmodeler.model.schema.code.Function;
import it.eg.sloth.dbmodeler.model.schema.code.Procedure;
import it.eg.sloth.dbmodeler.model.schema.sequence.Sequence;
import it.eg.sloth.dbmodeler.model.schema.table.Table;
import it.eg.sloth.dbmodeler.model.schema.view.View;
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
    private static final String ROW_BEAN_TEMPLATE = "/templates/rowBeanTemplate-{0}.java";
    private static final String DECODE_MAP_TEMPLATE = "/templates/decodeMapTemplate.java";
    private static final String SEQUENCE_DAO_TEMPLATE = "/templates/sequenceDaoTemplate-{0}.java";
    private static final String FUNCTION_DAO_TEMPLATE = "/templates/functionDaoTemplate.java";
    private static final String PROCEDURE_DAO_TEMPLATE = "/templates/procedureDaoTemplate.java";

    private static final String DECODE_MAP = ".bean.decodemap";
    private static final String TABLE_BEAN = ".bean.tablebean";
    private static final String VIEW_BEAN = ".bean.viewbean";
    private static final String DAO = ".dao";

    File outputJavaDirectory;
    String genPackage;

    VelocityEngine velocityEngine;
    Template tableBeanTemplate;
    Template rowBeanTemplateForTable;
    Template rowBeanTemplateForView;
    Template decodeMapTemplate;
    Template sequenceDaoTemplate;
    Template functionDaoTemplate;
    Template procedureDaoTemplate;

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
        rowBeanTemplateForTable = velocityEngine.getTemplate(MessageFormat.format(ROW_BEAN_TEMPLATE, "TABLE"));
        rowBeanTemplateForView = velocityEngine.getTemplate(MessageFormat.format(ROW_BEAN_TEMPLATE, "VIEW"));
        decodeMapTemplate = velocityEngine.getTemplate(DECODE_MAP_TEMPLATE);
        sequenceDaoTemplate = velocityEngine.getTemplate(MessageFormat.format(SEQUENCE_DAO_TEMPLATE, dataBaseType));
        functionDaoTemplate = velocityEngine.getTemplate(FUNCTION_DAO_TEMPLATE);
        procedureDaoTemplate = velocityEngine.getTemplate(PROCEDURE_DAO_TEMPLATE);
    }

    public void writeTables(Collection<Table> tableCollection) throws IOException {
        for (Table table : tableCollection) {
            writeTable(table);
        }
    }

    public void writeTable(Table table) throws IOException {
        // TableBean properties
        String tableBeanClassName = GenUtil.initCap(table.getName()) + "TableBean";
        String tableBeanPackageName = genPackage + TABLE_BEAN;
        File tableBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, tableBeanPackageName, tableBeanClassName);

        String rowBeanClassName = GenUtil.initCap(table.getName()) + "RowBean";
        String rowBeanPackageName = genPackage + TABLE_BEAN;
        File rowBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, rowBeanPackageName, rowBeanClassName);

        String decodeMapClassName = GenUtil.initCap(table.getName()) + "DecodeMap";
        String decodeMapPackageName = genPackage + DECODE_MAP;
        File decodeMapClassFile = GenUtil.getClassFile(outputJavaDirectory, decodeMapPackageName, decodeMapClassName);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("tableBeanClassName", tableBeanClassName);
        velocityContext.put("tableBeanPackageName", tableBeanPackageName);

        velocityContext.put("rowBeanClassName", rowBeanClassName);
        velocityContext.put("rowBeanObjectName", GenUtil.initLow(rowBeanClassName));

        velocityContext.put("decodeMapClassName", decodeMapClassName);
        velocityContext.put("decodeMapPackageName", decodeMapPackageName);

        velocityContext.put("tableName", table.getName().toUpperCase());
        velocityContext.put("table", table);

        velocityContext.put("DbUtil", DbUtil.class);
        velocityContext.put("GenUtil", GenUtil.class);

        // Write class - Table Bean
        FileUtils.forceMkdir(tableBeanClassFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(tableBeanClassFile)) {
            tableBeanTemplate.merge(velocityContext, fileWriter);
        }

        // Write class - Row Bean
        FileUtils.forceMkdir(rowBeanClassFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(rowBeanClassFile)) {
            rowBeanTemplateForTable.merge(velocityContext, fileWriter);
        }

        // Write class - Decode Map
        FileUtils.forceMkdir(decodeMapClassFile.getParentFile());
        if (table.getName().toUpperCase().contains("DEC_")) {
            try (FileWriter fileWriter = new FileWriter(decodeMapClassFile)) {
                decodeMapTemplate.merge(velocityContext, fileWriter);
            }
        }
    }

    public void writeViews(Collection<View> viewCollection) throws IOException {
        for (View view : viewCollection) {
            writeView(view);
        }
    }

    public void writeView(View view) throws IOException {
        // TableBean properties
        String tableBeanClassName = GenUtil.initCap(view.getName()) + "TableBean";
        String tableBeanPackageName = genPackage + VIEW_BEAN;
        File tableBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, tableBeanPackageName, tableBeanClassName);

        String rowBeanClassName = GenUtil.initCap(view.getName()) + "RowBean";
        String rowBeanPackageName = genPackage + VIEW_BEAN;
        File rowBeanClassFile = GenUtil.getClassFile(outputJavaDirectory, rowBeanPackageName, rowBeanClassName);

        String decodeMapClassName = GenUtil.initCap(view.getName()) + "DecodeMap";
        String decodeMapPackageName = genPackage + DECODE_MAP;
        File decodeMapClassFile = GenUtil.getClassFile(outputJavaDirectory, decodeMapPackageName, decodeMapClassName);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("tableBeanClassName", tableBeanClassName);
        velocityContext.put("tableBeanPackageName", tableBeanPackageName);

        velocityContext.put("rowBeanClassName", rowBeanClassName);
        velocityContext.put("rowBeanObjectName", GenUtil.initLow(rowBeanClassName));

        velocityContext.put("decodeMapClassName", decodeMapClassName);
        velocityContext.put("decodeMapPackageName", decodeMapPackageName);

        velocityContext.put("tableName", view.getName().toUpperCase());
        velocityContext.put("table", view);

        velocityContext.put("DbUtil", DbUtil.class);
        velocityContext.put("GenUtil", GenUtil.class);

        // Write class - Table Bean
        FileUtils.forceMkdir(tableBeanClassFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(tableBeanClassFile)) {
            tableBeanTemplate.merge(velocityContext, fileWriter);
        }

        // Write class - Row Bean
        FileUtils.forceMkdir(rowBeanClassFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(rowBeanClassFile)) {
            rowBeanTemplateForView.merge(velocityContext, fileWriter);
        }

        // Write class - Decode Map
        FileUtils.forceMkdir(decodeMapClassFile.getParentFile());
        if (view.getName().toUpperCase().contains("DEC_")) {
            try (FileWriter fileWriter = new FileWriter(decodeMapClassFile)) {
                decodeMapTemplate.merge(velocityContext, fileWriter);
            }
        }
    }


    public void writeSequence(Collection<Sequence> sequenceCollection) throws IOException {
        // SequenceDao properties
        String sequencesDaoClassName = "SequencesDao";
        String sequencesDaoPackageName = genPackage + DAO;
        File sequencesDaoClassFile = GenUtil.getClassFile(outputJavaDirectory, sequencesDaoPackageName, sequencesDaoClassName);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("sequencesDaoPackageName", sequencesDaoPackageName);
        velocityContext.put("sequenceCollection", sequenceCollection);

        // SequenceDao
        FileUtils.forceMkdir(sequencesDaoClassFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(sequencesDaoClassFile)) {
            sequenceDaoTemplate.merge(velocityContext, fileWriter);
        }
    }

    public void writeFunction(Collection<Function> functionCollection) throws IOException {
        // FunctionDao properties
        String functionsDaoClassName = "FunctionsDao";
        String functionsDaoPackageName = genPackage + DAO;
        File functionsDaoClassFile = GenUtil.getClassFile(outputJavaDirectory, functionsDaoPackageName, functionsDaoClassName);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("functionsDaoPackageName", functionsDaoPackageName);
        velocityContext.put("functionCollection", functionCollection);
        velocityContext.put("DbUtil", DbUtil.class);
        velocityContext.put("GenUtil", GenUtil.class);

        // FunctionsDao
        FileUtils.forceMkdir(functionsDaoClassFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(functionsDaoClassFile)) {
            functionDaoTemplate.merge(velocityContext, fileWriter);
        }
    }

    public void writeProcedure(Collection<Procedure> procedureCollection) throws IOException {
        // ProcedureDao properties
        String proceduresDaoClassName = "ProceduresDao";
        String proceduresDaoPackageName = genPackage + DAO;
        File proceduresDaoClassFile = GenUtil.getClassFile(outputJavaDirectory, proceduresDaoPackageName, proceduresDaoClassName);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("proceduresDaoPackageName", proceduresDaoPackageName);
        velocityContext.put("procedureCollection", procedureCollection);
        velocityContext.put("DbUtil", DbUtil.class);
        velocityContext.put("GenUtil", GenUtil.class);

        // SequenceDao
        FileUtils.forceMkdir(proceduresDaoClassFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(proceduresDaoClassFile)) {
            procedureDaoTemplate.merge(velocityContext, fileWriter);
        }
    }

}
