// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class CommandRunner {
    private final String command;
    private final String input;
    private final Map<String, String> environmentVariables;

    private final TimeMeasurement timeMeasurement = new TimeMeasurement();
    private final List<Throwable> exceptions = new ArrayList<Throwable>();

    private OutputStream stdin;

    protected Process process;
    protected StringBuffer outputBuffer = new StringBuffer();
    protected StringBuffer errorBuffer = new StringBuffer();
    protected int exitCode = -1;

    public CommandRunner(String command, String input) {
        this(command, input, null);
    }

    public CommandRunner(String command, String input, Map<String, String> environmentVariables) {
        this.command = command;
        this.input = input;
        this.environmentVariables = environmentVariables == null ? null : Collections.unmodifiableMap(environmentVariables);
    }

    public void asynchronousStart() throws IOException {
        timeMeasurement.start();
        String[] environmentVariables = determineEnvironment();
        process = Runtime.getRuntime().exec(command, environmentVariables);
        stdin = process.getOutputStream();

        new Thread(new OutputReadingRunnable(process.getInputStream(), outputBuffer), "CommandRunner stdout").start();
        new Thread(new OutputReadingRunnable(process.getErrorStream(), errorBuffer), "CommandRunner error").start();

        sendInput();
    }

    private String[] determineEnvironment() {
        if (environmentVariables == null) {
            return null;
        }
        Map<String, String> systemVariables = new HashMap<String, String>(System.getenv());
        systemVariables.putAll(environmentVariables);
        List<String> systemVariableAssignments = new ArrayList<String>();
        for (Map.Entry<String, String> entry : systemVariables.entrySet()) {
            systemVariableAssignments.add(entry.getKey() + "=" + entry.getValue());
        }
        return systemVariableAssignments.toArray(new String[systemVariableAssignments.size()]);
    }

    public void run() throws Exception {
        asynchronousStart();
        join();
    }

    public void join() throws InterruptedException {
        process.waitFor();
        timeMeasurement.stop();
        exitCode = process.exitValue();
    }

    public void kill() {
        if (process != null) {
            process.destroy();
            try {
                join();
            } catch (InterruptedException e) {
                // ok
            }
        }
    }

    public String getCommand() {
        return command;
    }

    public String getOutput() {
        return outputBuffer.toString();
    }

    public String getError() {
        return errorBuffer.toString();
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    public boolean hasExceptions() {
        return exceptions.size() > 0;
    }

    public boolean wroteToErrorStream() {
        return errorBuffer.length() > 0;
    }

    public boolean wroteToOutputStream() {
        return outputBuffer.length() > 0;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void exceptionOccurred(Exception e) {
        exceptions.add(e);
    }

    public long getExecutionTime() {
        return timeMeasurement.elapsed();
    }

    private void sendInput() {
        try {
            stdin.write(input.getBytes("UTF-8"));
            stdin.flush();
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        } catch (Exception e) {
            exceptionOccurred(e);
        } finally {
            try {
                stdin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readOutput(InputStream input, StringBuffer buffer) {
        try {
            int c;
            while ((c = input.read()) != -1)
                buffer.append((char) c);
        } catch (Exception e) {
            exceptionOccurred(e);
        }
    }

    public void waitFor() throws InterruptedException {
        process.waitFor();
    }

    private class OutputReadingRunnable implements Runnable {
        private final InputStream input;
        private final StringBuffer buffer;

        public OutputReadingRunnable(InputStream input, StringBuffer buffer) {
            this.input = input;
            this.buffer = buffer;
        }

        public void run() {
            readOutput(input, buffer);
        }
    }
}