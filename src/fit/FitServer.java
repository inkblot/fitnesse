// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import fit.exception.FitParseException;
import fitnesse.FitNesseModule;
import org.apache.commons.io.IOUtils;
import util.CommandLine;
import util.FileUtil;
import util.StreamReader;

import java.io.*;
import java.net.Socket;

public class FitServer {
    private final Counts counts = new Counts();
    private OutputStream output;
    private StreamReader input;
    private final boolean verbose;
    private final String host;
    private final int port;
    private final int socketToken;

    private Socket socket;
    private final boolean noExit;
    private final boolean sentinel;

    @Inject
    @Named("inject")
    public static boolean inject = true;

    public FitServer(String host, int port, int socketToken, boolean verbose, boolean noExit, boolean sentinel) {
        this.host = host;
        this.port = port;
        this.socketToken = socketToken;
        this.verbose = verbose;
        this.noExit = noExit;
        this.sentinel = sentinel;
    }

    public static void main(String argv[]) throws IOException {
        if (inject)
            Guice.createInjector(new FitNesseModule());
        FitServer fitServer = FitServer.create(argv);
        fitServer.run();
        if (!fitServer.noExit)
            System.exit(exitCode(fitServer.counts));
    }

    private void run() throws IOException {
        File sentinelFile = null;
        if (sentinel) {
            String sentinelName = sentinelName(port);
            sentinelFile = new File(sentinelName);
            sentinelFile.createNewFile();
        }
        establishConnection();
        validateConnection();
        process();
        IOUtils.closeQuietly(socket);
        if (sentinel)
            FileUtil.deleteFile(sentinelFile);
        exit();
    }

    public static String sentinelName(int thePort) {
        return String.format("fitserverSentinel%d", thePort);
    }

    private void process() {
        FixtureListener fixtureListener = new TablePrintingFixtureListener();
        Fixture fixture = newFixture(fixtureListener);
        try {
            int size;
            while ((size = FitProtocol.readSize(input)) != 0) {
                try {
                    print("processing document of size: " + size + "\n");
                    String document = FitProtocol.readDocument(input, size);
                    //TODO MDM if the page name was always the first line of the body, it could be printed here.
                    Parse tables = new Parse(document);
                    fixture.doTables(tables);
                    print("\tresults: " + fixture.counts() + "\n");
                    counts.tally(fixture.counts);
                } catch (FitParseException e) {
                    exception(fixture, e);
                }
                fixture = newFixture(fixtureListener);
            }
            print("completion signal recieved" + "\n");
        } catch (Exception e) {
            exception(fixture, e);
        }
    }

    private static Fixture newFixture(FixtureListener fixtureListener) {
        Fixture fixture = new Fixture();
        fixture.listener = fixtureListener;
        return fixture;
    }

    private static FitServer create(String[] argv) {
        CommandLine commandLine = new CommandLine("[-v][-x][-s] host port socketToken");
        if (commandLine.parse(argv)) {
            return new FitServer(
                    commandLine.getArgument("host"),
                    Integer.parseInt(commandLine.getArgument("port")),
                    Integer.parseInt(commandLine.getArgument("socketToken")),
                    commandLine.hasOption("v"),
                    commandLine.hasOption("x"),
                    commandLine.hasOption("s"));
        } else {
            usage();
            assert false : "Usage exits";
            return null;
        }
    }

    private static void usage() {
        System.out.println("usage: java fit.FitServer [-v] host port socketTicket");
        System.out.println("\t-v\tverbose");
        System.exit(-1);
    }

    private void exception(Fixture fixture, Exception e) {
        print("Exception occurred!" + "\n");
        print("\t" + e.getMessage() + "\n");
        Parse tables = new Parse("span", "Exception occurred: ", null, null);
        fixture.exception(tables, e);
        counts.exceptions += 1;
        fixture.listener.tableFinished(tables);
        fixture.listener.tablesFinished(counts); //TODO shouldn't this be fixture.counts
    }

    public void exit() {
        print("exiting" + "\n");
        print("\tend results: " + counts.toString() + "\n");
    }

    private static int exitCode(Counts counts) {
        return counts.wrong + counts.exceptions;
    }

    private void establishConnection() throws IOException {
        establishConnection(makeHttpRequest());
    }

    private void establishConnection(String httpRequest) throws IOException {
        socket = new Socket(host, port);
        output = socket.getOutputStream();
        input = new StreamReader(socket.getInputStream());
        byte[] bytes = httpRequest.getBytes("UTF-8");
        output.write(bytes);
        output.flush();
        print("http request sent" + "\n");
    }

    private String makeHttpRequest() {
        return "GET /?responder=socketCatcher&ticket=" + socketToken + " HTTP/1.1\r\n\r\n";
    }

    private void validateConnection() throws IOException {
        print("validating connection...");
        int statusSize = FitProtocol.readSize(input);
        if (statusSize == 0)
            print("...ok" + "\n");
        else {
            String errorMessage = FitProtocol.readDocument(input, statusSize);
            print("...failed because: " + errorMessage + "\n");
            System.out.println("An error occurred while connecting to client.");
            System.out.println(errorMessage);
            System.exit(-1);
        }
    }

    private void print(String message) {
        if (verbose)
            System.out.print(message);
    }

    private static byte[] readTable(Parse table) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = new OutputStreamWriter(byteBuffer, "UTF-8");
        PrintWriter writer = new PrintWriter(streamWriter);
        Parse more = table.more;
        table.more = null;
        if (table.trailer == null)
            table.trailer = "";
        table.print(writer);
        table.more = more;
        writer.close();
        return byteBuffer.toByteArray();
    }

    class TablePrintingFixtureListener implements FixtureListener {
        public void tableFinished(Parse table) {
            try {
                byte[] bytes = readTable(table);
                if (bytes.length > 0)
                    FitProtocol.writeData(bytes, output);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void tablesFinished(Counts count) {
            try {
                FitProtocol.writeCounts(count, output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

