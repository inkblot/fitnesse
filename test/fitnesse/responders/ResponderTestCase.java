// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.WikiPage;
import junit.framework.TestCase;

public abstract class ResponderTestCase extends TestCase {
    protected WikiPage root;
    protected MockRequest request;
    protected Responder responder;
    protected PageCrawler crawler;
    protected FitNesseContext context;

    public void setUp() throws Exception {
        context = new FitNesseContext("RooT");
        root = context.root;
        crawler = root.getPageCrawler();
        request = new MockRequest();
        responder = responderInstance();
    }

    // Return an instance of the Responder being tested.
    protected abstract Responder responderInstance();
}
