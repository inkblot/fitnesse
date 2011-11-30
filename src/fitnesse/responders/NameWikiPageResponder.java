// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.html.HtmlPageFactory;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class NameWikiPageResponder extends BasicWikiPageResponder {
    @Inject
    public NameWikiPageResponder(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root) {
        super(htmlPageFactory, root);
    }

    protected String contentFrom(WikiPage requestedPage)
            throws Exception {
        List<String> pages = new ArrayList<String>();
        for (WikiPage child : requestedPage.getChildren()) {
            if (request.hasInput("ShowChildCount")) {
                String name = child.getName() + " " + Integer.toString(child.getChildren().size());
                pages.add(name);
            } else
                pages.add(child.getName());

        }

        String format = (String) request.getInput("format");
        if ("json".equalsIgnoreCase(format)) {
            JSONArray jsonPages = new JSONArray(pages);
            return jsonPages.toString();
        }
        return StringUtils.join(pages, System.getProperty("line.separator"));
    }

    protected String getContentType() {
        return "text/plain";
    }

    @Override
    public SecureOperation getSecureOperation() {
        return new SecureReadOperation();
    }
}
