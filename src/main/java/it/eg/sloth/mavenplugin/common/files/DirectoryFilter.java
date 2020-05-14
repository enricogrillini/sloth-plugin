package it.eg.sloth.mavenplugin.common.files;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.filefilter.IOFileFilter;

public class DirectoryFilter implements IOFileFilter {

  public static Set<String> REPOSITORY_EXCLUDE_SET = new HashSet<String>();

  static {
    REPOSITORY_EXCLUDE_SET.add(".svn");
  }

  private Set<String> excludeDirectory;

  public DirectoryFilter() {
    this(REPOSITORY_EXCLUDE_SET);
  }

  public DirectoryFilter(Set<String> excludeDirectory) {
    this.excludeDirectory = excludeDirectory;
  }

  public boolean accept(File pathname) {
    if (!pathname.isDirectory()) {
      return false;
    }

    for (String exclude : excludeDirectory) {
      if (pathname.getName().toLowerCase().endsWith(exclude.toLowerCase())) {
        return false;
      }
    }

    return true;
  }

  public boolean accept(File directory, String fileName) {
    return accept(new File(directory.getPath() + "/" + fileName));
  }
}