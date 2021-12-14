package it.eg.sloth.mavenplugin.writer.bean2.oracle;

import it.eg.sloth.dbmodeler.model.database.DataBaseType;
import it.eg.sloth.dbmodeler.model.schema.sequence.Sequence;
import it.eg.sloth.mavenplugin.writer.bean2.AbstractBeanWriter;
import it.eg.sloth.mavenplugin.writer.bean2.BeanWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

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
public class OracleBeanWriter extends AbstractBeanWriter implements BeanWriter {

    public OracleBeanWriter(File outputJavaDirectory, String genPackage, DataBaseType dataBaseType) {
        super(outputJavaDirectory, genPackage, dataBaseType);
    }

}
