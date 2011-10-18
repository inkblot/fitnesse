// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.SingleContextBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class ErrorResponderTest extends SingleContextBaseTestCase {

    private HtmlPageFactory htmlPageFactory;
    private FitNesseContext context;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, FitNesseContext context) {
        this.htmlPageFactory = htmlPageFactory;
        this.context = context;
    }

    @Test
    public void testResponse() throws Exception {
        Responder responder = new ErrorResponder(new Exception("some error message"), htmlPageFactory);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, new MockRequest());

        assertEquals(400, response.getStatus());

        String body = response.getContent();

        assertHasRegexp("<html>", body);
        assertHasRegexp("<body", body);
        assertHasRegexp("java.lang.Exception: some error message", body);
    }

    @Test
    public void testWithMessage() throws Exception {
        Responder responder = new ErrorResponder("error Message", htmlPageFactory);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, new MockRequest());
        String body = response.getContent();

        assertSubString("error Message", body);
    }
}
