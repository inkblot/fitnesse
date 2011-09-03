// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fitnesse.http.MockSocket;
import fitnesse.responders.run.SocketDonor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    @Override
    public InputStream donateInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream donateOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public void finishedWithSocket() {
        finished = true;
        IOUtils.closeQuietly(socket);
    }
}
