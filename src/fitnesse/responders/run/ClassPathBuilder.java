// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.wiki.InheritedItemBuilder;
import fitnesse.wiki.WikiPage;
import util.Wildcard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassPathBuilder extends InheritedItemBuilder {
    private List<String> allPaths;
    private StringBuffer pathsString;
    private Set<String> addedPaths;

    public String getClasspath(WikiPage page) throws IOException {
        List<String> paths = getInheritedPathElements(page, new HashSet<WikiPage>());
        return createClassPathString(paths, getPathSeparator(page));
    }

    public String getPathSeparator(WikiPage page) throws IOException {
        return TestSystem.getPathSeparator(page.getData());
    }

    public List<String> getInheritedPathElements(WikiPage page, Set<WikiPage> visitedPages) throws IOException {
        return getInheritedItems(page, visitedPages);
    }

    public String createClassPathString(List<String> paths, String separator) {
        if (paths.isEmpty())
            return "defaultPath";

        pathsString = new StringBuffer();
        paths = expandWildcards(paths);
        addedPaths = new HashSet<String>();

        for (String path : paths)
            addPathToClassPathString(separator, path);

        return pathsString.toString();
    }

    private void addPathToClassPathString(String separator, String path) {
        path = surroundPathWithQuotesIfItHasSpaces(path);

        if (!addedPaths.contains(path)) {
            addedPaths.add(path);
            addSeparatorIfNecessary(pathsString, separator);
            pathsString.append(path);
        }
    }

    private String surroundPathWithQuotesIfItHasSpaces(String path) {
        if (path.matches(".*\\s.*") && !path.contains("\""))
            path = "\"" + path + "\"";
        return path;
    }

    private List<String> expandWildcards(Iterable<String> paths) {
        allPaths = new ArrayList<String>();
        for (String path : paths)
            expandWildcards(path);

        return allPaths;
    }

    private void expandWildcards(String path) {
        File file = new File(path);
        File dir = new File(file.getAbsolutePath()).getParentFile();
        if (isExpandableDoubleWildcard(path, dir))
            recursivelyAddMatchingFiles(path, dir);
        else if (isExpandableSingleWildcard(path, dir))
            addMatchingFiles(path, dir);
        else
            allPaths.add(path);
    }

    private void recursivelyAddMatchingFiles(String path, File dir) {
        String singleWildcardPath = convertDoubleToSingleWildcard(path);
        addMatchingSubFiles(singleWildcardPath, dir);
    }

    private boolean isExpandableSingleWildcard(String path, File dir) {
        return pathHasSingleWildcard(path) && dir.exists();
    }

    private boolean isExpandableDoubleWildcard(String path, File dir) {
        return pathHasDoubleWildCard(path) && dir.exists();
    }

    private boolean pathHasSingleWildcard(String path) {
        return path.indexOf('*') != -1;
    }

    private String convertDoubleToSingleWildcard(String path) {
        path = path.replaceFirst("\\*\\*", "*");
        return path;
    }

    private boolean pathHasDoubleWildCard(String path) {
        return path.contains("**");
    }

    private void addMatchingFiles(String path, File dir) {
        String fileName = new File(path).getName();
        File[] files = dir.listFiles(new Wildcard(fileName));
        for (File file : files) {
            allPaths.add(file.getPath());
        }
    }

    private void addMatchingSubFiles(String path, File dir) {
        addMatchingFiles(path, dir);
        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                addMatchingSubFiles(path, file);
        }
    }

    private void addSeparatorIfNecessary(StringBuffer pathsString, String separator) {
        if (pathsString.length() > 0)
            pathsString.append(separator);
    }

    protected List<String> getItemsFromPage(WikiPage page) throws IOException {
        return page.getData().getClasspaths();
    }
}