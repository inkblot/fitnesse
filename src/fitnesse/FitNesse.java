// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import fitnesse.http.*;
import fitnesse.wiki.WikiModule;
import util.socketservice.SocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.net.BindException;

@Singleton
public class FitNesse {

    private static final Logger logger = LoggerFactory.getLogger(FitNesse.class);
    public static final FitNesseVersion VERSION = new FitNesseVersion();

    private final Injector injector;
    private final String rootPagePath;
    private final Integer port;

    private SocketService theService;

    public static void main(String[] args) throws Exception {
        System.out.println("DEPRECATED:  use java -jar fitnesse.jar or java -cp fitnesse.jar fitnesseMain.FitNesseMain");
        Class<?> mainClass = Class.forName("fitnesseMain.FitNesseMain");
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, new Object[]{args});
    }

    private static void printBadPortMessage(int port) {
        System.err.println("FitNesse cannot be started...");
        System.err.println("Port " + port + " is already in use.");
        System.err.println("Use the -p <port#> command line argument to use a different port.");
    }

    private static void establishDirectory(String path) {
        File filesDir = new File(path);
        if (!filesDir.exists())
            filesDir.mkdir();
    }

    @Inject
    public FitNesse(@Named(WikiModule.ROOT_PAGE_PATH) String rootPagePath, @Named(FitNesseModule.PORT) Integer port, Injector injector) {
        this.injector = injector;
        this.rootPagePath = rootPagePath;
        this.port = port;
        establishRequiredDirectories();
    }

    public boolean start() {
        try {
            if (port > 0) {
                theService = new SocketService(port, new FitNesseServer(injector));
                theService.start();
            }
            return true;
        } catch (BindException e) {
            printBadPortMessage(port);
        } catch (IOException e) {
            logger.error("Could not start FitNesse server", e);
        }
        return false;
    }

    public void stop() throws Exception {
        if (theService != null) {
            theService.close();
            theService = null;
        }
    }

    private void establishRequiredDirectories() {
        establishDirectory(rootPagePath);
        establishDirectory(rootPagePath + "/files");
    }

    public boolean isRunning() {
        return theService != null;
    }

    public void executeSingleCommand(String command, OutputStream out) throws Exception {
        Request request = new MockRequestBuilder(command).build();
        FitNesseExpediter expediter = new FitNesseExpediter(injector, new PipedInputStream(), new PipedOutputStream(new PipedInputStream()));
        Response response = expediter.createGoodResponse(request);
        MockResponseSender sender = new MockResponseSender.OutputStreamSender(out);
        sender.doSending(response);
    }
}
