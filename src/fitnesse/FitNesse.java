// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import fitnesse.http.*;
import fitnesse.socketservice.SocketService;
import fitnesse.http.MockSocket;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.BindException;

@Singleton
public class FitNesse {

    public static final FitNesseVersion VERSION = new FitNesseVersion();

    private final FitNesseContext context;
    private final Updater updater;
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
    public FitNesse(FitNesseContext context, Updater updater, @Named(FitNesseContext.ROOT_PAGE_PATH) String rootPagePath, @Named(FitNesseContext.PORT) Integer port) {
        this.updater = updater;
        this.context = context;
        this.rootPagePath = rootPagePath;
        this.port = port;
        establishRequiredDirectories();
    }

    public boolean start() {
        try {
            if (port > 0) {
                theService = new SocketService(port, new FitNesseServer(context));
                theService.start();
            }
            return true;
        } catch (BindException e) {
            printBadPortMessage(port);
        } catch (Exception e) {
            e.printStackTrace();
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

    public void applyUpdates() throws Exception {
        updater.update();
    }


    public boolean isRunning() {
        return theService != null;
    }

    public void executeSingleCommand(String command, OutputStream out) throws Exception {
        Request request = new MockRequestBuilder(command).build();
        FitNesseExpediter expediter = new FitNesseExpediter(new MockSocket(), context, context.getHtmlPageFactory());
        Response response = expediter.createGoodResponse(request);
        MockResponseSender sender = new MockResponseSender.OutputStreamSender(out);
        sender.doSending(response);
    }
}
