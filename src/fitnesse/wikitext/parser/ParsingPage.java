package fitnesse.wikitext.parser;

import com.google.inject.Key;
import util.Maybe;

import java.util.HashMap;

public class ParsingPage implements VariableSource {
    private final SourcePage page;
    private final SourcePage namedPage;
    private final HashMap<String, HashMap<String, Maybe<String>>> variableCache;

    public ParsingPage(SourcePage page) {
        this(page, page, new HashMap<String, HashMap<String, Maybe<String>>>());
    }

    private ParsingPage(SourcePage page, SourcePage namedPage, HashMap<String, HashMap<String, Maybe<String>>> variableCache) {
        this.page = page;
        this.namedPage = namedPage;
        this.variableCache = variableCache;
    }

    public ParsingPage copyForPage(SourcePage page) {
        return new ParsingPage(page, page, this.variableCache);
    }

    public ParsingPage copyForNamedPage(SourcePage namedPage) {
        return new ParsingPage(this.page, namedPage, this.variableCache);
    }

    public SourcePage getPage() {
        return page;
    }

    public SourcePage getNamedPage() {
        return namedPage;
    }

    public boolean inCache(SourcePage page) {
        return variableCache.containsKey(page.getFullName());
    }

    public Maybe<String> findVariable(SourcePage page, String name) {
        String key = page.getFullName();
        if (!variableCache.containsKey(key)) return Maybe.noString;
        if (!variableCache.get(key).containsKey(name)) return Maybe.noString;
        return variableCache.get(key).get(name);
    }

    @Override
    public Maybe<String> findVariable(String name) {
        return findVariable(page, name);
    }

    public void putVariable(SourcePage page, String name, Maybe<String> value) {
        String key = page.getFullName();
        if (!variableCache.containsKey(key)) variableCache.put(key, new HashMap<String, Maybe<String>>());
        variableCache.get(key).put(name, value);
    }

    public void putVariable(String name, String value) {
        putVariable(page, name, new Maybe<String>(value));
    }

    public VariableSource getVariableSource() {
        VariableSource contextVariableSource = getPage().getInjector().getInstance(Key.get(VariableSource.class, Context.class));
        return new VariableFinder(
                new SpecialVariableSource(this),
                contextVariableSource,
                new PageVariableSource(this),
                new EnvironmentVariableSource(),
                new PropertiesVariableSource(System.getProperties()));
    }
}
