package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class NewlineTest extends WikiBaseTestCase {
    @Test
    public void translatesNewlines() {
        ParserTestHelper.assertTranslatesTo("hi\nmom", "hi" + ParserTestHelper.newLineRendered + "mom", injector);
    }
}
