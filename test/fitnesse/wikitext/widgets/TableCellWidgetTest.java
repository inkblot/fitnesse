// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;
import junit.framework.TestCase;

import static util.RegexAssertions.assertSubString;

public class TableCellWidgetTest extends TestCase {
    public TableRowWidget row;
    private StandardTableWidget table;

    public void setUp() throws Exception {
        table = new StandardTableWidget(new MockWidgetRoot(), "");
        row = new TableRowWidget(table, "", false);
    }

    public void testSimpleCell() throws Exception {
        TableCellWidget cell = new TableCellWidget(row, "a", false);
        assertEquals(1, cell.numberOfChildren());
        WikiWidget child = cell.nextChild();
        assertEquals(TextWidget.class, child.getClass());
        assertEquals("a", ((TextWidget) child).getText());
    }

    public void testTrimsWhiteSpace() throws Exception {
        TableCellWidget cell = new TableCellWidget(row, " 1 item ", false);
        assertEquals(1, cell.numberOfChildren());
        WikiWidget child = cell.nextChild();
        assertEquals(TextWidget.class, child.getClass());
        assertEquals("1 item", ((TextWidget) child).getText());
    }

    public void testLiteralCell() throws Exception {
        TableCellWidget cell = new TableCellWidget(row, "''italic'' '''bold''", true);
        assertEquals(1, cell.numberOfChildren());
        assertSubString("''italic'' '''bold''", cell.render());
    }

    public void testLiteralInLiteralCell() throws Exception {
        ParentWidget root = new MockWidgetRoot();
        root.defineLiteral("blah");
        table = new StandardTableWidget(root, "");
        row = new TableRowWidget(table, "", true);
        //Paren Literal: () -> ??
        TableCellWidget cell = new TableCellWidget(row, "''!lit?0?''", true);
        assertSubString("''blah''", cell.render());
    }

    public void testVariableInLiteralCell() throws Exception {
        ParentWidget root = new MockWidgetRoot();
        root.addVariable("X", "abc");
        table = new StandardTableWidget(root, "");
        row = new TableRowWidget(table, "", true);
        TableCellWidget cell = new TableCellWidget(row, "''${X}''", true);
        assertSubString("''abc''", cell.render());
    }
}
