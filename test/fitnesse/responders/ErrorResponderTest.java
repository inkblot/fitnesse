// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class ErrorResponderTest extends FitnesseBaseTestCase {

    @Test
    public void testResponse() throws Exception {
        Responder responder = new ErrorResponder(new Exception("some error message"));
        SimpleResponse response = (SimpleResponse) responder.makeResponse(makeContext(), new MockRequest());

        assertEquals(400, response.getStatus());

        String body = response.getContent();

        assertHasRegexp("<html>", body);
        assertHasRegexp("<body", body);
        assertHasRegexp("java.lang.Exception: some error message", body);
    }

    @Test
    public void testWithMessage() throws Exception {
        Responder responder = new ErrorResponder("error Message");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(makeContext(), new MockRequest());
        String body = response.getContent();

        assertSubString("error Message", body);
    }
}
