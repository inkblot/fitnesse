// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.*;
import org.junit.Before;
import util.FileUtil;

import java.util.Properties;

public abstract class UpdateTestCase extends FitnesseBaseTestCase {

    protected WikiPage root;
    protected Update update;
    protected UpdaterBase updater;
    protected WikiPage pageOne;
    protected WikiPage pageTwo;
    protected FitNesseContext context;
    protected PageCrawler crawler;

    @Override
    protected Properties getFitNesseProperties() {
        Properties properties = super.getFitNesseProperties();
        properties.remove(WikiPageFactory.WIKI_PAGE_CLASS);
        return properties;
    }

    @Before
    public final void beforeUpdateTest() throws Exception {
        context = makeContext(FileSystemPage.class);
        root = context.root;

        FileUtil.makeDir(getRootPath());
        crawler = root.getPageCrawler();

        pageOne = crawler.addPage(root, PathParser.parse("PageOne"), "some content");
        pageTwo = crawler.addPage(pageOne, PathParser.parse("PageTwo"), "page two content");

        updater = new UpdaterBase(getRootPagePath());
        update = makeUpdate();

    }

    protected Update makeUpdate() throws Exception {
        return null;
    }
}
