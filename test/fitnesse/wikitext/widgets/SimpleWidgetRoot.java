package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 11/27/11
 * Time: 1:44 PM
 */
public class SimpleWidgetRoot extends WidgetRoot {
    public SimpleWidgetRoot(WikiPage page) throws IOException {
        this("", page);
    }

    public SimpleWidgetRoot(String value, WikiPage page) throws IOException {
        super(value, page, WidgetBuilder.htmlWidgetBuilder);
    }
}
