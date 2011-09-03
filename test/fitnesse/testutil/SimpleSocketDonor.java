// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fitnesse.http.MockSocket;
import fitnesse.responders.run.SocketDonor;

import java.net.Socket;

public class SimpleSocketDonor implements SocketDonor {
    public Socket socket;
    public boolean finished = false;

    public SimpleSocketDonor() {
        socket = new MockSocket("SimpleSocketDonor");
    }

    public SimpleSocketDonor(Socket socket) {
        this.socket = socket;
    }

    public Socket donateSocket() {
        return socket;
    }

    public void finishedWithSocket() throws Exception {
        finished = true;
        socket.close();
    }
}
