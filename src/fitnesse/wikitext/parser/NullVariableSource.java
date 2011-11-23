package fitnesse.wikitext.parser;

import util.Maybe;

/**
* Created by IntelliJ IDEA.
* User: inkblot
* Date: 11/23/11
* Time: 11:04 AM
*/
class NullVariableSource implements VariableSource {
    @Override
    public Maybe<String> findVariable(String name) {
        return Maybe.noString;
    }
}
