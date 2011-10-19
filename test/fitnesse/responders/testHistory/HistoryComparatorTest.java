package fitnesse.responders.testHistory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.wiki.*;
import org.apache.velocity.app.VelocityEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static util.RegexAssertions.assertSubString;

public class HistoryComparatorTest extends FitnesseBaseTestCase {
    private HistoryComparator comparator;
    public FitNesseContext context;
    public WikiPage root;
    public String firstContent;
    public String secondContent;

    @Inject
    public void inject(FitNesseContext context, @Named(FitNesseContextModule.ROOT_PAGE) WikiPage root) {
        this.context = context;
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        comparator = new HistoryComparator() {
            public String getFileContent(String filePath) {
                if (filePath.equals("TestFolder/FileOne"))
                    return "this is file one";
                else if (filePath.equals("TestFolder/FileTwo"))
                    return "this is file two";
                else
                    return null;
            }
        };
        firstContent = getContentWith("pass");
        secondContent = getContentWith("fail");
        comparator.firstTableResults = new ArrayList<String>();
        comparator.secondTableResults = new ArrayList<String>();
        comparator.matchedTables = new ArrayList<HistoryComparator.MatchedPair>();
    }

    @Test
    public void shouldBeAbleToGrabTwoFilesToBeCompared() throws Exception {
        FileUtil.createFile("TestFolder/FileOne", "this is file one");
        FileUtil.createFile("TestFolder/FileTwo", "this is file two");
        comparator.compare("TestFolder/FileOne", "TestFolder/FileTwo");
        assertEquals("this is file one", comparator.firstFileContent);
        assertEquals("this is file two", comparator.secondFileContent);
        FileUtil.deleteFileSystemDirectory("TestFolder");
    }

    @Test
    public void shouldKnowIfTheTwoFilesAreTheSameFile() throws Exception {
        FileUtil.createFile("TestFolder/FileOne", "this is file one");
        boolean compareWorked = comparator.compare("TestFolder/FileOne", "TestFolder/FileOne");
        assertFalse(compareWorked);
        FileUtil.deleteFileSystemDirectory("TestFolder");
    }


    @Test
    public void shouldCompareTwoSetsOfTables() throws Exception {
        comparator.firstFileContent = "<table><tr><td>x</td></tr></table><table><tr><td>y</td></tr></table>";
        comparator.secondFileContent = "<table><tr><td>x</td></tr></table><table><tr><td>y</td></tr></table>";
        assertTrue(comparator.grabAndCompareTablesFromHtml());
        assertEquals(2, comparator.getResultContent().size());
        assertEquals("pass", comparator.getResultContent().get(0));
        assertEquals("pass", comparator.getResultContent().get(1));
    }

    @Test
    public void shouldCompareUnevenAmountsOfTables() throws Exception {
        comparator.firstFileContent = "<table><tr><td>x</td></tr></table><table><tr><td>y</td></tr></table>";
        comparator.secondFileContent = "<table><tr><td>x</td></tr></table>";
        assertTrue(comparator.grabAndCompareTablesFromHtml());
        assertEquals(2, comparator.getResultContent().size());
        assertEquals("pass", comparator.getResultContent().get(0));
        assertEquals("fail", comparator.getResultContent().get(1));
    }

    @Test
    public void findMatchScoreByFirstIndex() throws Exception {
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(1, 2, 1.1));
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(3, 4, 1.0));
        assertEquals(1.1, comparator.findScoreByFirstTableIndex(1), .0001);
        assertEquals(1.0, comparator.findScoreByFirstTableIndex(3), .0001);
    }

    @Test
    public void shouldBeAbleToFindMatchScoreByFirstIndexAndReturnAPercentString() throws Exception {
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(1, 2, 1.1));
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(3, 4, 1.0));
        assertSubString("91.67", comparator.findScoreByFirstTableIndexAsStringAsPercent(1));
        assertSubString("83.33", comparator.findScoreByFirstTableIndexAsStringAsPercent(3));
    }

    @Test
    public void shouldBeAbleToTellIfTableListsWereACompleteMatch() throws Exception {
        assertFalse(comparator.allTablesMatch());
        comparator.firstTableResults.add("A");
        comparator.firstTableResults.add("B");
        comparator.secondTableResults.add("A");
        comparator.secondTableResults.add("B");
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(0, 0, HistoryComparator.MAX_MATCH_SCORE));
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(1, 1, 1.0));
        assertFalse(comparator.allTablesMatch());
        comparator.matchedTables.remove(new HistoryComparator.MatchedPair(1, 1, 1.0));
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(1, 1, HistoryComparator.MAX_MATCH_SCORE));
        assertTrue(comparator.allTablesMatch());
        comparator.firstTableResults.add("C");
        assertFalse(comparator.allTablesMatch());

    }


    @Test
    public void shouldBeAbleToLineUpMisMatchedTables() throws Exception {
        comparator.firstTableResults.add("A");
        comparator.firstTableResults.add("B");
        comparator.firstTableResults.add("C");
        comparator.firstTableResults.add("D");
        comparator.secondTableResults.add("X");
        comparator.secondTableResults.add("Y");
        comparator.secondTableResults.add("B");
        comparator.secondTableResults.add("Z");
        comparator.secondTableResults.add("D");
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(1, 2, 1.0));
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(3, 4, 1.0));
        comparator.lineUpTheTables();
        assertEquals("A", comparator.firstTableResults.get(0));
        assertEquals("<table><tr><td></td></tr></table>", comparator.firstTableResults.get(1));
        assertEquals("B", comparator.firstTableResults.get(2));
        assertEquals("D", comparator.firstTableResults.get(4));

        assertEquals("X", comparator.secondTableResults.get(0));
        assertEquals("D", comparator.secondTableResults.get(4));
    }

    @Test
    public void shouldBeAbleToLineUpMoreMisMatchedTables() throws Exception {
        comparator.firstTableResults.add("A");
        comparator.firstTableResults.add("B");
        comparator.firstTableResults.add("C");
        comparator.firstTableResults.add("D");
        comparator.secondTableResults.add("B");
        comparator.secondTableResults.add("X");
        comparator.secondTableResults.add("Y");
        comparator.secondTableResults.add("Z");
        comparator.secondTableResults.add("D");
        comparator.secondTableResults.add("shouldMatchWithBlank");
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(1, 0, 1.0));
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(3, 4, 1.0));
        comparator.lineUpTheTables();
        assertEquals("A", comparator.firstTableResults.get(0));
        assertEquals("B", comparator.firstTableResults.get(1));
        assertEquals("<table><tr><td></td></tr></table>", comparator.firstTableResults.get(3));
        assertEquals("<table><tr><td></td></tr></table>", comparator.firstTableResults.get(4));
        assertEquals("D", comparator.firstTableResults.get(5));
        assertEquals("<table><tr><td></td></tr></table>", comparator.firstTableResults.get(6));

        assertEquals("<table><tr><td></td></tr></table>", comparator.secondTableResults.get(0));
        assertEquals("B", comparator.secondTableResults.get(1));
        assertEquals("Y", comparator.secondTableResults.get(3));
        assertEquals("Z", comparator.secondTableResults.get(4));
        assertEquals("D", comparator.secondTableResults.get(5));
        assertEquals("shouldMatchWithBlank", comparator.secondTableResults.get(6));
    }

    @Test
    public void shouldGuarenteeThatBothResultFilesAreTheSameLength() throws Exception {
        comparator.firstTableResults.add("A");
        comparator.firstTableResults.add("B");
        comparator.firstTableResults.add("C");
        comparator.firstTableResults.add("D");
        comparator.secondTableResults.add("X");
        comparator.secondTableResults.add("Y");
        comparator.lineUpTheTables();
        assertEquals(comparator.firstTableResults.size(), comparator.secondTableResults.size());
        assertEquals("<table><tr><td></td></tr></table>", comparator.secondTableResults.get(2));
        assertEquals("<table><tr><td></td></tr></table>", comparator.secondTableResults.get(3));
    }

    @Test
    public void shouldAddBlankRowsForUnmatchedTables() throws Exception {
        comparator.firstTableResults.add("A");
        comparator.firstTableResults.add("B");
        comparator.firstTableResults.add("C");
        comparator.firstTableResults.add("D");
        comparator.secondTableResults.add("X");
        comparator.secondTableResults.add("B");
        comparator.secondTableResults.add("Y");
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(1, 1, 1.0));
        comparator.lineUpTheTables();
        comparator.addBlanksToUnmatchingRows();
        assertEquals(comparator.firstTableResults.size(), comparator.secondTableResults.size());
        assertEquals("A", comparator.firstTableResults.get(0));
        assertEquals("<table><tr><td></td></tr></table>", comparator.firstTableResults.get(1));
        assertEquals("B", comparator.firstTableResults.get(2));
        assertEquals("C", comparator.firstTableResults.get(3));
        assertEquals("<table><tr><td></td></tr></table>", comparator.firstTableResults.get(4));
        assertEquals("D", comparator.firstTableResults.get(5));

        assertEquals("<table><tr><td></td></tr></table>", comparator.secondTableResults.get(0));
        assertEquals("X", comparator.secondTableResults.get(1));
        assertEquals("B", comparator.secondTableResults.get(2));
        assertEquals("<table><tr><td></td></tr></table>", comparator.secondTableResults.get(3));
        assertEquals("Y", comparator.secondTableResults.get(4));
        assertEquals("<table><tr><td></td></tr></table>", comparator.secondTableResults.get(5));
    }

    @Test
    public void shouldHaveCorrectPassFailResults() throws Exception {
        comparator.firstTableResults.add("A");
        comparator.firstTableResults.add("B");
        comparator.firstTableResults.add("C");
        comparator.firstTableResults.add("D");
        comparator.secondTableResults.add("X");
        comparator.secondTableResults.add("B");
        comparator.secondTableResults.add("Y");
        comparator.secondTableResults.add("D");
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(1, 1, HistoryComparator.MAX_MATCH_SCORE));
        comparator.matchedTables.add(new HistoryComparator.MatchedPair(3, 3, HistoryComparator.MAX_MATCH_SCORE));
        comparator.lineUpTheTables();
        comparator.addBlanksToUnmatchingRows();
        comparator.makePassFailResultsFromMatches();
        assertEquals("fail", comparator.getResultContent().get(0));
        assertEquals("fail", comparator.getResultContent().get(1));
        assertEquals("pass", comparator.getResultContent().get(2));
        assertEquals("fail", comparator.getResultContent().get(3));
        assertEquals("fail", comparator.getResultContent().get(4));
        assertEquals("pass", comparator.getResultContent().get(5));

    }


    @Test
    public void compareShouldGetReportHtmlAndSetResultContentWithPassIfTheFilesWereTheSame() throws Exception {
        HistoryComparator comparator = new HistoryComparator();
        FileUtil.createFile("TestFolder/FirstFile", firstContent);
        FileUtil.createFile("TestFolder/SecondFile", firstContent);
        boolean worked = comparator.compare("TestFolder/FirstFile", "TestFolder/SecondFile");
        assertTrue(worked);
        String expectedResult = "pass";
        assertEquals(expectedResult, comparator.getResultContent().get(0));
        assertEquals(expectedResult, comparator.getResultContent().get(1));
    }

    @Test
    public void compareShouldGetReportFileHtmlAndSetResultContentWithFailIfTheFilesDiffer() throws Exception {
        HistoryComparator comparator = new HistoryComparator();
        FileUtil.createFile("TestFolder/FirstFile", firstContent);
        FileUtil.createFile("TestFolder/SecondFile", secondContent);
        boolean worked = comparator.compare("TestFolder/FirstFile", "TestFolder/SecondFile");
        assertTrue(worked);
        assertEquals("pass", comparator.getResultContent().get(0));
        assertEquals("fail", comparator.getResultContent().get(1));
    }

    public String generateHtmlFromWiki(String passOrFail) throws Exception {
        PageCrawler crawler = root.getPageCrawler();
        String pageText =
                "|myTable|\n" +
                        "La la\n" +
                        "|NewTable|\n" +
                        "|!style_" + passOrFail + "(a)|b|c|\n" +
                        "La la la";
        WikiPage myPage = crawler.addPage(root, PathParser.parse("MyPage"), pageText);
        PageData myData = myPage.getData();
        return myData.getHtml();
    }

    private String getContentWith(String passOrFail) throws Exception {
        TestExecutionReport report = new TestExecutionReport();
        TestExecutionReport.TestResult result = new TestExecutionReport.TestResult();
        result.right = "2";
        result.wrong = "0";
        result.ignores = "0";
        result.exceptions = "0";
        result.content = generateHtmlFromWiki(passOrFail);
        result.relativePageName = "testPageOne";
        report.results.add(result);
        Writer writer = new StringWriter();
        VelocityEngine engine = VelocityFactory.getVelocityEngine();
        report.toXml(writer, engine);
        return writer.toString();
    }

    @After
    public void tearDown() {
        FileUtil.deleteFileSystemDirectory("TestFolder");
    }
}