package fitnesse.slim;

import com.google.inject.Module;
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
        return new Module[] {new UtilModule()};
    }
}
