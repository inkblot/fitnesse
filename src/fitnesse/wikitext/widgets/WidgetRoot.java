// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import com.google.inject.Key;
import com.google.inject.name.Names;
import fitnesse.FitNesseModule;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.WidgetBuilder;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.parser.VariableSource;
import util.Maybe;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

public class WidgetRoot extends ParentWidget {

    private static final String RUNNING_PAGE_NAME = "RUNNING_PAGE_NAME";
    private static final String RUNNING_PAGE_PATH = "RUNNING_PAGE_PATH";

    private static final Map<String, String> includingPagePropertyMap = new HashMap<String, String>();

    static {
        includingPagePropertyMap.put(RUNNING_PAGE_NAME, "PAGE_NAME");
        includingPagePropertyMap.put(RUNNING_PAGE_PATH, "PAGE_PATH");
    }

    private final Map<String, String> variables;
    private final WidgetBuilder builder;
    private final WikiPage page;
    private final boolean doEscaping;
    private final List<String> literals;
    private final boolean ignoringText;
    private final WidgetRoot includingPage;

    public WidgetRoot(WikiPage page) throws IOException {
        this("", page);
    }

    public WidgetRoot(String value, WikiPage page) throws IOException {
        this(value, page, WidgetBuilder.htmlWidgetBuilder);
    }

    public WidgetRoot(String value, WikiPage page, WidgetBuilder builder) throws IOException {
        this(value, page, builder, new HashMap<String, String>(), false, true, new LinkedList<String>(), null, new LinkedList<WikiWidget>(), 0);
    }

    //Constructor for IncludeWidget support (alias locale & scope)
    public WidgetRoot(WikiPage aliasPage, ParentWidget impostorWidget) throws IOException {
        this(null, aliasPage, impostorWidget.getBuilder(), impostorWidget.getRoot().variables, impostorWidget.getRoot().isIgnoringText(),
                impostorWidget.getRoot().doEscaping, impostorWidget.getRoot().literals, impostorWidget.getRoot(), impostorWidget.getChildren(),
                impostorWidget.getCurrentChild());
    }

    private WidgetRoot(String value, WikiPage page, WidgetBuilder builder, Map<String, String> variables, boolean ignoringText,
                       boolean escaping, List<String> literals, WidgetRoot includingPage, List<WikiWidget> children, int currentChild) throws IOException {
        super(null, children, currentChild);
        this.variables = variables;
        this.page = page;
        this.builder = builder;
        this.ignoringText = ignoringText;
        this.doEscaping = escaping;
        this.literals = literals;
        this.includingPage = includingPage;
        if (value != null)
            buildWidgets(value);
    }

    public WidgetRoot getRoot() {
        return this;
    }

    public boolean isIgnoringText() {
        return ignoringText;
    }

    public WidgetBuilder getBuilder() {
        return builder;
    }

    protected void buildWidgets(String value) throws IOException {
        String strippedText = stripTrailingWhiteSpaceInLines(value);
        String nonLiteralContent = processLiterals(strippedText);
        addChildWidgets(nonLiteralContent);
    }

    public String render() throws IOException {
        return childHtml();
    }

    @Override
    public VariableSource getVariableSource() {
        return new VariableSource() {
            @Override
            public Maybe<String> findVariable(String name) {
                return WidgetRoot.this.findVariable(name);
            }
        };
    }

    public Maybe<String> findVariable(String name) {
        try {
            String value = getVariable(name);
            return value == null ? Maybe.noString : new Maybe<String>(value);
        } catch (IOException e) {
            return Maybe.noString;
        }
    }

    private String getVariable(String key) throws IOException {
        String value = getValueOfVariableFromAllPossibleSources(key);
        if (value != null) {
            value = replaceAllKnownVariables(value);

            value = value.replaceAll(VariableWidget.prefixDisabled, VariableWidget.prefix);
            variables.put(key, value);
        }
        return value;
    }

    // TODO: NR: is this interpolation?
    private String replaceAllKnownVariables(String value) throws IOException {
        int pos = 0;
        while ((pos = includesVariableAt(value, pos)) != -1) {
            value = replaceVariable(value, pos);
            pos++;
        }
        return value;
    }

    private String getValueOfVariableFromAllPossibleSources(String key) throws IOException {
        String value = getSpecialVariableValue(key);
        if (value == null)
            value = variables.get(key);
        if (value == null)
            value = getVariableFromIncludingPage(key);
        if (value == null)
            value = getVariableFromParentPages(key);
        if (value == null)
            value = System.getenv(key);
        if (value == null)
            value = System.getProperty(key);
        return value;
    }

    private String getVariableFromParentPages(String key) throws IOException {
        String value = null;
        WikiPage page = getWikiPage();
        while (value == null && !page.getPageCrawler().isRoot(page)) {
            WikiPage parent = page.getParentForVariables(); // follow parents for variables
            if (parent == page) break;
            page = parent;
            // Gain access to page data to set parent's literal list
            PageData pageData = page.getData();
            value = pageData.getVariable(key);
        }
        return value;
    }

    private String getSpecialVariableValue(String key) {
        String value = null;
        if (key.equals("PAGE_NAME"))
            value = page.getName();
        else if (key.equals("PAGE_PATH"))
            value = getWikiPage().getPageCrawler().getFullPath(page).parentPath().toString();
        else if (key.equals("FITNESSE_PORT"))
            value = page.getInjector().getInstance(Key.get(Integer.class, Names.named(FitNesseModule.PORT))).toString();
        else if (key.equals("FITNESSE_ROOTPATH"))
            value = page.getInjector().getInstance(Key.get(String.class, Names.named(WikiPageFactory.ROOT_PATH)));
        return value;
    }

    private String getVariableFromIncludingPage(String key) throws IOException {
        String value = null;
        if (includingPagePropertyMap.containsKey(key)) {
            String newKey = includingPagePropertyMap.get(key);
            if (includingPage == null) {
                value = getVariable(newKey);
            } else {
                value = includingPage.getVariable(key);
                if (value == null) {
                    value = includingPage.getVariable(newKey);
                }
            }
        }
        return value;
    }

    private int includesVariableAt(String string, int pos) {
        Matcher matcher = VariableWidget.pattern.matcher(string);
        if (matcher.find(pos))
            return matcher.start();
        else
            return -1;
    }

    //
    // If it has a variable, get it and replace newlines with literals.
    // If the result is a table, then ignore the replacement and leave
    // the variable reference unexpanded.
    //
    // Nested tables cannot be expanded in place due to ambiguities, and
    // newlines internal to table cells wreak havoc on table recognition.
    //
    private String replaceVariable(String string, int pos) throws IOException {
        Matcher matcher = VariableWidget.pattern.matcher(string);
        if (matcher.find(pos)) {
            String name = matcher.group(1);
            String variableText = getVariable(name);
            if (variableText == null) {
                return string;
            }
            String replacedValue = variableText.replaceAll("(^|[^|])\n", "$1" + PreProcessorLiteralWidget.literalNewline);
            String value = processLiterals(replacedValue);
            Matcher tblMatcher = StandardTableWidget.pattern.matcher(value);
            if (tblMatcher.find()) value = "!{" + name + "}";
            return string.substring(0, matcher.start()) + value + string.substring(matcher.end());
        }
        return string;
    }

    public void addVariable(String key, String value) {
        variables.put(key, value);
    }

    public int defineLiteral(String literal) {
        int literalNumber = literals.size();
        literals.add(literal);
        return literalNumber;
    }

    public String getLiteral(int literalNumber) {
        if (literalNumber >= literals.size())
            return "literal(" + literalNumber + ") not found.";
        return literals.get(literalNumber);
    }

    public WikiPage getWikiPage() {
        return page;
    }

    public boolean doEscaping() {
        return doEscaping;
    }

    public List<String> getLiterals() {
        return literals;
    }

    public String asWikiText() {
        return childWikiText();
    }
}

