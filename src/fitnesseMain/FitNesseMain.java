package fitnesseMain;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import fitnesse.*;
import fitnesse.components.PluginsClassLoader;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.responders.run.formatters.BaseFormatter;
import fitnesse.wiki.PageVersionPruner;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.parser.SymbolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CommandLine;
import util.CommandLineParseException;
import util.FileUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

public class FitNesseMain {
    private static final Logger logger = LoggerFactory.getLogger(FitNesseMain.class);

    public static void main(String[] argv) throws Exception {
        try {
            Arguments args = new Arguments(argv);
            Injector injector = Guice.createInjector(
                    new FitNesseModule(
                            FileUtil.loadProperties(new File(args.getRootPath(), FitNesseContextModule.PROPERTIES_FILE)),
                            args.getUserpass()));
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

        Properties pluginProperties = injector.getInstance(Key.get(Properties.class, Names.named(FitNesseContextModule.PROPERTIES_FILE)));

        Injector contextInjector = FitNesseContextModule.makeContext(injector, pluginProperties, arguments.getUserpass(), arguments.getRootPath(), arguments.getRootDirectory(), arguments.getPort(), arguments.isOmittingUpdates());

        FitNesseContext context = contextInjector.getInstance(FitNesseContext.class);
        SymbolProvider wikiSymbols = contextInjector.getInstance(Key.get(SymbolProvider.class, Names.named(SymbolProvider.WIKI_PARSING)));
        ResponderFactory responderFactory = contextInjector.getInstance(ResponderFactory.class);
        FitNesse fitnesse = contextInjector.getInstance(FitNesse.class);
        WikiPageFactory wikiPageFactory = contextInjector.getInstance(WikiPageFactory.class);

        String extraOutput = ComponentFactory.loadPlugins(
                responderFactory,
                wikiPageFactory,
                wikiSymbols,
                pluginProperties);
        extraOutput += ComponentFactory.loadResponders(responderFactory, pluginProperties);
        extraOutput += ComponentFactory.loadSymbolTypes(pluginProperties, wikiSymbols);

        WikiImportTestEventListener.register();

        PageVersionPruner.daysTillVersionsExpire = arguments.getDaysTillVersionsExpire();
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
