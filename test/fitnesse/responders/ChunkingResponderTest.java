// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertSubString;

public class ChunkingResponderTest extends FitnesseBaseTestCase {

    private Exception exception;
    private ChunkedResponse response;
    private FitNesseContext context;
    private ChunkingResponder responder;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    @Before
    public void setUp() throws Exception {
        context = makeContext();
        responder = new ChunkingResponder(htmlPageFactory) {
            protected void doSending(FitNesseContext context, WikiPage root) throws Exception {
                throw exception;
            }
        };
    }

    @Test
    public void testException() throws Exception {
        exception = new Exception("test exception");
        response = (ChunkedResponse) responder.makeResponse(context, new MockRequest());
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String responseSender = sender.sentData();
        assertSubString("test exception", responseSender);
    }

    @Test
    public void chunkingShouldBeTurnedOffIfnochunkParameterIsPresent() throws Exception {
        MockRequest request = new MockRequest();
        request.addInput("nochunk", null);
        response = (ChunkedResponse) responder.makeResponse(context, request);
        assertTrue(response.isChunkingTurnedOff());
    }
}
