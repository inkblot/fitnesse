// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.responders.editing.*;
import fitnesse.responders.files.*;
import fitnesse.responders.refactoring.*;
import fitnesse.responders.run.*;
import fitnesse.responders.search.ExecuteSearchPropertiesResponder;
import fitnesse.responders.search.SearchFormResponder;
import fitnesse.responders.search.SearchResponder;
import fitnesse.responders.search.WhereUsedResponder;
import fitnesse.responders.testHistory.HistoryComparatorResponder;
import fitnesse.responders.testHistory.PageHistoryResponder;
import fitnesse.responders.testHistory.PurgeHistoryResponder;
import fitnesse.responders.testHistory.TestHistoryResponder;
import fitnesse.responders.versions.RollbackResponder;
import fitnesse.responders.versions.VersionResponder;
import fitnesse.responders.versions.VersionSelectionResponder;
import fitnesse.wikitext.widgets.WikiWordWidget;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

@Singleton
public class ResponderFactory {
    private final Injector injector;
    private final String rootPath;
    private final Map<String, Class<? extends Responder>> responderMap;

    @Inject
    public ResponderFactory(Injector injector, @Named("fitnesse.rootPath") String rootPath, @Named("fitnesse.rootPageName") String rootPageName) {
        this(injector, rootPath + File.separator + rootPageName);
    }

    public ResponderFactory(Injector injector, String rootPath) {
        this.rootPath = rootPath;
        this.injector = injector;
        responderMap = new HashMap<String, Class<? extends Responder>>();
        addResponder("edit", EditResponder.class);
        addResponder("saveData", SaveResponder.class);
        addResponder("search", SearchResponder.class);
        addResponder("searchForm", SearchFormResponder.class);
        addResponder("stoptest", StopTestResponder.class);
        addResponder("test", TestResponder.class);
        addResponder("suite", SuiteResponder.class);
        addResponder("proxy", SerializedPageResponder.class);
        addResponder("versions", VersionSelectionResponder.class);
        addResponder("viewVersion", VersionResponder.class);
        addResponder("rollback", RollbackResponder.class);
        addResponder("names", NameWikiPageResponder.class);
        addResponder("properties", PropertiesResponder.class);
        addResponder("saveProperties", SavePropertiesResponder.class);
        addResponder("executeSearchProperties", ExecuteSearchPropertiesResponder.class);
        addResponder("whereUsed", WhereUsedResponder.class);
        addResponder("refactor", RefactorPageResponder.class);
        addResponder("deletePage", DeletePageResponder.class);
        addResponder("renamePage", RenamePageResponder.class);
        addResponder("movePage", MovePageResponder.class);
        addResponder("pageData", PageDataWikiPageResponder.class);
        addResponder("createDir", CreateDirectoryResponder.class);
        addResponder("upload", UploadResponder.class);
        addResponder("socketCatcher", SocketCatchingResponder.class);
        addResponder("fitClient", FitClientResponder.class);
        addResponder("deleteFile", DeleteFileResponder.class);
        addResponder("renameFile", RenameFileResponder.class);
        addResponder("deleteConfirmation", DeleteConfirmationResponder.class);
        addResponder("renameConfirmation", RenameFileConfirmationResponder.class);
        addResponder("raw", RawContentResponder.class);
        addResponder("rss", RssResponder.class);
        addResponder("import", WikiImportingResponder.class);
        addResponder("files", FileResponder.class);
        addResponder("shutdown", ShutdownResponder.class);
        addResponder("format", TestResultFormattingResponder.class);
        addResponder("symlink", SymbolicLinkResponder.class);
        addResponder("importAndView", ImportAndViewResponder.class);
        addResponder("getPage", WikiPageResponder.class);
        addResponder("packet", PacketResponder.class);
        addResponder("testHistory", TestHistoryResponder.class);
        addResponder("pageHistory", PageHistoryResponder.class);
        addResponder("addChild", AddChildPageResponder.class);
        addResponder("purgeHistory", PurgeHistoryResponder.class);
        addResponder("compareHistory", HistoryComparatorResponder.class);
        addResponder("replace", SearchReplaceResponder.class);
    }

    public void addResponder(String key, String responderClassName) throws ClassNotFoundException {
        responderMap.put(key, (Class<? extends Responder>) Class.forName(responderClassName));
    }

    public void addResponder(String key, Class<? extends Responder> responderClass) {
        responderMap.put(key, responderClass);
    }

    public String getResponderKey(Request request) {
        String fullQuery;
        if (request.hasInput("responder"))
            fullQuery = (String) request.getInput("responder");
        else
            fullQuery = request.getQueryString();

        if (fullQuery == null)
            return null;

        int argStart = fullQuery.indexOf('&');
        return (argStart <= 0) ? fullQuery : fullQuery.substring(0, argStart);
    }

    public Responder makeResponder(Request request) {
        String resource = request.getResource();
        String responderKey = getResponderKey(request);
        if (isNotEmpty(responderKey)) {
            return lookupResponder(responderKey);
        } else if (isEmpty(resource)) {
            return new WikiPageResponder();
        } else if (resource.startsWith("files/") || resource.equals("files")) {
            return FileResponder.makeResponder(request.getResource(), rootPath);
        } else if (WikiWordWidget.isWikiWord(resource) || "root".equals(resource)) {
            return new WikiPageResponder();
        } else {
            return new NotFoundResponder();
        }
    }

    private Responder lookupResponder(String responderKey) {
        Class<? extends Responder> responderClass = getResponderClass(responderKey);
        if (responderClass != null) {
            return injector.getInstance(responderClass);
        } else {
            return new DefaultResponder();
        }
    }

    public Class<? extends Responder> getResponderClass(String responderKey) {
        return responderMap.get(responderKey);
    }

}
