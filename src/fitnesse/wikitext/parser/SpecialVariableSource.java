package fitnesse.wikitext.parser;

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
        else
            return Maybe.noString;
        return new Maybe<String>(value);
    }
}
