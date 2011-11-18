// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import com.google.inject.Injector;
import util.socketservice.SocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class FitNesseServer implements SocketServer {
    private static final Logger logger = LoggerFactory.getLogger(FitNesseServer.class);
    private final Injector injector;

    public FitNesseServer(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void serve(Socket s) {
        serve(s, 10000);
    }

    public void serve(Socket s, long requestTimeout) {
        try {
            FitNesseExpediter sender = new FitNesseExpediter(injector, s.getInputStream(), s.getOutputStream(), requestTimeout);
            sender.start();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Could not ");
        }
    }
}
