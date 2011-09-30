// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.UnauthorizedResponder;
import fitnesse.http.*;
import fitnesse.http.MockSocket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FitNesseExpediterTest extends FitnesseBaseTestCase {
    private FitNesseExpediter expediter;
    private MockSocket socket;
    private FitNesseContext context;
    private PipedInputStream clientInput;
    private PipedOutputStream clientOutput;
    private ResponseParser response;

    @Before
    public void setUp() throws Exception {
        context = makeContext();
        context.root.addChildPage("FrontPage");
        socket = new MockSocket();
        expediter = new FitNesseExpediter(socket, context);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAuthenticationGetsCalled() throws Exception {
        context.authenticator = new StoneWallAuthenticator();
        MockRequest request = new MockRequest();
        Response response = expediter.createGoodResponse(request);
        assertEquals(401, response.getStatus());
    }

    @Test
    public void testClosedSocketMidResponse() throws Exception {
        try {
            MockRequest request = new MockRequest();
            Response response = expediter.createGoodResponse(request);
            socket.close();
            response.readyToSend(expediter);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail("no IOException should be thrown");
        }
    }

    @Test
    public void testIncompleteRequestsTimeOut() throws Exception {
        final FitNesseExpediter sender = preparePipedFitNesseExpediter();

        Thread senderThread = makeSendingThread(sender);
        senderThread.start();
        Thread parseResponseThread = makeParsingThread();
        parseResponseThread.start();
        Thread.sleep(300);

        parseResponseThread.join();

        assertEquals(408, response.getStatus());
    }

    private FitNesseExpediter preparePipedFitNesseExpediter() throws Exception {
        PipedInputStream socketInput = new PipedInputStream();
        clientOutput = new PipedOutputStream(socketInput);
        clientInput = new PipedInputStream();
        PipedOutputStream socketOutput = new PipedOutputStream(clientInput);
        MockSocket socket = new MockSocket(socketInput, socketOutput);
        return new FitNesseExpediter(socket, context, 200);
    }

    @Test
    public void testCompleteRequest() throws Exception {
        final FitNesseExpediter sender = preparePipedFitNesseExpediter();

        Thread senderThread = makeSendingThread(sender);
        senderThread.start();
        Thread parseResponseThread = makeParsingThread();
        parseResponseThread.start();

        clientOutput.write("GET /root HTTP/1.1\r\n\r\n".getBytes());
        clientOutput.flush();

        parseResponseThread.join();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testSlowButCompleteRequest() throws Exception {
        final FitNesseExpediter sender = preparePipedFitNesseExpediter();

        Thread senderThread = makeSendingThread(sender);
        senderThread.start();
        Thread parseResponseThread = makeParsingThread();
        parseResponseThread.start();

        byte[] bytes = "GET /root HTTP/1.1\r\n\r\n".getBytes();
        try {
            for (byte aByte : bytes) {
                clientOutput.write(aByte);
                clientOutput.flush();
                Thread.sleep(20);
            }
        } catch (IOException pipedClosed) {
        }

        parseResponseThread.join();

        assertEquals(200, response.getStatus());
    }

    private Thread makeSendingThread(final FitNesseExpediter sender) {
        return new Thread(new Runnable() {
            public void run() {
                try {
                    sender.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Thread makeParsingThread() {
        return new Thread(new Runnable() {
            public void run() {
                try {
                    response = new ResponseParser(clientInput);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class StoneWallAuthenticator extends Authenticator {
        public Responder authenticate(FitNesseContext context, Request request, Responder privilegedResponder) {
            return new UnauthorizedResponder();
        }

        public boolean isAuthenticated(String username, String password) {
            return false;
        }
    }

}
