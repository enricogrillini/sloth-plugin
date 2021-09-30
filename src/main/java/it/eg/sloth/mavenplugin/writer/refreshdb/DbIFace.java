package it.eg.sloth.mavenplugin.writer.refreshdb;

import it.eg.sloth.framework.common.exception.FrameworkException;
import it.eg.sloth.jaxb.dbschema.Packages;
import it.eg.sloth.jaxb.dbschema.Sequences;
import it.eg.sloth.jaxb.dbschema.Tables;
import it.eg.sloth.jaxb.dbschema.Views;

import java.io.IOException;
import java.sql.SQLException;

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
 *
 */
public interface DbIFace {

    public String getOwner();

    public Tables loadTables(String tableName) throws SQLException, IOException, FrameworkException;

    public Views loadViews() throws SQLException, IOException, FrameworkException;

    public Packages loadPackages() throws SQLException, IOException, FrameworkException;

    public Sequences loadSequences() throws SQLException, IOException, FrameworkException;

}
