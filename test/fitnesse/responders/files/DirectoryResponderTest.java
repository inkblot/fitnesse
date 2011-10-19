// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ResponderFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;

public class DirectoryResponderTest extends SingleContextBaseTestCase {

    private MockRequest request;
    private SimpleResponse response;
    private FitNesseContext context;
    private String rootPagePath;
    private SampleFileUtility samples;

    @Inject
    public void inject(FitNesseContext context, @Named(FitNesseContextModule.ROOT_PAGE_PATH) String rootPagePath, SampleFileUtility samples) {
        this.context = context;
        this.rootPagePath = rootPagePath;
        this.samples = samples;
    }

    @Before
    public void setUp() throws Exception {
        request = new MockRequest();
        samples.makeSampleFiles();
        request.setResource("files/testDir/");
        Responder responder = ResponderFactory.makeFileResponder(context.getInjector(), request.getResource(), rootPagePath);
        response = (SimpleResponse) responder.makeResponse(context, request);
    }

    @Test
    public void testDirectoryListing() throws Exception {
        assertHasRegexp("testDir", response.getContent());
        assertHasRegexp("testFile2</a>", response.getContent());
        assertHasRegexp("testFile3</a>", response.getContent());
        assertHasRegexp("<a href=\"/", response.getContent());
    }

    @Test
    public void testButtons() throws Exception {
        assertHasRegexp("upload form", response.getContent());
        assertHasRegexp("create directory form", response.getContent());
    }

    @Test
    public void testHtml() throws Exception {
        assertHasRegexp("/files/", response.getContent());
    }

    @Test
    public void testRedirectForDirectory() throws Exception {
        request.setResource("files/testDir");
        Responder responder = ResponderFactory.makeFileResponder(context.getInjector(), request.getResource(), rootPagePath);
        Response response = responder.makeResponse(context, request);

        assertEquals(303, response.getStatus());
        assertEquals("/files/testDir/", response.getHeader("Location"));
    }

    @Test
    public void testFrontPageSidebarButtonPresent() throws Exception {
        assertHasRegexp("<div class=\"sidebar\">", response.getContent());
        assertHasRegexp("<div class=\"actions\">", response.getContent());
        assertHasRegexp("<a href=\"/FrontPage\" accesskey=\"f\">FrontPage</a>", response.getContent());
    }

    @Test
    public void testSizeString() throws Exception {
        assertEquals("", DirectoryResponder.getSizeString(samples.testDir));
        assertEquals("13 bytes", DirectoryResponder.getSizeString(samples.testFile1));
    }
}
