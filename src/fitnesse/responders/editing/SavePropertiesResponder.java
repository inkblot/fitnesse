// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.responders.editing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.RecentChanges;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class SavePropertiesResponder implements SecureResponder {
    private final HtmlPageFactory htmlPageFactory;
    private final WikiPage root;

    @Inject
    public SavePropertiesResponder(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    public Response makeResponse(Request request) throws Exception {
        SimpleResponse response = new SimpleResponse();
        String resource = request.getResource();
        WikiPagePath path = PathParser.parse(resource);
        WikiPage page = root.getPageCrawler().getPage(root, path);
        if (page == null)
            return new NotFoundResponder(htmlPageFactory).makeResponse(request);
        PageData data = page.getData();
        saveAttributes(request, data);
        VersionInfo commitRecord = page.commit(data);
        response.addHeader("Previous-Version", commitRecord.getName());
        RecentChanges.updateRecentChanges(data);
        response.redirect(resource);

        return response;
    }

    private void saveAttributes(Request request, PageData data) throws Exception {
        setPageTypeAttribute(request, data);

        List<String> attrs = new LinkedList<String>();
        attrs.addAll(Arrays.asList(PageData.NON_SECURITY_ATTRIBUTES));
        attrs.addAll(Arrays.asList(PageData.SECURITY_ATTRIBUTES));

        for (String attribute : attrs) {
            if (isChecked(request, attribute))
                data.setAttribute(attribute);
            else
                data.removeAttribute(attribute);
        }

        String value = (String) request.getInput(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE);
        value = value == null ? "" : value;
        if (!value.equals(data.getAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE))) {
            WikiPage page = data.getWikiPage();
            if (page.hasExtension(VirtualCouplingExtension.NAME)) {
                VirtualCouplingExtension extension = (VirtualCouplingExtension) page.getExtension(VirtualCouplingExtension.NAME);
                extension.resetVirtualCoupling();
            }
        }
        if (isEmpty(value))
            data.removeAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE);
        else
            data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, value);

        String suites = (String) request.getInput("Suites");
        data.setAttribute(PageData.PropertySUITES, suites);

        String helpText = (String) request.getInput("HelpText");
        data.setAttribute(PageData.PropertyHELP, helpText);
    }

    private void setPageTypeAttribute(Request request, PageData data)
            throws Exception {
        String pageType = getPageType(request);

        if (pageType == null)
            return;

        List<String> types = new LinkedList<String>();
        types.addAll(Arrays.asList(PageData.PAGE_TYPE_ATTRIBUTES));
        data.setAttribute(pageType);

        for (String type : types) {
            if (!pageType.equals(type))
                data.removeAttribute(type);
        }
    }

    private String getPageType(Request request) {
        return (String) request.getInput(PageData.PAGE_TYPE_ATTRIBUTE);
    }

    private boolean isChecked(Request request, String name) {
        return (request.getInput(name) != null);
    }

    public SecureOperation getSecureOperation() {
        return new AlwaysSecureOperation();
    }
}
