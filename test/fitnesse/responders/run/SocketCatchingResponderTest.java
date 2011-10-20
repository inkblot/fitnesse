// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.SimpleSocketSeeker;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertHasRegexp;

public class SocketCatchingResponderTest extends FitnesseBaseTestCase {
    private SocketDealer socketDealer;
    private SimpleSocketSeeker seeker;
    private MockResponseSender sender;
    private SocketCatchingResponder responder;
    private FitNesseContext context;
    private MockRequest request;

    @Inject
    public void inject(FitNesseContext context, SocketDealer socketDealer) {
        this.context = context;
        this.socketDealer = socketDealer;
    }

    @Before
    public void setUp() throws Exception {
        seeker = new SimpleSocketSeeker();
        sender = new MockResponseSender();
        responder = new SocketCatchingResponder(socketDealer);
        request = new MockRequest();
    }

    @Test
    public void testSuccess() throws Exception {
        int ticket = socketDealer.seekingSocket(seeker);
        request.addInput("ticket", ticket + "");
        Response response = responder.makeResponse(context, request);
        response.readyToSend(sender);

        assertEquals("", sender.sentData());
    }

    @Test
    public void testMissingSeeker() throws Exception {
        request.addInput("ticket", "123");
        Response response = responder.makeResponse(context, request);
        response.readyToSend(sender);

        assertHasRegexp("There are no clients waiting for a socket with ticketNumber 123", sender.sentData());
        assertTrue(sender.isClosed());
        assertEquals(404, response.getStatus());
    }

}
