// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import fit.exception.FitParseException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.io.*;
import java.net.Socket;

public class FitServer {
    private static final Logger logger = LoggerFactory.getLogger(FitServer.class);

    private final Counts counts = new Counts();
    private OutputStream output;
    private StreamReader input;
    private final String host;
    private final int port;
    private final int socketToken;

    private Socket socket;
    private final boolean sentinel;

    public FitServer(String host, int port, int socketToken, boolean sentinel) {
        this.host = host;
        this.port = port;
        this.socketToken = socketToken;
        this.sentinel = sentinel;
    }

    public static void main(String argv[]) throws IOException {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new UtilModule());
            }
        });
        Counts counts = runFitServer(argv);
        System.exit(exitCode(counts));
    }

    public static Counts runFitServer(String[] argv) throws IOException {
        FitServer fitServer = FitServer.create(argv);
        fitServer.run();
        return fitServer.counts;
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
                    logger.info("processing document of size: " + size);
                    String document = FitProtocol.readDocument(input, size);
                    //TODO MDM if the page name was always the first line of the body, it could be printed here.
                    Parse tables = new Parse(document);
                    fixture.doTables(tables);
                    logger.info("results: " + fixture.counts());
                    counts.tally(fixture.counts);
                } catch (FitParseException e) {
                    exception(fixture, e);
                }
                fixture = newFixture(fixtureListener);
            }
            logger.info("completion signal recieved");
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
        try {
            CommandLine commandLine = new CommandLine("[-s] host port socketToken", argv);
            return new FitServer(
                    commandLine.getArgument("host"),
                    Integer.parseInt(commandLine.getArgument("port")),
                    Integer.parseInt(commandLine.getArgument("socketToken")),
                    commandLine.hasOption("s"));
        } catch (CommandLineParseException e) {
            usage();
            throw new ImpossibleException("usage exits");
        }
    }

    private static void usage() {
        System.out.println("usage: java fit.FitServer host port socketTicket");
        System.out.println("\t-v\tverbose");
        System.exit(-1);
    }

    private void exception(Fixture fixture, Exception e) {
        logger.error("Exception occurred!", e);
        Parse tables = new Parse("span", "Exception occurred: ", null, null);
        fixture.exception(tables, e);
        counts.exceptions += 1;
        fixture.listener.tableFinished(tables);
        fixture.listener.tablesFinished(counts); //TODO shouldn't this be fixture.counts
    }

    public void exit() {
        logger.info("Exiting");
        logger.info("end results: " + counts.toString());
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
        byte[] bytes;
        try {
            bytes = httpRequest.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
        output.write(bytes);
        output.flush();
        logger.debug("http request sent");
    }

    private String makeHttpRequest() {
        return "GET /?responder=socketCatcher&ticket=" + socketToken + " HTTP/1.1\r\n\r\n";
    }

    private void validateConnection() throws IOException {
        int statusSize = FitProtocol.readSize(input);
        if (statusSize == 0)
            logger.debug("Connection ok");
        else {
            String errorMessage = FitProtocol.readDocument(input, statusSize);
            logger.error("Could not connect to client: " + errorMessage);
            System.exit(-1);
        }
    }

    private static byte[] readTable(Parse table) {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter;
        try {
            streamWriter = new OutputStreamWriter(byteBuffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
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
            } catch (IOException e) {
                logger.error("Could not handle tableFinished event", e);
            }
        }

        public void tablesFinished(Counts count) {
            try {
                FitProtocol.writeCounts(count, output);
            } catch (IOException e) {
                logger.error("Could not handle tablesFinished event", e);
            }
        }
    }
}

