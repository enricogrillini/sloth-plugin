package it.eg.sloth.mavenplugin.writer.bean;

import it.eg.sloth.jaxb.dbschema.Package;
import it.eg.sloth.jaxb.dbschema.Table;
import it.eg.sloth.jaxb.dbschema.Type;
import it.eg.sloth.jaxb.dbschema.View;

import java.io.IOException;
import java.sql.SQLException;

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
 * Interfaccia per la generazione dei Beam Java
 *
 * @author Enrico Grillini
 *
 */
public interface BeanGenInterface {

  public void writeTableBean(Table table) throws IOException;

  public void writeDecodeTableBean(Table table) throws IOException, SQLException;

  public void writeDecodeViewBean(View view) throws IOException, SQLException;

  public void writePackageBean(Package dbPackage) throws IOException;

  public void writeViewBean(View dbView) throws IOException;

  public void writeTdeBean(Table table) throws IOException, SQLException;

  public void writeDBTypeBean(Type dbType) throws IOException;

}