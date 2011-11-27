package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPage;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 11/27/11
 * Time: 2:06 PM
 */
public class AliasingWidgetRoot extends WidgetRoot {
    public AliasingWidgetRoot(WikiPage aliasPage, ParentWidget impostorWidget) throws IOException {
        super(aliasPage, impostorWidget);
    }
}
