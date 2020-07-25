package it.eg.sloth.mavenplugin.writer.form.factory;

import java.util.List;

import it.eg.sloth.framework.common.base.StringUtil;
import it.eg.sloth.jaxb.form.Attribute;
import it.eg.sloth.jaxb.form.AutoComplete;
import it.eg.sloth.jaxb.form.Button;
import it.eg.sloth.jaxb.form.CheckBox;
import it.eg.sloth.jaxb.form.ComboBox;
import it.eg.sloth.jaxb.form.DataType;
import it.eg.sloth.jaxb.form.DecodedText;
import it.eg.sloth.jaxb.form.Element;
import it.eg.sloth.jaxb.form.Elements;
import it.eg.sloth.jaxb.form.Field;
import it.eg.sloth.jaxb.form.File;
import it.eg.sloth.jaxb.form.Hidden;
import it.eg.sloth.jaxb.form.Input;
import it.eg.sloth.jaxb.form.InputTotalizer;
import it.eg.sloth.jaxb.form.Level;
import it.eg.sloth.jaxb.form.Link;
import it.eg.sloth.jaxb.form.Measure;
import it.eg.sloth.jaxb.form.MultipleAutoComplete;
import it.eg.sloth.jaxb.form.RadioGroup;
import it.eg.sloth.jaxb.form.Semaphore;
import it.eg.sloth.jaxb.form.Series;
import it.eg.sloth.jaxb.form.Text;
import it.eg.sloth.jaxb.form.TextArea;
import it.eg.sloth.jaxb.form.TextTotalizer;
import it.eg.sloth.jaxb.form.Labels;
import it.eg.sloth.jaxb.form.ViewModality;
import it.eg.sloth.mavenplugin.common.GenUtil;

/**
 * Project: sloth-plugin
 * Copyright (C) 2019-2020 Enrico Grillini
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Enrico Grillini
 */
public class FieldFactory {

    private FieldFactory() {
        // nothing
    }

    public static void writeFieldsCostanti(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            stringBuilder.append("    public final static String _" + StringUtil.toJavaConstantName(element.getName()) + " = \"" + element.getName() + "\";\n");
        }
        stringBuilder.append("\n");
    }

    public static void writeFieldsCostanti2(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            if (!(element instanceof Elements))
                stringBuilder.append("    public final static String " + StringUtil.toJavaConstantName(element.getName()) + " = NAME + \".\" + \"" + element.getName() + "\";\n");
        }
        stringBuilder.append("\n");
    }

    public static void writeFieldsVariabili(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            String simpleClassName = element.getClass().getSimpleName();

            if (element instanceof Series || element instanceof TextTotalizer || element instanceof InputTotalizer || element instanceof Semaphore) {
                stringBuilder.append("    private " + simpleClassName + " " + StringUtil.toJavaObjectName(element.getName()) + ";\n");

            } else if (element instanceof MultipleAutoComplete) {
                MultipleAutoComplete field = (MultipleAutoComplete) element;
                stringBuilder.append("    private " + simpleClassName + getListGenerics(field.getDataType()) + " " + StringUtil.toJavaObjectName(element.getName()) + ";\n");

            } else if (element instanceof Field) {
                Field field = (Field) element;
                stringBuilder.append("    private " + simpleClassName + getGenerics(field.getDataType()) + " " + StringUtil.toJavaObjectName(element.getName()) + ";\n");

            } else if (!(element instanceof Elements)) {
                stringBuilder.append("    private " + simpleClassName + " " + StringUtil.toJavaObjectName(element.getName()) + ";\n");
            }
        }
        stringBuilder.append("\n");
    }

    public static void writeFieldsGetter(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            String simpleClassName = element.getClass().getSimpleName();

            if (element instanceof Series || element instanceof TextTotalizer || element instanceof InputTotalizer || element instanceof Semaphore) {
                stringBuilder.append(" public " + simpleClassName + " get" + StringUtil.toJavaClassName(element.getName()) + "() {\n");
                stringBuilder.append(" return (" + simpleClassName + ") " + StringUtil.toJavaObjectName(element.getName()) + ";\n");
                stringBuilder.append(" }\n");
                stringBuilder.append("\n");
            } else if (element instanceof MultipleAutoComplete) {
                MultipleAutoComplete field = (MultipleAutoComplete) element;
                stringBuilder.append(" public " + simpleClassName + getListGenerics(field.getDataType()) + " get" + StringUtil.toJavaClassName(element.getName()) + "() {\n");
                stringBuilder.append(" return (" + simpleClassName + getListGenerics(field.getDataType()) + ")" + StringUtil.toJavaObjectName(element.getName()) + ";\n");
                stringBuilder.append(" }\n");
                stringBuilder.append("\n");
            } else if (element instanceof Field) {
                Field field = (Field) element;
                stringBuilder.append(" public " + simpleClassName + getGenerics(field.getDataType()) + " get" + StringUtil.toJavaClassName(element.getName()) + "() {\n");
                stringBuilder.append(" return (" + simpleClassName + getGenerics(field.getDataType()) + ")" + StringUtil.toJavaObjectName(element.getName()) + ";\n");
                stringBuilder.append(" }\n");
                stringBuilder.append("\n");
            } else if (!(element instanceof Elements)) {
                stringBuilder.append(" public " + simpleClassName + " get" + StringUtil.toJavaClassName(element.getName()) + "() {\n");
                stringBuilder.append(" return (" + simpleClassName + ")" + StringUtil.toJavaObjectName(element.getName()) + ";\n");
                stringBuilder.append(" }\n");
                stringBuilder.append("\n");
            }
        }
        stringBuilder.append("\n");
    }

    public static String decodeViewModality(ViewModality viewModalityImpl) {
        if (viewModalityImpl == null)
            return "ViewModality.VIEW_AUTO";
        else if (viewModalityImpl.equals(ViewModality.VISUALIZZAZIONE))
            return "ViewModality.VIEW_VISUALIZZAZIONE";
        else
            return "ViewModality.VIEW_MODIFICA";
    }

    private static String getGenerics(DataType dataType) {
        if (dataType == null)
            return "";

        if (DataType.DATE.equals(dataType) || DataType.DATETIME.equals(dataType) || DataType.TIME.equals(dataType) || DataType.HOUR.equals(dataType) || DataType.MONTH.equals(dataType)) {
            return "<Timestamp>";
        } else if (DataType.DECIMAL.equals(dataType) || DataType.INTEGER.equals(dataType) || DataType.CURRENCY.equals(dataType) || DataType.PERC.equals(dataType)) {
            return "<BigDecimal>";
        } else if (DataType.STRING.equals(dataType) || DataType.MAIL.equals(dataType) || DataType.PARTITA_IVA.equals(dataType) || DataType.CODICE_FISCALE.equals(dataType) || DataType.URL.equals(dataType)) {
            return "<String>";
        }

        return "";
    }

    private static String getListGenerics(DataType dataType) {
        if (dataType == null)
            return "";

        if (DataType.DATE.equals(dataType) || DataType.DATETIME.equals(dataType) || DataType.TIME.equals(dataType) || DataType.HOUR.equals(dataType) || DataType.MONTH.equals(dataType)) {
            return "<List<Timestamp>, Timestamp>";
        } else if (DataType.DECIMAL.equals(dataType) || DataType.INTEGER.equals(dataType) || DataType.CURRENCY.equals(dataType) || DataType.PERC.equals(dataType) || DataType.NUMBER.equals(dataType)) {
            return "<List<BigDecimal>, BigDecimal>";
        } else if (DataType.STRING.equals(dataType) || DataType.MAIL.equals(dataType) || DataType.PARTITA_IVA.equals(dataType) || DataType.CODICE_FISCALE.equals(dataType) || DataType.URL.equals(dataType)) {
            return "<List<String>, String>";
        }

        return "";
    }

    public static void writeAddFields(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            if (element instanceof TextTotalizer) {
                TextTotalizer textTotalizer = (TextTotalizer) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(textTotalizer.getName()) + " = TextTotalizer." + getGenerics(textTotalizer.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(textTotalizer.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(textTotalizer.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(textTotalizer.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(textTotalizer.getTooltip()) + ")\n")
                        .append("        .dataType(" + (textTotalizer.getDataType() == null ? "null" : "DataTypes." + textTotalizer.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(textTotalizer.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(textTotalizer.getBaseLink()) + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(textTotalizer.getName()) + ");\n");

            } else if (element instanceof Text) {
                Text text = (Text) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(text.getName()) + " = Text." + getGenerics(text.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(text.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(text.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(text.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(text.getTooltip()) + ")\n")
                        .append("        .dataType(" + (text.getDataType() == null ? "null" : "DataTypes." + text.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(text.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(text.getBaseLink()) + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(text.getName()) + ");\n");

            } else if (element instanceof TextArea) {
                TextArea textArea = (TextArea) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(textArea.getName()) + " = TextArea." + getGenerics(textArea.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(textArea.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(textArea.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(textArea.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(textArea.getTooltip()) + ")\n")
                        .append("        .dataType(" + (textArea.getDataType() == null ? "null" : "DataTypes." + textArea.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(textArea.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(textArea.getBaseLink()) + ")\n")
                        .append("        .required(" + textArea.isRequired() + ")\n")
                        .append("        .readOnly(" + textArea.isReadOnly() + ")\n")
                        .append("        .hidden(" + textArea.isHidden() + ")\n")
                        .append("        .viewModality(" + decodeViewModality(textArea.getViewModality()) + ")\n")
                        .append("        .maxLength(" + textArea.getMaxLength() + ")\n")
                        .append("        .forceCase(" + textArea.getForceCase() + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(textArea.getName()) + ");\n");

            } else if (element instanceof Hidden) {
                Hidden hidden = (Hidden) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(hidden.getName()) + " = Hidden." + getGenerics(hidden.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(hidden.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(hidden.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(hidden.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(hidden.getTooltip()) + ")\n")
                        .append("        .dataType(" + (hidden.getDataType() == null ? "null" : "DataTypes." + hidden.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(hidden.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(hidden.getBaseLink()) + ")\n")
                        .append("        .required(" + hidden.isRequired() + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(hidden.getName()) + ");\n");

            } else if (element instanceof InputTotalizer) {
                InputTotalizer inputTotalizer = (InputTotalizer) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(inputTotalizer.getName()) + " = InputTotalizer." + getGenerics(inputTotalizer.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(inputTotalizer.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(inputTotalizer.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(inputTotalizer.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(inputTotalizer.getTooltip()) + ")\n")
                        .append("        .dataType(" + (inputTotalizer.getDataType() == null ? "null" : "DataTypes." + inputTotalizer.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(inputTotalizer.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(inputTotalizer.getBaseLink()) + ")\n")
                        .append("        .required(" + inputTotalizer.isRequired() + ")\n")
                        .append("        .readOnly(" + inputTotalizer.isReadOnly() + ")\n")
                        .append("        .hidden(" + inputTotalizer.isHidden() + ")\n")
                        .append("        .viewModality(" + decodeViewModality(inputTotalizer.getViewModality()) + ")\n")
                        .append("        .maxLength(" + inputTotalizer.getMaxLength() + ")\n")
                        .append("        .forceCase(" + inputTotalizer.getForceCase() + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(inputTotalizer.getName()) + ");\n");

            } else if (element instanceof Input) {
                Input input = (Input) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(input.getName()) + " = Input." + getGenerics(input.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(input.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(input.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(input.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(input.getTooltip()) + ")\n")
                        .append("        .dataType(" + (input.getDataType() == null ? "null" : "DataTypes." + input.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(input.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(input.getBaseLink()) + ")\n")
                        .append("        .required(" + input.isRequired() + ")\n")
                        .append("        .readOnly(" + input.isReadOnly() + ")\n")
                        .append("        .hidden(" + input.isHidden() + ")\n")
                        .append("        .viewModality(" + decodeViewModality(input.getViewModality()) + ")\n")
                        .append("        .maxLength(" + input.getMaxLength() + ")\n")
                        .append("        .forceCase(" + input.getForceCase() + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(input.getName()) + ");\n");

            } else if (element instanceof ComboBox) {
                ComboBox comboBox = (ComboBox) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(comboBox.getName()) + " = ComboBox." + getGenerics(comboBox.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(comboBox.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(comboBox.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(comboBox.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(comboBox.getTooltip()) + ")\n")
                        .append("        .dataType(" + (comboBox.getDataType() == null ? "null" : "DataTypes." + comboBox.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(comboBox.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(comboBox.getBaseLink()) + ")\n")
                        .append("        .required(" + comboBox.isRequired() + ")\n")
                        .append("        .readOnly(" + comboBox.isReadOnly() + ")\n")
                        .append("        .hidden(" + comboBox.isHidden() + ")\n")
                        .append("        .viewModality(" + decodeViewModality(comboBox.getViewModality()) + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(comboBox.getName()) + ");\n");

            } else if (element instanceof AutoComplete) {
                AutoComplete autoComplete = (AutoComplete) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(autoComplete.getName()) + " = AutoComplete." + getGenerics(autoComplete.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(autoComplete.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(autoComplete.getAlias()) + ")\n")
                        .append("        .decodeAlias(" + GenUtil.stringToJava(autoComplete.getDecodeAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(autoComplete.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(autoComplete.getTooltip()) + ")\n")
                        .append("        .dataType(" + (autoComplete.getDataType() == null ? "null" : "DataTypes." + autoComplete.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(autoComplete.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(autoComplete.getBaseLink()) + ")\n")
                        .append("        .required(" + autoComplete.isRequired() + ")\n")
                        .append("        .readOnly(" + autoComplete.isReadOnly() + ")\n")
                        .append("        .hidden(" + autoComplete.isHidden() + ")\n")
                        .append("        .viewModality(" + decodeViewModality(autoComplete.getViewModality()) + ")\n")
                        .append("        .sizeLimit(" + autoComplete.getSizeLimit() + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(autoComplete.getName()) + ");\n");

            } else if (element instanceof MultipleAutoComplete) {
                MultipleAutoComplete multipleAutoComplete = (MultipleAutoComplete) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(multipleAutoComplete.getName()) + " = new MultipleAutoComplete" + getListGenerics(multipleAutoComplete.getDataType()) + " (" + "_")
                        .append(StringUtil.toJavaConstantName(multipleAutoComplete.getName()) + ", ")
                        .append((multipleAutoComplete.getAlias() == null ? "null" : " \"" + multipleAutoComplete.getAlias() + "\"") + ", ")
                        .append((multipleAutoComplete.getDecodeAlias() == null ? "null" : " \"" + multipleAutoComplete.getDecodeAlias() + "\"") + ", ")
                        .append((multipleAutoComplete.getDescription() == null ? "null" : " \"" + multipleAutoComplete.getDescription() + "\"") + ", ")
                        .append((multipleAutoComplete.getTooltip() == null ? "null" : " \"" + multipleAutoComplete.getTooltip() + "\"") + ", ")
                        .append((multipleAutoComplete.getDataType() == null ? "null" : " DataTypes." + multipleAutoComplete.getDataType()) + ", ")
                        .append((multipleAutoComplete.getFormat() == null ? "null" : " \"" + multipleAutoComplete.getFormat() + "\"") + ", ")
                        .append((multipleAutoComplete.getBaseLink() == null ? "null" : " \"" + multipleAutoComplete.getBaseLink() + "\"") + ", ")
                        .append(multipleAutoComplete.isRequired() + ", ")
                        .append(multipleAutoComplete.isReadOnly() + ", ")
                        .append(multipleAutoComplete.isHidden() + ", ")
                        .append(decodeViewModality(multipleAutoComplete.getViewModality()) + "));\n");

            } else if (element instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(checkBox.getName()) + " = CheckBox." + getGenerics(checkBox.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(checkBox.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(checkBox.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(checkBox.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(checkBox.getTooltip()) + ")\n")
                        .append("        .dataType(" + (checkBox.getDataType() == null ? "null" : "DataTypes." + checkBox.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(checkBox.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(checkBox.getBaseLink()) + ")\n")
                        .append("        .required(" + checkBox.isRequired() + ")\n")
                        .append("        .readOnly(" + checkBox.isReadOnly() + ")\n")
                        .append("        .hidden(" + checkBox.isHidden() + ")\n")
                        .append("        .viewModality(" + decodeViewModality(checkBox.getViewModality()) + ")\n")
                        .append("        .valChecked(" + (checkBox.getValChecked() == null ? "\"S\"" : " \"" + checkBox.getValChecked() + "\"") + ")\n")
                        .append("        .valUnChecked(" + (checkBox.getValUnChecked() == null ? "\"N\"" : " \"" + checkBox.getValUnChecked()) + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(checkBox.getName()) + ");\n");

            } else if (element instanceof RadioGroup) {
                RadioGroup radioGroup = (RadioGroup) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(radioGroup.getName()) + " = RadioGroup." + getGenerics(radioGroup.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(radioGroup.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(radioGroup.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(radioGroup.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(radioGroup.getTooltip()) + ")\n")
                        .append("        .dataType(" + (radioGroup.getDataType() == null ? "null" : "DataTypes." + radioGroup.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(radioGroup.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(radioGroup.getBaseLink()) + ")\n")
                        .append("        .required(" + radioGroup.isRequired() + ")\n")
                        .append("        .readOnly(" + radioGroup.isReadOnly() + ")\n")
                        .append("        .hidden(" + radioGroup.isHidden() + ")\n")
                        .append("        .viewModality(" + decodeViewModality(radioGroup.getViewModality()) + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(radioGroup.getName()) + ");\n");

            } else if (element instanceof Semaphore) {
                Semaphore semaphore = (Semaphore) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(semaphore.getName()) + " = Semaphore." + getGenerics(semaphore.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(semaphore.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(semaphore.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(semaphore.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(semaphore.getTooltip()) + ")\n")
                        .append("        .dataType(" + (semaphore.getDataType() == null ? "null" : "DataTypes." + semaphore.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(semaphore.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(semaphore.getBaseLink()) + ")\n")
                        .append("        .required(" + semaphore.isRequired() + ")\n")
                        .append("        .readOnly(" + semaphore.isReadOnly() + ")\n")
                        .append("        .hidden(" + semaphore.isHidden() + ")\n")
                        .append("        .viewModality(" + decodeViewModality(semaphore.getViewModality()) + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(semaphore.getName()) + ");\n");

            } else if (element instanceof Link) {
                Link button = (Link) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(button.getName()) + " = Link.builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(button.getName()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(button.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(button.getTooltip()) + ")\n")
                        .append("        .hidden(" + GenUtil.booleanToJava(button.isHidden()) + ")\n")
                        .append("        .disabled(" + GenUtil.booleanToJava(button.isDisabled()) + ")\n")
                        .append("        .buttonType(" + (button.getType() == null ? "null" : "ButtonType." + button.getType()) + ")\n")
                        .append("        .imgHtml(" + GenUtil.stringToJava(button.getImgHtml()) + ")\n")
                        .append("        .href(" + GenUtil.stringToJava(button.getHref()) + ")\n")
                        .append("        .target(" + GenUtil.stringToJava(button.getTarget()) + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(button.getName()) + ");\n");

            } else if (element instanceof Button) {
                Button button = (Button) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(button.getName()) + " = Button.builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(button.getName()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(button.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(button.getTooltip()) + ")\n")
                        .append("        .hidden(" + GenUtil.booleanToJava(button.isHidden()) + ")\n")
                        .append("        .disabled(" + GenUtil.booleanToJava(button.isDisabled()) + ")\n")
                        .append("        .buttonType(" + (button.getType() == null ? "null" : "ButtonType." + button.getType()) + ")\n")
                        .append("        .imgHtml(" + GenUtil.stringToJava(button.getImgHtml()) + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(button.getName()) + ");\n");

            } else if (element instanceof File) {
                File file = (File) element;

                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(file.getName()) + " = File.builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(file.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(file.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(file.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(file.getTooltip()) + ")\n")
                        .append("        .required(" + file.isRequired() + ")\n")
                        .append("        .readOnly(" + file.isReadOnly() + ")\n")
                        .append("        .hidden(" + file.isHidden() + ")\n")
                        .append("        .viewModality(" + decodeViewModality(file.getViewModality()) + ")\n")
                        .append("        .maxSize(" + file.getMaxSize() + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(file.getName()) + ");\n");

            } else if (element instanceof Level) {
                Level level = (Level) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(level.getName()) + " = new Level" + getGenerics(level.getDataType()) + " (")
                        .append("_" + StringUtil.toJavaConstantName(level.getName()) + ", ")
                        .append((level.getAlias() == null ? "null" : " \"" + level.getAlias() + "\"") + ", ")
                        .append((level.getDescription() == null ? "null" : " \"" + level.getDescription() + "\"") + ", ")
                        .append((level.getTooltip() == null ? "null" : " \"" + level.getTooltip() + "\"") + ", ")
                        .append((level.getDataType() == null ? "null" : " DataTypes." + level.getDataType()) + ", ")
                        .append((level.getFormat() == null ? "null" : " \"" + level.getFormat() + "\"") + ", ")
                        .append((level.getBaseLink() == null ? "null" : " \"" + level.getBaseLink() + "\"") + "));\n");

            } else if (element instanceof Attribute) {
                Attribute attribute = (Attribute) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(attribute.getName()) + " = new Attribute" + getGenerics(attribute.getDataType()) + "(")
                        .append("_" + StringUtil.toJavaConstantName(attribute.getName()) + ", ")
                        .append((attribute.getAlias() == null ? "null" : " \"" + attribute.getAlias() + "\"") + ", ")
                        .append((attribute.getDescription() == null ? "null" : " \"" + attribute.getDescription() + "\"") + ", ")
                        .append((attribute.getTooltip() == null ? "null" : " \"" + attribute.getTooltip() + "\"") + ", ")
                        .append((attribute.getDataType() == null ? "null" : " DataTypes." + attribute.getDataType()) + ", ")
                        .append((attribute.getFormat() == null ? "null" : " \"" + attribute.getFormat() + "\"") + ", ")
                        .append((attribute.getBaseLink() == null ? "null" : " \"" + attribute.getBaseLink() + "\"") + "));\n");

            } else if (element instanceof Measure) {
                Measure measure = (Measure) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(measure.getName()) + " = new Measure" + getGenerics(measure.getDataType()) + " (")
                        .append("_" + StringUtil.toJavaConstantName(measure.getName()) + ", ")
                        .append((measure.getAlias() == null ? "null" : " \"" + measure.getAlias() + "\"") + ", ")
                        .append((measure.getDescription() == null ? "null" : " \"" + measure.getDescription() + "\"") + ", ")
                        .append((measure.getTooltip() == null ? "null" : " \"" + measure.getTooltip() + "\"") + ", ")
                        .append((measure.getDataType() == null ? "null" : " DataTypes." + measure.getDataType()) + ", ")
                        .append((measure.getFormat() == null ? "null" : " \"" + measure.getFormat() + "\"") + ", ")
                        .append((measure.getBaseLink() == null ? "null" : " \"" + measure.getBaseLink() + "\"") + "));\n");

            } else if (element instanceof Labels) {
                Labels labels = (Labels) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(element.getName()) + " = Labels." + getGenerics(labels.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(labels.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(labels.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(labels.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(labels.getTooltip()) + ")\n")
                        .append("        .dataType(" + (labels.getDataType() == null ? "null" : "DataTypes." + labels.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(labels.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(labels.getBaseLink()) + ")\n")
                        .append("        .rotation(" + labels.getRotation() + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(labels.getName()) + ");\n");

            } else if (element instanceof Series) {
                Series series = (Series) element;
                stringBuilder
                        .append("\n")
                        .append("      " + StringUtil.toJavaObjectName(element.getName()) + " = Series." + getGenerics(series.getDataType()) + "builder()\n")
                        .append("        .name(_" + StringUtil.toJavaConstantName(series.getName()) + ")\n")
                        .append("        .alias(" + GenUtil.stringToJava(series.getAlias()) + ")\n")
                        .append("        .description(" + GenUtil.stringToJava(series.getDescription()) + ")\n")
                        .append("        .tooltip(" + GenUtil.stringToJava(series.getTooltip()) + ")\n")
                        .append("        .dataType(" + (series.getDataType() == null ? "null" : "DataTypes." + series.getDataType()) + ")\n")
                        .append("        .format(" + GenUtil.stringToJava(series.getFormat()) + ")\n")
                        .append("        .baseLink(" + GenUtil.stringToJava(series.getBaseLink()) + ")\n")
                        .append("        .build();\n")
                        .append("      addChild(" + StringUtil.toJavaObjectName(series.getName()) + ");\n");

            } else if (element instanceof DecodedText) {
                DecodedText decodedText = (DecodedText) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(decodedText.getName()) + " = new DecodedText" + getGenerics(decodedText.getDataType()) + " (" + "_")
                        .append(StringUtil.toJavaConstantName(decodedText.getName()) + ", ")
                        .append((decodedText.getAlias() == null ? "null" : " \"" + decodedText.getAlias() + "\"") + ", ")
                        .append((decodedText.getDescription() == null ? "null" : " \"" + decodedText.getDescription() + "\"") + ", ")
                        .append((decodedText.getTooltip() == null ? "null" : " \"" + decodedText.getTooltip() + "\"") + ", ")
                        .append((decodedText.getDataType() == null ? "null" : " DataTypes." + decodedText.getDataType()) + ", ")
                        .append((decodedText.getFormat() == null ? "null" : " \"" + decodedText.getFormat() + "\"") + ", ")
                        .append((decodedText.getBaseLink() == null ? "null" : " \"" + decodedText.getBaseLink() + "\"") + "));\n");
            }

        }

    }
}
