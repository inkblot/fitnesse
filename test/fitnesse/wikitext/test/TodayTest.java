package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;
import util.SystemTimeKeeper;
import util.TestTimeKeeper;

import java.util.GregorianCalendar;

public class TodayTest extends WikiBaseTestCase {
    @Test
    public void translatesTodays() {
        SystemTimeKeeper.instance = new TestTimeKeeper(new GregorianCalendar(2002, 2, 4, 15, 6, 7).getTime());
        ParserTestHelper.assertTranslatesTo("!today", "04 Mar, 2002", injector);
        ParserTestHelper.assertTranslatesTo("!today -t", "04 Mar, 2002 15:06", injector);
        ParserTestHelper.assertTranslatesTo("!today -xml", "2002-03-04T15:06:07", injector);
        ParserTestHelper.assertTranslatesTo("!today (MMM)", "Mar", injector);
        ParserTestHelper.assertTranslatesTo("!today (dd MMM)", "04 Mar", injector);
        ParserTestHelper.assertTranslatesTo("!today (dd MMM", "!today (dd MMM", injector);
    }

    @Test
    public void translatesWithDayIncrements() {
        SystemTimeKeeper.instance = new TestTimeKeeper(new GregorianCalendar(2002, 2, 4, 15, 6, 7).getTime());
        ParserTestHelper.assertTranslatesTo("!today +5", "09 Mar, 2002", injector);
        ParserTestHelper.assertTranslatesTo("!today -5", "27 Feb, 2002", injector);
        ParserTestHelper.assertTranslatesTo("!today -5.", "27 Feb, 2002.", injector);
    }

    @Test
    public void translatesWithDayIncrementsAndCustomFormat() {
        SystemTimeKeeper.instance = new TestTimeKeeper(new GregorianCalendar(2002, 2, 4, 15, 6, 7).getTime());
        ParserTestHelper.assertTranslatesTo("!today (ddMMM) +5", "09Mar", injector);
    }
}
