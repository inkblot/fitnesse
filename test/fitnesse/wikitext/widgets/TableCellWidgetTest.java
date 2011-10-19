// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.wikitext.WikiWidget;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertSubString;

public class TableCellWidgetTest extends FitnesseBaseTestCase {
    public TableRowWidget row;
    private StandardTableWidget table;

    @Before
    public void setUp() throws Exception {
        table = new StandardTableWidget(new MockWidgetRoot(injector), "");
        row = new TableRowWidget(table, "", false);
    }

    @Test
    public void testSimpleCell() throws Exception {
        TableCellWidget cell = new TableCellWidget(row, "a", false);
        assertEquals(1, cell.numberOfChildren());
        WikiWidget child = cell.nextChild();
        assertEquals(TextWidget.class, child.getClass());
        assertEquals("a", ((TextWidget) child).getText());
    }

    @Test
    public void testTrimsWhiteSpace() throws Exception {
        TableCellWidget cell = new TableCellWidget(row, " 1 item ", false);
        assertEquals(1, cell.numberOfChildren());
        WikiWidget child = cell.nextChild();
        assertEquals(TextWidget.class, child.getClass());
        assertEquals("1 item", ((TextWidget) child).getText());
    }

    @Test
    public void testLiteralCell() throws Exception {
        TableCellWidget cell = new TableCellWidget(row, "''italic'' '''bold''", true);
        assertEquals(1, cell.numberOfChildren());
        assertSubString("''italic'' '''bold''", cell.render());
    }

    @Test
    public void testLiteralInLiteralCell() throws Exception {
        ParentWidget root = new MockWidgetRoot(injector);
        root.defineLiteral("blah");
        table = new StandardTableWidget(root, "");
        row = new TableRowWidget(table, "", true);
        //Paren Literal: () -> ??
        TableCellWidget cell = new TableCellWidget(row, "''!lit?0?''", true);
        assertSubString("''blah''", cell.render());
    }

    @Test
    public void testVariableInLiteralCell() throws Exception {
        ParentWidget root = new MockWidgetRoot(injector);
        root.addVariable("X", "abc");
        table = new StandardTableWidget(root, "");
        row = new TableRowWidget(table, "", true);
        TableCellWidget cell = new TableCellWidget(row, "''${X}''", true);
        assertSubString("''abc''", cell.render());
    }
}
