package it.eg.sloth.mavenplugin.writer.refreshdb;

import it.eg.sloth.jaxb.dbschema.Packages;
import it.eg.sloth.jaxb.dbschema.Sequences;
import it.eg.sloth.jaxb.dbschema.Tables;
import it.eg.sloth.jaxb.dbschema.Views;

import java.io.IOException;
import java.sql.SQLException;

public interface DbIFace {

    public String getOwner();

    public Tables loadTables(String tableName) throws SQLException, IOException;

    public Views loadViews() throws SQLException, IOException;

    public Packages loadPackages() throws SQLException, IOException;

    public Sequences loadSequences() throws SQLException, IOException;

}
