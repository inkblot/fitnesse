// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.Injector;
import fitnesse.components.CommandRunningFitClient;
import fitnesse.responders.run.slimResponder.HtmlSlimTestSystem;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestSystemGroup {
    private final Map<TestSystem.Descriptor, TestSystem> testSystems = new HashMap<TestSystem.Descriptor, TestSystem>();
    private final WikiPage page;
    private final TestSystemListener testSystemListener;
    private final CompositeExecutionLog log;
    private final int port;
    private final SocketDealer socketDealer;
    private final Injector injector;

    public TestSystemGroup(WikiPage page, TestSystemListener listener, int port, SocketDealer socketDealer, Injector injector) {
        this.page = page;
        this.testSystemListener = listener;
        this.port = port;
        this.socketDealer = socketDealer;
        this.injector = injector;
        log = new CompositeExecutionLog(page);
    }

    public CompositeExecutionLog getExecutionLog() {
        return log;
    }

    public void bye() throws IOException {
        for (TestSystem testSystem : testSystems.values()) {
            testSystem.bye();
        }
    }

    public void kill() throws IOException {
        for (TestSystem testSystem : testSystems.values()) {
            testSystem.kill();
        }
    }

    public boolean isSuccessfullyStarted() {
        for (TestSystem testSystem : testSystems.values())
            if (!testSystem.isSuccessfullyStarted())
                return false;
        return true;
    }

    TestSystem startTestSystem(TestSystem.Descriptor descriptor, String classPath) throws IOException {
        TestSystem testSystem = null;
        if (!testSystems.containsKey(descriptor)) {
            testSystem = makeTestSystem(descriptor);
            testSystems.put(descriptor, testSystem);
            log.add(descriptor.testSystemName, testSystem.getExecutionLog(classPath, descriptor));
            testSystem.start();
        }
        return testSystem;
    }

    private TestSystem makeTestSystem(TestSystem.Descriptor descriptor) {
        if ("slim".equalsIgnoreCase(TestSystem.getTestSystemType(descriptor.testSystemName)))
            return new HtmlSlimTestSystem(page, testSystemListener);
        else
            return new FitTestSystem(page, testSystemListener, injector.getInstance(CommandRunningFitClient.FitTestMode.class), port, socketDealer);
    }

}
