package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class TextTest extends WikiBaseTestCase {
    @Test
    public void scansTextAsWords() {
        ParserTestHelper.assertScans("hi mom", "Text=hi,Whitespace= ,Text=mom", injector);
    }

    @Test
    public void translatesText() {
        ParserTestHelper.assertTranslatesTo("hi mom", "hi mom", injector);
        ParserTestHelper.assertTranslatesTo("Hi MOM", "Hi MOM", injector);
        ParserTestHelper.assertTranslatesTo("Hi+Mom", "Hi+Mom", injector);
        ParserTestHelper.assertTranslatesTo("A", "A", injector);
        ParserTestHelper.assertTranslatesTo("Aa", "Aa", injector);
        ParserTestHelper.assertTranslatesTo(".", ".", injector);
        ParserTestHelper.assertTranslatesTo("<hi>", "&lt;hi&gt;", injector);
        ParserTestHelper.assertTranslatesTo("text &bar; &bang; &dollar;", "text | ! $", injector);
        ParserTestHelper.assertTranslatesTo("HiMOM02", "HiMOM02", injector);
    }
}
