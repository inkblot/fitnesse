// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.socketservice;

import java.io.*;
import java.net.Socket;

public interface SocketServer {
    public void serve(Socket s);

    static class StreamUtility {
        public static PrintStream GetPrintStream(Socket s) throws IOException {
            return GetPrintStream(s.getOutputStream());
        }

        public static PrintStream GetPrintStream(OutputStream output) {
            return new PrintStream(output);
        }

        public static BufferedReader GetBufferedReader(Socket s) throws IOException {
            return GetBufferedReader(s.getInputStream());
        }

        public static BufferedReader GetBufferedReader(InputStream input) {
            return new BufferedReader(new InputStreamReader(input));
        }
    }
}
