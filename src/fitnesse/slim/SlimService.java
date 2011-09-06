// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.socketservice.SocketService;
import util.CommandLine;
import util.CommandLineParseException;

import java.io.IOException;

public class SlimService extends SocketService {
    public static SlimService service = null;

    @Inject
    @Named("inject")
    public static boolean inject = true;

    public static void main(String[] argv) throws IOException {
        Arguments args = parseCommandLine(argv);
        if (args != null) {
            if (inject)
                Guice.createInjector(new FitNesseModule());
            service = new SlimService(args.getPort(), new JavaSlimFactory().getSlimServer(args.isVerbose()));
        }
    }

    private static Arguments parseCommandLine(String[] argv) {
        try {
            return new Arguments(argv);
        } catch (CommandLineParseException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private SlimService(int port, SlimServer slimServer) throws IOException {
        super(port, slimServer);
        start();
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
