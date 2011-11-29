package fitnesse.wikitext.parser;

import util.Maybe;

/**
* Created by IntelliJ IDEA.
* User: inkblot
* Date: 11/28/11
* Time: 8:50 AM
*/
class EnvironmentVariableSource implements VariableSource {
    @Override
    public Maybe<String> findVariable(String name) {
        String value = System.getenv(name);
        return value != null ? new Maybe<String>(value) : Maybe.noString;
    }
}
