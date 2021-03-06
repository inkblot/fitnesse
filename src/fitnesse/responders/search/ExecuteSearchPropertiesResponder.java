package fitnesse.responders.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.components.AttributeWikiPageFinder;
import fitnesse.components.PageFinder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.wiki.PageType;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static fitnesse.responders.search.SearchFormResponder.SEARCH_ACTION_ATTRIBUTES;
import static fitnesse.responders.search.SearchFormResponder.SPECIAL_ATTRIBUTES;
import static fitnesse.wiki.PageData.*;

public class ExecuteSearchPropertiesResponder extends ResultResponder {

    public static final String IGNORED = "Any";
    public static final String ACTION = "Action";
    public static final String SECURITY = "Security";
    public static final String SPECIAL = "Special";

    @Inject
    public ExecuteSearchPropertiesResponder(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root, RunningTestingTracker runningTestingTracker, @Named(FitNesseModule.ENABLE_CHUNKING) boolean chunkingEnabled) {
        super(htmlPageFactory, root, runningTestingTracker, chunkingEnabled);
    }

    public SecureOperation getSecureOperation() {
        return new SecureReadOperation();
    }

    protected List<PageType> getPageTypesFromInput(Request request) {
        String requestedPageTypes = (String) request.getInput(PAGE_TYPE_ATTRIBUTE);
        if (requestedPageTypes == null) {
            return null;
        }

        List<PageType> types = new ArrayList<PageType>();

        for (String type : requestedPageTypes.split(",")) {
            types.add(PageType.fromString(type));
        }
        return types;
    }

    protected String getSuitesFromInput(Request request) {
        if (!isSuitesGiven(request))
            return null;

        return (String) request.getInput(PropertySUITES);
    }

    private boolean isSuitesGiven(Request request) {
        return request.hasInput(PropertySUITES);
    }

    protected Map<String, Boolean> getAttributesFromInput(Request request) {
        Map<String, Boolean> attributes = new LinkedHashMap<String, Boolean>();

        getListBoxAttributesFromRequest(request, ACTION, SEARCH_ACTION_ATTRIBUTES,
                attributes);
        getListBoxAttributesFromRequest(request, SECURITY, SECURITY_ATTRIBUTES,
                attributes);

        getListBoxAttributesFromRequest(request, SPECIAL, SPECIAL_ATTRIBUTES,
                attributes);

        // this is an ugly renaming we need to make
        Boolean obsoleteFlag = attributes.remove("obsolete");
        if (obsoleteFlag != null)
            attributes.put(PropertyPRUNE, obsoleteFlag);

        return attributes;
    }

    private void getListBoxAttributesFromRequest(Request request,
                                                 String inputAttributeName, String[] attributeList,
                                                 Map<String, Boolean> attributes) {
        String requested = (String) request.getInput(inputAttributeName);
        if (requested == null) {
            requested = "";
        }
        if (!IGNORED.equals(requested)) {
            for (String searchAttribute : attributeList) {
                attributes.put(searchAttribute, requested.contains(searchAttribute));
            }
        }
    }

    @Override
    protected String getTitle() {
        return "Search Page Properties Results";
    }

    @Override
    protected void startSearching(WikiPage root, WikiPage page) throws IOException {
        super.startSearching(root, page);
        List<PageType> pageTypes = getPageTypesFromInput(request);
        Map<String, Boolean> attributes = getAttributesFromInput(request);
        String suites = getSuitesFromInput(request);

        if (pageTypes == null && attributes.isEmpty() && suites == null) {
            response.add("No search properties were specified.");
            return;
        }

        PageFinder finder = new AttributeWikiPageFinder(this, pageTypes,
                attributes, suites);
        finder.search(page);
    }

}
