package fitnesse.wikitext.test;

import fitnesse.FitnesseBaseTestCase;
import org.junit.Test;

public class NewlineTest extends FitnesseBaseTestCase {
    @Test
    public void translatesNewlines() {
        ParserTestHelper.assertTranslatesTo("hi\nmom", "hi" + ParserTestHelper.newLineRendered + "mom", injector);
    }
}
