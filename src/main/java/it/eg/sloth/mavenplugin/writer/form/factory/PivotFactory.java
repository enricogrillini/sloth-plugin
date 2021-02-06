package it.eg.sloth.mavenplugin.writer.form.factory;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Pivot;
import it.eg.sloth.jaxb.form.PivotElement;
import it.eg.sloth.jaxb.form.PivotValue;
import it.eg.sloth.mavenplugin.common.GenUtil;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.List;

@Slf4j
public class PivotFactory {

    private static final String PIVOT = "      {0} = Pivot.builder().name({1}).title({2}).build();\n";

    private static final String PIVOT_ELEMENT = "      {0}.addChild({1}.builder().name({2}).fieldAlias({3}).description({4}).build());\n";
    private static final String PIVOT_VALUE = "      {0}.addChild({1}.builder().name({2}).fieldAlias({3}).description({4}).consolidateFunction({5}).build());\n";

    private PivotFactory() {
        // NOP
    }

    public static void writeAddPivots(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            if (element instanceof Pivot) {
                Pivot pivot = (Pivot) element;
                String objectName = StringUtil.toJavaObjectName(pivot.getName());

                stringBuilder.append(MessageFormat.format(
                        PIVOT,
                        objectName,
                        "_" + StringUtil.toJavaConstantName(pivot.getName()),
                        GenUtil.stringToJava(pivot.getTitle())
                ));


                for (PivotElement pivotElement : pivot.getPivotFilterOrPivotRowOrPivotColumn()) {
                    if (pivotElement instanceof PivotValue) {
                        PivotValue pivotValue = (PivotValue) pivotElement;

                        stringBuilder.append(MessageFormat.format(
                                PIVOT_VALUE,
                                objectName,
                                pivotElement.getClass().getSimpleName(),
                                GenUtil.stringToJava(pivotValue.getName()),
                                GenUtil.stringToJava(pivotElement.getFieldAlias()),
                                GenUtil.stringToJava(pivotElement.getDescription()),
                                pivotValue.getConsolidateFunction() == null ? null : "ConsolidateFunction." + pivotValue.getConsolidateFunction()
                        ));

                    } else {
                        stringBuilder.append(MessageFormat.format(
                                PIVOT_ELEMENT,
                                objectName,
                                pivotElement.getClass().getSimpleName(),
                                GenUtil.stringToJava(pivotElement.getName()),
                                GenUtil.stringToJava(pivotElement.getFieldAlias()),
                                GenUtil.stringToJava(pivotElement.getDescription())));
                    }
                }

                stringBuilder.append("      addPivot(" + StringUtil.toJavaObjectName(pivot.getName()) + ");\n\n");
            }
        }

    }
}
