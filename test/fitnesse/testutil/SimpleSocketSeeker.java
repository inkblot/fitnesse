// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fitnesse.responders.run.SocketDonor;
import fitnesse.responders.run.SocketSeeker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SimpleSocketSeeker implements SocketSeeker {
    public SocketDonor donor;
    public InputStream input;
    public OutputStream output;

    public void acceptSocketFrom(SocketDonor donor) throws IOException {
        this.donor = donor;
        this.input = donor.donateInputStream();
        this.output = donor.donateOutputStream();
    }
}
