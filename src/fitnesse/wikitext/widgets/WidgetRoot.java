// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import com.google.inject.Key;
import com.google.inject.name.Names;
import fitnesse.FitNesseModule;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PagePointer;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.WidgetBuilder;
import fitnesse.wikitext.parser.VariableSource;
import util.Maybe;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class WidgetRoot extends ParentWidget implements VariableSource {
    private Map<String, String> variables = new HashMap<String, String>();
    private WidgetBuilder builder;
    private final WikiPage page;
    private boolean doEscaping = true;
    private List<String> literals = new LinkedList<String>();
    private boolean isGatheringInfo = false;
    private WidgetRoot includingPage;
    private static final Map<String, String> includingPagePropertyMap = new HashMap<String, String>();

    private static final String RUNNING_PAGE_NAME = "RUNNING_PAGE_NAME";
    private static final String RUNNING_PAGE_PATH = "RUNNING_PAGE_PATH";

    static {
        includingPagePropertyMap.put(RUNNING_PAGE_NAME, "PAGE_NAME");
        includingPagePropertyMap.put(RUNNING_PAGE_PATH, "PAGE_PATH");
    }

    //Constructor for IncludeWidget support (alias locale & scope)
    public WidgetRoot(WikiPage aliasPage, ParentWidget imposterWidget) {
        super(imposterWidget, /*is alias=*/ true);
        WidgetRoot aliasRoot = imposterWidget.getRoot();

        this.builder = imposterWidget.getBuilder();
        this.variables = aliasRoot.variables;
        this.doEscaping = aliasRoot.doEscaping;
        this.literals = aliasRoot.literals;
        this.isGatheringInfo = aliasRoot.isGatheringInfo;
        this.page = aliasPage;
        this.includingPage = aliasRoot;
    }

    public WidgetRoot getRoot() {
        return this;
    }

    public boolean isGatheringInfo() {
        return isGatheringInfo;
    }

    public WidgetRoot(WikiPage page) throws IOException {
        this("", page, WidgetBuilder.htmlWidgetBuilder);
    }

    public WidgetRoot(String value, WikiPage page) throws IOException {
        this(value, page, WidgetBuilder.htmlWidgetBuilder);
    }

    public WidgetRoot(String value, WikiPage page, WidgetBuilder builder) throws IOException {
        this(value, page, builder, false);
    }

    public WidgetRoot(String value, WikiPage page, WidgetBuilder builder, boolean isGathering) throws IOException {
        super(null);
        this.page = page;
        this.builder = builder;
        this.isGatheringInfo = isGathering;
        if (value != null)
            buildWidgets(value);
    }

    public WidgetRoot(PagePointer pagePointer) throws IOException {
        this("", pagePointer, WidgetBuilder.htmlWidgetBuilder);
    }

    public WidgetRoot(String value, PagePointer pagePointer) throws IOException {
        this(value, pagePointer, WidgetBuilder.htmlWidgetBuilder);
    }

    public WidgetRoot(String value, PagePointer pagePointer, WidgetBuilder builder) throws IOException {
        super(null);
        this.page = pagePointer.getPage();
        this.builder = builder;
        if (value != null)
            buildWidgets(value);
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

    public String getVariable(String key) throws IOException {
        String value = getValueOfVariableFromAllPossibleSources(key);
        if (value != null) {
            value = replaceAllKnownVariables(value);

            value = value.replaceAll(VariableWidget.prefixDisabled, VariableWidget.prefix);
            variables.put(key, value);
        }
        return value;
    }

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
            pageData.setLiterals(this.getLiterals());
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

    public int includesVariableAt(String string, int pos) {
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
    public String replaceVariable(String string, int pos) throws IOException {
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

    public void setLiterals(List<String> literals) {
        this.literals = literals;
    }

    public String asWikiText() {
        return childWikiText();
    }

    @Override
    public Maybe<String> findVariable(String name) {
        try {
            return new Maybe<String>(getVariable(name));
        } catch (IOException e) {
            return Maybe.noString;
        }

    }
}

