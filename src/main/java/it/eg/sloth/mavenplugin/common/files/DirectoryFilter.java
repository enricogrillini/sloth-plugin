package it.eg.sloth.mavenplugin.common.files;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.filefilter.IOFileFilter;

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
public class DirectoryFilter implements IOFileFilter {

    private static final Set<String> REPOSITORY_EXCLUDE_SET = new HashSet<>(Arrays.asList(".svn"));

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