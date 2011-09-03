// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.FitnesseBaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertHasRegexp;

public class CommandRunnerTest extends FitnesseBaseTestCase {

    @Test
    public void testBasics() throws Exception {
        String classPath = classPath();
        CommandRunner runner = new CommandRunner("java -cp " + classPath + " fitnesse.testutil.Echo", "echo this!");
        runner.run();
        assertHasRegexp("echo this!", runner.getOutput());
        assertEquals("", runner.getError());
        assertEquals(false, runner.hasExceptions());
        assertEquals(0, runner.getExitCode());
    }

    @Test
    public void testClassNotFound() throws Exception {
        CommandRunner runner = new CommandRunner("java BadClass", null);
        runner.run();
        assertHasRegexp("java.lang.NoClassDefFoundError", runner.getError());
        assertEquals("", runner.getOutput());
        assertTrue(0 != runner.getExitCode());
    }
}
