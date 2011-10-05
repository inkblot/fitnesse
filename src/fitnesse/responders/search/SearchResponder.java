// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import com.google.inject.Inject;
import fitnesse.components.RegularExpressionWikiPageFinder;
import fitnesse.components.TitleWikiPageFinder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.LITERAL;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class SearchResponder extends ResultResponder {

    @Inject
    public SearchResponder(HtmlPageFactory htmlPageFactory) {
        super(htmlPageFactory);
    }

    private String getSearchString() {
        return (String) request.getInput("searchString");
    }

    private String getSearchType() {
        String searchType = (String) request.getInput("searchType");
        searchType = searchType.toLowerCase();

        if (searchType.contains("title"))
            return "Title";
        else
            return "Content";
    }

    protected String getTitle() {
        return getSearchType() + " Search Results for '" + getSearchString() + "'";
    }

    protected void startSearching(WikiPage root, WikiPage page) throws IOException {
        super.startSearching(root, page);
        String searchString = getSearchString();
        if (isNotEmpty(searchString)) {
            String searchType = getSearchType();
            if ("Title".equals(searchType))
                new TitleWikiPageFinder(searchString, this).search(root);
            else {
                Pattern regularExpression = Pattern.compile(searchString, CASE_INSENSITIVE + LITERAL);
                new RegularExpressionWikiPageFinder(regularExpression, this).search(root);
            }
        }
    }

    protected boolean shouldRespondWith404() {
        return false;
    }

}
