// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import com.google.inject.Key;
import com.google.inject.name.Names;
import fitnesse.*;
import fitnesse.wiki.WikiModule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.*;

public class FitNesseMainTest extends FitnesseBaseTestCase {

    @Test
    public void commandArgCallsExecuteSingleCommand() throws Exception {
        FitNesseMain.Arguments args = new FitNesseMain.Arguments();
        args.setCommand("command");
        FitNesse fitnesse = mock(FitNesse.class);
        when(fitnesse.start()).thenReturn(true);
        FitNesseMain.updateAndLaunch(args, injector, fitnesse, null);
        verify(fitnesse, times(1)).start();
        verify(fitnesse, times(1)).executeSingleCommand("command", System.out);
        verify(fitnesse, times(1)).stop();
    }

    @Test
    public void testDirCreations() throws Exception {
        injector.getInstance(FitNesse.class);

        String rootPagePath = injector.getInstance(Key.get(String.class, Names.named(WikiModule.ROOT_PAGE_PATH)));

        assertTrue(new File(rootPagePath).exists());
        assertTrue(new File(rootPagePath, "files").exists());
    }

    @Test
    public void testIsRunning() throws Exception {
        FitNesse fitnesse = injector.getInstance(FitNesse.class);

        assertFalse(fitnesse.isRunning());

        fitnesse.start();
        assertTrue(fitnesse.isRunning());

        fitnesse.stop();
        assertFalse(fitnesse.isRunning());
    }

    @Test
    public void canRunSingleCommand() throws Exception {
        String response = runFitnesseMainWith("-c", "/root");
        assertThat(response, containsString("Command Output"));
    }

    @Test
    public void canRunSingleCommandWithAuthentication() throws Exception {
        String response = runFitnesseMainWith("-a", "user:pwd", "-c", "user:pwd:/FitNesse.ReadProtectedPage");
        assertThat(response, containsString("HTTP/1.1 200 OK"));
    }

    private String runFitnesseMainWith(String... args) throws Exception {
        PrintStream out = System.out;
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputBytes));
        FitNesseMain.launchFitNesse(new FitNesseMain.Arguments(args), getProperties());
        System.setOut(out);
        return outputBytes.toString();
    }
}
