// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ResponseSender {
    public void send(byte[] bytes);

    public void close();

    public InputStream getInputStream() throws IOException;

    public OutputStream getOutputStream() throws IOException;
}
