package fitnesse;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.wiki.VersionsController;
import fitnesse.wikitext.parser.SymbolProviderModule;
import util.UtilModule;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/29/11
 * Time: 11:02 PM
 */
public class FitNesseModule extends AbstractModule {
    private final Properties properties;
    private final String userpass;

    public FitNesseModule(Properties properties, String userpass) {
        this.properties = properties;
        this.userpass = userpass;
    }

    @Override
    protected void configure() {
        bind(Properties.class).annotatedWith(Names.named(FitNesseContextModule.PROPERTIES_FILE)).toInstance(properties);
        GuiceHelper.bindAuthenticator(binder(), properties, userpass);
        GuiceHelper.bindWikiPageClass(binder(), properties);
        GuiceHelper.bindFromProperty(binder(), VersionsController.class, properties);
        GuiceHelper.bindFromProperty(binder(), ContentFilter.class, properties);
        GuiceHelper.bindFromProperty(binder(), HtmlPageFactory.class, properties);
        install(new SymbolProviderModule());
        install(new UtilModule());
    }

}
