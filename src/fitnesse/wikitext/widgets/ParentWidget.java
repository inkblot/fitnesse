// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WidgetBuilder;
import fitnesse.wikitext.WikiWidget;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ParentWidget extends WikiWidget {
    private final List<WikiWidget> children;
    private int currentChild;

    public ParentWidget(ParentWidget parent) {
        this(parent, new LinkedList<WikiWidget>(), 0);
    }

    protected ParentWidget(ParentWidget parent, List<WikiWidget> children, int currentChild) {
        super(parent);
        this.children = children;
        this.currentChild = currentChild;
    }

    public void reset() {
        children.clear();
        currentChild = 0;
    }

    public void addChild(WikiWidget widget) {
        children.add(widget);
    }

    public int numberOfChildren() {
        return children.size();
    }

    public List<WikiWidget> getChildren() {
        return children;
    }

    public int getCurrentChild() {
        return currentChild;
    }

    public WikiWidget nextChild() {
        if (hasNextChild())
            return children.get(currentChild++);
        else
            throw new ArrayIndexOutOfBoundsException("No next child exists");
    }

    public boolean hasNextChild() {
        return (currentChild < numberOfChildren());
    }

    public String childHtml() throws IOException {
        currentChild = 0;
        StringBuilder html = new StringBuilder();
        while (hasNextChild()) {
            WikiWidget child = nextChild();
            html.append(child.render());
        }

        return html.toString();
    }

    public String childWikiText() {
        currentChild = 0;
        StringBuilder wikiText = new StringBuilder();
        while (hasNextChild()) {
            WikiWidget child = nextChild();
            wikiText.append(child.asWikiText());
        }

        return wikiText.toString();
    }

    public void addChildWidgets(String value) {
        getBuilder().addChildWidgets(value, this);
    }

    public WidgetBuilder getBuilder() {
        return getParent().getBuilder();
    }

    public boolean doEscaping() {
        return getParent().doEscaping();
    }

    public String processLiterals(String value) throws IOException {
        return new LiteralProcessingWidgetRoot(this, value).childHtml();
    }

    @SuppressWarnings("unchecked")
    public static WidgetBuilder preprocessingLiteralWidgetBuilder = new WidgetBuilder(
            new Class[]{PreProcessorLiteralWidget.class}
    );

    protected String expandVariables(String content) throws IOException {
        return (new VariableExpandingWidgetRoot(this, content)).childHtml();
    }

    protected String stripTrailingWhiteSpaceInLines(String value) {
        return Pattern.compile("[ \\t]+(\n)").matcher(value).replaceAll("$1");
    }

    public static class LiteralProcessingWidgetRoot extends ParentWidget {
        public LiteralProcessingWidgetRoot(ParentWidget parent, String content) {
            super(parent);
            if (content != null)
                addChildWidgets(content);
        }

        public String childHtml() throws IOException {
            StringBuilder html = new StringBuilder();
            while (hasNextChild()) {
                WikiWidget child = nextChild();
                //TODO  Checking for TextWidget here is a nightmare.
                if (child.getClass() == TextWidget.class) {
                    TextWidget tw = (TextWidget) child;
                    html.append(tw.getRawText());
                } else
                    html.append(child.render());
            }

            return html.toString();
        }

        public WidgetBuilder getBuilder() {
            return preprocessingLiteralWidgetBuilder;
        }

        public boolean doEscaping() {
            return false;
        }

        public String render() {
            return "";
        }

        protected void addToParent() {
        }
    }
}

