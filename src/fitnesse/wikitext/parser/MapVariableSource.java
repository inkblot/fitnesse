package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 11/28/11
 * Time: 8:56 PM
 */
public class MapVariableSource implements VariableSource {
    private final Map<String, String> map;

    public MapVariableSource(Map<String, String> map) {
        this.map = unmodifiableMap(map);
    }

    @Override
    public Maybe<String> findVariable(String name) {
        return map.containsKey(name) ? new Maybe<String>(map.get(name)) : Maybe.noString;
    }
}
