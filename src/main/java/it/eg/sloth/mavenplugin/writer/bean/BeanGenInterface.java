package it.eg.sloth.mavenplugin.writer.bean;

import it.eg.sloth.jaxb.dbschema.Package;
import it.eg.sloth.jaxb.dbschema.Table;
import it.eg.sloth.jaxb.dbschema.Type;
import it.eg.sloth.jaxb.dbschema.View;

import java.io.IOException;
import java.sql.SQLException;

/**
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