// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fit.FitServer;
import fitnesse.http.MockCommandRunner;
import fitnesse.responders.run.SocketDealer;
import fitnesse.responders.run.SocketDonor;
import fitnesse.responders.run.SocketSeeker;
import fitnesse.responders.run.TestSystemListener;

import java.io.IOException;
import java.util.Map;

public class CommandRunningFitClient extends FitClient implements SocketSeeker {
    public static int TIMEOUT = 60000;
    private static final String SPACE = " ";

    private int ticketNumber;
    public CommandRunner commandRunner;
    private SocketDonor donor;
    private boolean connectionEstablished = false;

    private Thread timeoutThread;
    private Thread earlyTerminationThread;
    private boolean fastTest = false;
    private Thread fastFitServer;

    public CommandRunningFitClient(TestSystemListener listener, String command, int port, SocketDealer dealer, boolean fastTest) throws IOException {
        this(listener, command, port, null, dealer, fastTest);
    }

    public CommandRunningFitClient(TestSystemListener listener, String command, int port, Map<String, String> environmentVariables, SocketDealer dealer, boolean fastTest) throws IOException {
        super(listener);
        this.fastTest = fastTest;
        ticketNumber = dealer.seekingSocket(this);
        String hostName = java.net.InetAddress.getLocalHost().getHostName();
        String fitArguments = hostName + SPACE + port + SPACE + ticketNumber;
        String commandLine = command + SPACE + fitArguments;
        if (fastTest) {
            commandRunner = new MockCommandRunner();
            createFitServer("-x " + fitArguments);
        } else
            commandRunner = new CommandRunner(commandLine, "", environmentVariables);
    }

    public CommandRunningFitClient(TestSystemListener listener, String command, int port, SocketDealer dealer) throws IOException {
        this(listener, command, port, null, dealer);
    }

    public CommandRunningFitClient(TestSystemListener listener, String command, int port, Map<String, String> environmentVariables, SocketDealer dealer) throws IOException {
        this(listener, command, port, environmentVariables, dealer, false);
    }

    //For testing only.  Makes responder faster.
    void createFitServer(String args) {
        final String fitArgs = args;
        Runnable fastFitServerRunnable = new Runnable() {
            public void run() {
                try {
                    while (!tryCreateFitServer(fitArgs))
                        Thread.sleep(10);
                } catch (InterruptedException e) {
                    // ok
                }
            }
        };
        fastFitServer = new Thread(fastFitServerRunnable);
        fastFitServer.start();
    }

    private boolean tryCreateFitServer(String args) {
        try {
            FitServer.main(args.trim().split(" "));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void start() {
        try {
            commandRunner.asynchronousStart();
            if (!fastTest) {
                timeoutThread = new Thread(new TimeoutRunnable(), "FitClient timeout");
                timeoutThread.start();
                earlyTerminationThread = new Thread(new EarlyTerminationRunnable(), "FitClient early termination");
                earlyTerminationThread.start();
            }
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

    void setTicketNumber(int ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    private void waitForConnection() throws InterruptedException {
        while (!isSuccessfullyStarted()) {
            Thread.sleep(100);
            checkForPulse();
        }
    }

    public void join() {
        try {
            if (fastTest) {
                fastFitServer.join();
            } else {
                commandRunner.join();
            }
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
        killVigilantThreads();
        commandRunner.kill();
    }

    private void killVigilantThreads() {
        if (timeoutThread != null)
            timeoutThread.interrupt();
        if (earlyTerminationThread != null)
            earlyTerminationThread.interrupt();
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
                commandRunner.process.waitFor();
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
}