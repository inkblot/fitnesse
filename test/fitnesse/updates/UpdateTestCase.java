// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.*;
import org.junit.Before;
import util.FileUtil;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public abstract class UpdateTestCase extends FitnesseBaseTestCase {

    protected WikiPage root;
    protected Update update;
    protected UpdaterBase updater;
    protected WikiPage pageOne;
    protected WikiPage pageTwo;
    protected FitNesseContext context;
    protected PageCrawler crawler;

    @Before
    public final void beforeUpdateTest() throws Exception {
        context = new FitNesseContext(injector, getRootPath(), "RooT");
        root = context.root;
        assertThat(root, instanceOf(FileSystemPage.class));

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
