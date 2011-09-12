// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import fitnesse.*;
import fitnesse.testutil.FitNesseUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.*;

public class FitNesseMainTest extends FitnesseBaseTestCase {

    private FitNesseContext context;

    @Before
    public void setUp() throws Exception {
        context = makeContext("testFitnesseRoot");
    }

    @Test
    public void testInstallOnly() throws Exception {
        FitNesseMain.Arguments args = new FitNesseMain.Arguments();
        args.setInstallOnly(true);
        FitNesse fitnesse = mock(FitNesse.class);
        FitNesseMain.updateAndLaunch(args, context, fitnesse, null);
        verify(fitnesse, never()).start();
        verify(fitnesse, times(1)).applyUpdates();
    }

    @Test
    public void commandArgCallsExecuteSingleCommand() throws Exception {
        FitNesseMain.Arguments args = new FitNesseMain.Arguments();
        args.setCommand("command");
        FitNesse fitnesse = mock(FitNesse.class);
        when(fitnesse.start()).thenReturn(true);
        FitNesseMain.updateAndLaunch(args, context, fitnesse, null);
        verify(fitnesse, times(1)).applyUpdates();
        verify(fitnesse, times(1)).start();
        verify(fitnesse, times(1)).executeSingleCommand("command", System.out);
        verify(fitnesse, times(1)).stop();
    }

    @Test
    public void testDirCreations() throws Exception {
        context.port = 80;
        new FitNesse(context);

        assertTrue(new File(context.rootPagePath).exists());
        assertTrue(new File(context.rootPagePath, "files").exists());
    }

    @Test
    public void testContextFitNesseGetSet() throws Exception {
        FitNesse fitnesse = new FitNesse(context, false);
        assertSame(fitnesse, context.fitnesse);
    }

    @Test
    public void testIsRunning() throws Exception {
        context.port = FitNesseUtil.DEFAULT_PORT;
        FitNesse fitnesse = new FitNesse(context, false);

        assertFalse(fitnesse.isRunning());

        fitnesse.start();
        assertTrue(fitnesse.isRunning());

        fitnesse.stop();
        assertFalse(fitnesse.isRunning());
    }

    @Test
    public void testShouldInitializeFitNesseContext() {
        context.port = FitNesseUtil.DEFAULT_PORT;
        new FitNesse(context, false);
        assertNotNull(FitNesseContext.globalContext);
    }

    @Test
    public void canRunSingleCommand() throws Exception {
        String response = runFitnesseMainWith("-o", "-c", "/root");
        assertThat(response, containsString("Command Output"));
    }

    @Test
    public void canRunSingleCommandWithAuthentication() throws Exception {
        String response = runFitnesseMainWith("-o", "-a", "user:pwd", "-c", "user:pwd:/FitNesse.ReadProtectedPage");
        assertThat(response, containsString("HTTP/1.1 200 OK"));
    }

    private String runFitnesseMainWith(String... args) throws Exception {
        PrintStream out = System.out;
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputBytes));
        FitNesseMain.launchFitNesse(new FitNesseMain.Arguments(args), injector);
        System.setOut(out);
        return outputBytes.toString();
    }
}
