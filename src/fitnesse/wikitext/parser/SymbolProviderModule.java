package fitnesse.wikitext.parser;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/18/11
 * Time: 7:20 PM
 */
public class SymbolProviderModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SymbolProvider.class).annotatedWith(Names.named(SymbolProvider.ALIAS_LINK)).toInstance(SymbolProvider.aliasLinkProvider);
        bind(SymbolProvider.class).annotatedWith(Names.named(SymbolProvider.LINK_TARGET)).toInstance(SymbolProvider.linkTargetProvider);
        bind(SymbolProvider.class).annotatedWith(Names.named(SymbolProvider.LITERAL_TABLE)).toInstance(SymbolProvider.literalTableProvider);
        bind(SymbolProvider.class).annotatedWith(Names.named(SymbolProvider.PATH_RULE)).toInstance(SymbolProvider.pathRuleProvider);
        bind(SymbolProvider.class).annotatedWith(Names.named(SymbolProvider.REFACTORING)).toInstance(SymbolProvider.refactoringProvider);
        bind(SymbolProvider.class).annotatedWith(Names.named(SymbolProvider.WIKI_PARSING)).toInstance(SymbolProvider.wikiParsingProvider);
    }
}
