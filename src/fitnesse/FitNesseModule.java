package fitnesse;

import com.google.inject.AbstractModule;
import util.UtilModule;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/29/11
 * Time: 11:02 PM
 */
public class FitNesseModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new UtilModule());
    }
}
