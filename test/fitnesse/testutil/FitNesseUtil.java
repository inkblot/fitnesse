// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import com.google.inject.Inject;
import fitnesse.FitNesse;

import static org.junit.Assert.assertFalse;

public class FitNesseUtil {
    public static final int DEFAULT_PORT = 1999;
    public static final String URL = "http://localhost:" + FitNesseUtil.DEFAULT_PORT + "/";

    private final FitNesse fitNesse;
    private boolean running;

    @Inject
    public FitNesseUtil(FitNesse fitNesse) {
        this.fitNesse = fitNesse;
        running = false;
    }

    public synchronized void startFitnesse() {
        assertFalse(running);
        fitNesse.start();
        running = true;
    }

    public synchronized void stopFitnesse() throws Exception {
        if (running) {
            fitNesse.stop();
            running = false;
        }
    }

}
