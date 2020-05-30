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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        } else if (DataType.STRING.equals(dataType) || DataType.MAIL.equals(dataType) || DataType.PIVA.equals(dataType) || DataType.CF.equals(dataType) || DataType.URL.equals(dataType)) {
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
        } else if (DataType.STRING.equals(dataType) || DataType.MAIL.equals(dataType) || DataType.PIVA.equals(dataType) || DataType.CF.equals(dataType) || DataType.URL.equals(dataType)) {
            return "<List<String>, String>";
        }

        return "";
    }

    public static void writeAddFields(StringBuilder stringBuilder, List<Element> elements) {
        for (Element element : elements) {
            if (element instanceof TextTotalizer) {
                TextTotalizer textTotalizer = (TextTotalizer) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(textTotalizer.getName()) + " = new TextTotalizer (")
                        .append("_" + StringUtil.toJavaConstantName(textTotalizer.getName()) + ", ")
                        .append((textTotalizer.getAlias() == null ? "null" : " \"" + textTotalizer.getAlias() + "\"") + ", ")
                        .append((textTotalizer.getDescription() == null ? "null" : " \"" + textTotalizer.getDescription() + "\"") + ", ")
                        .append((textTotalizer.getTooltip() == null ? "null" : " \"" + textTotalizer.getTooltip() + "\"") + ", ")
                        .append((textTotalizer.getDataType() == null ? "null" : " DataTypes." + textTotalizer.getDataType()) + ", ")
                        .append((textTotalizer.getFormat() == null ? "null" : " \"" + textTotalizer.getFormat() + "\"") + ", ")
                        .append((textTotalizer.getBaseLink() == null ? "null" : " \"" + textTotalizer.getBaseLink() + "\"") + "));\n");

            } else if (element instanceof Text) {
                Text text = (Text) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(text.getName()) + " = new Text" + getGenerics(text.getDataType()) + " (")
                        .append("_" + StringUtil.toJavaConstantName(text.getName()) + ", ")
                        .append((text.getAlias() == null ? "null" : " \"" + text.getAlias() + "\"") + ", ")
                        .append((text.getDescription() == null ? "null" : " \"" + text.getDescription() + "\"") + ", ")
                        .append((text.getTooltip() == null ? "null" : " \"" + text.getTooltip() + "\"") + ", ")
                        .append((text.getDataType() == null ? "null" : " DataTypes." + text.getDataType()) + ", ")
                        .append((text.getFormat() == null ? "null" : " \"" + text.getFormat() + "\"") + ", ")
                        .append((text.getBaseLink() == null ? "null" : " \"" + text.getBaseLink() + "\"") + "));\n");

            } else if (element instanceof TextArea) {
                TextArea textArea = (TextArea) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(textArea.getName()) + " = new TextArea" + getGenerics(textArea.getDataType()) + " (")
                        .append("_" + StringUtil.toJavaConstantName(textArea.getName()) + ", ")
                        .append((textArea.getAlias() == null ? "null" : " \"" + textArea.getAlias() + "\"") + ", ")
                        .append((textArea.getDescription() == null ? "null" : " \"" + textArea.getDescription() + "\"") + ", ")
                        .append((textArea.getTooltip() == null ? "null" : " \"" + textArea.getTooltip() + "\"") + ", ")
                        .append((textArea.getDataType() == null ? "null" : " DataTypes." + textArea.getDataType()) + ", ")
                        .append((textArea.getFormat() == null ? "null" : " \"" + textArea.getFormat() + "\"") + ", ")
                        .append((textArea.getBaseLink() == null ? "null" : " \"" + textArea.getBaseLink() + "\"") + ", ")
                        .append(textArea.isRequired() + ", ")
                        .append(textArea.isReadOnly() + ", ")
                        .append(textArea.isHidden() + ", ")
                        .append(decodeViewModality(textArea.getViewModality()) + ", ")
                        .append(textArea.getMaxLength() + ", ")
                        .append(textArea.getForceCase() == null ? "ForceCase.NONE));\n" : "ForceCase." + textArea.getForceCase() + "));\n");

            } else if (element instanceof Hidden) {
                Hidden hidden = (Hidden) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(hidden.getName()) + " = new Hidden" + getGenerics(hidden.getDataType()) + " (")
                        .append("_" + StringUtil.toJavaConstantName(hidden.getName()) + ", ")
                        .append((hidden.getAlias() == null ? "null" : " \"" + hidden.getAlias() + "\"") + ", ")
                        .append((hidden.getDescription() == null ? "null" : " \"" + hidden.getDescription() + "\"") + ", ")
                        .append((hidden.getTooltip() == null ? "null" : " \"" + hidden.getTooltip() + "\"") + ", ")
                        .append((hidden.getDataType() == null ? "null" : " DataTypes." + hidden.getDataType()) + ", ")
                        .append((hidden.getFormat() == null ? "null" : " \"" + hidden.getFormat() + "\"") + ", ")
                        .append((hidden.getBaseLink() == null ? "null" : " \"" + hidden.getBaseLink() + "\"") + ", ")
                        .append(hidden.isRequired() + "));\n");

            } else if (element instanceof InputTotalizer) {
                InputTotalizer inputTotalizer = (InputTotalizer) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(inputTotalizer.getName()) + " = new InputTotalizer (")
                        .append("_" + StringUtil.toJavaConstantName(inputTotalizer.getName()) + ", ")
                        .append((inputTotalizer.getAlias() == null ? "null" : " \"" + inputTotalizer.getAlias() + "\"") + ", ")
                        .append((inputTotalizer.getDescription() == null ? "null" : " \"" + inputTotalizer.getDescription() + "\"") + ", ")
                        .append((inputTotalizer.getTooltip() == null ? "null" : " \"" + inputTotalizer.getTooltip() + "\"") + ", ")
                        .append((inputTotalizer.getDataType() == null ? "null" : " DataTypes." + inputTotalizer.getDataType()) + ", ")
                        .append((inputTotalizer.getFormat() == null ? "null" : " \"" + inputTotalizer.getFormat() + "\"") + ", ")
                        .append((inputTotalizer.getBaseLink() == null ? "null" : " \"" + inputTotalizer.getBaseLink() + "\"") + ", ")
                        .append(inputTotalizer.isRequired() + ", ")
                        .append(inputTotalizer.isReadOnly() + ", ")
                        .append(inputTotalizer.isHidden() + ", ")
                        .append(decodeViewModality(inputTotalizer.getViewModality()) + ", ")
                        .append(inputTotalizer.getMaxLength() + ", ")
                        .append(inputTotalizer.getForceCase() == null ? "ForceCase.NONE" : "ForceCase." + inputTotalizer.getForceCase())
                        .append("));\n");

            } else if (element instanceof Input) {
                Input input = (Input) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(input.getName()) + " = new Input" + getGenerics(input.getDataType()) + " (")
                        .append("_" + StringUtil.toJavaConstantName(input.getName()) + ", ")
                        .append((input.getAlias() == null ? "null" : " \"" + input.getAlias() + "\"") + ", ")
                        .append((input.getDescription() == null ? "null" : " \"" + input.getDescription() + "\"") + ", ")
                        .append((input.getTooltip() == null ? "null" : " \"" + input.getTooltip() + "\"") + ", ")
                        .append((input.getDataType() == null ? "null" : " DataTypes." + input.getDataType()) + ", ")
                        .append((input.getFormat() == null ? "null" : " \"" + input.getFormat() + "\"") + ", ")
                        .append((input.getBaseLink() == null ? "null" : " \"" + input.getBaseLink() + "\"") + ", ")
                        .append(input.isRequired() + ", ")
                        .append(input.isReadOnly() + ", ")
                        .append(input.isHidden() + ", ")
                        .append(decodeViewModality(input.getViewModality()) + ", ")
                        .append(input.getMaxLength() + ", ")
                        .append(input.getForceCase() == null ? "ForceCase.NONE" : "ForceCase." + input.getForceCase())
                        .append("));\n");

            } else if (element instanceof ComboBox) {
                ComboBox comboBox = (ComboBox) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(comboBox.getName()) + " = new ComboBox" + getGenerics(comboBox.getDataType()) + " (" + "_")
                        .append(StringUtil.toJavaConstantName(comboBox.getName()) + ", ")
                        .append((comboBox.getAlias() == null ? "null" : " \"" + comboBox.getAlias() + "\"") + ", ")
                        .append((comboBox.getDescription() == null ? "null" : " \"" + comboBox.getDescription() + "\"") + ", ")
                        .append((comboBox.getTooltip() == null ? "null" : " \"" + comboBox.getTooltip() + "\"") + ", ")
                        .append((comboBox.getDataType() == null ? "null" : " DataTypes." + comboBox.getDataType()) + ", ")
                        .append((comboBox.getFormat() == null ? "null" : " \"" + comboBox.getFormat() + "\"") + ", ")
                        .append((comboBox.getBaseLink() == null ? "null" : " \"" + comboBox.getBaseLink() + "\"") + ", ")
                        .append(comboBox.isRequired() + ", ")
                        .append(comboBox.isReadOnly() + ", ")
                        .append(comboBox.isHidden() + ", ")
                        .append(decodeViewModality(comboBox.getViewModality()) + "));\n");

            } else if (element instanceof AutoComplete) {
                AutoComplete autoComplete = (AutoComplete) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(autoComplete.getName()) + " = new AutoComplete" + getGenerics(autoComplete.getDataType()) + " (" + "_")
                        .append(StringUtil.toJavaConstantName(autoComplete.getName()) + ", ")
                        .append((autoComplete.getAlias() == null ? "null" : " \"" + autoComplete.getAlias() + "\"") + ", ")
                        .append((autoComplete.getDecodeAlias() == null ? "null" : " \"" + autoComplete.getDecodeAlias() + "\"") + ", ")
                        .append((autoComplete.getDescription() == null ? "null" : " \"" + autoComplete.getDescription() + "\"") + ", ")
                        .append((autoComplete.getTooltip() == null ? "null" : " \"" + autoComplete.getTooltip() + "\"") + ", ")
                        .append((autoComplete.getDataType() == null ? "null" : " DataTypes." + autoComplete.getDataType()) + ", ")
                        .append((autoComplete.getFormat() == null ? "null" : " \"" + autoComplete.getFormat() + "\"") + ", ")
                        .append((autoComplete.getBaseLink() == null ? "null" : " \"" + autoComplete.getBaseLink() + "\"") + ", ")
                        .append(autoComplete.isRequired() + ", ")
                        .append(autoComplete.isReadOnly() + ", ")
                        .append(autoComplete.isHidden() + ", ")
                        .append(decodeViewModality(autoComplete.getViewModality()) + ", ")
                        .append(autoComplete.getSizeLimit() + "));\n");

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
                        .append("      addChild(" + StringUtil.toJavaObjectName(checkBox.getName()) + " = new CheckBox" + getGenerics(checkBox.getDataType()) + " (" + "_")
                        .append(StringUtil.toJavaConstantName(checkBox.getName()) + ", ")
                        .append((checkBox.getAlias() == null ? "null" : " \"" + checkBox.getAlias() + "\"") + ", ")
                        .append((checkBox.getDescription() == null ? "null" : " \"" + checkBox.getDescription() + "\"") + ", ")
                        .append((checkBox.getTooltip() == null ? "null" : " \"" + checkBox.getTooltip() + "\"") + ", ")
                        .append((checkBox.getDataType() == null ? "null" : " DataTypes." + checkBox.getDataType()) + ", ")
                        .append((checkBox.getFormat() == null ? "null" : " \"" + checkBox.getFormat() + "\"") + ", ")
                        .append((checkBox.getBaseLink() == null ? "null" : " \"" + checkBox.getBaseLink() + "\"") + ", ")
                        .append(checkBox.isRequired() + ", ")
                        .append(checkBox.isReadOnly() + ", ")
                        .append(checkBox.isHidden() + ", ")
                        .append(decodeViewModality(checkBox.getViewModality()) + ", ")
                        .append((checkBox.getValChecked() == null ? "\"S\"" : " \"" + checkBox.getValChecked() + "\"") + ", ")
                        .append((checkBox.getValUnChecked() == null ? "\"N\"" : " \"" + checkBox.getValUnChecked() + "\"") + "));\n");

            } else if (element instanceof RadioGroup) {
                RadioGroup radioGroup = (RadioGroup) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(radioGroup.getName()) + " = new RadioGroup" + getGenerics(radioGroup.getDataType()) + " (" + "_")
                        .append(StringUtil.toJavaConstantName(radioGroup.getName()) + ", ")
                        .append((radioGroup.getAlias() == null ? "null" : " \"" + radioGroup.getAlias() + "\"") + ", ")
                        .append((radioGroup.getDescription() == null ? "null" : " \"" + radioGroup.getDescription() + "\"") + ", ")
                        .append((radioGroup.getTooltip() == null ? "null" : " \"" + radioGroup.getTooltip() + "\"") + ", ")
                        .append((radioGroup.getDataType() == null ? "null" : " DataTypes." + radioGroup.getDataType()) + ", ")
                        .append((radioGroup.getFormat() == null ? "null" : " \"" + radioGroup.getFormat() + "\"") + ", ")
                        .append((radioGroup.getBaseLink() == null ? "null" : " \"" + radioGroup.getBaseLink() + "\"") + ", ")
                        .append(radioGroup.isRequired() + ", ")
                        .append(radioGroup.isReadOnly() + ", ")
                        .append(radioGroup.isHidden() + ", ")
                        .append(decodeViewModality(radioGroup.getViewModality()) + "));\n");

            } else if (element instanceof Semaphore) {
                Semaphore semaforo = (Semaphore) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(semaforo.getName()) + " = new Semaphore(" + "_")
                        .append(StringUtil.toJavaConstantName(semaforo.getName()) + ", ")
                        .append((semaforo.getAlias() == null ? "null" : " \"" + semaforo.getAlias() + "\"") + ", ")
                        .append((semaforo.getDescription() == null ? "null" : " \"" + semaforo.getDescription() + "\"") + ", ")
                        .append((semaforo.getTooltip() == null ? "null" : " \"" + semaforo.getTooltip() + "\"") + ", ")
                        .append((semaforo.getDataType() == null ? "null" : " DataTypes." + semaforo.getDataType()) + ", ")
                        .append((semaforo.getFormat() == null ? "null" : " \"" + semaforo.getFormat() + "\"") + ", ")
                        .append((semaforo.getBaseLink() == null ? "null" : " \"" + semaforo.getBaseLink() + "\"") + ", ")
                        .append(semaforo.isRequired() + ", ")
                        .append(semaforo.isReadOnly() + ", ")
                        .append(semaforo.isHidden() + ", ")
                        .append(decodeViewModality(semaforo.getViewModality()) + "));\n");

            } else if (element instanceof Link) {
                Link button = (Link) element;
                stringBuilder
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

//
//                stringBuilder
//                        .append("      addChild(" + StringUtil.toJavaObjectName(link.getName()) + " = new Link(" + "_")
//                        .append(StringUtil.toJavaConstantName(link.getName()) + ", ")
//                        .append((link.getDescription() == null ? "null" : " \"" + link.getDescription() + "\"") + ", ")
//                        .append((link.getTooltip() == null ? "null" : " \"" + link.getTooltip() + "\"") + ", ")
//                        .append(link.isHidden() + ", ")
//                        .append(link.isDisabled() + ", ")
//                        .append(link.getType() + ", ")
//                        .append("\"" + StringUtil.replace(link.getImgHtml(), "\"", "||\"") + "\"" + "));\n");

            } else if (element instanceof Button) {
                Button button = (Button) element;
                stringBuilder
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
                        .append("      addChild(" + StringUtil.toJavaObjectName(file.getName()) + " = new File(")
                        .append("_" + StringUtil.toJavaConstantName(file.getName()) + ", ")
                        .append((file.getAlias() == null ? "null" : " \"" + file.getAlias() + "\"") + ", ")
                        .append((file.getDescription() == null ? "null" : " \"" + file.getDescription() + "\"") + ", ")
                        .append((file.getTooltip() == null ? "null" : " \"" + file.getTooltip() + "\"") + ", ")
                        .append(file.isRequired() + ", ")
                        .append(file.isReadOnly() + ", ")
                        .append(file.isHidden() + ", ")
                        .append(decodeViewModality(file.getViewModality()) + ", ")
                        .append(file.getMaxSize() + "));\n");

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
                        .append("      addChild(" + StringUtil.toJavaObjectName(labels.getName()) + " = new Labels" + getGenerics(labels.getDataType()) + " (")
                        .append("_" + StringUtil.toJavaConstantName(labels.getName()) + ", ")
                        .append((labels.getAlias() == null ? "null" : " \"" + labels.getAlias() + "\"") + ", ")
                        .append((labels.getDescription() == null ? "null" : " \"" + labels.getDescription() + "\"") + ", ")
                        .append((labels.getTooltip() == null ? "null" : " \"" + labels.getTooltip() + "\"") + ", ")
                        .append((labels.getDataType() == null ? "null" : " DataTypes." + labels.getDataType()) + ", ")
                        .append((labels.getFormat() == null ? "null" : " \"" + labels.getFormat() + "\"") + ", ")
                        .append((labels.getBaseLink() == null ? "null" : " \"" + labels.getBaseLink() + "\"") + ", ")
                        .append(labels.getRotation() + "));\n");

            } else if (element instanceof Series) {
                Series series = (Series) element;
                stringBuilder
                        .append("      addChild(" + StringUtil.toJavaObjectName(series.getName()) + " = new Series (")
                        .append("_" + StringUtil.toJavaConstantName(series.getName()) + ", ")
                        .append((series.getAlias() == null ? "null" : " \"" + series.getAlias() + "\"") + ", ")
                        .append((series.getDescription() == null ? "null" : " \"" + series.getDescription() + "\"") + ", ")
                        .append((series.getTooltip() == null ? "null" : " \"" + series.getTooltip() + "\"") + ", ")
                        .append((series.getDataType() == null ? "null" : " DataTypes." + series.getDataType()) + ", ")
                        .append((series.getFormat() == null ? "null" : " \"" + series.getFormat() + "\"") + ", ")
                        .append((series.getBaseLink() == null ? "null" : " \"" + series.getBaseLink() + "\"") + "));\n");

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
