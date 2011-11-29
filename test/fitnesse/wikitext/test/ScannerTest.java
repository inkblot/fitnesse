package fitnesse.wikitext.test;

import com.google.inject.Inject;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wikitext.parser.Scanner;
import org.junit.Test;

public class ScannerTest extends FitnesseBaseTestCase {

    private TestSourcePage sourcePage;

    @Inject
    public void inject(TestSourcePage sourcePage) {
        this.sourcePage = sourcePage;
    }

    @Test
    public void copyRestoresState() {
        Scanner scanner = new Scanner(sourcePage, "stuff");
        Scanner backup = new Scanner(scanner);
        ParserTestHelper.assertScans("Text=stuff", scanner);
        ParserTestHelper.assertScans("", scanner);
        scanner.copy(backup);
        ParserTestHelper.assertScans("Text=stuff", scanner);
    }
}
