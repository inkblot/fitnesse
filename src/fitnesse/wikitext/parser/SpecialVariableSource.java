package fitnesse.wikitext.parser;

import com.google.inject.Key;
import com.google.inject.name.Names;
import fitnesse.FitNesseModule;
import fitnesse.wiki.WikiPageFactory;
import util.Maybe;

/**
* Created by IntelliJ IDEA.
* User: inkblot
* Date: 11/28/11
* Time: 8:50 AM
*/
class SpecialVariableSource implements VariableSource {
    private final ParsingPage page;

    public SpecialVariableSource(ParsingPage page) {
        this.page = page;
    }

    @Override
    public Maybe<String> findVariable(String name) {
        String value;
        if (name.equals("RUNNING_PAGE_NAME"))
            value = page.getPage().getName();
        else if (name.equals("RUNNING_PAGE_PATH"))
            value = page.getPage().getPath();
        else if (name.equals("PAGE_NAME"))
            value = page.getNamedPage().getName();
        else if (name.equals("PAGE_PATH"))
            value = page.getNamedPage().getPath();
        else if (name.equals("FITNESSE_PORT"))
            value = page.getPage().getInjector().getInstance(Key.get(Integer.class, Names.named(FitNesseModule.PORT))).toString();
        else if (name.equals("FITNESSE_ROOTPATH"))
            value = page.getPage().getInjector().getInstance(Key.get(String.class, Names.named(WikiPageFactory.ROOT_PATH)));
        else
            return Maybe.noString;
        return new Maybe<String>(value);
    }
}
