// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.IOException;

public class HttpException extends IOException {
    private static final long serialVersionUID = 1L;

    public HttpException(String message) {
        super(message);
    }

}
