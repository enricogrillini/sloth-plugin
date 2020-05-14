package it.eg.sloth.mavenplugin.writer.form.factory;

import java.util.List;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Skipper;

public class SkipperFactory {

    private SkipperFactory() {
        // nothing
    }

    public static void write(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            if (element instanceof Skipper) {
                write(stringBuilder, (Skipper) element);
            }
        }
    }

    public static void write(StringBuilder stringBuilder, Skipper skipper) {
        String className = StringUtil.toJavaClassName(skipper.getName());

        stringBuilder.append("  public class " + className + " extends Skipper {\n");
        stringBuilder.append("    \n");
        stringBuilder.append("    public final static String NAME = \"" + skipper.getName() + "\";\n");
        stringBuilder.append("\n");
        stringBuilder.append("    public " + className + " () {\n");
        stringBuilder.append("      super(NAME, " + skipper.isSkipBody() + ");\n");
        stringBuilder.append("    }\n");
        stringBuilder.append("  }\n");
    }

}
