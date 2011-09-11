package fitnesse;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import fitnesseMain.FitNesseMain;
import util.FileUtil;
import util.UtilModule;

import java.io.File;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/29/11
 * Time: 11:02 PM
 */
public class FitNesseModule extends AbstractModule {
    private final FitNesseMain.Arguments args;

    public FitNesseModule(FitNesseMain.Arguments args) {
        this.args = args;
    }

    @Override
    protected void configure() {
        File properties = new File(args.getRootPath(), ComponentFactory.PROPERTIES_FILE);
        bind(Properties.class).annotatedWith(Names.named("fitnesse.properties")).toInstance(FileUtil.loadProperties(properties));
        install(new UtilModule());
    }
}
