// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wikitext.widgets.*;
import org.junit.Before;
import org.junit.Test;
import util.TimeMeasurement;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class WidgetBuilderTest extends FitnesseBaseTestCase {
    private WikiPage mockSource;

    @Before
    public void setUp() throws Exception {
        mockSource = new WikiPageDummy(injector);
    }

    @Test
    public void testEmptyPage() throws Exception {
        ParentWidget widget = new SimpleWidgetRoot(null, mockSource);
        assertNotNull(widget);
        assertEquals(0, widget.numberOfChildren());
    }

    @Test
    public void testSimpleText() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("Hello, World!", mockSource);
        assertNotNull(page);
        assertEquals(1, page.numberOfChildren());
        WikiWidget widget = page.nextChild();
        testWidgetClassAndText(widget, TextWidget.class, "Hello, World!");
    }

    @Test
    public void testSimpleWikiWord() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("WikiWord", mockSource);
        WikiWidget widget = page.nextChild();
        testWidgetClassAndText(widget, WikiWordWidget.class, "WikiWord");
    }

    @Test
    public void testTextThenWikiWord() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("text WikiWord more Text", mockSource);
        assertEquals(3, page.numberOfChildren());
        WikiWidget widget1 = page.nextChild();
        WikiWidget widget2 = page.nextChild();
        WikiWidget widget3 = page.nextChild();
        testWidgetClassAndText(widget1, TextWidget.class, "text ");
        testWidgetClassAndText(widget2, WikiWordWidget.class, "WikiWord");
        testWidgetClassAndText(widget3, TextWidget.class, " more Text");
    }

    @Test
    public void testWikiWord_Text_WikiWord() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("WikiWord more WordWiki", mockSource);
        assertEquals(3, page.numberOfChildren());
        WikiWidget widget1 = page.nextChild();
        WikiWidget widget2 = page.nextChild();
        WikiWidget widget3 = page.nextChild();
        testWidgetClassAndText(widget1, WikiWordWidget.class, "WikiWord");
        testWidgetClassAndText(widget2, TextWidget.class, " more ");
        testWidgetClassAndText(widget3, WikiWordWidget.class, "WordWiki");
    }

    @Test
    public void testItalic_text_WikiWord() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("''italic'' text WikiWord", mockSource);
        assertEquals(3, page.numberOfChildren());
        WikiWidget widget1 = page.nextChild();
        WikiWidget widget2 = page.nextChild();
        WikiWidget widget3 = page.nextChild();
        assertEquals(ItalicWidget.class, widget1.getClass());
        testWidgetClassAndText(widget2, TextWidget.class, " text ");
        testWidgetClassAndText(widget3, WikiWordWidget.class, "WikiWord");
    }

    @Test
    public void testWikiWordInsideItalic() throws Exception {
        testWikiWordInParentWidget("''WikiWord''", ItalicWidget.class, "WikiWord", 1);
    }

    @Test
    public void testWikiWordInsideBold() throws Exception {
        testWikiWordInParentWidget("'''WikiWord'''", BoldWidget.class, "WikiWord", 1);
    }

    @Test
    public void testMultiLineWidget() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("{{{\npreformatted\n}}}", mockSource);
        assertEquals(1, page.numberOfChildren());
        assertEquals(PreformattedWidget.class, page.nextChild().getClass());
    }

    @Test
    public void testEmailWidget() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("someone@somewhere.com", mockSource);
        assertEquals(1, page.numberOfChildren());
        assertEquals(EmailWidget.class, page.nextChild().getClass());
    }

    @Test
    public void testHrule() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("-----", mockSource);
        WikiWidget widget = page.nextChild();
        assertEquals(HruleWidget.class, widget.getClass());
    }

    @Test
    public void testAnchorsMarker() throws Exception {
        ParentWidget page = new SimpleWidgetRoot(".#name ", mockSource);
        WikiWidget widget = page.nextChild();
        assertEquals(AnchorMarkerWidget.class, widget.getClass());
    }

    @Test
    public void testAnchorsDeclaration() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("!anchor name ", mockSource);
        WikiWidget widget = page.nextChild();
        assertEquals(AnchorDeclarationWidget.class, widget.getClass());

    }

    @Test
    public void testWikiWordInsideHeader() throws Exception {
        testWikiWordInParentWidget("!1 WikiWord\n", HeaderWidget.class, "WikiWord", 1);
    }

    @Test
    public void testWikiWordInsideCenter() throws Exception {
        testWikiWordInParentWidget("!c WikiWord\n", CenterWidget.class, "WikiWord", 1);
    }

    @Test
    public void testTable() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("|a|b|\n|c|d|\n", mockSource);
        assertEquals(1, page.numberOfChildren());
        WikiWidget widget = page.nextChild();
        assertEquals(StandardTableWidget.class, widget.getClass());
    }

    @Test
    public void testList() throws Exception {
        ParentWidget page = new SimpleWidgetRoot(" *Item1\n", mockSource);
        assertEquals(1, page.numberOfChildren());
        assertEquals(ListWidget.class, page.nextChild().getClass());
    }

    @Test
    public void testClasspath() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("!path something", mockSource);
        assertEquals(1, page.numberOfChildren());
        assertEquals(ClasspathWidget.class, page.nextChild().getClass());
    }

    @Test
    public void testStrike() throws Exception {
        testWikiWordInParentWidget("--WikiWord--", StrikeWidget.class, "WikiWord", 1);
    }

    @Test
    public void testNoteWidget() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("!note something", mockSource);
        assertEquals(1, page.numberOfChildren());
        assertEquals(NoteWidget.class, page.nextChild().getClass());
    }

    @Test
    public void testCollapsableWidget() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("!* title\ncontent\n*!\n", mockSource);
        assertEquals(1, page.numberOfChildren());
        assertEquals(CollapsableWidget.class, page.nextChild().getClass());
    }

    @Test
    public void testNullPointerError() throws Exception {
        String wikiText = "''\nsome text that should be in italics\n''";
        ParentWidget root = new SimpleWidgetRoot(wikiText, new WikiPageDummy(injector));

        try {
            root.render();
        } catch (Exception e) {
            fail("should be no exception\n" + e);
        }
    }

    @Test
    public void testVirtualWikiWidget() throws Exception {
        ParentWidget page = new SimpleWidgetRoot("!virtualwiki http://localhost/FrontPage", mockSource);
        assertEquals(1, page.numberOfChildren());
        assertEquals(VirtualWikiWidget.class, page.nextChild().getClass());
    }

// TODO MdM A Test that reveals FitNesse's weakness for parsing large wiki documents.
//    @Test
    public void testLargeTable() throws Exception {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 1000; i++)
            buffer.append("|'''bold'''|''italic''|!c centered|\n");

        try {
            ParentWidget root = new SimpleWidgetRoot(buffer.toString(), new WikiPageDummy(injector));
            root.render();
        } catch (StackOverflowError e) {
            fail("Got error with big table: " + e);
        }
    }

    private void testWikiWordInParentWidget(String input, Class<?> expectedClass, String wikiWordText, int subChildren) throws Exception {
        ParentWidget page = new SimpleWidgetRoot(input, mockSource);
        assertEquals(1, page.numberOfChildren());
        WikiWidget widget = page.nextChild();
        assertEquals(expectedClass, widget.getClass());

        ParentWidget iWidget = (ParentWidget) widget;
        assertEquals(subChildren, iWidget.numberOfChildren());
        WikiWidget childWidget = iWidget.nextChild();
        testWidgetClassAndText(childWidget, WikiWordWidget.class, wikiWordText);
    }

    private void testWidgetClassAndText(WikiWidget widget, Class<?> expectedClass, String expectedText) {
        assertEquals(expectedClass, widget.getClass());
        if (widget instanceof TextWidget)
            assertEquals(expectedText, ((TextWidget) widget).getText());
    }

    @Test
    public void testConcurrentAddWidgets() throws Exception {
        WidgetBuilder widgetBuilder = new WidgetBuilder(new Class[]{BoldWidget.class});
        String text = "'''bold text'''";
        ParentWidget parent = new BoldWidget(new MockWidgetRoot(injector), "'''bold text'''");
        AtomicBoolean failFlag = new AtomicBoolean();
        failFlag.set(false);

        //This is our best attempt to get a race condition
        //by creating large number of threads.
        for (int i = 0; i < 1000; i++) {
            WidgetBuilderThread widgetBuilderThread = new WidgetBuilderThread(widgetBuilder, text, parent, failFlag);
            Thread thread = new Thread(widgetBuilderThread);
            try {
                thread.start();
            } catch (OutOfMemoryError e) {
                break;
            }
        }
        assertEquals(false, failFlag.get());
    }

    //Parsing Line Breaks used to be very slow because it was done with the LineBreakWidget
    //which used regular expressions.  Now we just have the text widget replace line ends
    // with <br/>.  This is much faster.  This test is here to make sure it stays fast.
    @Test
    public void testParsingManyLineBreaksIsFast() throws Exception {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 100; i++)
            b.append("****************************************************************\n");
        for (int i = 0; i < 100; i++)
            b.append("****************************************************************\n");
        for (int i = 0; i < 100; i++)
            b.append("****************************************************************\n");

        TimeMeasurement measurement = new TimeMeasurement().start();
        ParentWidget root = new SimpleWidgetRoot(b.toString(), mockSource);
        String html = root.childHtml();
        long duration = measurement.elapsed();

        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < 300; i++)
            expected.append("****************************************************************<br/>");

        assertEquals(expected.toString(), html);
        assertTrue(String.format("parsing took %s ms.", duration), duration < 500);
    }

    class WidgetBuilderThread implements Runnable {
        WidgetBuilder widjetBuilder = null;
        String text = null;
        ParentWidget parent = null;
        AtomicBoolean failFlag = null;

        public WidgetBuilderThread(WidgetBuilder widjetBuilder, String text, ParentWidget parent, AtomicBoolean failFlag) {
            this.widjetBuilder = widjetBuilder;
            this.text = text;
            this.parent = parent;
            this.failFlag = failFlag;
        }

        public void run() {
            try {
                this.widjetBuilder.addChildWidgets(this.text, this.parent);
            } catch (Exception e) {
                this.failFlag.set(true);
            }
        }
    }
}

