package fitnesse.junit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import fitnesse.FitNesseModule;
import fitnesse.responders.run.JavaFormatter;
import junit.framework.AssertionFailedError;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class FitNesseSuite extends ParentRunner<String> {

    private Injector injector;

    /**
     * The <code>Name</code> annotation specifies the name of the Fitnesse suite
     * to be run, e.g.: MySuite.MySubSuite
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Name {
        public String value();
    }

    /**
     * The <code>DebugMode</code> annotation specifies whether the test is run
     * with the REST debug option. Default is true
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface DebugMode {
        public boolean value();
    }

    /**
     * The <code>SuiteFilter</code> annotation specifies the suite filter of the Fitnesse suite
     * to be run, e.g.: fasttests
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface SuiteFilter {
        public String value();
    }

    /**
     * The <code>FitnesseDir</code> annotation specifies the absolute or relative
     * path to the directory in which FitNesseRoot can be found
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface FitnesseDir {
        public String value();
    }

    /**
     * The <code>OutputDir</code> annotation specifies where the html reports of
     * run suites and tests will be found after running them. You can either
     * specify a relative or absolute path directly, e.g.:
     * <code>@OutputDir("/tmp/trinidad-results")</code>, or you can specify a
     * system property the content of which will be taken as base dir and
     * optionally give a path extension, e.g.:
     * <code>@OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "trinidad-results")</code>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface OutputDir {
        public String value() default "";

        public String systemProperty() default "";

        public String pathExtension() default "";
    }

    /**
     * The <code>Port</code> annotation specifies the port used by the FitNesse
     * server. Default is the standard FitNesse port.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Port {
        public int value() default 0;

        public String systemProperty() default "";
    }

    private final Class<?> suiteClass;
    private final String suiteName;
    private String fitNesseDir;
    private String outputDir;
    private String suiteFilter;
    private boolean debugMode = false;
    private int port = 0;

    public FitNesseSuite(Class<?> suiteClass, RunnerBuilder builder) throws InitializationError {
        super(suiteClass);
        this.suiteClass = suiteClass;
        this.suiteName = getSuiteName(suiteClass);
        this.fitNesseDir = getFitnesseDir(suiteClass);
        this.outputDir = getOutputDir(suiteClass);
        this.suiteFilter = getSuiteFilter(suiteClass);
        this.debugMode = useDebugMode(suiteClass);
        this.port = getPort(suiteClass);
        this.injector = Guice.createInjector(new FitNesseModule(new Properties(), null));
    }

    @Override
    protected Description describeChild(String child) {
        return Description.createTestDescription(suiteClass, child);
    }

    @Override
    protected List<String> getChildren() {
        return JavaFormatter.getInstance(suiteName).getTestsExecuted();
    }

    static String getFitnesseDir(Class<?> klass)
            throws InitializationError {
        FitnesseDir fitnesseDirAnnotation = klass.getAnnotation(FitnesseDir.class);
        if (fitnesseDirAnnotation == null) {
            throw new InitializationError("There must be a @FitnesseDir annotation");
        }
        return fitnesseDirAnnotation.value();
    }

    static String getSuiteFilter(Class<?> klass) {
        SuiteFilter suiteFilterAnnotation = klass.getAnnotation(SuiteFilter.class);
        if (suiteFilterAnnotation == null) {
            return null;
        }
        return suiteFilterAnnotation.value();
    }

    static String getSuiteName(Class<?> klass) throws InitializationError {
        Name nameAnnotation = klass.getAnnotation(Name.class);
        if (nameAnnotation == null) {
            throw new InitializationError("There must be a @Name annotation");
        }
        return nameAnnotation.value();
    }

    static String getOutputDir(Class<?> klass) throws InitializationError {
        OutputDir outputDirAnnotation = klass.getAnnotation(OutputDir.class);
        if (outputDirAnnotation == null) {
            throw new InitializationError("There must be a @OutputDir annotation");
        }
        if (isNotEmpty(outputDirAnnotation.value())) {
            return outputDirAnnotation.value();
        }
        if (isNotEmpty(outputDirAnnotation.systemProperty())) {
            String baseDir = System.getProperty(outputDirAnnotation.systemProperty());
            File outputDir = new File(baseDir, outputDirAnnotation.pathExtension());
            return outputDir.getAbsolutePath();
        }
        throw new InitializationError(
                "In annotation @OutputDir you have to specify either 'value' or 'systemProperty'");
    }

    public static boolean useDebugMode(Class<?> klass) {
        DebugMode debugModeAnnotation = klass.getAnnotation(DebugMode.class);
        return null == debugModeAnnotation || debugModeAnnotation.value();
    }

    public static int getPort(Class<?> klass) {
        Port portAnnotation = klass.getAnnotation(Port.class);
        if (null == portAnnotation) {
            return 0;
        }
        int lport = portAnnotation.value();
        if (isNotEmpty(portAnnotation.systemProperty())) {
            lport = Integer.getInteger(portAnnotation.systemProperty(), lport);
        }
        return lport;
    }

    @Override
    public void run(final RunNotifier notifier) {
        JUnitHelper helper = createJUnitHelper(notifier);
        try {
            helper.assertSuitePasses(suiteName, suiteFilter);
        } catch (AssertionFailedError e) {
            notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
        } catch (Exception e) {
            notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
        }
    }

    @Override
    protected void runChild(String test, RunNotifier notifier) {
        JUnitHelper helper = createJUnitHelper(notifier);
        try {
            helper.assertTestPasses(suiteName);
        } catch (AssertionFailedError e) {
            notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
        } catch (Exception e) {
            notifier.fireTestFailure(new Failure(Description.createSuiteDescription(suiteClass), e));
        }
    }

    private JUnitHelper createJUnitHelper(final RunNotifier notifier) {
        JUnitHelper jUnitHelper = new JUnitHelper(this.fitNesseDir, this.outputDir, new JUnitRunNotifierResultsListener(notifier, suiteClass), injector);
        jUnitHelper.setDebugMode(debugMode);
        jUnitHelper.setPort(port);
        return jUnitHelper;
    }

}

