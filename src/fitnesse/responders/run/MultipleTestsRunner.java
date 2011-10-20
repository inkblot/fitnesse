// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.components.ClassPathBuilder;
import fitnesse.html.SetupTeardownAndLibraryIncluder;
import fitnesse.responders.run.TestSystem.Descriptor;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import util.TimeMeasurement;

import java.io.IOException;
import java.util.*;

public class MultipleTestsRunner implements TestSystemListener, Stoppable {

    private final ResultsListener resultsListener;
    private final RunningTestingTracker runningTestingTracker;
    private final WikiPage page;
    private final List<WikiPage> testPagesToRun;
    private final int port;
    private final SocketDealer socketDealer;

    private boolean isFastTest = false;
    private boolean isRemoteDebug = false;

    private LinkedList<WikiPage> processingQueue = new LinkedList<WikiPage>();
    private WikiPage currentTest = null;

    private TestSystemGroup testSystemGroup = null;
    private boolean isStopped = false;
    private String stopId = null;
    private PageListSetUpTearDownSurrounder surrounder;
    TimeMeasurement currentTestTime, totalTestTime;

    public MultipleTestsRunner(final List<WikiPage> testPagesToRun,
                               RunningTestingTracker runningTestingTracker, final WikiPage page,
                               final ResultsListener resultsListener,
                               WikiPage root, int port, SocketDealer socketDealer) {
        this.testPagesToRun = testPagesToRun;
        this.resultsListener = resultsListener;
        this.page = page;
        this.runningTestingTracker = runningTestingTracker;
        this.port = port;
        this.socketDealer = socketDealer;
        surrounder = new PageListSetUpTearDownSurrounder(root);
    }

    public void setDebug(boolean isDebug) {
        isRemoteDebug = isDebug;
    }

    public void setFastTest(boolean isFastTest) {
        this.isFastTest = isFastTest;
    }

    public void executeTestPages() {
        try {
            internalExecuteTestPages();
            allTestingComplete();
        } catch (Exception exception) {
            //hoped to write exceptions to log file but will take some work.
            exception.printStackTrace(System.out);
            exceptionOccurred(exception);
        }
    }

    void allTestingComplete() throws IOException {
        TimeMeasurement completionTimeMeasurement = new TimeMeasurement().start();
        resultsListener.allTestingComplete(totalTestTime.stop());
        completionTimeMeasurement.stop(); // a non-trivial amount of time elapses here
    }

    private void internalExecuteTestPages() throws IOException {
        synchronized (this) {
            testSystemGroup = new TestSystemGroup(page, this, port, socketDealer);
            stopId = runningTestingTracker.addStartedProcess(this);
        }
        testSystemGroup.setFastTest(isFastTest);

        resultsListener.setExecutionLogAndTrackingId(stopId, testSystemGroup.getExecutionLog());
        PagesByTestSystem pagesByTestSystem = makeMapOfPagesByTestSystem();
        announceTotalTestsToRun(pagesByTestSystem);
        for (TestSystem.Descriptor descriptor : pagesByTestSystem.keySet()) {
            executePagesInTestSystem(descriptor, pagesByTestSystem);
        }
        runningTestingTracker.removeEndedProcess(stopId);
    }

    private void executePagesInTestSystem(TestSystem.Descriptor descriptor,
                                          PagesByTestSystem pagesByTestSystem) throws IOException {
        List<WikiPage> pagesInTestSystem = pagesByTestSystem.get(descriptor);

        startTestSystemAndExecutePages(descriptor, pagesInTestSystem);
    }

    private void startTestSystemAndExecutePages(TestSystem.Descriptor descriptor, List<WikiPage> testSystemPages) throws IOException {
        TestSystem testSystem = null;
        synchronized (this) {
            if (!isStopped) {
                testSystem = testSystemGroup.startTestSystem(descriptor, buildClassPath());
                resultsListener.testSystemStarted(testSystem, descriptor.testSystemName, descriptor.testRunner);
            }
        }
        if (testSystem != null) {
            if (testSystem.isSuccessfullyStarted()) {
                executeTestSystemPages(testSystemPages, testSystem);
                try {
                    waitForTestSystemToSendResults();
                } catch (InterruptedException e) {
                    // ok
                }
            } else {
                // TODO: choose a better exception type
                throw new RuntimeException("Test system not started");
            }

            synchronized (this) {
                if (!isStopped) {
                    testSystem.bye();
                }
            }
        }
    }

    private void executeTestSystemPages(List<WikiPage> pagesInTestSystem, TestSystem testSystem) throws IOException {
        for (WikiPage testPage : pagesInTestSystem) {
            addToProcessingQueue(testPage);
            PageData pageData = testPage.getData();
            SetupTeardownAndLibraryIncluder.includeSetupsTeardownsAndLibrariesBelowTheSuite(pageData, page);
            testSystem.runTestsAndGenerateHtml(pageData);
        }
    }

    void addToProcessingQueue(WikiPage testPage) {
        processingQueue.addLast(testPage);
    }

    private void waitForTestSystemToSendResults() throws InterruptedException {
        while ((processingQueue.size() > 0) && isNotStopped())
            Thread.sleep(50);
    }

    PagesByTestSystem makeMapOfPagesByTestSystem() throws IOException {
        return addSuiteSetUpAndTearDownToAllTestSystems(mapWithAllPagesButSuiteSetUpAndTearDown());
    }

    private PagesByTestSystem mapWithAllPagesButSuiteSetUpAndTearDown() throws IOException {
        PagesByTestSystem pagesByTestSystem = new PagesByTestSystem();

        for (WikiPage testPage : testPagesToRun) {
            if (!SuiteContentsFinder.isSuiteSetupOrTearDown(testPage)) {
                addPageToListWithinMap(pagesByTestSystem, testPage);
            }
        }
        return pagesByTestSystem;
    }

    private void addPageToListWithinMap(PagesByTestSystem pagesByTestSystem, WikiPage testPage) throws IOException {
        Descriptor descriptor = TestSystem.getDescriptor(testPage.getData(), isRemoteDebug);
        getOrMakeListWithinMap(pagesByTestSystem, descriptor).add(testPage);
    }

    private LinkedList<WikiPage> getOrMakeListWithinMap(PagesByTestSystem pagesByTestSystem, Descriptor descriptor) {
        LinkedList<WikiPage> pagesForTestSystem;
        if (!pagesByTestSystem.containsKey(descriptor)) {
            pagesForTestSystem = new LinkedList<WikiPage>();
            pagesByTestSystem.put(descriptor, pagesForTestSystem);
        } else {
            pagesForTestSystem = pagesByTestSystem.get(descriptor);
        }
        return pagesForTestSystem;
    }

    private PagesByTestSystem addSuiteSetUpAndTearDownToAllTestSystems(PagesByTestSystem pagesByTestSystem) throws IOException {
        if (testPagesToRun.size() == 0)
            return pagesByTestSystem;
        for (LinkedList<WikiPage> pagesForTestSystem : pagesByTestSystem.values())
            surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(pagesForTestSystem);

        return pagesByTestSystem;
    }

    void announceTotalTestsToRun(PagesByTestSystem pagesByTestSystem) {
        int tests = 0;
        for (LinkedList<WikiPage> listOfPagesToRun : pagesByTestSystem.values()) {
            tests += listOfPagesToRun.size();
        }
        resultsListener.announceNumberTestsToRun(tests);
        totalTestTime = new TimeMeasurement().start();
    }

    public String buildClassPath() throws IOException {
        final ClassPathBuilder classPathBuilder = new ClassPathBuilder();
        final String pathSeparator = classPathBuilder.getPathSeparator(page);
        List<String> classPathElements = new ArrayList<String>();
        Set<WikiPage> visitedPages = new HashSet<WikiPage>();

        for (WikiPage testPage : testPagesToRun) {
            addClassPathElements(testPage, classPathElements, visitedPages);
        }

        return classPathBuilder.createClassPathString(classPathElements, pathSeparator);
    }

    private void addClassPathElements(WikiPage page, List<String> classPathElements, Set<WikiPage> visitedPages) throws IOException {
        List<String> pathElements = new ClassPathBuilder().getInheritedPathElements(page, visitedPages);
        classPathElements.addAll(pathElements);
    }

    public void acceptOutputFirst(String output) throws IOException {
        WikiPage firstInQueue = processingQueue.isEmpty() ? null : processingQueue.getFirst();
        boolean isNewTest = firstInQueue != null && firstInQueue != currentTest;
        if (isNewTest) {
            startingNewTest(firstInQueue);
        }
        resultsListener.testOutputChunk(output);
    }

    void startingNewTest(WikiPage test) throws IOException {
        currentTest = test;
        currentTestTime = new TimeMeasurement().start();
        resultsListener.newTestStarted(currentTest, currentTestTime);
    }

    public void testComplete(TestSummary testSummary) throws IOException {
        WikiPage testPage = processingQueue.removeFirst();
        resultsListener.testComplete(testPage, testSummary, currentTestTime.stop());
    }

    public void exceptionOccurred(Throwable e) {
        try {
            resultsListener.errorOccurred();
            stop();
        } catch (Exception e1) {
            if (isNotStopped()) {
                e1.printStackTrace();
            }
        }
    }

    private synchronized boolean isNotStopped() {
        return !isStopped;
    }

    public void stop() throws IOException {
        boolean wasNotStopped = isNotStopped();
        synchronized (this) {
            isStopped = true;
            if (stopId != null) {
                runningTestingTracker.removeEndedProcess(stopId);
            }
        }

        if (wasNotStopped) {
            testSystemGroup.kill();
        }
    }
}

class PagesByTestSystem extends HashMap<TestSystem.Descriptor, LinkedList<WikiPage>> {
    private static final long serialVersionUID = 1L;
}
