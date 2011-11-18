// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import util.socketservice.SocketServer;
import util.socketservice.SocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ImpossibleException;
import util.StreamReader;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class SlimServer implements SocketServer {
    private transient final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String EXCEPTION_TAG = "__EXCEPTION__:";
    public static final String EXCEPTION_STOP_TEST_TAG = "__EXCEPTION__:ABORT_SLIM_TEST:";
    private StreamReader reader;
    private BufferedWriter writer;
    private ListExecutor executor;
    private boolean verbose;
    private SlimFactory slimFactory;
    private SocketService socketService;

    public SlimServer(boolean verbose, SlimFactory slimFactory) {
        this.verbose = verbose;
        this.slimFactory = slimFactory;
    }

    @Override
    public void serve(Socket s) {
        try {
            tryProcessInstructions(s.getInputStream(), s.getOutputStream());
        } catch (IOException e) {
            logger.error("Could not process slim instructions", e);
        } finally {
            slimFactory.stop();
            close();
            closeEnclosingServiceInSeperateThread();
        }
    }

    private void tryProcessInstructions(InputStream input, OutputStream output) throws IOException {
        initialize(input, output);
        boolean more = true;
        while (more)
            more = processOneSetOfInstructions();
    }

    private void initialize(InputStream input, OutputStream output) throws IOException {
        executor = slimFactory.getListExecutor(verbose);
        reader = new StreamReader(input);
        try {
            writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
        writer.write(String.format("Slim -- V%s\n", SlimVersion.VERSION));
        writer.flush();
    }

    private boolean processOneSetOfInstructions() throws IOException {
        String instructions = getInstructionsFromClient();
        return instructions == null || processTheInstructions(instructions);
    }

    private String getInstructionsFromClient() throws IOException {
        int instructionLength = Integer.parseInt(reader.read(6));
        reader.read(1);
        return reader.read(instructionLength);
    }

    private boolean processTheInstructions(String instructions) throws IOException {
        if (instructions.equalsIgnoreCase("bye")) {
            return false;
        } else {
            List<Object> results = executeInstructions(instructions);
            sendResultsToClient(results);
            return true;
        }
    }

    private List<Object> executeInstructions(String instructions) {
        List<Object> statements = ListDeserializer.deserialize(instructions);
        return executor.execute(statements);
    }

    private void sendResultsToClient(List<Object> results) throws IOException {
        String resultString = ListSerializer.serialize(results);
        try {
            writer.write(String.format("%06d:%s", resultString.getBytes("UTF-8").length, resultString));
        } catch (IOException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
        writer.flush();
    }

    private void close() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            // ignore
        }
    }

    private void closeEnclosingServiceInSeperateThread() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getSocketService().close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }).start();
    }

    public SocketService getSocketService() {
        return socketService;
    }

    public void setSocketService(SocketService socketService) {
        this.socketService = socketService;
    }
}
