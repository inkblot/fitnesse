// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fitnesse.responders.run.SocketDonor;
import fitnesse.responders.run.SocketSeeker;

import java.net.Socket;

public class SimpleSocketSeeker implements SocketSeeker {
    public SocketDonor donor;
    public Socket socket;

    public void acceptSocketFrom(SocketDonor donor) throws Exception {
        this.donor = donor;
        this.socket = donor.donateSocket();
    }
}
