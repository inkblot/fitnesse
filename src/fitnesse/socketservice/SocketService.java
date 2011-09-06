// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.socketservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;

public class SocketService {
    private final int port;
    private final SocketServer server;
    private final Thread serviceThread = new Thread(
            new Runnable() {
                public void run() {
                    serviceThread();
                }
            }
    );
    private final LinkedList<Thread> threads = new LinkedList<Thread>();

    private ServerSocket serverSocket = null;
    private volatile boolean running = false;
    private volatile boolean everRan = false;

    public SocketService(int port, SocketServer server) {
        this.server = server;
        this.port = port;
    }

    public synchronized void start() throws IOException {
        serverSocket = new ServerSocket(this.port);
        serviceThread.start();
        try {
            waitForServiceThreadToStart();
        } catch (InterruptedException e) {
            close();
        }
    }

    public synchronized void close() throws IOException {
        running = false;
        serverSocket.close();
        try {
            serviceThread.join();
            waitForServerThreads();
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private synchronized void waitForServiceThreadToStart() throws InterruptedException {
        if (everRan) return;
        while (!running) {
            wait();
        }
    }

    private void serviceThread() {
        synchronized (this) {
            running = true;
            everRan = true;
            notifyAll();
        }
        while (running) {
            try {
                Socket s = serverSocket.accept();
                startServerThread(s);
            } catch (java.lang.OutOfMemoryError e) {
                System.err.println("Can't create new thread.  Out of Memory.  Aborting");
                e.printStackTrace();
                System.exit(99);
            } catch (SocketException sox) {
                running = false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void startServerThread(Socket s) {
        Thread serverThread = new Thread(new ServerRunner(s));
        synchronized (threads) {
            threads.add(serverThread);
        }
        serverThread.start();
    }

    private void waitForServerThreads() throws InterruptedException {
        while (threads.size() > 0) {
            Thread t;
            synchronized (threads) {
                if (threads.size() < 1)
                    return;
                t = threads.getFirst();
            }
            t.join();
        }
    }

    private class ServerRunner implements Runnable {
        private Socket socket;

        ServerRunner(Socket s) {
            socket = s;
        }

        public void run() {
            try {
                server.serve(socket);
                synchronized (threads) {
                    threads.remove(Thread.currentThread());
                }
            } catch (Exception e) {
                // kablooie!
            }
        }
    }

}
