// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import com.google.inject.ImplementedBy;
import fitnesse.responders.run.SocketDealer;
import fitnesse.responders.run.SocketDonor;
import fitnesse.responders.run.SocketSeeker;
import fitnesse.responders.run.TestSystemListener;
import util.CommandRunner;

import java.io.IOException;
import java.util.Map;

import static java.net.InetAddress.getLocalHost;

public class CommandRunningFitClient extends FitClient implements SocketSeeker {
    public static int TIMEOUT = 60000;

    public final CommandRunner commandRunner;
    private final FitTestMode testMode;

    private SocketDonor donor;
    private boolean connectionEstablished = false;

    public CommandRunningFitClient(TestSystemListener listener, String command, int port, SocketDealer dealer) throws IOException {
        this(listener, command, port, null, dealer, new DefaultTestMode());
    }

    public CommandRunningFitClient(TestSystemListener listener, String command, int port, Map<String, String> environmentVariables, SocketDealer dealer, FitTestMode fitTestMode) throws IOException {
        super(listener);
        this.testMode = fitTestMode;
        int ticketNumber = dealer.seekingSocket(this);
        String fitArguments = getLocalHost().getHostName() + " " + port + " " + ticketNumber;
        commandRunner = testMode.initialize(fitArguments, command, environmentVariables);
    }

    public void start() {
        try {
            commandRunner.asynchronousStart();
            testMode.start(this);
            waitForConnection();
        } catch (Exception e) {
            listener.exceptionOccurred(e);
        }
    }

    public void acceptSocketFrom(SocketDonor donor) throws IOException {
        this.donor = donor;
        acceptSocket(donor.donateInputStream(), donor.donateOutputStream());
        connectionEstablished = true;

        synchronized (this) {
            notify();
        }
    }

    private void waitForConnection() throws InterruptedException {
        while (!isSuccessfullyStarted()) {
            Thread.sleep(100);
            checkForPulse();
        }
    }

    public void join() {
        try {
            testMode.join(this);
            super.join();
            if (donor != null)
                donor.finishedWithSocket();
            killVigilantThreads();
        } catch (InterruptedException e) {
            // ok
        }
    }

    public void kill() {
        super.kill();
        testMode.killVigilantThreads();
        commandRunner.kill();
    }

    private void killVigilantThreads() {
    }

    public void exceptionOccurred(Exception e) {
        commandRunner.exceptionOccurred(e);
        super.exceptionOccurred(e);
    }

    private class TimeoutRunnable implements Runnable {

        public void run() {
            try {
                Thread.sleep(TIMEOUT);
                synchronized (CommandRunningFitClient.this) {
                    if (!isSuccessfullyStarted()) {
                        CommandRunningFitClient.this.notify();
                        listener.exceptionOccurred(new Exception(
                                "FitClient: communication socket was not received on time."));
                    }
                }
            } catch (InterruptedException e) {
                // ok
            }
        }
    }

    private class EarlyTerminationRunnable implements Runnable {
        public void run() {
            try {
                Thread.sleep(1000);  // next waitFor() can finish too quickly on Linux!
                commandRunner.waitFor();
                synchronized (CommandRunningFitClient.this) {
                    if (!connectionEstablished) {
                        CommandRunningFitClient.this.notify();
                        listener.exceptionOccurred(new Exception(
                                "FitClient: external process terminated before a connection could be established."));
                    }
                }
            } catch (InterruptedException e) {
                // ok
            }
        }
    }

    @ImplementedBy(DefaultTestMode.class)
    public static interface FitTestMode {

        public CommandRunner initialize(String fitArguments, String command, Map<String, String> environmentVariables);

        void join(CommandRunningFitClient commandRunningFitClient) throws InterruptedException;

        void killVigilantThreads();

        void start(CommandRunningFitClient commandRunningFitClient);
    }

    public static class DefaultTestMode implements FitTestMode {

        private Thread timeoutThread;
        private Thread earlyTerminationThread;

        @Override
        public CommandRunner initialize(String fitArguments, String command, Map<String, String> environmentVariables) {
            String commandLine = command + " " + fitArguments;
            return new CommandRunner(commandLine, "", environmentVariables);
        }

        @Override
        public void join(CommandRunningFitClient commandRunningFitClient) throws InterruptedException {
            commandRunningFitClient.commandRunner.join();
        }

        @Override
        public void start(CommandRunningFitClient commandRunningFitClient) {
            timeoutThread = new Thread(commandRunningFitClient.new TimeoutRunnable(), "FitClient timeout");
            timeoutThread.start();
            earlyTerminationThread = new Thread(commandRunningFitClient.new EarlyTerminationRunnable(), "FitClient early termination");
            earlyTerminationThread.start();
        }

        @Override
        public void killVigilantThreads() {
            if (timeoutThread != null)
                timeoutThread.interrupt();
            if (earlyTerminationThread != null)
                earlyTerminationThread.interrupt();
        }
    }

}