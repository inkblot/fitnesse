// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.formatters;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNeseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.TestSummary;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;
import util.TimeMeasurement;

import static util.RegexAssertions.assertSubString;

public class TestHtmlFormatterTest extends FitnesseBaseTestCase {
    private BaseFormatter formatter;
    private StringBuffer pageBuffer = new StringBuffer();
    private WikiPage page;
    private WikiPage root;
    private FitNesseContext context;

    @Inject
    public void inject(@Named(FitNeseModule.ROOT_PAGE) WikiPage root, FitNesseContext context) {
        this.root = root;
        this.context = context;
    }

    @Before
    public void setUp() throws Exception {
        page = root.addChildPage("NewPage");
        page.getData().setContent("page content here");

        formatter = new TestHtmlFormatter(context, page, new HtmlPageFactory()) {
            @Override
            protected void writeData(String output) {
                pageBuffer.append(output);
            }
        };
    }

    @Test
    public void testHead() throws Exception {
        formatter.writeHead("test");

        assertSubString("<div id=\"test-summary\">Running Tests ...</div>", pageBuffer.toString());
    }

    @Test
    public void testTestSummaryTestPass() throws Exception {
        TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();
        TimeMeasurement timeMeasurement = new TimeMeasurement();
        formatter.writeHead("test");
        formatter.announceNumberTestsToRun(1);
        formatter.newTestStarted(page, timeMeasurement.start());
        formatter.testComplete(page, new TestSummary(4, 0, 0, 0), timeMeasurement.stop());
        formatter.allTestingComplete(totalTimeMeasurement.stop());
        assertSubString("<script>document.getElementById(\"test-summary\").innerHTML =", pageBuffer.toString());
        assertSubString("<strong>Assertions:</strong> 4 right, 0 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
        assertSubString("document.getElementById(\"test-summary\").className = \"pass\"", pageBuffer.toString());
    }

    @Test
    public void testTestSummaryTestFail() throws Exception {
        TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();
        TimeMeasurement timeMeasurement = new TimeMeasurement();
        formatter.writeHead("test");
        formatter.announceNumberTestsToRun(1);
        formatter.newTestStarted(page, timeMeasurement.start());
        formatter.testComplete(page, new TestSummary(4, 1, 0, 0), timeMeasurement.stop());
        formatter.allTestingComplete(totalTimeMeasurement.stop());
        assertSubString("<strong>Assertions:</strong> 4 right, 1 wrong, 0 ignored, 0 exceptions", pageBuffer.toString());
        assertSubString("document.getElementById(\"test-summary\").className = \"fail\"", pageBuffer.toString());
    }

    @Test
    public void testExecutionStatusHtml() throws Exception {
        TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();
        TimeMeasurement timeMeasurement = new TimeMeasurement();
        formatter.writeHead("test");
        formatter.setExecutionLogAndTrackingId("2", new CompositeExecutionLog(root.addChildPage("ErrorLogs")));
        formatter.announceNumberTestsToRun(1);
        formatter.newTestStarted(page, timeMeasurement.start());
        formatter.testComplete(page, new TestSummary(4, 1, 0, 0), timeMeasurement.stop());
        formatter.allTestingComplete(totalTimeMeasurement.stop());
        assertSubString("<div id=\"execution-status\">", pageBuffer.toString());
    }

    @Test
    public void testTail() throws Exception {
        TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();
        TimeMeasurement timeMeasurement = new TimeMeasurement();
        formatter.writeHead("test");
        formatter.announceNumberTestsToRun(1);
        formatter.newTestStarted(page, timeMeasurement.start());
        formatter.testComplete(page, new TestSummary(4, 1, 0, 0), timeMeasurement.stop());
        formatter.allTestingComplete(totalTimeMeasurement.stop());

        assertSubString("</html>", pageBuffer.toString());
    }

    @Test
    public void testStop() throws Exception {
        TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();
        TimeMeasurement timeMeasurement = new TimeMeasurement();
        formatter.writeHead("test");
        formatter.setExecutionLogAndTrackingId("2", new CompositeExecutionLog(root.addChildPage("ErrorLogs")));
        formatter.announceNumberTestsToRun(1);
        formatter.newTestStarted(page, timeMeasurement.start());
        formatter.testComplete(page, new TestSummary(4, 1, 0, 0), timeMeasurement.stop());
        formatter.allTestingComplete(totalTimeMeasurement.stop());
        //assert stop button added
        assertSubString("<a href=\"#\" onclick=\"doSilentRequest('?responder=stoptest&id=2')\">", pageBuffer.toString());
        //assert stop button removed
        assertSubString("document.getElementById(\"stop-test\").innerHTML = \"\"", pageBuffer.toString());
    }

    @Test
    public void testIncompleteMessageAfterException() throws Exception {
        TimeMeasurement timeMeasurement = new TimeMeasurement();
        formatter.writeHead("test");
        formatter.setExecutionLogAndTrackingId("2", new CompositeExecutionLog(root.addChildPage("ErrorLogs")));
        formatter.announceNumberTestsToRun(1);
        formatter.newTestStarted(page, timeMeasurement.start());
        pageBuffer.setLength(0);
        formatter.errorOccurred();
        //assert stop button added
        assertSubString("Testing was interupted", pageBuffer.toString());
        //assert stop button removed
        assertSubString("className = \"fail\"", pageBuffer.toString());
    }

    @Test
    public void testTimingShouldAppearInSummary() throws Exception {
        TimeMeasurement totalTimeMeasurement = newConstantElapsedTimeMeasurement(987).start();
        TimeMeasurement timeMeasurement = newConstantElapsedTimeMeasurement(600);
        formatter.writeHead("test");
        formatter.announceNumberTestsToRun(1);
        formatter.newTestStarted(page, timeMeasurement.start());
        formatter.testComplete(page, new TestSummary(1, 2, 3, 4), timeMeasurement.stop());
        formatter.allTestingComplete(totalTimeMeasurement.stop());
        assertSubString("<strong>Assertions:</strong> 1 right, 2 wrong, 3 ignored, 4 exceptions (0.600 seconds)", pageBuffer.toString());
    }

    private TimeMeasurement newConstantElapsedTimeMeasurement(final long theElapsedTime) {
        return new TimeMeasurement() {
            @Override
            public long elapsed() {
                return theElapsedTime;
            }
        };
    }
}
