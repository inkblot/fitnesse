package fitnesse.slim;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import fitnesse.wikitext.parser.Context;
import fitnesse.wikitext.parser.VariableFinder;
import fitnesse.wikitext.parser.VariableSource;
import org.movealong.junit.BaseInjectedTestCase;
import util.UtilModule;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/13/11
 * Time: 8:15 PM
 */
public abstract class SlimBaseTestCase extends BaseInjectedTestCase {
    @Override
    protected final Module[] getBaseModules() {
        return new Module[] {
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(VariableSource.class).annotatedWith(Context.class).toInstance(new VariableFinder());
                    }
                },
                new UtilModule()
        };
    }
}
