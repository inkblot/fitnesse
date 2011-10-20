// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import fitnesse.Responder;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;

public class NotFoundResponderTest extends FitnesseBaseTestCase {
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    @Test
    public void testResponse() throws Exception {
        MockRequest request = new MockRequest();
        request.setResource("some page");

        Responder responder = new NotFoundResponder(htmlPageFactory);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(request);

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

        Responder responder = new NotFoundResponder(htmlPageFactory);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(request);

        assertHasRegexp("\"PageOne[.]PageTwo[?]edit\"", response.getContent());
    }

}
