// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fit.Counts;
import fit.FitProtocol;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystemListener;
import util.StreamReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FitClient {

    protected TestSystemListener listener;
    private boolean initialized = false;
    private OutputStream fitInput;
    private StreamReader fitOutput;

    private volatile int sent = 0;
    private volatile int received = 0;
    private volatile boolean isDoneSending = false;
    protected volatile boolean killed = false;
    protected Thread fitListeningThread;

    public FitClient(TestSystemListener listener) {
        this.listener = listener;
    }

    public void acceptSocket(InputStream input, OutputStream output) throws IOException {
        checkForPulse();
        fitInput = output;
        FitProtocol.writeData("", fitInput);
        fitOutput = new StreamReader(input);
        initialized = true;

        fitListeningThread = new Thread(new FitListeningRunnable(), "FitClient fitOutput");
        fitListeningThread.start();
    }

    public void send(String data) throws Exception {
        checkForPulse();
        FitProtocol.writeData(data, fitInput);
        sent++;
    }

    public void done() throws Exception {
        checkForPulse();
        FitProtocol.writeSize(0, fitInput);
        isDoneSending = true;
    }

    public void join() throws InterruptedException {
        if (fitListeningThread != null)
            fitListeningThread.join();
    }

    public void kill() {
        killed = true;
        if (fitListeningThread != null)
            fitListeningThread.interrupt();
    }

    public void exceptionOccurred(Exception e) {
        listener.exceptionOccurred(e);
    }

    protected void checkForPulse() {
        if (killed) {
            // TODO: Bad flow control.  Use something other than an exception
            throw new RuntimeException("FitClient was killed");
        }
    }

    private void listenToFit() {
        try {
            attemptToListenToFit();
        } catch (Exception e) {
            exceptionOccurred(e);
        }
    }

    private void attemptToListenToFit() throws Exception {
        while (!finishedReading()) {
            int size;
            size = FitProtocol.readSize(fitOutput);
            if (size != 0) {
                String readValue = fitOutput.read(size);
                if (fitOutput.byteCount() < size)
                    throw new Exception("I was expecting " + size + " bytes but I only got " + fitOutput.byteCount());
                listener.acceptOutputFirst(readValue);
            } else {
                Counts counts = FitProtocol.readCounts(fitOutput);
                TestSummary summary = new TestSummary();
                summary.right = counts.right;
                summary.wrong = counts.wrong;
                summary.ignores = counts.ignores;
                summary.exceptions = counts.exceptions;
                listener.testComplete(summary);
                received++;
            }
        }
    }

    private boolean finishedReading() {
        while (stateIndeterminate())
            shortSleep();
        return isDoneSending && received == sent;
    }

    /**
     * @return true if the current state of the transission is indeterminate.
     *         <p/>
     *         When the number of pages sent and recieved is the same, we may be done with the whole job,
     *         or we may just be waiting for FitNesse to send the next page.  There's no way to know until
     *         FitNesse either calls send, or done.
     */
    private boolean stateIndeterminate() {
        return (received == sent) && !isDoneSending;
    }

    private void shortSleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isSuccessfullyStarted() {
        return initialized;
    }

    private class FitListeningRunnable implements Runnable {
        public void run() {
            listenToFit();
        }
    }
}
