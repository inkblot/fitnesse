// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fit.FitProtocol;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SocketCatchingResponder implements Responder, SocketDonor, ResponsePuppeteer {
    private int ticketNumber;
    private SocketDealer dealer;
    private ResponseSender sender;
    private PuppetResponse response;
    private InputStream input;
    private OutputStream output;

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        dealer = context.socketDealer;
        ticketNumber = Integer.parseInt(request.getInput("ticket").toString());
        response = new PuppetResponse(this);
        return response;
    }

    public void readyToSend(ResponseSender sender) throws Exception {
        input = sender.getInputStream();
        output = sender.getOutputStream();
        this.sender = sender;
        if (dealer.isWaiting(ticketNumber))
            dealer.dealSocketTo(ticketNumber, this);
        else {
            String errorMessage = "There are no clients waiting for a socket with ticketNumber " + ticketNumber;
            FitProtocol.writeData(errorMessage, output);
            response.setStatus(404);
            sender.close();
        }
    }

    @Override
    public InputStream donateInputStream() throws IOException {
        return input;
    }

    @Override
    public OutputStream donateOutputStream() throws IOException {
        return output;
    }

    public void finishedWithSocket() {
        sender.close();
    }
}
