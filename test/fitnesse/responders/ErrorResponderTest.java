// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import junit.framework.TestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;

import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class ErrorResponderTest extends TestCase {
    public void testResponse() throws Exception {
        Responder responder = new ErrorResponder(new Exception("some error message"));
        SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext("RooT"), new MockRequest());

        assertEquals(400, response.getStatus());

        String body = response.getContent();

        assertHasRegexp("<html>", body);
        assertHasRegexp("<body", body);
        assertHasRegexp("java.lang.Exception: some error message", body);
    }

    public void testWithMessage() throws Exception {
        Responder responder = new ErrorResponder("error Message");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext("RooT"), new MockRequest());
        String body = response.getContent();

        assertSubString("error Message", body);
    }
}
