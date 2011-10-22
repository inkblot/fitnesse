// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;

public class MockResponseSender implements ResponseSender {
    public MockSocket socket;
    protected boolean closed = false;

    public MockResponseSender() {
        socket = new MockSocket("Mock");
        closed = false;
    }

    public void send(byte[] bytes) {
        //Todo Timing Problem -- Figure out why this is necessary.
        for (int i = 0; i < 1000; i++) Thread.yield();
        try {
            socket.getOutputStream().write(bytes);
        } catch (IOException e) {
            // output stream closed prematurely, probably due to user action
        }
    }

    public synchronized void close() {
        closed = true;
        notifyAll();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public String sentData() throws Exception {
        return socket.getOutput();
    }

    public void doSending(Response response) throws IOException {
        response.readyToSend(this);
        waitForClose(10000);
    }

    // Utility method that returns when this.closed is true. Throws an exception
    // if the timeout is reached.
    public synchronized void waitForClose(long timeoutMillis) throws IOException {
        try {
            while (!closed && timeoutMillis > 0) {
                wait(100);
                timeoutMillis -= 100;
            }
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while waiting for close", e);
        }
        if (!closed)
            throw new IOException("MockResponseSender could not be closed");
    }

    public boolean isClosed() {
        return closed;
    }

    public static class OutputStreamSender extends MockResponseSender {
        public OutputStreamSender(OutputStream out) {
            socket = new MockSocket(new PipedInputStream(), out);
        }

        public void doSending(Response response) throws IOException {
            response.readyToSend(this);
            try {
                while (!closed)
                    Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while sending", e);
            }
        }
    }
}
