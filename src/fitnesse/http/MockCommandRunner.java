// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import util.CommandRunner;

public class MockCommandRunner extends CommandRunner {
    public MockCommandRunner() {
        super("", "");
    }

    public MockCommandRunner(String command, int exitCode) {
        super(command, "");
        this.exitCode = exitCode;
    }

    public void run() {
    }

    public void join() {
    }

    public void kill() {
    }

    public void asynchronousStart() {
    }

    public void setOutput(String output) {
        outputBuffer = new StringBuffer(output);
    }

    public void setError(String error) {
        errorBuffer = new StringBuffer(error);
    }

    public long getExecutionTime() {
        return -1;
    }
}
