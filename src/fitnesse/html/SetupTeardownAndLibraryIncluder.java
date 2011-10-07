// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import fitnesse.wiki.*;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SetupTeardownAndLibraryIncluder {
    private PageData pageData;
    private boolean isSuite;
    private WikiPage testPage;
    private StringBuffer newPageContent;
    private PageCrawler pageCrawler;


    public static void includeInto(PageData pageData) throws IOException {
        includeInto(pageData, false);
    }

    public static void includeInto(PageData pageData, boolean isSuite) throws IOException {
        new SetupTeardownAndLibraryIncluder(pageData).includeInto(isSuite);
    }

    public static void includeSetupsTeardownsAndLibrariesBelowTheSuite(PageData pageData, WikiPage suitePage) throws IOException {
        new SetupTeardownAndLibraryIncluder(pageData).includeSetupsTeardownsAndLibrariesBelowTheSuite(suitePage);
    }


    private SetupTeardownAndLibraryIncluder(PageData pageData) {
        this.pageData = pageData;
        testPage = pageData.getWikiPage();
        pageCrawler = testPage.getPageCrawler();
        newPageContent = new StringBuffer();
    }

    private void includeInto(boolean isSuite) throws IOException {
        this.isSuite = isSuite;
        if (isTestPage())
            includeSetupTeardownAndLibraryPages();
    }

    private boolean isTestPage() {
        return pageData.hasAttribute("Test");
    }

    private void includeSetupTeardownAndLibraryPages() throws IOException {
        includeScenarioLibraries();
        includeSetupPages();
        includePageContent();
        includeTeardownPages();
        updatePageContent();
    }

    private void includeSetupsTeardownsAndLibrariesBelowTheSuite(WikiPage suitePage) throws IOException {
        String pageName = testPage.getName();
        includeScenarioLibraryBelow(suitePage);
        if (!isSuiteSetUpOrTearDownPage(pageName))
            includeSetupPages();
        includePageContent();
        if (!isSuiteSetUpOrTearDownPage(pageName))
            includeTeardownPages();
        updatePageContent();
    }

    private boolean isSuiteSetUpOrTearDownPage(String pageName) {
        return PageData.SUITE_SETUP_NAME.equals(pageName) || PageData.SUITE_TEARDOWN_NAME.equals(pageName);
    }

    private void includeScenarioLibraryBelow(WikiPage suitePage) throws IOException {
        includeScenarioLibrariesIfAppropriate(new BelowSuiteLibraryFilter(suitePage));
    }

    private void includeScenarioLibraries() throws IOException {
        includeScenarioLibrariesIfAppropriate(AllLibrariesFilter.instance);

    }

    private void includeSetupPages() throws IOException {
        if (isSuite)
            includeSuiteSetupPage();
        includeSetupPage();
    }

    private void includeSuiteSetupPage() throws IOException {
        include(PageData.SUITE_SETUP_NAME, "-setup");
    }

    private void includeSetupPage() throws IOException {
        include("SetUp", "-setup");
    }

    private void includePageContent() {
        newPageContent.append(pageData.getContent());
    }

    private void includeTeardownPages() throws IOException {
        includeTeardownPage();
        if (isSuite)
            includeSuiteTeardownPage();
    }

    private void includeTeardownPage() throws IOException {
        include("TearDown", "-teardown");
    }

    private void includeSuiteTeardownPage() throws IOException {
        include(PageData.SUITE_TEARDOWN_NAME, "-teardown");
    }

    private void updatePageContent() {
        pageData.setContent(newPageContent.toString());
    }

    private void include(String pageName, String arg) throws IOException {
        WikiPage inheritedPage = findInheritedPage(pageName);
        if (inheritedPage != null) {
            String pagePathName = getPathNameForPage(inheritedPage);
            includePage(pagePathName, arg);
        }
    }

    private void includeScenarioLibrariesIfAppropriate(LibraryFilter libraryFilter) throws IOException {
        if (isSlim(testPage))
            includeScenarioLibrariesIfAny(libraryFilter);
    }

    private void includeScenarioLibrariesIfAny(LibraryFilter libraryFilter) throws IOException {
        List<WikiPage> uncles = PageCrawlerImpl.getAllUncles("ScenarioLibrary", testPage);

        List<WikiPage> filteredUncles = filter(uncles, libraryFilter);
        if (filteredUncles.size() > 0)
            includeScenarioLibraries(filteredUncles);
    }

    private List<WikiPage> filter(List<WikiPage> widgets, LibraryFilter filter) {
        List<WikiPage> filteredList = new LinkedList<WikiPage>();
        for (WikiPage widget : widgets) {
            if (filter.canUse(widget))
                filteredList.add(widget);
        }
        return filteredList;
    }

    private boolean isSlim(WikiPage page) throws IOException {
        String testSystem = page.getData().getVariable("TEST_SYSTEM");
        return "slim".equalsIgnoreCase(testSystem);
    }

    private void includeScenarioLibraries(List<WikiPage> uncles) {
        Collections.reverse(uncles);
        newPageContent.append("!*> Scenario Libraries\n");
        for (WikiPage uncle : uncles)
            includeScenarioLibrary(uncle);
        newPageContent.append("*!\n");
    }

    private void includeScenarioLibrary(WikiPage uncle) {
        newPageContent.append("!include -c .");
        newPageContent.append(PathParser.render(pageCrawler.getFullPath(uncle)));
        newPageContent.append("\n");
    }

    private WikiPage findInheritedPage(String pageName) throws IOException {
        return PageCrawlerImpl.getClosestInheritedPage(pageName, testPage);
    }

    private String getPathNameForPage(WikiPage page) {
        WikiPagePath pagePath = pageCrawler.getFullPath(page);
        return PathParser.render(pagePath);
    }

    private void includePage(String pagePathName, String arg) {
        newPageContent
                .append("\n!include ")
                .append(arg)
                .append(" .")
                .append(pagePathName)
                .append("\n");
    }

    private static interface LibraryFilter {
        boolean canUse(WikiPage libraryPage);
    }

    private static class AllLibrariesFilter implements LibraryFilter {
        public static AllLibrariesFilter instance = new AllLibrariesFilter();

        @Override
        public boolean canUse(WikiPage libraryPage) {
            return true;
        }
    }

    private class BelowSuiteLibraryFilter implements LibraryFilter {
        private int minimumPathLength;

        public BelowSuiteLibraryFilter(WikiPage suitePage) {
            minimumPathLength = suitePage.getPageCrawler().getFullPath(suitePage).addNameToEnd("ScenarioLibrary").toString().length();
        }

        @Override
        public boolean canUse(WikiPage libraryPage) {
            return libraryPage.getPageCrawler().getFullPath(libraryPage).toString().length() > minimumPathLength;
        }
    }


}
