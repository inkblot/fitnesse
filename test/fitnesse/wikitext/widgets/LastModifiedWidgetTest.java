// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

//created by Jason Sypher

public class LastModifiedWidgetTest extends WidgetTestCase {
    private WikiPage page;
    private LastModifiedWidget widget;

    @Before
    public void setUp() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"), "some text");
        widget = new LastModifiedWidget(new WidgetRoot(page), "!lastmodified");
    }

    @Test
    public void testRegularExpression() throws Exception {
        assertMatchEquals("!lastmodified", "!lastmodified");
    }

    @Test
    public void testResults() throws Exception {
        setUp();
        Date date = page.getData().getProperties().getLastModificationTime();
        String formattedDate = LastModifiedWidget.formatDate(date);
        assertHasRegexp(formattedDate, widget.render());
    }

    @Test
    public void testDateFormat() throws Exception {
        Locale.setDefault(Locale.US);
        GregorianCalendar date = new GregorianCalendar(2003, 3, 1, 11, 41, 30);
        String formattedDate = LastModifiedWidget.formatDate(date.getTime());
        assertEquals("Apr 01, 2003 at 11:41:30 AM", formattedDate);
    }

    @Test
    public void testDefaultUsername() throws Exception {
        assertSubString("Last modified anonymously", widget.render());
    }

    @Test
    public void testUsername() throws Exception {
        PageData data = page.getData();
        data.setAttribute(PageData.LAST_MODIFYING_USER, "Aladdin");
        page.commit(data);

        assertSubString("Last modified by Aladdin", widget.render());
    }

    protected String getRegexp() {
        return LastModifiedWidget.REGEXP;
    }

}
