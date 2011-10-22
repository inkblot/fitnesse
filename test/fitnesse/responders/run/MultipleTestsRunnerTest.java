// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import util.TimeMeasurement;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static util.RegexAssertions.assertSubString;

public class MultipleTestsRunnerTest extends FitnesseBaseTestCase {
    private WikiPage root;
    private WikiPage suite;
    private WikiPage testPage;
    private PageCrawler crawler;
    private final String simpleSlimDecisionTable = "!define TEST_SYSTEM {slim}\n" +
            "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
            "|string|get string arg?|\n" +
            "|wow|wow|\n";
    private List<WikiPage> testPages;
    private SocketDealer socketDealer;
    private RunningTestingTracker runningTestingTracker;

    @Inject
    public void inject(@Named(FitNesseModule.ROOT_PAGE) WikiPage root, SocketDealer socketDealer, RunningTestingTracker runningTestingTracker) {
        this.root = root;
        this.socketDealer = socketDealer;
        this.runningTestingTracker = runningTestingTracker;
    }

    @Before
    public void setUp() throws Exception {
        String suitePageName = "SuitePage";
        crawler = root.getPageCrawler();
        PageData data = root.getData();
        data.setContent(classpathWidgets());
        root.commit(data);
        suite = crawler.addPage(root, PathParser.parse(suitePageName), "This is the test suite\n");
        testPages = new LinkedList<WikiPage>();
        testPage = addTestPage(suite, "TestOne", "My test");
    }

    @Test
    public void testBuildClassPath() throws Exception {
        MultipleTestsRunner runner = new MultipleTestsRunner(testPages, runningTestingTracker, suite, null, root, getPort(), socketDealer, injector);

        String classpath = runner.buildClassPath();
        assertSubString("classes", classpath);
        assertSubString("dummy.jar", classpath);
    }

    @Test
    public void testGenerateSuiteMapWithMultipleTestSystems() throws Exception {
        WikiPage slimPage = addTestPage(suite, "SlimTest", simpleSlimDecisionTable);

        MultipleTestsRunner runner = new MultipleTestsRunner(testPages, runningTestingTracker, suite, null, root, getPort(), socketDealer, injector);
        Map<TestSystem.Descriptor, LinkedList<WikiPage>> map = runner.makeMapOfPagesByTestSystem();

        TestSystem.Descriptor fitDescriptor = TestSystem.getDescriptor(testPage.getData(), false);
        TestSystem.Descriptor slimDescriptor = TestSystem.getDescriptor(slimPage.getData(), false);
        List<WikiPage> fitList = map.get(fitDescriptor);
        List<WikiPage> slimList = map.get(slimDescriptor);

        assertEquals(1, fitList.size());
        assertEquals(1, slimList.size());
        assertEquals(testPage, fitList.get(0));
        assertEquals(slimPage, slimList.get(0));
    }

    @Test
    public void testPagesForTestSystemAreSurroundedBySuiteSetupAndTeardown() throws Exception {
        WikiPage slimPage = addTestPage(suite, "AaSlimTest", simpleSlimDecisionTable);
        WikiPage setUp = crawler.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
        WikiPage tearDown = crawler.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");

        testPages = new LinkedList<WikiPage>();
        testPages.add(setUp);
        testPages.add(slimPage);
        testPages.add(testPage);
        testPages.add(tearDown);

        MultipleTestsRunner runner = new MultipleTestsRunner(testPages, runningTestingTracker, suite, null, root, getPort(), socketDealer, injector);
        Map<TestSystem.Descriptor, LinkedList<WikiPage>> map = runner.makeMapOfPagesByTestSystem();
        TestSystem.Descriptor fitDescriptor = TestSystem.getDescriptor(testPage.getData(), false);
        TestSystem.Descriptor slimDescriptor = TestSystem.getDescriptor(slimPage.getData(), false);

        List<WikiPage> fitList = map.get(fitDescriptor);
        List<WikiPage> slimList = map.get(slimDescriptor);

        assertEquals(3, fitList.size());
        assertEquals(3, slimList.size());

        assertEquals(setUp, fitList.get(0));
        assertEquals(testPage, fitList.get(1));
        assertEquals(tearDown, fitList.get(2));

        assertEquals(setUp, slimList.get(0));
        assertEquals(slimPage, slimList.get(1));
        assertEquals(tearDown, slimList.get(2));
    }


    private WikiPage addTestPage(WikiPage page, String name, String content) throws Exception {
        WikiPage testPage = crawler.addPage(page, PathParser.parse(name), content);
        PageData data = testPage.getData();
        data.setAttribute("Test");
        testPage.commit(data);
        testPages.add(testPage);

        return testPage;
    }

    private String classpathWidgets() {
        return "!path classes\n" +
                "!path lib/dummy.jar\n";
    }

    @Test
    public void startingNewTestShouldStartTimeMeasurementAndNotifyListener() throws Exception {
        List<WikiPage> testPagesToRun = mock(List.class);
        WikiPage page = mock(WikiPage.class);
        ResultsListener resultsListener = mock(ResultsListener.class);

        MultipleTestsRunner runner = new MultipleTestsRunner(testPagesToRun, runningTestingTracker, page, resultsListener, root, getPort(), socketDealer, injector);

        runner.startingNewTest(page);
        verify(resultsListener).newTestStarted(same(page), same(runner.currentTestTime));
        assertThat(runner.currentTestTime, isAStartedTimeMeasurement());
    }

    private ArgumentMatcher<TimeMeasurement> isAStartedTimeMeasurement() {
        return new ArgumentMatcher<TimeMeasurement>() {
            @Override
            public boolean matches(Object argument) {
                return ((TimeMeasurement) argument).startedAt() > 0;
            }
        };
    }

    @Test
    public void testCompleteShouldRemoveHeadOfQueueAndNotifyListener() throws Exception {
        List<WikiPage> testPagesToRun = mock(List.class);
        WikiPage page = mock(WikiPage.class);
        ResultsListener resultsListener = mock(ResultsListener.class);

        MultipleTestsRunner runner = new MultipleTestsRunner(testPagesToRun, runningTestingTracker, page, resultsListener, root, getPort(), socketDealer, injector);
        runner.addToProcessingQueue(page);

        TestSummary testSummary = mock(TestSummary.class);

        runner.startingNewTest(page);
        runner.testComplete(testSummary);
        verify(resultsListener).testComplete(same(page), same(testSummary), same(runner.currentTestTime));
        assertThat(runner.currentTestTime, isAStoppedTimeMeasurement());
    }

    private ArgumentMatcher<TimeMeasurement> isAStoppedTimeMeasurement() {
        return new ArgumentMatcher<TimeMeasurement>() {
            @Override
            public boolean matches(Object argument) {
                return ((TimeMeasurement) argument).stoppedAt() > 0;
            }
        };
    }

    @Test
    public void announceTotalTestsToRunShouldStartTotalTimeMeasurement() throws Exception {
        List<WikiPage> testPagesToRun = mock(List.class);
        WikiPage page = mock(WikiPage.class);
        ResultsListener resultsListener = mock(ResultsListener.class);
        MultipleTestsRunner runner = new MultipleTestsRunner(testPagesToRun, runningTestingTracker, page, resultsListener, root, getPort(), socketDealer, injector);

        runner.announceTotalTestsToRun(new PagesByTestSystem());
        verify(resultsListener).announceNumberTestsToRun(0);
        assertThat(runner.totalTestTime, isAStartedTimeMeasurement());
    }

    @Test
    public void allTestingCompleteShouldStopTotalTimeMeasurement() throws Exception {
        List<WikiPage> testPagesToRun = mock(List.class);
        WikiPage page = mock(WikiPage.class);
        ResultsListener resultsListener = mock(ResultsListener.class);
        MultipleTestsRunner runner = new MultipleTestsRunner(testPagesToRun, runningTestingTracker, page, resultsListener, root, getPort(), socketDealer, injector);
        runner.announceTotalTestsToRun(new PagesByTestSystem());

        runner.allTestingComplete();
        verify(resultsListener).allTestingComplete(same(runner.totalTestTime));
        assertThat(runner.totalTestTime, isAStoppedTimeMeasurement());
    }
}
