// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.http.MockSocket;
import fitnesse.testutil.SimpleSocketSeeker;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;

public class SocketDealerTest extends TestCase {
    private SocketDealer dealer;
    private SimpleSocketSeeker seeker;
    private SimpleDonor doner;

    public void setUp() throws Exception {
        dealer = new SocketDealer();
    }

    public void tearDown() throws Exception {
    }

    public static class SimpleDonor implements SocketDonor {
        public MockSocket socket = new MockSocket("");
        public InputStream input = socket.getInputStream();
        public OutputStream output = socket.getOutputStream();
        boolean finished = false;

        @Override
        public InputStream donateInputStream() throws IOException {
            return input;
        }

        @Override
        public OutputStream donateOutputStream() throws IOException {
            return output;
        }

        public void finishedWithSocket() {
            finished = true;
        }
    }

    public void testAddSeeker() throws Exception {
        SocketSeeker seeker = new SimpleSocketSeeker();
        dealer.seekingSocket(seeker);

        Collection<SocketSeeker> waiting = dealer.getWaitingList();
        assertEquals(1, waiting.size());
        assertTrue(waiting.contains(seeker));
    }

    public void testUniqueTicketNumber() throws Exception {
        int ticketNumber1 = dealer.seekingSocket(new SimpleSocketSeeker());
        int ticketNumber2 = dealer.seekingSocket(new SimpleSocketSeeker());
        assertTrue(ticketNumber1 != ticketNumber2);
    }

    public void testDealSocketTo() throws Exception {
        doSimpleDealing();
        assertSame(doner.input, seeker.input);
        assertSame(doner.output, seeker.output);
    }

    private void doSimpleDealing() throws Exception {
        seeker = new SimpleSocketSeeker();
        int ticket = dealer.seekingSocket(seeker);
        doner = new SimpleDonor();
        dealer.dealSocketTo(ticket, doner);
    }

    public void testDealSocketToMultipleSeekers() throws Exception {
        SimpleSocketSeeker seeker1 = new SimpleSocketSeeker();
        SimpleSocketSeeker seeker2 = new SimpleSocketSeeker();
        int ticket1 = dealer.seekingSocket(seeker1);
        int ticket2 = dealer.seekingSocket(seeker2);
        SimpleDonor doner1 = new SimpleDonor();
        SimpleDonor doner2 = new SimpleDonor();
        dealer.dealSocketTo(ticket1, doner1);
        dealer.dealSocketTo(ticket2, doner2);

        assertSame(doner1.input, seeker1.input);
        assertSame(doner1.output, seeker1.output);
        assertSame(doner2.input, seeker2.input);
        assertSame(doner2.output, seeker2.output);
    }

    public void testSeekerRemovedAfterDeltTo() throws Exception {
        doSimpleDealing();
        Collection<SocketSeeker> waiting = dealer.getWaitingList();
        assertEquals(0, waiting.size());
    }

    public void testSeekerIsWaiting() throws Exception {
        assertFalse(dealer.isWaiting(23));
        int ticket = dealer.seekingSocket(new SimpleSocketSeeker());
        assertTrue(dealer.isWaiting(ticket));
    }
}
