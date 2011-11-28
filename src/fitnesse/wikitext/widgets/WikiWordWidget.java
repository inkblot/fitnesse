// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.Utils;
import fitnesse.wikitext.WikiWordUtil;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class WikiWordWidget extends TextWidget {
    public static final String REGEXP = WikiWordUtil.REGEXP;

    public WikiPage parentPage;

    public WikiWordWidget(ParentWidget parent, String text) {
        super(parent, text);
        WikiPage wikiPage = getWikiPage();
        parentPage = wikiPage.getParent();
    }

    public String render() throws IOException {
        WikiPagePath pathOfWikiWord = PathParser.parse(getWikiWord());
        WikiPagePath fullPathOfWikiWord = parentPage.getPageCrawler().getFullPathOfChild(parentPage, pathOfWikiWord);
        String qualifiedName = PathParser.render(fullPathOfWikiWord);
        WikiPage targetPage = parentPage.getPageCrawler().getPage(parentPage, PathParser.parse(getWikiWord()));
        if (targetPage != null)
            return makeLinkToExistingWikiPage(qualifiedName, targetPage);
        else
            return makeLinkToNonExistentWikiPage(qualifiedName);
    }

    private String makeLinkToNonExistentWikiPage(String qualifiedName) {
        StringBuilder html = new StringBuilder();
        html.append(Utils.escapeHTML(getText()));
        html.append("<a title=\"create page\" href=\"").append(qualifiedName);
        html.append("?edit&nonExistent=true");
        html.append("\">[?]</a>");
        return html.toString();
    }

    public boolean isRegracing() {
        return "true".equals(getVariableSource().findVariable(WikiWordUtil.REGRACE_LINK).getValue());
    }

    private String makeLinkToExistingWikiPage(String qualifiedName, WikiPage wikiPage) throws IOException {
        HtmlTag link = HtmlUtil.makeLink(qualifiedName, Utils.escapeHTML(regrace(getText())));
        addHelpText(link, wikiPage);
        return link.htmlInline();
    }

    private void addHelpText(HtmlTag link, WikiPage wikiPage) throws IOException {
        String helpText = wikiPage.getHelpText();
        if (helpText != null) link.addAttribute("title", helpText);
    }

    // If pageToRename is referenced somewhere in this wiki word (could be a parent, etc.),
    // rename it to newName.
    public void renamePageIfReferenced(WikiPage pageToRename, String newName) {
        String fullPathToReferent = getQualifiedWikiWord();
        WikiPagePath pathToPageBeingRenamed = pageToRename.getPageCrawler().getFullPath(pageToRename);
        pathToPageBeingRenamed.makeAbsolute();
        String absolutePathToPageBeingRenamed = PathParser.render(pathToPageBeingRenamed);

        if (refersTo(fullPathToReferent, absolutePathToPageBeingRenamed)) {
            int oldNameLength = absolutePathToPageBeingRenamed.length();
            String renamedPath = "." + rename(absolutePathToPageBeingRenamed.substring(1), newName);
            String pathAfterRenamedPage = fullPathToReferent.substring(oldNameLength);
            String fullRenamedPathToReferent = renamedPath + pathAfterRenamedPage;
            String renamedReference = makeRenamedRelativeReference(PathParser.parse(fullRenamedPathToReferent));
            setText(renamedReference);
        }
    }

    public void renameMovedPageIfReferenced(WikiPage pageToBeMoved, String newParentName) {
        WikiPagePath pathOfPageToBeMoved = pageToBeMoved.getPageCrawler().getFullPath(pageToBeMoved);
        pathOfPageToBeMoved.makeAbsolute();
        String QualifiedNameOfPageToBeMoved = PathParser.render(pathOfPageToBeMoved);
        String reference = getQualifiedWikiWord();
        if (refersTo(reference, QualifiedNameOfPageToBeMoved)) {
            String referenceTail = reference.substring(QualifiedNameOfPageToBeMoved.length());
            String childPortionOfReference = pageToBeMoved.getName();
            if (referenceTail.length() > 0)
                childPortionOfReference += referenceTail;
            String newQualifiedName;
            if (isEmpty(newParentName))
                newQualifiedName = "." + childPortionOfReference;
            else
                newQualifiedName = "." + newParentName + "." + childPortionOfReference;

            setText(newQualifiedName);
        }
    }

    public String makeRenamedRelativeReference(WikiPagePath renamedPathToReferent) {
        String rawReference = getText();
        WikiPagePath parentPath = parentPage.getPageCrawler().getFullPath(parentPage);
        parentPath.makeAbsolute();

        if (rawReference.startsWith("."))
            return PathParser.render(renamedPathToReferent);
        else if (rawReference.startsWith("<")) {
            return buildBackwardSearchReference(parentPath, renamedPathToReferent);
        } else {
            boolean parentPathNotRenamed = renamedPathToReferent.startsWith(parentPath);
            if (parentPathNotRenamed) {
                WikiPagePath relativePath = renamedPathToReferent.subtractFromFront(parentPath);
                if (rawReference.startsWith("^") || rawReference.startsWith(">"))
                    return ">" + PathParser.render(relativePath.getRest());
                else
                    return PathParser.render(relativePath);
            }
        }
        return rawReference;
    }

    static String buildBackwardSearchReference(WikiPagePath parentPath, WikiPagePath renamedPathToReferent) {
        int branchPoint = findBranchPoint(parentPath.getNames(), renamedPathToReferent.getNames());
        List<String> referentPath = renamedPathToReferent.getNames();
        List<String> referentPathAfterBranchPoint = referentPath.subList(branchPoint, referentPath.size());
        return "<" + StringUtils.join(referentPathAfterBranchPoint, ".");
    }

    private static int findBranchPoint(List<String> list1, List<String> list2) {
        int i;
        for (i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) break;
        }
        return Math.max(0, i - 1);
    }

    static boolean refersTo(String qualifiedReference, String qualifiedTarget) {
        return qualifiedReference.equals(qualifiedTarget) || qualifiedReference.startsWith(qualifiedTarget + ".");
    }

    private String getQualifiedWikiWord() {
        String pathName = expandPrefix(getText());
        WikiPagePath expandedPath = PathParser.parse(pathName);
        if (expandedPath == null)
            return getText();
        WikiPagePath fullPath = parentPage.getPageCrawler().getFullPathOfChild(parentPage, expandedPath);
        return "." + PathParser.render(fullPath); //todo rcm 2/6/05 put that '.' into pathParser.  Perhaps WikiPagePath.setAbsolute()
    }

    private String rename(String oldQualifiedName, String newPageName) {
        String newQualifiedName;

        int lastDotIndex = oldQualifiedName.lastIndexOf(".");
        if (lastDotIndex < 1)
            newQualifiedName = newPageName;
        else
            newQualifiedName = oldQualifiedName.substring(0, lastDotIndex + 1) + newPageName;
        return newQualifiedName;
    }

    String getWikiWord() {
        return expandPrefix(getText());
    }

    protected String expandPrefix(String theWord) {
        WikiPage wikiPage = getWikiPage();
        return WikiWordUtil.expandPrefix(wikiPage, theWord);
    }

}
