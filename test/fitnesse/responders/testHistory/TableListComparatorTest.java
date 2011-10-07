package fitnesse.responders.testHistory;

import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.Table;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TableListComparatorTest {
    private TableListComparator comparator;

    @Before
    public void setUp() throws Exception {
        HtmlTableScanner leftHandScanner = new HtmlTableScanner("<table>empty</table>");
        HtmlTableScanner rightHandScanner = new HtmlTableScanner("<table>empty</table>");
        comparator = new TableListComparator(leftHandScanner, rightHandScanner);
    }


    @Test
    public void shouldOnlyUseTheBestMatchForTheFirstTable() throws Exception {
        comparator.saveMatch(1, 1, 1.0);
        comparator.saveMatch(1, 2, 1.1);
        comparator.sortMatchesByScore();
        comparator.saveOnlyTheBestMatches();
        assertEquals(1.1, comparator.tableMatches.get(0).matchScore, .01);
    }

    @Test
    public void shouldOnlyReplaceAMatchIfThereIsNoBetterMatchForEitherTable() throws Exception {
        comparator.saveMatch(1, 1, 1.0);
        comparator.saveMatch(3, 2, 1.2);
        comparator.saveMatch(1, 2, 1.1);
        comparator.sortMatchesByScore();
        comparator.saveOnlyTheBestMatches();
        assertEquals(1.2, comparator.tableMatches.get(0).matchScore, .001);
        assertEquals(1.0, comparator.tableMatches.get(1).matchScore, .001);
        assertEquals(2, comparator.tableMatches.size());
    }

    @Test
    public void shouldRemoveOldMatchesIfBetterOnesAreFound() throws Exception {
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(1, 1, 1.0));
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(3, 2, 1.0));
        comparator.saveMatch(1, 2, 1.1);
        comparator.sortMatchesByScore();
        comparator.saveOnlyTheBestMatches();
        assertEquals(1.1, comparator.tableMatches.get(0).matchScore, .001);
        assertEquals(1, comparator.tableMatches.size());
    }

    @Test
    public void shouldReplaceOldMatchForSecondTableEvenIfThereIsNoMatchForFirstTable() throws Exception {
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(3, 2, 1.0));
        comparator.saveMatch(1, 2, 1.1);
        comparator.sortMatchesByScore();
        comparator.saveOnlyTheBestMatches();
        assertEquals(1.1, comparator.tableMatches.get(0).matchScore, .001);
        assertEquals(1, comparator.tableMatches.size());
    }

    @Test
    public void shouldGetAScoreBackFromCompareTables() throws Exception {
        String table1text = "<table><tr><td>x</td></tr></table>";
        Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
        String table2text = "<table><tr><td>x</td></tr></table>";
        Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
        double score = comparator.compareTables(table1, table2);
        assertEquals(HistoryComparator.MAX_MATCH_SCORE, score, .01);
    }

    @Test
    public void shouldCompareTwoSimpleEqualTables() throws Exception {
        String table1text = "<table><tr><td>x</td></tr></table>";
        Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
        String table2text = "<table><tr><td>x</td></tr></table>";
        Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
        assertTrue(comparator.theTablesMatch(comparator.compareTables(table1, table2)));
    }

    @Test
    public void shouldCompareTwoSimpleUnequalTables() throws Exception {
        String table1text = "<table><tr><td>x</td></tr></table>";
        Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
        String table2text = "<table><tr><td>y</td></tr></table>";
        Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
        assertFalse(comparator.theTablesMatch(comparator.compareTables(table1, table2)));
    }

    @Test
    public void shouldCompareTwoDifferentlySizedTables() throws Exception {
        String table1text = "<table><tr><td>x</td></tr></table>";
        Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
        String table2text = "<table><tr><td>x</td><td>y</td></tr></table>";
        Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
        assertFalse(comparator.theTablesMatch(comparator.compareTables(table1, table2)));
    }

    @Test
    public void shouldIgnoreCollapsedTables() throws Exception {
        String table1text = "<table><tr><td>has collapsed table</td><td><div class=\"collapse_rim\"> <tr><td>bleh1</td></tr></div></td></tr></table>";
        String table2text = "<table><tr><td>has collapsed table</td><td><div class=\"collapse_rim\"> <tr><td>HAHA</td></tr></div></td></tr></table>";
        Table table1 = (new HtmlTableScanner(table1text)).getTable(0);
        Table table2 = (new HtmlTableScanner(table2text)).getTable(0);
        double score = comparator.compareTables(table1, table2);
        assertEquals(HistoryComparator.MAX_MATCH_SCORE, score, .01);
        assertTrue(comparator.theTablesMatch(score));
    }

    @Test
    public void shouldCheckTheMatchScoreToSeeIfTablesMatch() throws Exception {
        double score = 1.0;
        assertTrue(comparator.theTablesMatch(score));
        score = .79;
        assertFalse(comparator.theTablesMatch(score));
        score = 1.1;
        assertTrue(comparator.theTablesMatch(score));
    }

    @Test
    public void shouldKeepTheBestScoreForATableEvenIfItIsHasABetterMatchItCantKeep() throws Exception {
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(6, 6, 1.0));
        comparator.saveMatch(6, 7, 1.1);
        comparator.saveMatch(7, 7, 1.2);
        comparator.sortMatchesByScore();
        comparator.saveOnlyTheBestMatches();
        assertEquals(1.2, comparator.tableMatches.get(0).matchScore, .001);
        assertEquals(1.0, comparator.tableMatches.get(1).matchScore, .001);
        assertEquals(2, comparator.tableMatches.size());
    }

    @Test
    public void shouldBeAbleToOrderTheMatchesHighestToLowest() throws Exception {
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(6, 6, 1.0));
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(5, 5, .9));
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(4, 4, 1.1));
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(7, 7, 1.05));
        comparator.sortMatchesByScore();
        assertEquals(1.1, comparator.tableMatches.get(0).matchScore, .001);
        assertEquals(1.05, comparator.tableMatches.get(1).matchScore, .001);
        assertEquals(1.0, comparator.tableMatches.get(2).matchScore, .001);
        assertEquals(.9, comparator.tableMatches.get(3).matchScore, .001);
    }

    @Test
    public void shouldBeAbleToOrderTheMatchesByTableIndex() throws Exception {
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(6, 6, 1.0));
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(5, 5, .9));
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(4, 4, 1.1));
        comparator.tableMatches.add(new HistoryComparator.MatchedPair(7, 7, 1.05));
        comparator.sortMatchesByTableIndex();
        assertEquals(4, comparator.tableMatches.get(0).first);

    }

}
