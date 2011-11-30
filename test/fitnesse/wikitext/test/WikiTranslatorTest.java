package fitnesse.wikitext.test;

import com.google.inject.Inject;
import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WikiTranslatorTest extends WikiBaseTestCase {

    private TestSourcePage sourcePage;

    @Inject
    public void inject(TestSourcePage sourcePage) {
        this.sourcePage = sourcePage;
    }

    @Test
    public void DefineVariableIsPreserved() throws Exception {
        String content = "${something} or other";
        String newContent = ParserTestHelper.roundTrip(sourcePage, content);
        assertEquals(content, newContent);
    }

    @Test
    public void DefineVariableInLinkIsPreserved() throws Exception {
        String content = "http://localhost/${somepath}/something";
        String newContent = ParserTestHelper.roundTrip(sourcePage, content);
        assertEquals(content, newContent);
    }

    @Test
    public void DefineVariableInAliasIsPreserved() throws Exception {
        String content = "[[${a}][${b}]]";
        String newContent = ParserTestHelper.roundTrip(sourcePage, content);
        assertEquals(content, newContent);
    }
}
