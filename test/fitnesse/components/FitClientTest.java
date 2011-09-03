// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.http.MockSocket;
import fitnesse.responders.run.SocketDealer;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.testutil.FitSocketReceiver;
import fitnesse.testutil.SimpleSocketDonor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.TimeMeasurement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertSubString;

public class FitClientTest extends FitnesseBaseTestCase implements TestSystemListener {
    private List<String> outputs = new ArrayList<String>();
    private List<TestSummary> counts = new ArrayList<TestSummary>();
    private CommandRunningFitClient client;
    private boolean exceptionOccurred = false;
    private int port = 9080;
    private FitSocketReceiver receiver;
    private SimpleSocketDonor doner;

    @Before
    public void setUp() throws Exception {
        CommandRunningFitClient.TIMEOUT = 5000;
        client = new CommandRunningFitClient(this, "java -cp " + classPath() + " fit.FitServer -v", port, new SocketDealer());
        receiver = new CustomFitSocketReceiver(port);
    }

    private class CustomFitSocketReceiver extends FitSocketReceiver {
        public CustomFitSocketReceiver(int port) {
            super(port, null);
        }

        protected void dealSocket(int ticket) throws Exception {
            doner = new SimpleSocketDonor(socket);
            client.acceptSocketFrom(doner);
        }
    }

    @After
    public void tearDown() throws Exception {
        receiver.close();
    }

    public void acceptOutputFirst(String output) {
        outputs.add(output);
    }

    public void testComplete(TestSummary testSummary) {
        this.counts.add(testSummary);
    }

    public void exceptionOccurred(Throwable e) {
        exceptionOccurred = true;
        try {
            client.kill();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Test
    public void testOneRunUsage() throws Exception {
        doSimpleRun();
        assertFalse(exceptionOccurred);
        assertEquals(1, outputs.size());
        assertEquals(1, counts.size());
        assertSubString("class", outputs.get(0));
        assertEquals(1, counts.get(0).getRight());
    }

    private void doSimpleRun() throws Exception {
        receiver.receiveSocket();
        client.start();
        Thread.sleep(100);
        client.send("<html><table><tr><td>fitnesse.testutil.PassFixture</td></tr></table></html>");
        client.done();
        client.join();
    }

    @Test
    public void testStandardError() throws Exception {
        client = new CommandRunningFitClient(this, "java blah", port, new SocketDealer());
        client.start();
        Thread.sleep(100);
        client.join();
        assertTrue(exceptionOccurred);
        assertSubString("Exception", client.commandRunner.getError());
    }

    @Test
    public void testDoesntwaitForTimeoutOnBadCommand() throws Exception {
        CommandRunningFitClient.TIMEOUT = 5000;
        TimeMeasurement measurement = new TimeMeasurement().start();
        client = new CommandRunningFitClient(this, "java blah", port, new SocketDealer());
        client.start();
        Thread.sleep(50);
        client.join();
        assertTrue(exceptionOccurred);
        assertTrue(measurement.elapsed() < CommandRunningFitClient.TIMEOUT);

    }

    @Test
    public void testOneRunWithManyTables() throws Exception {
        receiver.receiveSocket();
        client.start();
        client.send("<html><table><tr><td>fitnesse.testutil.PassFixture</td></tr></table>" +
                "<table><tr><td>fitnesse.testutil.FailFixture</td></tr></table>" +
                "<table><tr><td>fitnesse.testutil.ErrorFixture</td></tr></table></html>");
        client.done();
        client.join();
        assertFalse(exceptionOccurred);
        assertEquals(3, outputs.size());
        assertEquals(1, counts.size());
        TestSummary count = counts.get(0);
        assertEquals(1, count.getRight());
        assertEquals(1, count.getWrong());
        assertEquals(1, count.getExceptions());
    }

    @Test
    public void testManyRuns() throws Exception {
        receiver.receiveSocket();
        client.start();
        client.send("<html><table><tr><td>fitnesse.testutil.PassFixture</td></tr></table></html>");
        client.send("<html><table><tr><td>fitnesse.testutil.FailFixture</td></tr></table></html>");
        client.send("<html><table><tr><td>fitnesse.testutil.ErrorFixture</td></tr></table></html>");
        client.done();
        client.join();

        assertFalse(exceptionOccurred);
        assertEquals(3, outputs.size());
        assertEquals(3, counts.size());
        assertEquals(1, (counts.get(0)).getRight());
        assertEquals(1, (counts.get(1)).getWrong());
        assertEquals(1, (counts.get(2)).getExceptions());
    }

    @Test
    public void testDonerIsNotifiedWhenFinished_success() throws Exception {
        doSimpleRun();
        assertTrue(doner.finished);
    }

    @Test
    public void testReadyForSending() throws Exception {
        CommandRunningFitClient.TIMEOUT = 5000;
        Thread startThread = new Thread() {
            public void run() {
                try {
                    client.start();
                } catch (InterruptedException ie) {
                    // yep, we're ignoring it
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        startThread.start();
        Thread.sleep(100);
        assertFalse(client.isSuccessfullyStarted());

        client.acceptSocketFrom(new SimpleSocketDonor(new MockSocket("")));
        Thread.sleep(100);
        assertTrue(client.isSuccessfullyStarted());

        startThread.interrupt();
    }

    @Test
    public void testUnicodeCharacters() throws Exception {
        receiver.receiveSocket();
        client.start();
        client.send("<html><table><tr><td>fitnesse.testutil.EchoFixture</td><td>\uba80\uba81\uba82\uba83</td></tr></table></html>");
        client.done();
        client.join();

        assertFalse(exceptionOccurred);
        StringBuilder buffer = new StringBuilder();
        for (String output : outputs) buffer.append(output);

        assertSubString("\uba80\uba81\uba82\uba83", buffer.toString());
    }
}
