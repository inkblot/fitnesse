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

public class NotFoundResponderTest extends FitnesseBaseTestCase {
    @Test
    public void testResponse() throws Exception {
        MockRequest request = new MockRequest();
        request.setResource("some page");

        Responder responder = new NotFoundResponder();
        SimpleResponse response = (SimpleResponse) responder.makeResponse(makeContext(), request);

        assertEquals(404, response.getStatus());

        String body = response.getContent();

        assertHasRegexp("<html>", body);
        assertHasRegexp("<body", body);
        assertHasRegexp("some page", body);
        assertHasRegexp("Not Found", body);
    }

    @Test
    public void testHasEditLinkForWikiWords() throws Exception {
        MockRequest request = new MockRequest();
        request.setResource("PageOne.PageTwo");

        Responder responder = new NotFoundResponder();
        SimpleResponse response = (SimpleResponse) responder.makeResponse(makeContext(), request);

        assertHasRegexp("\"PageOne[.]PageTwo[?]edit\"", response.getContent());
    }

}
