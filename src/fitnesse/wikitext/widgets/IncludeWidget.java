// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import fitnesse.wikitext.WikiWordUtil;
import util.Maybe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IncludeWidget extends ParentWidget {
    public static final String REGEXP =
            "^!include(?: +-setup| +-teardown| +-seamless| +-c)? " + WikiWordUtil.REGEXP + "\n" + "?";
    static final Pattern pattern = Pattern.compile("^!include *(-setup|-teardown|-seamless|-c)? (.*)");

    public static final String COLLAPSE_SETUP = "COLLAPSE_SETUP";
    public static final String COLLAPSE_TEARDOWN = "COLLAPSE_TEARDOWN";

    protected String pageName;
    protected WikiPage includingPage;
    protected WikiPage includedPage; //Retain from getIncludedPageContent()
    protected WikiPage parentPage;

    private static Map<String, String> optionPrefixMap = buildOptionPrefixMap();
    private static Map<String, String> optionCssMap = buildOptionsCssMap();
    private String includeOption;

    public IncludeWidget(ParentWidget parent, String text) throws IOException {
        super(parent);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            initializeWidget(parent, matcher);
            addChildWidgetsThatRepresentTheInclusion();
        }
    }

    private void addChildWidgetsThatRepresentTheInclusion() throws IOException {
        if (isParentOf(includedPage, includingPage))
            addChildWidgets(String.format("!meta Error! Cannot include parent page (%s).\n", getPageName()));
        else
            buildWidget();
    }

    private void initializeWidget(ParentWidget parent, Matcher matcher) throws IOException {
        includeOption = getOption(matcher);
        pageName = getPageName(matcher);
        includingPage = parent.getWikiPage();
        parentPage = includingPage.getParent();
        includedPage = getIncludedPage();
    }

    private boolean isParentOf(WikiPage possibleParent, WikiPage page) {
        for (; page.getParent() != page; page = page.getParent()) {
            if (possibleParent == page)
                return true;
        }
        return false;
    }

    public String getPageName() {
        return pageName;
    }

    protected String getIncludedPageContent() throws IOException {
        PageCrawler crawler = parentPage.getPageCrawler();
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        WikiPagePath pagePath = PathParser.parse(getPageName());
        includedPage = crawler.getSiblingPage(includingPage, pagePath); //Retain this

        if (includedPage != null) {
            includedPage.setParentForVariables(getWikiPage().getParentForVariables());
            return includedPage.getData().getContent();
        } else if (includingPage instanceof ProxyPage) {
            ProxyPage proxy = (ProxyPage) includingPage;
            String host = proxy.getHost();
            int port = proxy.getHostPort();
            try {
                ProxyPage remoteIncludedPage = new ProxyPage("RemoteIncludedPage", null, host, port, pagePath, includedPage.getInjector());
                return remoteIncludedPage.getData().getContent();
            } catch (Exception e) {
                return "!meta '''Remote page " + host + ":" + port + "/" + getPageName() + " does not exist.'''";
            }
        } else {
            return "!meta '''Page include failed because the page " + getPageName() + " does not exist.'''";
        }
    }

    protected WikiPage getIncludedPage() throws IOException {
        PageCrawler crawler = parentPage.getPageCrawler();
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        return crawler.getPage(parentPage, PathParser.parse(getPageName()));
    }

    private String getOption(Matcher match) {
        return match.group(1);
    }

    private String getPageName(Matcher match) {
        return match.group(2);
    }

    //TODO MDM I know this is bad...  But it seems better then creating two new widgets.
    private void buildWidget() throws IOException {
        String includedText = getIncludedPageContent(includeOption);
        String widgetText = processLiterals(includedText);

        //Create impostor root with alias = this if included page found.
        ParentWidget incRoot = (includedPage == null) ? this : new AliasingWidgetRoot(includedPage, this);

        if (isSeamLess(includeOption) || getRoot().isIgnoringText()) {  //Use the impostor if found.
            incRoot.addChildWidgets(widgetText + "\n");
        } else {  //Use new constructor with dual scope.
            new CollapsableWidget(
                    incRoot,
                    this,
                    getPrefix(includeOption) + getPageName(),
                    widgetText,
                    getCssClass(includeOption),
                    isCollapsed(includeOption)
            );
        }
    }

    //TODO MG There was no better way to nest in this behaviour. As future evolution point we can
    //        expand the if clause to also accept regular includes and replace PAGE_NAME all the time.
    private String getIncludedPageContent(String option) throws IOException {

        if (isSetup(option) || isTeardown(option)) {
            return replaceSpecialVariables(getIncludedPageContent());
        }

        return getIncludedPageContent();
    }

    //TODO MG What about PAGE_PATH?
    private String replaceSpecialVariables(String includedPageContent) {
        return includedPageContent.replaceAll("\\$\\{PAGE_NAME\\}", includingPage.getName());
    }

    private boolean isSeamLess(String option) {
        return "-seamless".equals(option);
    }

    private String getCssClass(String option) {
        return optionCssMap.get(option);
    }

    private String getPrefix(String option) {
        return optionPrefixMap.get(option);
    }

    private boolean isCollapsed(String option) throws IOException {
        if (isSetup(option) && isSetupCollapsed())
            return true;
        else if (isTeardown(option) && isTeardownCollapsed())
            return true;
        else if ("-c".equals(option))
            return true;
        return false;
    }

    private static Map<String, String> buildOptionsCssMap() {
        Map<String, String> optionCssMap = new HashMap<String, String>();
        optionCssMap.put("-setup", "setup");
        optionCssMap.put("-teardown", "teardown");
        optionCssMap.put("-c", "included");
        optionCssMap.put(null, "included");
        return optionCssMap;
    }

    private static Map<String, String> buildOptionPrefixMap() {
        Map<String, String> optionPrefixMap = new HashMap<String, String>();
        optionPrefixMap.put("-setup", "Set Up: ");
        optionPrefixMap.put("-teardown", "Tear Down: ");
        optionPrefixMap.put("-c", "Included page: ");
        optionPrefixMap.put(null, "Included page: ");
        return optionPrefixMap;
    }

    private boolean isTeardownCollapsed() {
        final Maybe<String> teardownCollapseVariable = getVariableSource().findVariable(COLLAPSE_TEARDOWN);
        return teardownCollapseVariable.isNothing() || "true".equals(teardownCollapseVariable.getValue());
    }

    private boolean isTeardown(String option) {
        return "-teardown".equals(option);
    }

    private boolean isSetupCollapsed() {
        final Maybe<String> setupCollapseVariable = getVariableSource().findVariable(COLLAPSE_SETUP);
        return setupCollapseVariable.isNothing() || "true".equals(setupCollapseVariable.getValue());
    }

    private boolean isSetup(String option) {
        return "-setup".equals(option);
    }

    public String render() throws IOException {
        return childHtml();
    }

    public String asWikiText() {
        return "";
    }
}
