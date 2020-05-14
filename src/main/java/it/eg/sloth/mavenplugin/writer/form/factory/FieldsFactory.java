package it.eg.sloth.mavenplugin.writer.form.factory;

import java.io.IOException;
import java.util.List;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Fields;


public class FieldsFactory {

    private FieldsFactory() {
        // nothing
    }

    public static void write(StringBuilder stringBuilder, List<Element> elements) throws IOException {
        for (Element element : elements) {
            if (element instanceof Fields) {
                write(stringBuilder, (Fields) element);
            }
        }
    }

    private static void write(StringBuilder stringBuilder, Fields fields) throws IOException {
        String className = StringUtil.toJavaClassName(fields.getName());
        
        stringBuilder.append("  public static class " + className + " extends Fields<DataSource> {\n");
        stringBuilder.append("    \n");
        stringBuilder.append("    public final static String NAME = \"" + fields.getName() + "\";\n");
        stringBuilder.append("  \n");

        // Costanti e variabili
        FieldFactory.writeFieldsCostanti(stringBuilder, fields.getTextOrInputOrTextArea());
        FieldFactory.writeFieldsCostanti2(stringBuilder, fields.getTextOrInputOrTextArea());
        FieldFactory.writeFieldsVariabili(stringBuilder, fields.getTextOrInputOrTextArea());

        // Costruttore
        stringBuilder.append("\n");
        stringBuilder.append("    public " + className + "() {\n");
        stringBuilder.append("      super(NAME);\n");
        FieldFactory.writeAddFields(stringBuilder, fields.getTextOrInputOrTextArea());
        stringBuilder.append("  }\n");

        // Getter/Setter
        FieldFactory.writeFieldsGetter(stringBuilder, fields.getTextOrInputOrTextArea());

        stringBuilder.append("  }\n");
        stringBuilder.append("\n");
    }
}
