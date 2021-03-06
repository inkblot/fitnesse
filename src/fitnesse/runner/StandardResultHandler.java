// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import fitnesse.responders.run.TestSummary;

import java.io.PrintStream;

import static org.apache.commons.lang.StringUtils.isBlank;

//TODO MDM Rename to VerboseResultHandler
public class StandardResultHandler implements ResultHandler {
    private PrintStream output;
    private TestSummary pageCounts = new TestSummary();

    public StandardResultHandler(PrintStream output) {
        this.output = output;
    }

    public void acceptResult(PageResult result) throws Exception {
        TestSummary testSummary = result.testSummary();
        pageCounts.tallyPageCounts(testSummary);
        for (int i = 0; i < testSummary.getRight(); i++)
            output.print(".");
        if (testSummary.getWrong() > 0 || testSummary.getExceptions() > 0) {
            output.println();
            if (testSummary.getWrong() > 0)
                output.println(pageDescription(result) + " has failures");
            if (testSummary.getExceptions() > 0)
                output.println(pageDescription(result) + " has errors");
        }
    }

    private String pageDescription(PageResult result) {
        String description = result.title();
        if (isBlank(description))
            description = "The test";
        return description;
    }

    public void acceptFinalCount(TestSummary testSummary) throws Exception {
        output.println();
        output.println("Test Pages: " + pageCounts);
        output.println("Assertions: " + testSummary);
    }

}
