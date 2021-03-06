// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import fitnesse.wiki.PageData;
import fitnesse.wiki.TraversalListener;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.*;

import java.io.IOException;

public abstract class ReferenceRenamer implements TraversalListener, SymbolTreeWalker {
    protected WikiPage root;
    protected WikiPage currentPage;

    public ReferenceRenamer(WikiPage root) {
        this.root = root;
    }

    public void renameReferences() throws IOException {
        root.getPageCrawler().traverse(root, this);
    }

    public void processPage(WikiPage currentPage) throws IOException {
        PageData data = currentPage.getData();
        String content = data.getContent();

        WikiSourcePage sourcePage = new WikiSourcePage(currentPage);
        Symbol syntaxTree = Parser.make(
                new ParsingPage(sourcePage),
                content,
                SymbolProvider.refactoringProvider)
                .parse();
        this.currentPage = currentPage;
        syntaxTree.walkPreOrder(this);
        String newContent = new WikiTranslator(sourcePage).translateTree(syntaxTree);

        boolean pageHasChanged = !newContent.equals(content);
        if (pageHasChanged) {
            data.setContent(newContent);
            currentPage.commit(data);
        }
    }
}
