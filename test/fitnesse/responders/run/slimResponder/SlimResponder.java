// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.ClassPathBuilder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.WikiPageResponder;
import fitnesse.responders.run.ExecutionLog;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import util.Clock;

import java.io.IOException;
import java.util.Properties;

/*
This responder is a test rig for SlimTestSystemTest, which makes sure that the SlimTestSystem works nicely with
responders in general.
*/
public abstract class SlimResponder extends WikiPageResponder implements TestSystemListener {
    ExecutionLog log;
    private SlimTestSystem.SlimTestMode testMode = new SlimTestSystem.DefaultTestMode();
    SlimTestSystem testSystem;

    public SlimResponder(Properties properties, HtmlPageFactory htmlPageFactory, Clock clock) {
        super(properties, htmlPageFactory, clock);
    }


    protected String generateHtml(PageData pageData, WikiPage page) throws IOException {
        testSystem = getTestSystem(pageData);
        String classPath = new ClassPathBuilder().getClasspath(page);
        TestSystem.Descriptor descriptor = TestSystem.getDescriptor(page.getData(), false);
        descriptor.testRunner = "fitnesse.slim.SlimService";
        log = testSystem.getExecutionLog(classPath, descriptor);
        testSystem.start();
        testSystem.setTestMode(testMode);
        String html = testSystem.runTestsAndGenerateHtml(pageData);
        testSystem.bye();

        // TODO: Why is this sleep here?
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            // ok
        }
        return html;
    }

    abstract SlimTestSystem getTestSystem(PageData pageData);

    public SecureOperation getSecureOperation() {
        return new SecureTestOperation();
    }

    public TestSummary getTestSummary() {
        return testSystem.getTestSummary();
    }

    public void setTestMode(SlimTestSystem.SlimTestMode testMode) {
        this.testMode = testMode;
    }

    public void acceptOutputFirst(String output) {
    }

    public void testComplete(TestSummary testSummary) {
    }

    public void exceptionOccurred(Throwable e) {
        //todo remove std out
        System.err.println("SlimResponder.exceptionOccurred:" + e.getMessage());
    }

}

