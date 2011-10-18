// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseContextModule;
import fitnesse.SingleContextBaseTestCase;
import fitnesse.Updater;
import fitnesse.wiki.*;
import org.junit.Before;
import util.FileUtil;

import java.util.Properties;

public abstract class UpdateTestCase extends SingleContextBaseTestCase {

    protected WikiPage root;
    protected String rootPagePath;
    protected Update update;
    protected UpdaterBase updater;
    protected WikiPage pageOne;
    protected WikiPage pageTwo;
    protected FitNesseContext context;
    protected PageCrawler crawler;

    @Override
    protected Module getOverrideModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Updater.class).to(UpdaterBase.class);
            }
        };
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.remove(WikiPageFactory.WIKI_PAGE_CLASS);
        return properties;
    }

    @Inject
    public void inject(FitNesseContext context, @Named(FitNesseContextModule.ROOT_PAGE) WikiPage root, @Named(FitNesseContextModule.ROOT_PAGE_PATH) String rootPagePath, Updater updater) {
        this.context = context;
        this.root = root;
        this.rootPagePath = rootPagePath;
        this.updater = (UpdaterBase) updater;
    }

    @Before
    public final void beforeUpdateTest() throws Exception {
        FileUtil.makeDir(getRootPath());
        crawler = root.getPageCrawler();

        pageOne = crawler.addPage(root, PathParser.parse("PageOne"), "some content");
        pageTwo = crawler.addPage(pageOne, PathParser.parse("PageTwo"), "page two content");

        update = makeUpdate();

    }

    protected Update makeUpdate() throws Exception {
        return null;
    }
}
