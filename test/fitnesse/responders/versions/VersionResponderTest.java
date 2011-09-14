// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import org.junit.Test;

import static util.RegexAssertions.*;

public class VersionResponderTest extends FitnesseBaseTestCase {
    private String oldVersion;
    private SimpleResponse response;

    private void makeTestResponse(String pageName) throws Exception {
        FitNesseContext context = new FitNesseContext("RooT");
        WikiPage page = context.root.getPageCrawler().addPage(context.root, PathParser.parse(pageName), "original content");
        PageData data = page.getData();
        data.setContent("new stuff");
        VersionInfo commitRecord = page.commit(data);
        oldVersion = commitRecord.getName();

        MockRequest request = new MockRequest();
        request.setResource(pageName);
        request.addInput("version", oldVersion);

        Responder responder = new VersionResponder();
        response = (SimpleResponse) responder.makeResponse(context, request);
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
