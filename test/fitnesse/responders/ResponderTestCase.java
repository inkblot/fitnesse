// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.Responder;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.WikiPage;
import org.junit.Before;

public abstract class ResponderTestCase extends FitnesseBaseTestCase {
    protected WikiPage root;
    protected MockRequest request;
    protected Responder responder;
    protected PageCrawler crawler;
    protected HtmlPageFactory htmlPageFactory;

    @Inject
    public final void inject(@Named(FitNesseModule.ROOT_PAGE) WikiPage root, HtmlPageFactory htmlPageFactory) {
        this.root = root;
        this.htmlPageFactory = htmlPageFactory;
    }

    @Before
    public void beforeResponderTests() throws Exception {
        crawler = root.getPageCrawler();
        request = new MockRequest();
        responder = responderInstance();
    }

    // Return an instance of the Responder being tested.
    protected abstract Responder responderInstance();
}
