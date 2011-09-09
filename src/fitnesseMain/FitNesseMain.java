package fitnesseMain;

import com.google.inject.Guice;
import com.google.inject.Injector;
import fitnesse.*;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.PluginsClassLoader;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.responders.run.formatters.BaseFormatter;
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

    public static void main(String[] argv) throws Exception {
        Injector injector = Guice.createInjector(new FitNesseModule());
        try {
            Arguments args = new Arguments(argv);
            launchFitNesse(args, injector);
            if (args.getCommand() != null) {
                System.exit(BaseFormatter.finalErrorCount);
            }
        } catch (CommandLineParseException e) {
            logger.error("Invalid command line: ", Arrays.asList(argv));
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
    }

    public static void launchFitNesse(Arguments arguments, Injector injector) throws Exception {
        new PluginsClassLoader().addPluginsToClassLoader();
        WikiPageFactory wikiPageFactory = new WikiPageFactory();
        ComponentFactory componentFactory = new ComponentFactory(arguments.getRootPath());
        WikiPage root = wikiPageFactory.makeRootPage(arguments.getRootPath(), arguments.getRootDirectory(), componentFactory);
        FitNesseContext context = new FitNesseContext(root, arguments.getRootPath());
        context.port = arguments.getPort();
        String defaultNewPageContent = componentFactory.getProperty(ComponentFactory.DEFAULT_NEWPAGE_CONTENT);
        if (defaultNewPageContent != null) {
            context.defaultNewPageContent = defaultNewPageContent;
        }
        context.authenticator = makeAuthenticator(arguments.getUserpass(), componentFactory);
        context.htmlPageFactory = componentFactory.getHtmlPageFactory(new HtmlPageFactory());

        String extraOutput = componentFactory.loadPlugins(context.responderFactory, wikiPageFactory);
        extraOutput += componentFactory.loadWikiPage(wikiPageFactory);
        extraOutput += componentFactory.loadResponders(context.responderFactory);
        extraOutput += componentFactory.loadSymbolTypes();
        extraOutput += componentFactory.loadContentFilter();

        WikiImportTestEventListener.register();

        PageVersionPruner.daysTillVersionsExpire = arguments.getDaysTillVersionsExpire();
        FitNesse fitnesse = new FitNesse(context, !arguments.isOmittingUpdates() ? new UpdaterImplementation(context) : null);
        updateAndLaunch(arguments, context, fitnesse, extraOutput);
    }

    static void updateAndLaunch(Arguments arguments, FitNesseContext context,
                                FitNesse fitnesse, String extraOutput) throws Exception {
        if (!arguments.isOmittingUpdates()) {
            fitnesse.applyUpdates();
        }
        if (!arguments.isInstallOnly()) {
            if (fitnesse.start()) {
                System.out.println("FitNesse (" + FitNesse.VERSION + ") Started...");
                System.out.print(context.toString());
                System.out.println("\tpage version expiration set to "
                        + arguments.getDaysTillVersionsExpire() + " days.");
                if (extraOutput != null)
                    System.out.print(extraOutput);
                if (arguments.getCommand() != null) {
                    context.doNotChunk = true;
                    BaseFormatter.finalErrorCount = 0;
                    System.out.println("Executing command: " + arguments.getCommand());
                    System.out.println("-----Command Output-----");
                    fitnesse.executeSingleCommand(arguments.getCommand(), System.out);
                    System.out.println("-----Command Complete-----");
                    fitnesse.stop();
                }
            }
        }
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

    public static class Arguments {
        private String rootPath = FitNesseContext.DEFAULT_PATH;
        private int port = -1;
        private String rootDirectory = FitNesseContext.DEFAULT_ROOT;
        private String logDirectory;
        private boolean omitUpdate = false;
        private int daysTillVersionsExpire = FitNesseContext.DEFAULT_VERSION_DAYS;
        private String userpass;
        private boolean installOnly;
        private String command = null;

        public Arguments(String... argv) throws CommandLineParseException {
            CommandLine commandLine = new CommandLine("[-p port][-d dir][-r root][-l logDir][-e days][-o][-i][-a userpass][-c command]", argv);
            if (commandLine.hasOption("p"))
                this.setPort(commandLine.getOptionArgument("p", "port"));
            if (commandLine.hasOption("d"))
                this.setRootPath(commandLine.getOptionArgument("d", "dir"));
            if (commandLine.hasOption("r"))
                this.setRootDirectory(commandLine.getOptionArgument("r", "root"));
            if (commandLine.hasOption("l"))
                this.setLogDirectory(commandLine.getOptionArgument("l", "logDir"));
            if (commandLine.hasOption("e"))
                this.setDaysTillVersionsExpire(commandLine.getOptionArgument("e", "days"));
            if (commandLine.hasOption("a"))
                this.setUserpass(commandLine.getOptionArgument("a", "userpass"));
            if (commandLine.hasOption("c"))
                this.setCommand(commandLine.getOptionArgument("c", "command"));
            this.setOmitUpdates(commandLine.hasOption("o"));
            this.setInstallOnly(commandLine.hasOption("i"));
        }

        public String getRootPath() {
            return rootPath;
        }

        public void setRootPath(String rootPath) {
            this.rootPath = rootPath;
        }

        public int getPort() {
            return port == -1 ? getDefaultPort() : port;
        }

        private int getDefaultPort() {
            return command == null ? FitNesseContext.DEFAULT_PORT : FitNesseContext.DEFAULT_COMMAND_PORT;
        }

        public void setPort(String port) {
            this.port = Integer.parseInt(port);
        }

        public String getRootDirectory() {
            return rootDirectory;
        }

        public void setRootDirectory(String rootDirectory) {
            this.rootDirectory = rootDirectory;
        }

        public String getLogDirectory() {
            return logDirectory;
        }

        public void setLogDirectory(String logDirectory) {
            this.logDirectory = logDirectory;
        }

        public void setOmitUpdates(boolean omitUpdates) {
            this.omitUpdate = omitUpdates;
        }

        public boolean isOmittingUpdates() {
            return omitUpdate;
        }

        public void setUserpass(String userpass) {
            this.userpass = userpass;
        }

        public String getUserpass() {
            if (userpass == null || userpass.length() == 0)
                return null;
            else
                return userpass;
        }

        public int getDaysTillVersionsExpire() {
            return daysTillVersionsExpire;
        }

        public void setDaysTillVersionsExpire(String daysTillVersionsExpire) {
            this.daysTillVersionsExpire = Integer.parseInt(daysTillVersionsExpire);
        }

        public boolean isInstallOnly() {
            return installOnly;
        }

        public void setInstallOnly(boolean installOnly) {
            this.installOnly = installOnly;
        }

        public String getCommand() {
            if (command == null)
                return null;
            else
                return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }
    }
}
