// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import org.junit.Before;
import org.junit.Test;

import static fitnesse.responders.search.SearchFormResponder.SEARCH_ACTION_ATTRIBUTES;
import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class SearchFormResponderTest {
    private String content;

    @Before
    public void setUp() throws Exception {
        FitNesseContext context = new FitNesseContext("RooT");
        SearchFormResponder responder = new SearchFormResponder();
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, new MockRequest());
        content = response.getContent();
    }

    @Test
    public void testFocusOnSearchBox() throws Exception {
        assertSubString("onload=\"document.forms[0].searchString.focus()\"", content);
    }

    @Test
    public void testHtml() throws Exception {
        assertHasRegexp("form", content);
        assertHasRegexp("input", content);
        assertSubString("<input", content);
        assertSubString("type=\"hidden\"", content);
        assertSubString("name=\"responder\"", content);
        assertSubString("value=\"search\"", content);
    }

    @Test
    public void testForTwoSearchTypes() throws Exception {
        assertSubString("type=\"submit\"", content);
        assertSubString("value=\"Search Titles!\"", content);
        assertSubString("value=\"Search Content!\"", content);
    }

    @Test
    public void propertiesForm() throws Exception {
        assertHasRegexp("<input.*value=\"Search Properties\".*>", content);
        assertHasRegexp("<input.*name=\"responder\".*value=\"executeSearchProperties\"", content);

        for (String attributeName : SEARCH_ACTION_ATTRIBUTES) {
            assertAttributeOptionCreated(content, attributeName);
        }
    }

    private void assertAttributeOptionCreated(String content, String attributeName) {
        assertSubString("<option>" + attributeName, content);
    }
}
