// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.components.CommandRunningFitClient;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.Map;

public class FitTestSystem extends TestSystem {
    private final boolean fastTest;
    private final int port;
    private final SocketDealer socketDealer;

    private CommandRunningFitClient client;

    public FitTestSystem(WikiPage page, TestSystemListener listener, boolean fastTest, int port, SocketDealer socketDealer) {
        super(page, listener);
        this.fastTest = fastTest;
        this.port = port;
        this.socketDealer = socketDealer;
    }

    protected ExecutionLog createExecutionLog(String classPath, Descriptor descriptor) throws IOException {
        String command = buildCommand(descriptor, classPath);
        Map<String, String> environmentVariables = createClasspathEnvironment(classPath);
        client = new CommandRunningFitClient(this, command, port, environmentVariables, socketDealer, fastTest);
        return new ExecutionLog(page, client.commandRunner);
    }


    public void bye() throws IOException {
        client.done();
        client.join();
    }

    public String runTestsAndGenerateHtml(PageData pageData) throws IOException {
        String html = pageData.getHtml();
        if (html.length() == 0)
            client.send(emptyPageContent);
        else
            client.send(html);
        return html;
    }

    public boolean isSuccessfullyStarted() {
        return client.isSuccessfullyStarted();
    }

    public void kill() {
        client.kill();
    }

    public void start() {
        client.start();
    }

}