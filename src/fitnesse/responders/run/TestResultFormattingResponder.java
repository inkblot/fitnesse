// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fit.Counts;
import fit.FitProtocol;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.InputStreamResponse;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.runner.*;
import util.ImpossibleException;
import util.StreamReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class TestResultFormattingResponder implements Responder {
    public ResultFormatter formatter = new MockResultFormatter();
    public Counts finalCounts;
    private FitNesseContext context;
    private String baseUrl;
    private String rootPath;

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        init(context, request);

        String results = (String) request.getInput("results");
        byte[] bytes;
        try {
            bytes = results.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
        processResults(new ByteArrayInputStream(bytes));

        InputStreamResponse response = new InputStreamResponse();
        response.setBody(formatter.getResultStream(), formatter.getByteCount());

        return response;
    }

    public void init(FitNesseContext context, Request request) throws Exception {
        this.context = context;
        baseUrl = (String) request.getHeader("Host");
        rootPath = request.getResource();
        formatter = makeFormatter(request);
    }

    public void processResults(InputStream input) throws Exception {
        StreamReader reader = new StreamReader(input);
        boolean readingResults = true;
        while (readingResults) {
            int bytesToRead = FitProtocol.readSize(reader);
            if (bytesToRead != 0) {
                String resultString = reader.read(bytesToRead);
                PageResult result = PageResult.parse(resultString);
                formatter.acceptResult(result);
            } else
                readingResults = false;
        }
        Counts counts = FitProtocol.readCounts(reader);
        TestSummary testSummary =
                new TestSummary(counts.right, counts.wrong, counts.ignores, counts.exceptions);
        formatter.acceptFinalCount(testSummary);
    }

    public ResultFormatter makeFormatter(Request request) throws Exception {
        String format = (String) request.getInput("format");
        if (format != null) {
            if ("html".equals(format))
                return new HtmlResultFormatter(context, baseUrl, rootPath);
            if ("xml".equals(format))
                return new XmlResultFormatter(baseUrl, rootPath);
        }
        return new MockResultFormatter();
    }
}
