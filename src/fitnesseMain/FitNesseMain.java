package fitnesseMain;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.PluginsClassLoader;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.responders.run.formatters.TestTextFormatter;
import fitnesse.updates.UpdaterImplementation;
import fitnesse.wiki.PageVersionPruner;
import fitnesse.wiki.WikiPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CommandLine;
import util.CommandLineParseException;

import java.io.File;
import java.util.Arrays;

public class FitNesseMain {
    private static final Logger logger = LoggerFactory.getLogger(FitNesseMain.class);
    private static String extraOutput;
    public static boolean dontExitAfterSingleCommand;

    @Inject
    @Named("inject")
    public static boolean inject = true;

    public static void main(String[] args) throws Exception {
        Arguments arguments = parseCommandLine(args);
        if (arguments != null) {
            launchFitNesse(arguments);
        } else {
            printUsage();
            System.exit(1);
        }
    }

    public static void launchFitNesse(Arguments arguments) throws Exception {
        if (inject)
            Guice.createInjector(new FitNesseModule());
        loadPlugins();
        FitNesseContext context = loadContext(arguments);
        Updater updater = null;
        if (!arguments.isOmittingUpdates())
            updater = new UpdaterImplementation(context);
        PageVersionPruner.daysTillVersionsExpire = arguments
                .getDaysTillVersionsExpire();
        FitNesse fitnesse = new FitNesse(context, updater);
        updateAndLaunch(arguments, context, fitnesse);
    }

    private static void loadPlugins() throws Exception {
        new PluginsClassLoader().addPluginsToClassLoader();
    }

    static void updateAndLaunch(Arguments arguments, FitNesseContext context,
                                FitNesse fitnesse) throws Exception {
        if (!arguments.isOmittingUpdates())
            fitnesse.applyUpdates();
        if (!arguments.isInstallOnly()) {
            runFitNesse(arguments, context, fitnesse);
        }
    }

    private static void runFitNesse(Arguments arguments, FitNesseContext context, FitNesse fitnesse) throws Exception {
        boolean started = fitnesse.start();
        if (started) {
            printStartMessage(arguments, context);
            if (arguments.getCommand() != null) {
                executeSingleCommand(arguments, fitnesse, context);
            }
        }
    }

    private static void executeSingleCommand(Arguments arguments, FitNesse fitnesse, FitNesseContext context) throws Exception {
        context.doNotChunk = true;
        TestTextFormatter.finalErrorCount = 0;
        System.out.println("Executing command: " + arguments.getCommand());
        System.out.println("-----Command Output-----");
        fitnesse.executeSingleCommand(arguments.getCommand(), System.out);
        System.out.println("-----Command Complete-----");
        fitnesse.stop();
        if (shouldExitAfterSingleCommand())
            System.exit(TestTextFormatter.finalErrorCount);
    }

    private static boolean shouldExitAfterSingleCommand() {
        return !dontExitAfterSingleCommand;
    }

    private static FitNesseContext loadContext(Arguments arguments)
            throws Exception {
        WikiPageFactory wikiPageFactory = new WikiPageFactory();
        ComponentFactory componentFactory = new ComponentFactory(arguments.getRootPath());
        WikiPage root = wikiPageFactory.makeRootPage(arguments.getRootPath(),
                arguments.getRootDirectory(), componentFactory);
        FitNesseContext context = new FitNesseContext(root, arguments.getRootPath());
        context.port = arguments.getPort();
        String defaultNewPageContent = componentFactory
                .getProperty(ComponentFactory.DEFAULT_NEWPAGE_CONTENT);
        if (defaultNewPageContent != null)
            context.defaultNewPageContent = defaultNewPageContent;
        context.authenticator = makeAuthenticator(arguments.getUserpass(),
                componentFactory);
        context.htmlPageFactory = componentFactory
                .getHtmlPageFactory(new HtmlPageFactory());

        extraOutput = componentFactory.loadPlugins(context.responderFactory,
                wikiPageFactory);
        extraOutput += componentFactory.loadWikiPage(wikiPageFactory);
        extraOutput += componentFactory.loadResponders(context.responderFactory);
        extraOutput += componentFactory.loadSymbolTypes();
        extraOutput += componentFactory.loadContentFilter();


        WikiImportTestEventListener.register();

        return context;
    }

    public static Arguments parseCommandLine(String[] args) {
        Arguments arguments = null;
        try {
            CommandLine commandLine = new CommandLine("[-p port][-d dir][-r root][-l logDir][-e days][-o][-i][-a userpass][-c command]", args);
            arguments = new Arguments();
            if (commandLine.hasOption("p"))
                arguments.setPort(commandLine.getOptionArgument("p", "port"));
            if (commandLine.hasOption("d"))
                arguments.setRootPath(commandLine.getOptionArgument("d", "dir"));
            if (commandLine.hasOption("r"))
                arguments.setRootDirectory(commandLine.getOptionArgument("r", "root"));
            if (commandLine.hasOption("l"))
                arguments.setLogDirectory(commandLine.getOptionArgument("l", "logDir"));
            if (commandLine.hasOption("e"))
                arguments.setDaysTillVersionsExpire(commandLine.getOptionArgument("e", "days"));
            if (commandLine.hasOption("a"))
                arguments.setUserpass(commandLine.getOptionArgument("a", "userpass"));
            if (commandLine.hasOption("c"))
                arguments.setCommand(commandLine.getOptionArgument("c", "command"));
            arguments.setOmitUpdates(commandLine.hasOption("o"));
            arguments.setInstallOnly(commandLine.hasOption("i"));
        } catch (CommandLineParseException e) {
            logger.error("Invalid command line: ", Arrays.asList(args));
        }
        return arguments;
    }

    public static Authenticator makeAuthenticator(String authenticationParameter,
                                                  ComponentFactory componentFactory) throws Exception {
        Authenticator authenticator = new PromiscuousAuthenticator();
        if (authenticationParameter != null) {
            if (new File(authenticationParameter).exists())
                authenticator = new MultiUserAuthenticator(authenticationParameter);
            else {
                String[] values = authenticationParameter.split(":");
                authenticator = new OneUserAuthenticator(values[0], values[1]);
            }
        }

        return componentFactory.getAuthenticator(authenticator);
    }

    private static void printUsage() {
        System.err.println("Usage: java -jar fitnesse.jar [-pdrleoa]");
        System.err.println("\t-p <port number> {" + FitNesseContext.DEFAULT_PORT + "}");
        System.err.println("\t-d <working directory> {" + FitNesseContext.DEFAULT_PATH
                + "}");
        System.err.println("\t-r <page root directory> {" + FitNesseContext.DEFAULT_ROOT
                + "}");
        System.err.println("\t-l <log directory> {no logging}");
        System.err.println("\t-e <days> {" + FitNesseContext.DEFAULT_VERSION_DAYS
                + "} Number of days before page versions expire");
        System.err.println("\t-o omit updates");
        System.err
                .println("\t-a {user:pwd | user-file-name} enable authentication.");
        System.err.println("\t-i Install only, then quit.");
        System.err.println("\t-c <command> execute single command.");
    }

    private static void printStartMessage(Arguments args, FitNesseContext context) {
        System.out.println("FitNesse (" + FitNesse.VERSION + ") Started...");
        System.out.print(context.toString());
        System.out.println("\tpage version expiration set to "
                + args.getDaysTillVersionsExpire() + " days.");
        if (extraOutput != null)
            System.out.print(extraOutput);
    }
}
