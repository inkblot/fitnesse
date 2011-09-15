// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.ComponentFactory;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import util.FileUtil;

import java.util.Properties;

public abstract class UpdateTestCase extends FitnesseBaseTestCase {
    public static final String rootName = "RooT";

    protected WikiPage root;
    protected Update update;
    protected UpdaterBase updater;
    protected WikiPage pageOne;
    protected WikiPage pageTwo;
    protected FitNesseContext context;
    protected PageCrawler crawler;

    @Before
    public final void beforeUpdateTest() throws Exception {
        root = new FileSystemPage(getRootPath(), rootName);
        context = new FitNesseContext(root, getRootPath());

        FileUtil.makeDir(getRootPath());
        crawler = root.getPageCrawler();

        pageOne = crawler.addPage(root, PathParser.parse("PageOne"), "some content");
        pageTwo = crawler.addPage(pageOne, PathParser.parse("PageTwo"), "page two content");

        updater = new UpdaterBase(context);
        update = makeUpdate();

    }

    protected Update makeUpdate() throws Exception {
        return null;
    }
}
