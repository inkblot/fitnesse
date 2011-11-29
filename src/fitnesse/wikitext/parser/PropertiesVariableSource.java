package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.Properties;

/**
* Created by IntelliJ IDEA.
* User: inkblot
* Date: 11/28/11
* Time: 8:50 AM
*/
class PropertiesVariableSource implements VariableSource {
    private final Properties properties;

    public PropertiesVariableSource(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Maybe<String> findVariable(String name) {
        return properties.containsKey(name) ? new Maybe<String>(properties.getProperty(name)) : Maybe.noString;
    }
}
