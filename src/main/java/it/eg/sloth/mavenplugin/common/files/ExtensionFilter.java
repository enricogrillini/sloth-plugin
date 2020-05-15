package it.eg.sloth.mavenplugin.common.files;

import java.io.File;

import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Project: sloth-framework
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
 *
 */
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