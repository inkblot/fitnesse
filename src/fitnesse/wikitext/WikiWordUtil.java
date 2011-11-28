package fitnesse.wikitext;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 11/27/11
 * Time: 6:38 PM
 */
public class WikiWordUtil {
    public static final String SINGLE_WIKIWORD_REGEXP = "\\b[A-Z](?:[a-z0-9]+[A-Z][a-z0-9]*)+";
    public static final String REGEXP = "(?:[<>^.])?(?:" + SINGLE_WIKIWORD_REGEXP + "[.]?)+\\b";
    public static final String REGRACE_LINK = "REGRACE_LINK";

    public static String makeWikiWord(String input) {
        if (isWikiWord(input)) return input;
        String base = input;
        while (base.length() < 3) base += "a";
        return base.substring(0, 1).toUpperCase()
                + base.substring(1, base.length() - 1).toLowerCase()
                + base.substring(base.length() - 1).toUpperCase();
    }

    public static boolean isWikiWord(String word) {
        return Pattern.matches(REGEXP, word);
    }

    public static boolean isSingleWikiWord(String s) {
        return Pattern.matches(SINGLE_WIKIWORD_REGEXP, s);
    }

    public static String expandPrefix(WikiPage wikiPage, String theWord) {
        PageCrawler crawler = wikiPage.getPageCrawler();
        if (theWord.charAt(0) == '^' || theWord.charAt(0) == '>') {
            String prefix = wikiPage.getName();
            return String.format("%s.%s", prefix, theWord.substring(1));
        } else if (theWord.charAt(0) == '<') {
            String undecoratedPath = theWord.substring(1);
            String[] pathElements = undecoratedPath.split("\\.");
            String target = pathElements[0];
            //todo rcm, this loop is duplicated in PageCrawlerImpl.getSiblingPage
            for (WikiPage current = wikiPage.getParent(); !crawler.isRoot(current); current = current.getParent()) {
                if (current.getName().equals(target)) {
                    pathElements[0] = PathParser.render(crawler.getFullPath(current));
                    return "." + StringUtils.join(Arrays.asList(pathElements), ".");
                }
            }
            return "." + undecoratedPath;
        }
        return theWord;
    }
}
