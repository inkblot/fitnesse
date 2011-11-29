// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wikitext.parser.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ClockUtil;
import util.Maybe;
import util.StringUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static fitnesse.wiki.PageType.*;

public class PageData implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(PageData.class);

    private static SymbolProvider variableDefinitionSymbolProvider = new SymbolProvider(
            Literal.symbolType, new Define(), new Include(), SymbolType.CloseLiteral, Comment.symbolType,
            SymbolType.Whitespace, SymbolType.Newline, Variable.symbolType, Preformat.symbolType,
            SymbolType.ClosePreformat, SymbolType.Text);

    // TODO: Find a better place for us
    public static final String PropertyLAST_MODIFIED = "LastModified";
    public static final String PropertyHELP = "Help";
    public static final String PropertyPRUNE = "Prune";
    public static final String PropertySEARCH = "Search";
    public static final String PropertyRECENT_CHANGES = "RecentChanges";
    public static final String PropertyFILES = "Files";
    public static final String PropertyWHERE_USED = "WhereUsed";
    public static final String PropertyREFACTOR = "Refactor";
    public static final String PropertyPROPERTIES = "Properties";
    public static final String PropertyVERSIONS = "Versions";
    public static final String PropertyEDIT = "Edit";
    public static final String PropertySUITES = "Suites";

    public static final String PAGE_TYPE_ATTRIBUTE = "PageType";
    public static final String[] PAGE_TYPE_ATTRIBUTES = {NORMAL.toString(),
            TEST.toString(), SUITE.toString()};

    public static final String[] ACTION_ATTRIBUTES = {PropertyEDIT,
            PropertyVERSIONS, PropertyPROPERTIES, PropertyREFACTOR,
            PropertyWHERE_USED};

    public static final String[] NAVIGATION_ATTRIBUTES = {
            PropertyRECENT_CHANGES, PropertyFILES, PropertySEARCH, PropertyPRUNE};

    public static final String[] NON_SECURITY_ATTRIBUTES;

    static {
        List <String> nonSecurityAttributes = new ArrayList<String>(ACTION_ATTRIBUTES.length + NAVIGATION_ATTRIBUTES.length);
        nonSecurityAttributes.addAll(Arrays.asList(ACTION_ATTRIBUTES));
        nonSecurityAttributes.addAll(Arrays.asList(NAVIGATION_ATTRIBUTES));
        NON_SECURITY_ATTRIBUTES = nonSecurityAttributes.toArray(new String[nonSecurityAttributes.size()]);
    }

    public static final String PropertySECURE_READ = "secure-read";
    public static final String PropertySECURE_WRITE = "secure-write";
    public static final String PropertySECURE_TEST = "secure-test";
    public static final String[] SECURITY_ATTRIBUTES = {PropertySECURE_READ,
            PropertySECURE_WRITE, PropertySECURE_TEST};

    public static final String LAST_MODIFYING_USER = "LastModifyingUser";
    public static final String SUITE_SETUP_NAME = "SuiteSetUp";
    public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";

    public static final String COMMAND_PATTERN = "COMMAND_PATTERN";
    public static final String TEST_RUNNER = "TEST_RUNNER";
    public static final String PATH_SEPARATOR = "PATH_SEPARATOR";

    private transient WikiPage wikiPage;
    private String content;
    private WikiPageProperties properties = new WikiPageProperties();
    private Set<VersionInfo> versions;

    private Symbol contentSyntaxTree = null;
    private ParsingPage parsingPage;

    public PageData(WikiPage page) {
        wikiPage = page;
        initializeAttributes();
        versions = new HashSet<VersionInfo>();
    }

    public PageData(WikiPage page, String content) {
        this(page);
        setContent(content);
    }

    public PageData(PageData data) {
        this(data.getWikiPage(), data.content);
        properties = new WikiPageProperties(data.properties);
        versions.addAll(data.versions);
        contentSyntaxTree = data.contentSyntaxTree;
        parsingPage = data.parsingPage;
    }

    public void initializeAttributes() {
        properties.set(PropertyEDIT, Boolean.toString(true));
        properties.set(PropertyVERSIONS, Boolean.toString(true));
        properties.set(PropertyPROPERTIES, Boolean.toString(true));
        properties.set(PropertyREFACTOR, Boolean.toString(true));
        properties.set(PropertyWHERE_USED, Boolean.toString(true));
        properties.set(PropertyFILES, Boolean.toString(true));
        properties.set(PropertyRECENT_CHANGES, Boolean.toString(true));
        properties.set(PropertySEARCH, Boolean.toString(true));
        properties.setLastModificationTime(ClockUtil.currentDate());

        initTestOrSuiteProperty();
    }

    private void initTestOrSuiteProperty() {
        final String pageName = wikiPage.getName();
        if (pageName == null) {
            handleInvalidPageName(wikiPage);
            return;
        }

        if (isErrorLogsPage())
            return;

        PageType pageType = PageType.getPageTypeForPageName(pageName);

        if (NORMAL.equals(pageType))
            return;

        properties.set(pageType.toString(), Boolean.toString(true));
    }

    private boolean isErrorLogsPage() {
        PageCrawler crawler = wikiPage.getPageCrawler();
        String relativePagePath = crawler.getRelativeName(
                crawler.getRoot(wikiPage), wikiPage);
        return relativePagePath.startsWith("ErrorLogs");
    }

    private void handleInvalidPageName(WikiPage wikiPage) {
        try {
            String msg = "WikiPage " + wikiPage + " does not have a valid name!"
                    + wikiPage.getName();
            logger.error(msg);
            throw new RuntimeException(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public WikiPageProperties getProperties() {
        return properties;
    }

    public String getAttribute(String key) {
        return properties.get(key);
    }

    public void removeAttribute(String key)  {
        properties.remove(key);
    }

    public void setAttribute(String key, String value)  {
        properties.set(key, value);
    }

    public void setAttribute(String key)  {
        properties.set(key);
    }

    public boolean hasAttribute(String attribute) {
        return properties.has(attribute);
    }

    public void setProperties(WikiPageProperties properties) {
        this.properties = properties;
    }

    public String getContent() {
        return StringUtil.stripCarriageReturns(content);
    }

    public void setContent(String content) {
        this.content = content;
        contentSyntaxTree = null;
        parsingPage = null;
    }

    /* this is the public entry to page parse and translate */
    public String getHtml() {
        return translateToHtml(getSyntaxTree());
    }

    public String getHeaderPageHtml() throws IOException {
        WikiPage header = wikiPage.getHeaderPage();
        return header == null ? "" : header.getData().getHtml();
    }

    public String getFooterPageHtml() throws IOException {
        WikiPage footer = wikiPage.getFooterPage();
        return footer == null ? "" : footer.getData().getHtml();
    }

    public String getVariable(String name) {
        VariableSource variableSource = getParsingPage().getVariableSource();
        Maybe<String> variable = variableSource.findVariable(name);
        if (variable.isNothing()) return null;
        //todo: push this into parser/translator
        return new HtmlTranslator(null, getParsingPage()).translate(Parser.make(getParsingPage(), "${" + name + "}", variableSource, variableDefinitionSymbolProvider).parse());
    }

    public Symbol getSyntaxTree() {
        parsePageContent();
        return contentSyntaxTree;
    }

    public ParsingPage getParsingPage() {
        parsePageContent();
        return parsingPage;
    }

    private void parsePageContent() {
        if (contentSyntaxTree == null) {
            parsingPage = new ParsingPage(new WikiSourcePage(wikiPage));
            contentSyntaxTree = Parser.make(parsingPage, getContent()).parse();
        }
    }

    public void addVariable(String name, String value) {
        getParsingPage().putVariable(name, value);
    }

    public String translateToHtml(Symbol syntaxTree) {
        return new HtmlTranslator(parsingPage.getPage(), parsingPage).translateTree(syntaxTree);
    }

    public void setWikiPage(WikiPage page) {
        wikiPage = page;
    }

    public WikiPage getWikiPage() {
        return wikiPage;
    }

    public List<String> getClasspaths() {
        Symbol tree = getSyntaxTree();
        return new Paths(new HtmlTranslator(parsingPage.getPage(), parsingPage)).getPaths(tree);
    }

    public List<String> getXrefPages() throws IOException {
        final List<String> values = new ArrayList<String>();
        getSyntaxTree().walkPreOrder(new SymbolTreeWalker() {
            @Override
            public boolean visit(Symbol node) {
                if (node.isType(new See())) {
                    values.add(node.childAt(0).getContent());
                }
                return true;
            }

            @Override
            public boolean visitChildren(Symbol node) {
                return !node.isType(new See());
            }
        });
        return values;
    }

    public Set<VersionInfo> getVersions() {
        return versions;
    }

    public void addVersions(Collection<VersionInfo> newVersions) {
        versions.addAll(newVersions);
    }

    public boolean isEmpty() {
        return getContent() == null || getContent().length() == 0;
    }
}
