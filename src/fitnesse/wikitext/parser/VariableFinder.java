package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.List;

import static java.util.Arrays.asList;

public class VariableFinder implements VariableSource {
    private final List<VariableSource> sources;

    public VariableFinder(VariableSource... sources) {
        this.sources = asList(sources);
    }

    @Override
    public Maybe<String> findVariable(String name) {
        Maybe<String> result;
        for (VariableSource source : sources) {
            result = source.findVariable(name);
            if (!result.isNothing()) return result;
        }
        return Maybe.noString;
    }

}
