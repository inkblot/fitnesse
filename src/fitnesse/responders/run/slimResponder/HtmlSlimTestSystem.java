// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.responders.run.TestSystemListener;
import fitnesse.slimTables.HtmlTableScanner;
import fitnesse.slimTables.SlimTable;
import fitnesse.slimTables.Table;
import fitnesse.slimTables.TableScanner;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.Collapsible;
import fitnesse.wikitext.parser.Symbol;

import java.io.IOException;

public class HtmlSlimTestSystem extends SlimTestSystem {
    public HtmlSlimTestSystem(WikiPage page, TestSystemListener listener) {
        this(page, listener, new DefaultTestMode());
    }

    public HtmlSlimTestSystem(WikiPage page, TestSystemListener listener, SlimTestMode testMode) {
        super(page, listener, testMode);
    }

    protected TableScanner scanTheTables(PageData pageData) throws IOException {
        Symbol syntaxTree = pageData.getSyntaxTree();
        Symbol preparsedScenarioLibrary = getPreparsedScenarioLibrary();
        syntaxTree.addToFront(findCollapsibleSymbol(preparsedScenarioLibrary));
        String html = pageData.translateToHtml(syntaxTree);
        return new HtmlTableScanner(html);
    }

    private Symbol findCollapsibleSymbol(Symbol syntaxTree) {
        for (Symbol symbol : syntaxTree.getChildren()) {
            if (symbol.getType() instanceof Collapsible)
                return symbol;
        }
        // TODO: Choose a better exception type
        throw new RuntimeException("There must be a collapsible widget in here.");
    }

    @Override
    protected String createHtmlResults(SlimTable startWithTable, SlimTable stopBeforeTable) {
        replaceExceptionsWithLinks();
        evaluateTables();
        String exceptionsString = exceptions.toHtml();

        Table start = (startWithTable != null) ? startWithTable.getTable() : null;
        Table end = (stopBeforeTable != null) ? stopBeforeTable.getTable() : null;
        String testResultHtml = tableScanner.toHtml(start, end);
        return exceptionsString + testResultHtml;
    }
}
