package fitnesse.components;

import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class SuiteSpecificationMatchFinder extends WikiPageFinder {
    private String titleRegEx;
    private String contentRegEx;

    public SuiteSpecificationMatchFinder(String titleRegEx, String contentRegEx, SearchObserver observer) {
        super(observer);
        this.titleRegEx = titleRegEx;
        this.contentRegEx = contentRegEx;
    }

    protected boolean pageMatches(WikiPage page) throws IOException {
        if (isNotEmpty(titleRegEx) && isNotEmpty(contentRegEx))
            return patternMatches(titleRegEx, page.getName()) && patternMatches(contentRegEx, page.getData().getContent());
        else {
            return patternMatches(titleRegEx, page.getName()) || patternMatches(contentRegEx, page.getData().getContent());
        }
    }

    private boolean patternMatches(String regEx, String subject) {
        if (isNotEmpty(regEx)) {
            Pattern pattern = Pattern.compile(regEx, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(subject);
            if (matcher.find())
                return true;
        }
        return false;
    }

}
