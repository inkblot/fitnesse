// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import com.google.inject.Guice;
import fitnesse.FitNesseModule;
import fitnesse.socketservice.SocketService;
import util.CommandLine;
import util.CommandLineParseException;

import java.io.IOException;

public class SlimService {
    public static void main(String[] argv) throws IOException {
        Guice.createInjector(new FitNesseModule());
        startSlimService(argv);
    }

    public static void startSlimService(String[] argv) throws IOException {
        Arguments args = parseCommandLine(argv);
        if (args != null) {
            startSlimService(args.getPort(), args.isVerbose());
        }
    }

    public static void startSlimService(int port, boolean verbose) throws IOException {
        SlimServer slimServer = new JavaSlimFactory().getSlimServer(verbose);
        SocketService service = new SocketService(port, slimServer);
        slimServer.setSocketService(service);
        service.start();
    }

    private static Arguments parseCommandLine(String[] argv) {
        try {
            return new Arguments(argv);
        } catch (CommandLineParseException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static final class Arguments {
        private final boolean verbose;
        private final int port;

        public Arguments(String[] argv) throws CommandLineParseException {
            CommandLine commandLine = new CommandLine("[-v] port", argv);
            verbose = commandLine.hasOption("v");
            port = Integer.parseInt(commandLine.getArgument("port"));
        }

        public boolean isVerbose() {
            return verbose;
        }

        public int getPort() {
            return port;
        }
    }
}
