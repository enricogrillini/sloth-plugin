package it.eg.sloth.mavenplugin.common.files;

import java.io.File;

import org.apache.commons.io.filefilter.IOFileFilter;

public class ExtensionFilter implements IOFileFilter {

  private String extension;

  public ExtensionFilter(String extension) {
    this.extension = extension;
  }

  public boolean accept(File file) {
    return file.getName().endsWith(extension);
  }

  public boolean accept(File directory, String fileName) {
    return accept(new File(directory.getPath() + "/" + fileName));
  }

}