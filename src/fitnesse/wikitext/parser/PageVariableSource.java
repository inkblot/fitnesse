package fitnesse.wikitext.parser;

import util.Maybe;

/**
* Created by IntelliJ IDEA.
* User: inkblot
* Date: 11/28/11
* Time: 8:49 AM
*/
class PageVariableSource implements VariableSource {
    private final ParsingPage page;

    public PageVariableSource(ParsingPage page) {
        this.page = page;
    }

    @Override
    public Maybe<String> findVariable(String name) {
        Maybe<String> localVariable = page.findVariable(name);
        if (!localVariable.isNothing()) return new Maybe<String>(localVariable.getValue());
        return lookInParentPages(name);
    }

    private Maybe<String> lookInParentPages(String name) {
        for (SourcePage sourcePage : page.getPage().getAncestors()) {
            if (!page.inCache(sourcePage)) {
                Parser.make(page.copyForPage(sourcePage), sourcePage.getContent()).parse();
                // todo: make this a method on ParsingPage
                page.putVariable(sourcePage, "", Maybe.noString);
            }
            Maybe<String> result = page.findVariable(sourcePage, name);
            if (!result.isNothing()) return result;
        }
        return Maybe.noString;
    }
}
