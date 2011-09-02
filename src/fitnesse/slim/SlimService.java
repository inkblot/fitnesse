// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.socketservice.SocketService;
import util.CommandLine;

import java.util.Arrays;

public class SlimService extends SocketService {
    public static SlimService instance = null;
    public static boolean verbose;
    public static int port;

    @Inject
    @Named("inject")
    public static boolean inject = true;

    public static void main(String[] args) throws Exception {
        if (parseCommandLine(args)) {
            startWithFactory(args, new JavaSlimFactory());
        } else {
            parseCommandLineFailed(args);
        }
    }

    protected static void parseCommandLineFailed(String[] args) {
        System.err.println("Invalid command line arguments:" + Arrays.asList(args));
    }

    protected static void startWithFactory(String[] args, SlimFactory slimFactory) throws Exception {
        if (inject)
            Guice.createInjector(new FitNesseModule());
        new SlimService(port, slimFactory.getSlimServer(verbose));
    }

    protected static boolean parseCommandLine(String[] args) {
        CommandLine commandLine = new CommandLine("[-v] port");
        if (commandLine.parse(args)) {
            verbose = commandLine.hasOption("v");
            String portString = commandLine.getArgument("port");
            port = Integer.parseInt(portString);
            return true;
        }
        return false;
    }

    public SlimService(int port, SlimServer slimServer) throws Exception {
        super(port, slimServer);
        instance = this;
    }
}
