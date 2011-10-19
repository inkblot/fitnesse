// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNeseModule;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//created by Clare McLennan

public class HelpWidgetTest extends WidgetTestCase {
    private WikiPage page;
    private WikiPage pageNoHelp;
    private WikiPage root;

    @Inject
    public void inject(@Named(FitNeseModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"), "some text");
        pageNoHelp = root.getPageCrawler().addPage(root, PathParser.parse("NoHelp"), "some text too");
        PageData data = page.getData();
        data.setAttribute(PageData.PropertyHELP, "some page is about some text");
        page.commit(data);
    }

    @Test
    public void testRegularExpression() throws Exception {
        assertTrue(Pattern.matches(HelpWidget.REGEXP, "!help"));
        assertTrue(Pattern.matches(HelpWidget.REGEXP, "!help -editable"));
    }

    @Test
    public void testResultsWithHelp() throws Exception {
        setUp();
        HelpWidget widget = new HelpWidget(new WidgetRoot(page), "!help");
        assertEquals("some page is about some text", widget.render());

        HelpWidget editableWidget = new HelpWidget(new WidgetRoot(page), "!help -editable");
        assertEquals("some page is about some text " +
                "<a href=\"SomePage?properties\">(edit)</a>", editableWidget.render());
    }

    @Test
    public void testResultsWithoutHelp() throws Exception {
        setUp();
        HelpWidget widget = new HelpWidget(new WidgetRoot(pageNoHelp), "!help");
        assertEquals("", widget.render());

        HelpWidget editableWidget = new HelpWidget(new WidgetRoot(pageNoHelp), "!help -editable");
        assertEquals(" <a href=\"NoHelp?properties\">(edit help text)</a>", editableWidget.render());
    }


    protected String getRegexp() {
        return HelpWidget.REGEXP;
    }

}
