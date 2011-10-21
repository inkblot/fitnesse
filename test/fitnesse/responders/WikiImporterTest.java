// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Key;
import com.google.inject.name.Names;
import fitnesse.FitNesseModule;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import util.Clock;
import util.XmlUtil;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static util.RegexAssertions.assertSubString;

public class WikiImporterTest extends ImporterTestCase implements WikiImporterClient {
    private WikiImporter importer;
    private LinkedList<WikiPage> imports;
    private LinkedList<Exception> errors;

    private Clock clock;

    @Before
    public void setUp() throws Exception {
        clock = localInjector.getInstance(Clock.class);
        fitNesseUtil = remoteInjector.getInstance(FitNesseUtil.class);
        fitNesseUtil.startFitnesse();

        importer = new WikiImporter();
        importer.setWikiImporterClient(this);
        importer.parseUrl(FitNesseUtil.URL);

        imports = new LinkedList<WikiPage>();
        errors = new LinkedList<Exception>();
    }

    @After
    public void tearDown() throws Exception {
        fitNesseUtil.stopFitnesse();
    }

    @Test
    public void testEnterChildPage() throws Exception {
        importer.enterChildPage(pageOne, clock.currentClockDate());

        PageData data = pageOne.getData();
        assertEquals("page one", data.getContent());
    }

    @Test
    public void testChildPageAdded() throws Exception {
        importer.enterChildPage(pageOne, clock.currentClockDate());
        importer.enterChildPage(childPageOne, clock.currentClockDate());

        PageData data = childPageOne.getData();
        assertEquals("child one", data.getContent());
    }

    @Test
    public void testEnterChildPageWhenRemotePageNotModified() throws Exception {
        importer.enterChildPage(pageOne, clock.currentClockDate());
        importer.exitPage();

        PageData data = pageOne.getData();
        data.setContent("new content");
        pageOne.commit(data);

        importer.enterChildPage(pageOne, new Date(0));

        assertEquals("new content", pageOne.getData().getContent());
    }

    @Test
    public void testExiting() throws Exception {
        importer.enterChildPage(pageOne, clock.currentClockDate());
        importer.enterChildPage(childPageOne, clock.currentClockDate());
        importer.exitPage();
        importer.exitPage();
        importer.enterChildPage(pageTwo, clock.currentClockDate());

        PageData data = pageTwo.getData();
        assertEquals("page two", data.getContent());
    }

    @Test
    public void testGetPageTree() throws Exception {
        Document doc = importer.getPageTree();
        assertNotNull(doc);
        String xml = XmlUtil.xmlAsString(doc);

        assertSubString("PageOne", xml);
        assertSubString("PageTwo", xml);
    }

    @Test
    public void testUrlParsing() throws Exception {
        assertUrlParsing("http://mysite.com", "mysite.com", 80, "");
        assertUrlParsing("http://mysite.com/", "mysite.com", 80, "");
        assertUrlParsing("http://mysite.com:8080/", "mysite.com", 8080, "");
        assertUrlParsing("http://mysite.com:8080", "mysite.com", 8080, "");
        assertUrlParsing("http://mysite.com:80/", "mysite.com", 80, "");
        assertUrlParsing("http://mysite.com/PageOne", "mysite.com", 80, "PageOne");
        assertUrlParsing("http://mysite.com/PageOne.ChildOne", "mysite.com", 80, "PageOne.ChildOne");
    }

    private void assertUrlParsing(String url, String host, int port, String path) throws Exception {
        importer.parseUrl(url);
        assertEquals(host, importer.getRemoteHostname());
        assertEquals(port, importer.getRemotePort());
        assertEquals(path, PathParser.render(importer.getRemotePath()));
    }

    @Test
    public void testParsingBadUrl() throws Exception {
        try {
            importer.parseUrl("blah");
            fail("should have exception");
        } catch (MalformedURLException e) {
            // very good
        }
    }

    @Test
    public void testParsingUrlWithNonWikiWord() throws Exception {
        try {
            importer.parseUrl("http://blah.com/notawikiword");
            fail("should throw exception");
        } catch (Exception e) {
            assertEquals("The URL's resource path, notawikiword, is not a valid WikiWord.", e.getMessage());
        }
    }

    @Test
    public void testImportingWiki() throws Exception {
        localRoot = localInjector.getInstance(Key.get(WikiPage.class, Names.named(FitNesseModule.ROOT_PAGE)));
        importer.importWiki(localRoot);

        assertEquals(2, localRoot.getChildren().size());
        assertEquals(3, imports.size());
        assertEquals(0, errors.size());
    }

    @Test
    public void testFindsOrphansOnLocalWiki() throws Exception {
        performImportWithExtraLocalPages();

        List<WikiPagePath> orphans = importer.getOrphans();
        assertEquals(3, orphans.size());
        assertTrue(orphans.contains(new WikiPagePath().addNameToEnd("PageThree")));
        assertTrue(orphans.contains(new WikiPagePath().addNameToEnd("PageOne").addNameToEnd("ChildTwo")));
        assertTrue(orphans.contains(new WikiPagePath().addNameToEnd("PageOne").addNameToEnd("ChildOne").addNameToEnd("GrandChildOne")));
        assertFalse(orphans.contains(new WikiPagePath().addNameToEnd("PageThatDoesntImport")));
        assertFalse(orphans.contains(new WikiPagePath().addNameToEnd("OtherImportRoot")));
    }

    private void performImportWithExtraLocalPages() throws Exception {
        addLocalPageWithImportProperty(localRoot, "PageThree", false);
        addLocalPageWithImportProperty(pageOne, "ChildTwo", false);
        addLocalPageWithImportProperty(childPageOne, "GrandChildOne", false);
        localRoot.addChildPage("PageThatDoesntImport");
        addLocalPageWithImportProperty(localRoot, "OtherImportRoot", true);

        importer.importWiki(localRoot);
    }

    @Test
    public void testOrphansAreRemoved() throws Exception {
        performImportWithExtraLocalPages();

        assertFalse(localRoot.hasChildPage("PageThree"));
        assertFalse(pageOne.hasChildPage("ChildTwo"));
        assertFalse(childPageOne.hasChildPage("GrandChildOne"));

        assertTrue(localRoot.hasChildPage("PageThatDoesntImport"));
        assertTrue(localRoot.hasChildPage("OtherImportRoot"));
    }

    @Test
    public void testWholeTreeOrphaned() throws Exception {
        importer.importWiki(localRoot);

        remoteRoot.removeChildPage("PageOne");

        importer.importWiki(localRoot);

        assertFalse(localRoot.hasChildPage("PageOne"));
    }

    @Test
    public void testContextIsNotOrphanWhenUpdatingNonRoot() throws Exception {
        addLocalPageWithImportProperty(localRoot, "PageOne", false);
        importer.parseUrl(FitNesseUtil.URL + "PageOne");

        importer.importWiki(localRoot.getChildPage("PageOne"));

        assertEquals(0, importer.getOrphans().size());
    }

    @Test
    public void testAutoUpdatePropertySetOnRoot() throws Exception {
        addLocalPageWithImportProperty(localRoot, "PageOne", false);
        importer.parseUrl(FitNesseUtil.URL + "PageOne");
        importer.setAutoUpdateSetting(true);
        WikiPage importedPage = localRoot.getChildPage("PageOne");
        importer.importWiki(importedPage);

        WikiImportProperty importProp = WikiImportProperty.createFrom(importedPage.getData().getProperties());
        assertTrue(importProp.isAutoUpdate());

        importer.setAutoUpdateSetting(false);
        importer.importWiki(importedPage);

        importProp = WikiImportProperty.createFrom(importedPage.getData().getProperties());
        assertFalse(importProp.isAutoUpdate());
    }

    @Test
    public void testAutoUpdate_NewPage() throws Exception {
        importer.setAutoUpdateSetting(true);
        importer.enterChildPage(pageOne, clock.currentClockDate());

        WikiImportProperty importProps = WikiImportProperty.createFrom(pageOne.getData().getProperties());
        assertTrue(importProps.isAutoUpdate());
    }

    @Test
    public void testAutoUpdateWhenRemotePageNotModified() throws Exception {
        importer.enterChildPage(pageOne, clock.currentClockDate());
        importer.exitPage();

        PageData data = pageOne.getData();
        data.setContent("new content");
        pageOne.commit(data);

        importer.setAutoUpdateSetting(true);
        importer.enterChildPage(pageOne, new Date(0));

        WikiImportProperty importProps = WikiImportProperty.createFrom(pageOne.getData().getProperties());
        assertTrue(importProps.isAutoUpdate());
    }

    private WikiPage addLocalPageWithImportProperty(WikiPage parentPage, String pageName, boolean isRoot) throws Exception {
        WikiPage page = parentPage.addChildPage(pageName);
        PageData data = page.getData();

        WikiPagePath pagePath = localRoot.getPageCrawler().getFullPath(page);
        WikiImportProperty importProps = new WikiImportProperty(FitNesseUtil.URL + PathParser.render(pagePath));
        if (isRoot)
            importProps.setRoot(true);
        importProps.addTo(data.getProperties());
        page.commit(data);

        return page;
    }

    public void pageImported(WikiPage page) {
        imports.add(page);
    }

    public void pageImportError(WikiPage page, Exception e) {
        errors.add(e);
    }
}
