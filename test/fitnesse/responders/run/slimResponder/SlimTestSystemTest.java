// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.components.ClassPathBuilder;
import fitnesse.components.CommandRunner;
import fitnesse.http.MockCommandRunner;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.slim.SlimClient;
import fitnesse.slim.SlimServer;
import fitnesse.slim.SlimService;
import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.Table;
import fitnesse.slimTables.TableScanner;
import fitnesse.wiki.*;
import fitnesse.wikitext.Utils;
import fitnesse.wikitext.parser.Collapsible;
import fitnesse.wikitext.parser.Include;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.test.ParserTestHelper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class SlimTestSystemTest extends FitnesseBaseTestCase {
    private WikiPage root;
    private PageCrawler crawler;
    private TestSystemListener dummyListener = new DummyListener();
    private SlimTestSystem testSystem;

    @Before
    public void setUp() throws Exception {
        FitNesseContext context = makeContext();
        root = context.root;
        crawler = root.getPageCrawler();
    }

    @After
    public void tearDown() throws Exception {
        if (testSystem != null) {
            testSystem.bye();
            testSystem = null;
        }
        SlimTestSystem.clearSlimPortOffset();
    }

    private Matcher<String> contains(final String needle) {
        return new BaseMatcher<String>() {
            @Override
            public boolean matches(Object o) {
                assertThat(o, instanceOf(String.class));
                return ((String) o).contains(needle);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("contains");
            }
        };
    }

    private String getResultsForPageContents(String pageContents) throws Exception {
        WikiPage testPage = createTestPageWithContent(pageContents);
        PageData pageData = testPage.getData();
        testSystem = new HtmlSlimTestSystem(pageData.getWikiPage(), dummyListener, new FastTestMode());
        String classPath = new ClassPathBuilder().getClasspath(testPage);
        TestSystem.Descriptor descriptor = TestSystem.getDescriptor(testPage.getData(), false);
        testSystem.getExecutionLog(classPath, descriptor);
        testSystem.start();
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

    private WikiPage createTestPageWithContent(String pageContents) throws IOException {
        WikiPage testPage = crawler.addPage(root, PathParser.parse("TestPage"), "!define TEST_SYSTEM {slim}\n!path classes");
        PageData data = testPage.getData();
        data.setContent(data.getContent() + "\n" + pageContents);
        testPage.commit(data);
        return testPage;
    }

    @Test
    public void portRotates() throws Exception {
        for (int i = 1; i < 15; i++)
            assertEquals(8085 + (i % 10), SlimTestSystem.getNextSlimSocket(SlimTestSystem.getSlimPortBase(root)));
    }

    @Test
    public void portStartsAtSlimPortVariable() throws Exception {
        WikiPage pageWithSlimPortDefined = crawler.addPage(root, PathParser.parse("PageWithSlimPortDefined"), "!define SLIM_PORT {9000}\n");
        for (int i = 1; i < 15; i++)
            assertEquals(9000 + (i % 10), SlimTestSystem.getNextSlimSocket(SlimTestSystem.getSlimPortBase(pageWithSlimPortDefined)));
    }

    @Test
    public void badSlimPortVariableDefaults() throws Exception {
        WikiPage pageWithBadSlimPortDefined = crawler.addPage(root, PathParser.parse("PageWithBadSlimPortDefined"), "!define SLIM_PORT {BOB}\n");
        for (int i = 1; i < 15; i++)
            assertEquals(8085 + (i % 10), SlimTestSystem.getNextSlimSocket(SlimTestSystem.getSlimPortBase(pageWithBadSlimPortDefined)));
    }

    @Test
    public void slimHostDefaultsTolocalhost() throws Exception {
        WikiPage pageWithoutSlimHostVariable = crawler.addPage(root, PathParser.parse("PageWithoutSlimHostVariable"), "some gunk\n");
        SlimTestSystem sys = new HtmlSlimTestSystem(pageWithoutSlimHostVariable, dummyListener, new FastTestMode());
        assertEquals("localhost", sys.determineSlimHost());
    }

    @Test
    public void slimHostVariableSetsTheHost() throws Exception {
        WikiPage pageWithSlimHostVariable = crawler.addPage(root, PathParser.parse("PageWithSlimHostVariable"), "!define SLIM_HOST {somehost}\n");
        SlimTestSystem sys = new HtmlSlimTestSystem(pageWithSlimHostVariable, dummyListener, new FastTestMode());
        assertEquals("somehost", sys.determineSlimHost());
    }

    // TODO: This test runs a lot of code, but what does it test?
    @Test
    public void slimResponderStartsAndQuitsSlim() throws Exception {
        WikiPage testPage = createTestPageWithContent("");
        PageData pageData = testPage.getData();
        testSystem = new HtmlSlimTestSystem(pageData.getWikiPage(), dummyListener);
        String classPath = new ClassPathBuilder().getClasspath(testPage);
        TestSystem.Descriptor descriptor = TestSystem.getDescriptor(testPage.getData(), false);
        testSystem.getExecutionLog(classPath, descriptor);
        testSystem.start();
        testSystem.bye();

        // TODO: Why is this sleep here?
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            // ok
        }
    }

    @Test
    public void verboseOutputIfSlimFlagSet() throws Exception {
        WikiPage testPage = createTestPageWithContent("!define SLIM_FLAGS {-v}\n");
        testSystem = new HtmlSlimTestSystem(testPage.getData().getWikiPage(), dummyListener, new FastTestMode());
        testSystem.getExecutionLog(new ClassPathBuilder().getClasspath(testPage), TestSystem.getDescriptor(testPage.getData(), false));
        assertTrue(testSystem.getCommandLine().contains("fitnesse.slim.SlimService -v"));
    }

    @Test
    public void tableWithoutPrefixWillBeConstructed() throws Exception {
        String testResults = getResultsForPageContents("|XX|\n");
        assertThat(unescape(testResults), contains("<td>XX <span class=\"error\">Could not invoke constructor for XX[0]</span></td>"));
    }

    @Test
    public void emptyQueryTable() throws Exception {
        String testResults = getResultsForPageContents("|Query:x|\n");
        assertThat(unescape(testResults), contains("Query tables must have at least two rows."));
    }

    @Test
    public void queryFixtureHasNoQueryFunction() throws Exception {
        String testResults = getResultsForPageContents(
                "!|Query:fitnesse.slim.test.TestSlim|\n" +
                        "|x|y|\n"
        );
        assertThat(unescape(testResults), contains("Method query[0] not found in fitnesse.slim.test.TestSlim"));
    }

    @Test
    public void emptyOrderedQueryTable() throws Exception {
        String testResults = getResultsForPageContents("|ordered query:x|\n");
        assertThat(unescape(testResults), contains("Query tables must have at least two rows."));
    }

    @Test
    public void orderedQueryFixtureHasNoQueryFunction() throws Exception {
        String testResults = getResultsForPageContents(
                "!|ordered query:fitnesse.slim.test.TestSlim|\n" +
                        "|x|y|\n"
        );
        assertThat(unescape(testResults), contains("Method query[0] not found in fitnesse.slim.test.TestSlim"));
    }

    @Test
    public void emptySubsetQueryTable() throws Exception {
        String testResults = getResultsForPageContents("|subset query:x|\n");
        assertThat(unescape(testResults), contains("Query tables must have at least two rows."));
    }

    @Test
    public void subsetQueryFixtureHasNoQueryFunction() throws Exception {
        String testResults = getResultsForPageContents(
                "!|subset query:fitnesse.slim.test.TestSlim|\n" +
                        "|x|y|\n"
        );
        assertThat(unescape(testResults), contains("Method query[0] not found in fitnesse.slim.test.TestSlim"));
    }

    @Test
    public void scriptTableWithBadConstructor() throws Exception {
        String testResults = getResultsForPageContents("!|Script|NoSuchClass|\n");
        assertThat(unescape(testResults), contains("<span class=\"error\">Could not invoke constructor for NoSuchClass"));
    }

    @Test
    public void emptyImportTable() throws Exception {
        String testResults = getResultsForPageContents("|Import|\n");
        assertThat(unescape(testResults), contains("Import tables must have at least two rows."));
    }

    @Test
    public void emptyTableTable() throws Exception {
        String testResults = getResultsForPageContents("!|Table:TableFixture|\n");
        assertThat(unescape(testResults), contains("<span class=\"error\">Could not invoke constructor for TableFixture[0]</span>"));
    }

    @Test
    public void tableFixtureHasNoDoTableFunction() throws Exception {
        String testResults = getResultsForPageContents(
                "!|Table:fitnesse.slim.test.TestSlim|\n" +
                        "|a|b|\n"
        );
        assertThat(unescape(testResults), contains("Table fixture has no valid doTable method"));
    }


    @Test
    public void simpleDecisionTable() throws Exception {
        String testResults = getResultsForPageContents(
                "!|DT:fitnesse.slim.test.TestSlim|\n" +
                        "|returnInt?|\n" +
                        "|7|\n"
        );
        assertThat(unescape(testResults), contains("<span class=\"pass\">7</span>"));
    }

    @Test
    public void decisionTableIgnoresMethodMissingForResetExecuteaAndTable() throws Exception {
        String testResults = getResultsForPageContents(
                "!|DT:fitnesse.slim.test.DummyDecisionTable|\n" +
                        "|x?|\n" +
                        "|1|\n"
        );
        assertEquals(0, testSystem.getTestSummary().getExceptions());
    }

    @Test
    public void decisionTableWithNoResetDoesNotCountExceptionsForExecute() throws Exception {
        String testResults = getResultsForPageContents(
                "!|DT:fitnesse.slim.test.DummyDecisionTableWithExecuteButNoReset|\n" +
                        "|x?|\n" +
                        "|1|\n"
        );
        assertEquals(0, testSystem.getTestSummary().getExceptions());
    }

    @Test
    public void queryTableWithoutTableFunctionIgnoresMissingMethodException() throws Exception {
        String testResults = getResultsForPageContents(
                "!|query:fitnesse.slim.test.DummyQueryTableWithNoTableMethod|\n" +
                        "|x|\n" +
                        "|1|\n"
        );
        assertEquals(0, testSystem.getTestSummary().getExceptions());
    }

    @Test
    public void decisionTableWithExecuteThatThrowsDoesShowsException() throws Exception {
        String testResults = getResultsForPageContents(
                "!|DT:fitnesse.slim.test.DecisionTableExecuteThrows|\n" +
                        "|x?|\n" +
                        "|1|\n"
        );
        assertEquals(1, testSystem.getTestSummary().getExceptions());
        assertThat(unescape(testResults), contains("EXECUTE_THROWS"));
    }

    @Test
    public void tableWithException() throws Exception {
        String testResults = getResultsForPageContents(
                "!|DT:NoSuchClass|\n" +
                        "|returnInt?|\n" +
                        "|7|\n"
        );
        assertThat(unescape(testResults), contains("<span class=\"error\">Could not invoke constructor for NoSuchClass"));
    }

    @Test
    public void tableWithBadConstructorHasException() throws Exception {
        String testResults = getResultsForPageContents(
                "!|DT:fitnesse.slim.test.TestSlim|badArgument|\n" +
                        "|returnConstructorArgument?|\n" +
                        "|3|\n"
        );
        TableScanner ts = new HtmlTableScanner(testSystem.getTestResults().getHtml());
        ts.getTable(0);
        assertThat(unescape(testResults), contains("Could not invoke constructor"));
    }

    @Test
    public void tableWithBadVariableHasException() throws Exception {
        String testResults = getResultsForPageContents(
                "!|DT:fitnesse.slim.test.TestSlim|\n" +
                        "|noSuchVar|\n" +
                        "|3|\n"
        );
        assertThat(unescape(testResults), contains("<span class=\"error\">Method setNoSuchVar[1] not found in fitnesse.slim.test.TestSlim"));
    }

    @Test
    public void tableWithStopTestMessageException() throws Exception {
        String testResults = getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|\n" +
                "|throwStopTestExceptionWithMessage?|\n" +
                "| once |\n" +
                "| twice |\n");
        assertThat(unescape(testResults), contains("<td>once <span class=\"fail\">Stop Test</span></td>"));
        assertThat(unescape(testResults), contains("<td>twice <span class=\"ignore\">Test not run</span>"));
    }

    @Test
    public void tableWithMessageException() throws Exception {
        String testResults = getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|\n" +
                "|throwExceptionWithMessage?|\n" +
                "| once |\n");
        assertThat(unescape(testResults), contains("<td>once <span class=\"error\">Test message</span></td>"));
    }

    @Test
    public void tableWithStopTestExceptionThrown() throws Exception {
        String testResults = getResultsForPageContents("!|DT:fitnesse.slim.test.TestSlim|\n" +
                "|throwNormal?| throwStopping? |\n" +
                "| first | second  |\n" +
                "| should fail1| true           |\n" +
                "\n\n" +
                "!|DT:fitnesse.slim.test.ThrowException|\n" +
                "|throwNormal?|\n" +
                "| should fail2|\n"
        );
        assertThat(unescape(testResults), contains("<td><span class=\"error\">Exception: <a href"));
        assertThat(unescape(testResults), contains("<td><span class=\"error\">Exception: <a href"));
        assertThat(unescape(testResults), contains("<td>should fail1 <span class=\"ignore\">Test not run</span></td>"));
        assertThat(unescape(testResults), contains("<td>should fail2 <span class=\"ignore\">Test not run</span></td>"));
    }

    @Test
    public void tableWithSymbolSubstitution() throws Exception {
        String testResults = getResultsForPageContents(
                "!|DT:fitnesse.slim.test.TestSlim|\n" +
                        "|string|getStringArg?|\n" +
                        "|Bob|$V=|\n" +
                        "|$V|$V|\n"
        );
        TableScanner ts = getScannedResults(testResults);
        Table dt = ts.getTable(0);
        assertEquals("$V<-[Bob]", unescape(dt.getCellContents(1, 2)));
        assertEquals("$V->[Bob]", unescape(dt.getCellContents(0, 3)));
    }

    protected TableScanner getScannedResults(String testResults) throws Exception {
        return new HtmlTableScanner(testResults);
    }

    private static String unescape(String x) {
        return Utils.unescapeWiki(Utils.unescapeHTML(x));
    }

    @Test
    public void substituteSymbolIntoExpression() throws Exception {
        String testResults = getResultsForPageContents(
                "!|DT:fitnesse.slim.test.TestSlim|\n" +
                        "|string|getStringArg?|\n" +
                        "|3|$A=|\n" +
                        "|2|<$A|\n" +
                        "|5|$B=|\n" +
                        "|4|$A<_<$B|\n"
        );
        TableScanner ts = getScannedResults(testResults);
        Table dt = ts.getTable(0);
        assertEquals("<span class=\"pass\">2<$A->[3]</span>", unescape(dt.getCellContents(1, 3)));
        assertEquals("<span class=\"pass\">$A->[3]<4<$B->[5]</span>", unescape(dt.getCellContents(1, 5)));
    }

    @Test
    public void tableWithExpression() throws Exception {
        String testResults = getResultsForPageContents(
                "!|DT:fitnesse.slim.test.TestSlim|\n" +
                        "|string|getStringArg?|\n" +
                        "|${=3+4=}|7|\n"
        );
        TableScanner ts = getScannedResults(testResults);
        Table dt = ts.getTable(0);
        assertEquals("<span class=\"pass\">7</span>", dt.getCellContents(1, 2));
    }

    @Test
    public void noSuchConverter() throws Exception {
        String testResults = getResultsForPageContents(
                "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
                        "|noSuchConverter|noSuchConverter?|\n" +
                        "|x|x|\n"
        );
        TableScanner ts = getScannedResults(testResults);
        Table dt = ts.getTable(0);
        assertEquals("x <span class=\"error\">No converter for fitnesse.slim.test.TestSlim$NoSuchConverter.</span>", dt.getCellContents(0, 2));
    }

    @Test
    public void translateExceptionMessage() throws Exception {
        assertTranslatedException("Could not find constructor for SomeClass", "NO_CONSTRUCTOR SomeClass");
        assertTranslatedException("Could not invoke constructor for SomeClass", "COULD_NOT_INVOKE_CONSTRUCTOR SomeClass");
        assertTranslatedException("No converter for SomeClass", "NO_CONVERTER_FOR_ARGUMENT_NUMBER SomeClass");
        assertTranslatedException("Method someMethod not found in SomeClass", "NO_METHOD_IN_CLASS someMethod SomeClass");
        assertTranslatedException("The instance someInstance does not exist", "NO_INSTANCE someInstance");
        assertTranslatedException("Could not find class SomeClass", "NO_CLASS SomeClass");
        assertTranslatedException("The instruction [a, b, c] is malformed", "MALFORMED_INSTRUCTION [a, b, c]");
    }

    private void assertTranslatedException(String expected, String message) {
        assertEquals(expected, SlimTestSystem.translateExceptionMessage(message));
    }

    @Test
    public void returnedListsBecomeStrings() throws Exception {
        String testResults = getResultsForPageContents("!|script|\n" +
                "|start|fitnesse.slim.test.TestSlim|\n" +
                "|one list|1,2|\n" +
                "|check|get list arg|[1, 2]|\n");
        assertThat(unescape(testResults), contains("<td><span class=\"pass\">[1, 2]</span></td>"));
    }

    @Test
    public void nullStringReturned() throws Exception {
        String testResults = getResultsForPageContents("!|fitnesse.slim.test.TestSlim|\n" +
                "|nullString?|\n" +
                "|null|\n");
        assertThat(unescape(testResults), contains("<td><span class=\"pass\">null</span></td>"));
    }

    @Test
    public void reportableExceptionsAreReported() throws Exception {
        String testResults = getResultsForPageContents(
                "!|fitnesse.slim.test.ExecuteThrowsReportableException|\n" +
                        "|x|\n" +
                        "|1|\n");
        assertThat(unescape(testResults), contains("A Reportable Exception"));
    }

    @Test
    public void versionMismatchIsNotReported() throws Exception {
        String testResults = getResultsForPageContents("");
        assertThat(unescape(testResults), not(contains("Slim Protocol Version Error")));
    }

    @Test
    public void versionMismatchIsReported() throws Exception {
        SlimClient.MINIMUM_REQUIRED_SLIM_VERSION = 1000.0;  // I doubt will ever get here.
        String testResults = getResultsForPageContents("");
        assertThat(unescape(testResults), contains("Slim Protocol Version Error"));
    }

    @Test
    public void checkTestClassPrecededByDefine() throws Exception {
        String testResults = getResultsForPageContents("!define PI {3.141592}\n" +
                "!path classes\n" +
                "!path fitnesse.jar\n" +
                "|fitnesse.testutil.PassFixture|\n");
        assertThat(unescape(testResults), contains("PassFixture"));
    }

    @Test
    public void emptyScenarioTable() throws Exception {
        String testResults = getResultsForPageContents("|Scenario|\n");
        assertThat(unescape(testResults), contains("Scenario tables must have a name."));
    }

    @Test
    public void scenarioTableIsRegistered() throws Exception {
        String testResults = getResultsForPageContents("|Scenario|myScenario|\n");
        assertTrue("scenario should be registered", testSystem.getScenarios().containsKey("myScenario"));
    }

    @Test(expected = SocketException.class)
    public void createSlimServiceFailsFastWhenSlimPortIsNotAvailable() throws Exception {
        final int slimServerPort = 10258;
        ServerSocket slimSocket = new ServerSocket(slimServerPort);
        try {
            String slimArguments = String.format("%s %d", "", slimServerPort);
            FastTestMode.createSlimService(slimArguments);
        } finally {
            slimSocket.close();
        }
    }

    @Test
    public void gettingPrecompiledScenarioWidgetsForChildLibraryPage() throws Exception {
        WikiPage suitePage = crawler.addPage(root, PathParser.parse("MySuite"), "my suite content");
        crawler.addPage(suitePage, PathParser.parse("ScenarioLibrary"), "child library");
        SlimTestSystem sys = new HtmlSlimTestSystem(suitePage, dummyListener, new FastTestMode());

        Symbol scenarios = sys.getPreparsedScenarioLibrary();

        Symbol includeParent = getCollapsibleSymbol(scenarios);
        assertNotNull(includeParent);
        assertEquals("Precompiled Libraries", ParserTestHelper.serializeContent(includeParent.childAt(0)));
        Symbol childLibraryInclude = getIncludeSymbol(includeParent.childAt(1));
        assertTrue(ParserTestHelper.serializeContent(childLibraryInclude).contains("child library"));
    }

    @Test
    public void gettingPrecompiledScenarioWidgetsForUncleLibraryPage() throws Exception {
        WikiPage suitePage = crawler.addPage(root, PathParser.parse("ParentPage.MySuite"), "my suite content");
        crawler.addPage(root, PathParser.parse("ScenarioLibrary"), "uncle library");
        SlimTestSystem sys = new HtmlSlimTestSystem(suitePage, dummyListener, new FastTestMode());

        Symbol scenarios = sys.getPreparsedScenarioLibrary();

        Symbol includeParent = getCollapsibleSymbol(scenarios);
        assertNotNull(includeParent);
        assertEquals("Precompiled Libraries", ParserTestHelper.serializeContent(includeParent.childAt(0)));
        Symbol uncleLibraryInclude = getIncludeSymbol(includeParent.childAt(1));
        assertNotNull(uncleLibraryInclude);
        assertTrue(ParserTestHelper.serializeContent(uncleLibraryInclude).contains("uncle library"));
    }

    @Test
    public void precompiledScenarioWidgetsAreCreatedOnlyOnce() throws Exception {
        WikiPage suitePage = crawler.addPage(root, PathParser.parse("MySuite"), "my suite content");
        SlimTestSystem sys = new HtmlSlimTestSystem(suitePage, dummyListener, new FastTestMode());

        assertSame(sys.getPreparsedScenarioLibrary(), sys.getPreparsedScenarioLibrary());
    }

    private Symbol getIncludeSymbol(Symbol collapsibleSymbol) {
        for (Symbol symbol : collapsibleSymbol.getChildren())
            if (symbol.getType() instanceof Include)
                return symbol;
        return null;
    }

    private Symbol getCollapsibleSymbol(Symbol syntaxTree) throws Exception {
        for (Symbol symbol : syntaxTree.getChildren()) {
            if (symbol.getType() instanceof Collapsible)
                return symbol;
        }
        return null;
    }

    private static class DummyListener implements TestSystemListener {
        public void acceptOutputFirst(String output) {
        }

        public void testComplete(TestSummary testSummary) {
        }

        public void exceptionOccurred(Throwable e) {
        }
    }

    public static class FastTestMode implements SlimTestSystem.SlimTestMode {

        private SlimServer slimServer;

        private static SlimServer createSlimService(String args) throws SocketException {
            try {
                SlimServer slimServer;
                while ((slimServer = tryCreateSlimService(args)) == null)
                    Thread.sleep(10);
                return slimServer;
            } catch (InterruptedException e) {
                // ok
            }
            return null;
        }

        private static SlimServer tryCreateSlimService(String args) throws SocketException {
            try {
                return SlimService.startSlimService(args.trim().split(" "));
            } catch (SocketException e) {
                throw e;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public CommandRunner createSlimRunner(String classPath, SlimTestSystem testSystem) throws IOException {
            String slimArguments = String.format("%s %d", testSystem.getSlimFlags(), testSystem.getSlimSocket());
            slimServer = createSlimService(slimArguments);
            return new MockCommandRunner();
        }

        @Override
        public void bye(SlimTestSystem testSystem) throws IOException {
            if (slimServer != null)
                slimServer.getSocketService().close();
        }
    }
}
