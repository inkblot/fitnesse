// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import org.junit.Test;

import static util.RegexAssertions.*;

public class VersionResponderTest extends FitnesseBaseTestCase {
    private String oldVersion;
    private SimpleResponse response;
    private HtmlPageFactory htmlPageFactory;
    private WikiPage root;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    private void makeTestResponse(String pageName) throws Exception {
        WikiPage page = root.getPageCrawler().addPage(root, PathParser.parse(pageName), "original content");
        PageData data = page.getData();
        data.setContent("new stuff");
        VersionInfo commitRecord = page.commit(data);
        oldVersion = commitRecord.getName();

        MockRequest request = new MockRequest();
        request.setResource(pageName);
        request.addInput("version", oldVersion);

        Responder responder = new VersionResponder(htmlPageFactory, root);
        response = (SimpleResponse) responder.makeResponse(request);
    }

    @Test
    public void testVersionName() throws Exception {
        makeTestResponse("PageOne");

        assertHasRegexp("original content", response.getContent());
        assertDoesNotHaveRegexp("new stuff", response.getContent());
        assertHasRegexp(oldVersion, response.getContent());
    }

    @Test
    public void testButtons() throws Exception {
        makeTestResponse("PageOne");

        assertDoesNotHaveRegexp("Edit button", response.getContent());
        assertDoesNotHaveRegexp("Search button", response.getContent());
        assertDoesNotHaveRegexp("Test button", response.getContent());
        assertDoesNotHaveRegexp("Suite button", response.getContent());
        assertDoesNotHaveRegexp("Versions button", response.getContent());

        assertHasRegexp("Rollback button", response.getContent());
    }

    @Test
    public void testNameNoAtRootLevel() throws Exception {
        makeTestResponse("PageOne.PageTwo");
        assertSubString("PageOne.PageTwo?responder=", response.getContent());
    }
}
