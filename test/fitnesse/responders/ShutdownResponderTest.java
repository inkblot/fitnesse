// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.http.MockRequest;
import fitnesse.http.RequestBuilder;
import fitnesse.http.ResponseParser;
import fitnesse.testutil.FitNesseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShutdownResponderTest extends FitnesseBaseTestCase {
    private FitNesseContext context;
    private FitNesse fitnesse;
    private boolean doneShuttingDown;

    @Before
    public void setUp() throws Exception {
        context = makeContext();
        context.port = FitNesseUtil.DEFAULT_PORT;
        fitnesse = new FitNesse(context);
        fitnesse.start();
        context.fitnesse = fitnesse;
    }

    @After
    public void tearDown() throws Exception {
        fitnesse.stop();
    }

    @Test
    public void testFitNesseGetsShutdown() throws Exception {
        ShutdownResponder responder = new ShutdownResponder();
        responder.makeResponse(context, new MockRequest());
        Thread.sleep(200);
        assertFalse(fitnesse.isRunning());
    }

    @Test
    public void testShutdownCalledFromServer() throws Exception {
        Thread thread = new Thread() {
            public void run() {
                try {
                    RequestBuilder request = new RequestBuilder("/?responder=shutdown");
                    ResponseParser.performHttpRequest("localhost", FitNesseUtil.DEFAULT_PORT, request);
                    doneShuttingDown = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        Thread.sleep(500);

        assertTrue(doneShuttingDown);
        assertFalse(fitnesse.isRunning());
    }

    @Test
    public void testIsSecure() throws Exception {
        assertTrue(new ShutdownResponder().getSecureOperation() instanceof AlwaysSecureOperation);
    }
}
