// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertSubString;

public class ChunkingResponderTest extends FitnesseBaseTestCase {

    private Exception exception;
    private ChunkedResponse response;
    private WikiPage root;
    private ChunkingResponder responder;
    private HtmlPageFactory htmlPageFactory;
    private RunningTestingTracker runningTestingTracker;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root, RunningTestingTracker runningTestingTracker) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
        this.runningTestingTracker = runningTestingTracker;
    }

    @Before
    public void setUp() throws Exception {
        responder = new ChunkingResponder(htmlPageFactory, root, runningTestingTracker, isChunkingEnabled()) {
            protected void doSending(WikiPage root, WikiPagePath path, WikiPage page, RunningTestingTracker runningTestingTracker) throws Exception {
                throw exception;
            }
        };
    }

    @Test
    public void testException() throws Exception {
        exception = new Exception("test exception");
        response = (ChunkedResponse) responder.makeResponse(new MockRequest());
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String responseSender = sender.sentData();
        assertSubString("test exception", responseSender);
    }

    @Test
    public void chunkingShouldBeTurnedOffIfnochunkParameterIsPresent() throws Exception {
        MockRequest request = new MockRequest();
        request.addInput("nochunk", null);
        response = (ChunkedResponse) responder.makeResponse(request);
        assertTrue(response.isChunkingTurnedOff());
    }
}
