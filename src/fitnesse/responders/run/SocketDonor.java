// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SocketDonor {
    public InputStream donateInputStream() throws IOException;

    public OutputStream donateOutputStream() throws IOException;

    public void finishedWithSocket();
}
