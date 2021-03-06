// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.components.CommandRunningFitClient;
import fitnesse.components.FastTestMode;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitSocketReceiver;
import fitnesse.wiki.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import static fitnesse.responders.run.TestResponderTest.XmlTestUtilities.assertCounts;
import static fitnesse.responders.run.TestResponderTest.XmlTestUtilities.getXmlDocumentFromResults;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.*;
import static util.RegexAssertions.*;

public class SuiteResponderTest extends FitnesseBaseTestCase {
    private static final Date TEST_TIME = DateTimeUtil.getDateFromString("12/5/2008 01:19:00");
    private MockRequest request;
    private SuiteResponder responder;
    private WikiPage root;
    private SocketDealer socketDealer;
    private RunningTestingTracker runningTestingTracker;
    private WikiPage suite;
    private FitSocketReceiver receiver;
    private PageCrawler crawler;
    private final String fitPassFixture = "|!-fitnesse.testutil.PassFixture-!|\n";
    private final String fitFailFixture = "|!-fitnesse.testutil.FailFixture-!|\n";
    private final String simpleSlimDecisionTable = "!define TEST_SYSTEM {slim}\n" +
            "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
            "|string|get string arg?|\n" +
            "|wow|wow|\n";

    private Provider<Clock> clockProvider;
    private HtmlPageFactory htmlPageFactory;
    private File testResultsPath;

    @Override
    protected Module getOverrideModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(new DateAlteringClock(TEST_TIME).freeze());
                bind(CommandRunningFitClient.FitTestMode.class).to(FastTestMode.class);
            }
        };
    }

    @Override
    protected int getPort() {
        return 8084;
    }

    @Override
    protected boolean isChunkingEnabled() {
        return false;
    }

    @Inject
    public void inject(Provider<Clock> clockProvider, HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root, SocketDealer socketDealer, @Named(FitNesseModule.TEST_RESULTS_PATH) String testResultsPath, RunningTestingTracker runningTestingTracker) {
        this.clockProvider = clockProvider;
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
        this.socketDealer = socketDealer;
        this.runningTestingTracker = runningTestingTracker;
        this.testResultsPath = new File(testResultsPath);
    }

    @Before
    public void setUp() throws Exception {
        assertEquals(TEST_TIME, clockProvider.get().currentClockDate());

        String suitePageName = "SuitePage";

        crawler = root.getPageCrawler();
        PageData data = root.getData();
        data.setContent(classpathWidgets());
        root.commit(data);
        suite = crawler.addPage(root, PathParser.parse(suitePageName), "This is the test suite\n");
        addTestToSuite("TestOne", fitPassFixture);

        request = new MockRequest();
        request.setResource(suitePageName);
        responder = new SuiteResponder(htmlPageFactory, root, testResultsPath.getAbsolutePath(), getPort(), socketDealer, runningTestingTracker, isChunkingEnabled(), injector);

        receiver = new FitSocketReceiver(getPort(), socketDealer);
    }

    private WikiPage addTestToSuite(String name, String content) throws IOException {
        return addTestPage(suite, name, content);
    }

    private WikiPage addTestPage(WikiPage page, String name, String content) throws IOException {
        WikiPage testPage = crawler.addPage(page, PathParser.parse(name), content);
        PageData data = testPage.getData();
        data.setAttribute("Test");
        testPage.commit(data);
        return testPage;
    }

    @After
    public void tearDown() throws Exception {
        receiver.close();
        assertEquals(TEST_TIME, clockProvider.get().currentClockDate());
    }

    private String runSuite() throws Exception {
        receiver.receiveSocket();
        Response response = responder.makeResponse(request);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        return sender.sentData();
    }


    @Test
    public void testWithOneTest() throws Exception {
        String results = runSuite();
        assertSubString("href=\\\"#TestOne1\\\"", results);
        assertSubString("1 right", results);
        assertSubString("id=\"TestOne1\"", results);
        assertSubString(" href=\"SuitePage.TestOne\"", results);
        assertSubString("PassFixture", results);
    }

    @Test
    public void testPageWithXref() throws Exception {
        PageData data = suite.getData();
        data.setContent("!see XrefOne\r\n!see XrefTwo\n!see XrefThree\n");
        suite.commit(data);
        addTestPage(root, "XrefOne", fitPassFixture);
        addTestPage(root, "XrefTwo", fitPassFixture);

        String results = runSuite();
        assertSubString("href=\\\"#XrefOne2\\\"", results);
        assertSubString("href=\\\"#XrefTwo3\\\"", results);
    }

    @Test
    public void testWithTwoTests() throws Exception {
        addTestToSuite("TestTwo", "|!-fitnesse.testutil.FailFixture-!|\n\n|!-fitnesse.testutil.FailFixture-!|\n");
        String results = runSuite();

        assertSubString("href=\\\"#TestOne1\\\"", results);
        assertSubString("href=\\\"#TestTwo2\\\"", results);
        assertSubString("1 right", results);
        assertSubString("2 wrong", results);
        assertSubString("id=\"TestOne1\"", results);
        assertSubString("id=\"TestTwo2\"", results);
        assertSubString("PassFixture", results);
        assertSubString("FailFixture", results);
    }

    @Test
    public void testWithPrunedPage() throws Exception {
        WikiPage pageTwo = addTestToSuite("TestTwo",
                "|!-fitnesse.testutil.FailFixture-!|\n\n|!-fitnesse.testutil.FailFixture-!|\n"
        );
        PageData data = pageTwo.getData();
        data.setAttribute("Prune");
        pageTwo.commit(data);
        String results = runSuite();

        assertSubString("href=\\\"#TestOne1\\\"", results);
        assertNotSubString("href=\\\"#TestTwo2\\\"", results);
        assertSubString("1 right", results);
        assertSubString("0 wrong", results);
        assertSubString("id=\"TestOne1\"", results);
        assertNotSubString("id=\"TestTwo2\"", results);
        assertSubString("PassFixture", results);
        assertNotSubString("FailFixture", results);
    }

    @Test
    public void testSuiteWithEmptyPage() throws Exception {
        suite = crawler.addPage(root, PathParser.parse("SuiteWithEmptyPage"), "This is the empty page test suite\n");
        addTestPage(suite, "TestThatIsEmpty", "");
        request.setResource("SuiteWithEmptyPage");
        runSuite();

        WikiPagePath errorLogPath = PathParser.parse("ErrorLogs.SuiteWithEmptyPage");
        WikiPage errorLog = crawler.getPage(root, errorLogPath);
        PageData data = errorLog.getData();
        String errorLogContent = data.getContent();
        assertNotSubString("Exception", errorLogContent);
    }

    @Test
    public void testSuiteWithOneTestWithoutTable() throws Exception {
        addTestToSuite("TestWithoutTable", "This test has not table");
        addTestToSuite("TestTwo", fitPassFixture);
        addTestToSuite("TestThree", fitPassFixture);
        String results = runSuite();

        assertSubString("TestOne", results);
        assertSubString("TestTwo", results);
        assertSubString("TestThree", results);
        assertSubString("TestWithoutTable", results);
    }

    @Test
    public void testExitCodeHeader() throws Exception {
        String results = runSuite();
        assertSubString("Exit-Code: 0", results);
    }

    @Test
    public void exitCodeHeaderIsErrorCount() throws Exception {
        addTestToSuite("TestFailingTest", fitFailFixture);
        String results = runSuite();
        assertSubString("Exit-Code: 1", results);
    }


    @Test
    public void testExecutionStatusAppears() throws Exception {
        String results = runSuite();
        assertHasRegexp(divWithIdAndContent("execution-status", ".*?"), results);
    }

    @Test
    public void testTestSummaryInformationIncludesPageSummary() throws Exception {
        String results = runSuite();
        assertHasRegexp(divWithIdAndContent("test-summary",
                ".*?Test Pages:.*?&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.*?Assertions:.*?"
        ), results
        );
    }

    @Test
    public void testFormatTestSummaryInformation() throws Exception {
        String results = runSuite();
        assertHasRegexp(divWithIdAndContent("test-summary",
                ".*?<strong>Test Pages:</strong>.*?<strong>Assertions:</strong>.*?"
        ), results
        );
    }

    private String classpathWidgets() {
        return "!path classes\n" +
                "!path lib/dummy.jar\n";
    }

    @Test
    public void testNonMatchingSuiteFilter() throws Exception {
        addTestPagesWithSuiteProperty();
        request.setQueryString("suiteFilter=xxx");
        String results = runSuite();
        assertDoesNotHaveRegexp(".*href=\\\"#TestOne\\\".*", results);
        assertDoesNotHaveRegexp(".*href=\\\"#TestTwo\\\".*", results);
        assertDoesNotHaveRegexp(".*href=\\\"#TestThree\\\".*", results);
    }

    @Test
    public void testSimpleMatchingSuiteQuery() throws Exception {
        addTestPagesWithSuiteProperty();
        request.setQueryString("suiteFilter=foo");
        String results = runSuite();
        assertDoesNotHaveRegexp(".*href=\\\"#TestOne.*", results);
        assertSubString("href=\\\"#TestTwo1\\\"", results);
        assertDoesNotHaveRegexp(".*href=\\\"#TestThree.*", results);
    }

    @Test
    public void testSecondMatchingSuiteQuery() throws Exception {
        addTestPagesWithSuiteProperty();
        request.setQueryString("suiteFilter=smoke");
        String results = runSuite();
        assertDoesNotHaveRegexp(".*href=\\\"#TestOne.*", results);
        assertDoesNotHaveRegexp(".*href=\\\"#TestTwo.*", results);
        assertSubString("href=\\\"#TestThree1\\\"", results);
    }

    @Test
    public void multipleSuiteQuery() throws Exception {
        addTestPagesWithSuiteProperty();
        request.setQueryString("suiteFilter=smoke,foo");
        String results = runSuite();
        assertDoesNotHaveRegexp("#TestOne", results);
        assertHasRegexp("#TestTwo", results);
        assertHasRegexp("#TestThree", results);
    }


    @Test
    public void exculdeSuiteQuery() throws Exception {
        addTestPagesWithSuiteProperty();
        request.setQueryString("excludeSuiteFilter=foo");
        String results = runSuite();
        assertHasRegexp("#TestOne", results);
        assertDoesNotHaveRegexp("#TestTwo", results);
        assertHasRegexp("#TestThree", results);
    }


    @Test
    public void testFirstTest() throws Exception {
        addTestPagesWithSuiteProperty();
        request.setQueryString("firstTest=TestThree");
        String results = runSuite();
        assertDoesNotHaveRegexp("#TestOne", results);
        assertHasRegexp("#TestTwo", results);
        assertHasRegexp("#TestThree", results);
    }

    @Test
    public void testFirstTestWholePath() throws Exception {
        addTestPagesWithSuiteProperty();
        request.setQueryString("firstTest=SuitePage.TestThree");
        String results = runSuite();
        assertDoesNotHaveRegexp("#TestOne", results);
        assertHasRegexp("#TestTwo", results);
        assertHasRegexp("#TestThree", results);
    }


    @Test
    public void testTagsShouldBeInheritedFromSuite() throws Exception {
        PageData suiteData = suite.getData();
        suiteData.setAttribute(PageData.PropertySUITES, "tag");
        suite.commit(suiteData);
        addTestToSuite("TestInheritsTag", fitPassFixture);

        request.setQueryString("suiteFilter=tag");
        String results = runSuite();
        assertHasRegexp("#TestInheritsTag", results);
    }

    private void addTestPagesWithSuiteProperty() throws Exception {
        WikiPage test2 = addTestToSuite("TestTwo", fitPassFixture);
        WikiPage test3 = addTestToSuite("TestThree", fitPassFixture);
        PageData data2 = test2.getData();
        PageData data3 = test3.getData();
        data2.setAttribute(PageData.PropertySUITES, "foo");
        data3.setAttribute(PageData.PropertySUITES, "bar, smoke");
        test2.commit(data2);
        test3.commit(data3);
    }

    @Test
    public void testCanMixSlimAndFitTests() throws Exception {
        addTestToSuite("SlimTest", simpleSlimDecisionTable);
        String results = runSuite();
        assertHasRegexp("<td>fitnesse.testutil.PassFixture</td>", results);
        assertHasRegexp("<td><span class=\"pass\">wow</span></td>", results);
        assertHasRegexp("<h3>fit:fit.FitServer</h3>", results);
        assertHasRegexp("<h3>slim:fitnesse.slim.SlimService", results);
    }

    @Test
    public void xmlFormat() throws Exception {
        request.addInput("format", "xml");
        addTestToSuite("SlimTest", simpleSlimDecisionTable);
        String results = runSuite();
        Document testResultsDocument = getXmlDocumentFromResults(results);
        Element testResultsElement = testResultsDocument.getDocumentElement();
        assertEquals("testResults", testResultsElement.getNodeName());
        NodeList resultList = testResultsElement.getElementsByTagName("result");
        assertEquals(2, resultList.getLength());
        Element testResult;

        for (int elementIndex = 0; elementIndex < 2; elementIndex++) {
            testResult = (Element) resultList.item(elementIndex);
            String pageName = XmlUtil.getTextValue(testResult, "relativePageName");
            assertSubString(pageName + "?pageHistory&resultDate=", XmlUtil.getTextValue(testResult, "pageHistoryLink"));
            if ("SlimTest".equals(pageName)) {
                assertCounts(testResult, "1", "0", "0", "0");
            } else if ("TestOne".equals(pageName)) {
                assertCounts(testResult, "1", "0", "0", "0");
            } else {
                fail(pageName);
            }
        }
        Element finalCounts = XmlUtil.getElementByTagName(testResultsElement, "finalCounts");
        assertCounts(finalCounts, "2", "0", "0", "0");
    }

    @Test
    public void normalSuiteRunWithThreePassingTestsProducesSuiteResultFile() throws Exception {
        File xmlResultsFile = expectedXmlResultsFile();

        assertFalse("Unclean test environment.  tearDown did not successfully tear down.", xmlResultsFile.exists());

        addTestToSuite("SlimTestOne", simpleSlimDecisionTable);
        addTestToSuite("SlimTestTwo", simpleSlimDecisionTable);
        runSuite();

        FileInputStream xmlResultsStream = new FileInputStream(xmlResultsFile);
        XmlUtil.newDocument(xmlResultsStream);
        xmlResultsStream.close();
    }

    @Test
    public void NoHistory_avoidsProducingSuiteResultFile() throws Exception {
        File xmlResultsFile = expectedXmlResultsFile();

        assertFalse("Unclean test environment.  tearDown did not successfully tear down.", xmlResultsFile.exists());

        request.addInput("nohistory", "true");
        addTestToSuite("SlimTestOne", simpleSlimDecisionTable);
        addTestToSuite("SlimTestTwo", simpleSlimDecisionTable);
        runSuite();
        assertFalse(xmlResultsFile.exists());
    }

    @Test
    public void Includehtml_producesHTMLResultsInXMLSuite() throws Exception {
        request.addInput("format", "xml");
        request.addInput("includehtml", "true");
        addTestToSuite("SlimTestOne", simpleSlimDecisionTable);
        addTestToSuite("SlimTestTwo", simpleSlimDecisionTable);
        String results = runSuite();
        assertSubString("<content><![CDATA[", results);
    }

    private File expectedXmlResultsFile() {
        TestSummary counts = new TestSummary(3, 0, 0, 0);
        String resultsFileName = String.format("%s/SuitePage/20081205011900_%d_%d_%d_%d.xml",
                testResultsPath, counts.getRight(), counts.getWrong(), counts.getIgnores(), counts.getExceptions());
        return new File(resultsFileName);
    }

    @Test
    public void normalSuiteRunProducesIndivualTestHistoryFile() throws Exception {
        TestSummary counts = new TestSummary(1, 0, 0, 0);
        String resultsFileName = String.format("%s/SuitePage.SlimTest/20081205011900_%d_%d_%d_%d.xml",
                testResultsPath, counts.getRight(), counts.getWrong(), counts.getIgnores(), counts.getExceptions());
        File xmlResultsFile = new File(resultsFileName);

        assertFalse("Unclean test environment.  tearDown did not successfully tear down.", xmlResultsFile.exists());

        addTestToSuite("SlimTest", simpleSlimDecisionTable);
        runSuite();

        assertTrue(resultsFileName, xmlResultsFile.exists());
        FileInputStream xmlResultsStream = new FileInputStream(xmlResultsFile);
        XmlUtil.newDocument(xmlResultsStream);
        xmlResultsStream.close();
    }

    @Test
    public void exitCodeHeaderIsErrorCountForXml() throws Exception {
        request.addInput("format", "xml");
        addTestToSuite("TestFailingTest", fitFailFixture);
        String results = runSuite();
        assertSubString("Exit-Code: 1", results);
    }
}
