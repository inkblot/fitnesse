// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;
import util.Clock;

import static util.RegexAssertions.assertHasRegexp;

public class MergeResponderTest extends FitnesseBaseTestCase {
    private MockRequest request;
    private FitNesseContext context;
    private HtmlPageFactory htmlPageFactory;
    private WikiPage root;
    private Clock clock;

    @Inject
    public void inject(Clock clock, HtmlPageFactory htmlPageFactory, @Named(FitNesseContextModule.ROOT_PAGE) WikiPage root, FitNesseContext context) {
        this.clock = clock;
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
        this.context = context;
    }

    @Before
    public void setUp() throws Exception {
        root.getPageCrawler().addPage(context.root, PathParser.parse("SimplePage"), "this is SimplePage");
        request = new MockRequest();
        request.setResource("SimplePage");
        request.addInput(EditResponder.TIME_STAMP, "");
        request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
    }

    @Test
    public void testHtml() throws Exception {
        Responder responder = new MergeResponder(request, htmlPageFactory, clock);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, new MockRequest());
        assertHasRegexp("name=\\\"" + EditResponder.CONTENT_INPUT_NAME + "\\\"", response.getContent());
        assertHasRegexp("this is SimplePage", response.getContent());
        assertHasRegexp("name=\\\"oldContent\\\"", response.getContent());
        assertHasRegexp("some new content", response.getContent());
    }

    @Test
    public void testAttributeValues() throws Exception {
        request.addInput("Edit", "On");
        request.addInput("PageType", "Test");
        request.addInput("Search", "On");
        Responder responder = new MergeResponder(request, htmlPageFactory, clock);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, new MockRequest());

        assertHasRegexp("type=\"hidden\"", response.getContent());
        assertHasRegexp("name=\"Edit\"", response.getContent());
        assertHasRegexp("name=\"PageType\" value=\"Test\" checked", response.getContent());
        assertHasRegexp("name=\"Search\"", response.getContent());
    }
}
