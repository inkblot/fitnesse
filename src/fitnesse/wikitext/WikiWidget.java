// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.WidgetRoot;
import util.GracefulNamer;

import java.io.IOException;

public abstract class WikiWidget {
    private final ParentWidget parent;

    protected WikiWidget(ParentWidget parent) {
        this.parent = parent;
        addToParent();
    }

    public ParentWidget getParent() {
        return parent;
    }

    protected void addToParent() {
        if (getParent() != null)
            getParent().addChild(this);
    }

    public abstract String render() throws IOException;

    public WikiPage getWikiPage() {
        return getParent().getWikiPage();
    }

    public String asWikiText() {
        return getClass().toString() + ".asWikiText()";
    }

    public boolean isRegracing() {
        return false;
    }

    public String regrace(String disgracefulName) {
        String newName = disgracefulName;
        //todo don't use the GracefulNamer for this.  It's only for java instance and variable names.  Write a different tool.
        if (isRegracing()) newName = GracefulNamer.regrace(disgracefulName);
        return newName;
    }

    //!include: Expose the root widget via the parent
    public WidgetRoot getRoot() {
        return getParent().getRoot();
    }

    public VariableSource getVariableSource() {
        return getRoot().getVariableSource();
    }
}

