package fitnesse.wikitext.test;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlElement;
import org.junit.Test;

public class PreformatTest extends FitnesseBaseTestCase {
    @Test
    public void scansPreformats() {
        ParserTestHelper.assertScansTokenType("{{{stuff}}}", "Preformat", true, injector);
    }

    @Test
    public void translatesPreformats() {
        ParserTestHelper.assertTranslatesTo("{{{stuff}}}", "<pre>stuff</pre>" + HtmlElement.endl, injector);
        ParserTestHelper.assertTranslatesTo("{{{''stuff''}}}", "<pre>''stuff''</pre>" + HtmlElement.endl, injector);
        ParserTestHelper.assertTranslatesTo("{{{<stuff>}}}", "<pre>&lt;stuff&gt;</pre>" + HtmlElement.endl, injector);
    }

    @Test
    public void translatesVariablesInPreformats() {
        ParserTestHelper.assertTranslatesTo("{{{s${x}f}}}", new TestVariableSource("x", "tuf"), "<pre>stuff</pre>" + HtmlElement.endl);
    }
}
