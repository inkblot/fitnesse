// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Response;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.run.formatters.*;
import fitnesse.responders.testHistory.PageHistory;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class TestResponder extends ChunkingResponder implements SecureResponder {
    private static Collection<TestEventListener> eventListeners = new LinkedList<TestEventListener>();
    protected PageData data;
    protected CompositeFormatter formatters;
    private boolean isClosed = false;

    private boolean remoteDebug = false;
    protected TestSystem testSystem;
    private final HtmlPageFactory htmlPageFactory;
    private final String testResultsPath;
    protected final int port;
    protected final SocketDealer socketDealer;
    protected final Injector injector;

    @Inject
    public TestResponder(HtmlPageFactory htmlPageFactory,
                         @Named(FitNesseModule.ROOT_PAGE) WikiPage root,
                         @Named(FitNesseModule.TEST_RESULTS_PATH) String testResultsPath,
                         @Named(FitNesseModule.PORT) Integer port,
                         SocketDealer socketDealer,
                         RunningTestingTracker runningTestingTracker,
                         @Named(FitNesseModule.ENABLE_CHUNKING) boolean chunkingEnabled, Injector injector) {
        super(htmlPageFactory, root, runningTestingTracker, chunkingEnabled);
        this.htmlPageFactory = htmlPageFactory;
        this.testResultsPath = testResultsPath;
        this.port = port;
        this.socketDealer = socketDealer;
        this.injector = injector;
        formatters = new CompositeFormatter();
    }

    @Override
    protected void doSending(WikiPage root, WikiPagePath path, WikiPage page, RunningTestingTracker runningTestingTracker) throws Exception {
        checkArguments();
        data = page.getData();

        createFormatterAndWriteHead(new File(testResultsPath), page);
        sendPreTestNotification();
        performExecution(runningTestingTracker, root, page);

        int exitCode = formatters.getErrorCount();
        closeHtmlResponse(exitCode);
    }

    protected void checkArguments() {
        remoteDebug |= request.hasInput("remote_debug");
    }

    protected void createFormatterAndWriteHead(File testHistoryDirectory, WikiPage page) throws Exception {
        if (response.isXmlFormat())
            addXmlFormatter(testHistoryDirectory, page);
        else if (response.isTextFormat())
            addTextFormatter();
        else if (response.isJavaFormat())
            addJavaFormatter(page);
        else
            addHtmlFormatter(page);
        if (!request.hasInput("nohistory"))
            addTestHistoryFormatter(testHistoryDirectory, page);
        addTestInProgressFormatter(page);
        formatters.writeHead(getTitle());
    }

    String getTitle() {
        return "Test Results";
    }

    void addXmlFormatter(File testHistoryDirectory, WikiPage page) {
        XmlFormatter.WriterFactory writerSource = new XmlFormatter.WriterFactory() {
            @Override
            public Writer getWriter(WikiPage page, TestSummary counts, long time) {
                return makeResponseWriter();
            }
        };
        formatters.add(new XmlFormatter(page, writerSource));
    }

    void addTextFormatter() {
        formatters.add(new TestTextFormatter(response));
    }

    void addJavaFormatter(WikiPage page) {
        formatters.add(JavaFormatter.getInstance(new WikiPagePath(page).toString()));
    }

    protected Writer makeResponseWriter() {
        return new Writer() {
            @Override
            public void write(char[] buf, int off, int len) {
                String fragment = new String(buf, off, len);
                try {
                    response.add(fragment.getBytes());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
    }


    void addHtmlFormatter(WikiPage page) {
        BaseFormatter formatter = new TestHtmlFormatter(page, getHtmlPageFactory()) {
            @Override
            protected void writeData(String output) {
                addToResponse(output);
            }
        };
        formatters.add(formatter);
    }

    protected void addTestHistoryFormatter(File testHistoryDirectory, WikiPage page) {
        HistoryWriterFactory writerFactory = new HistoryWriterFactory(testHistoryDirectory);
        formatters.add(new PageHistoryFormatter(page, writerFactory));
    }

    protected void addTestInProgressFormatter(WikiPage page) {
        formatters.add(new PageInProgressFormatter(page));
    }

    protected void sendPreTestNotification() throws Exception {
        for (TestEventListener eventListener : eventListeners) {
            eventListener.notifyPreTest(this, data);
        }
    }

    protected void performExecution(RunningTestingTracker runningTestingTracker, WikiPage root, WikiPage page) throws Exception {
        List<WikiPage> test2run = new SuiteContentsFinder(page, null, root).makePageListForSingleTest();

        MultipleTestsRunner runner = new MultipleTestsRunner(test2run, runningTestingTracker, page, formatters, root, port, socketDealer, injector);
        runner.setDebug(isRemoteDebug());

        if (isEmpty(page))
            formatters.addMessageForBlankHtml();

        runner.executeTestPages();
    }

    private boolean isEmpty(WikiPage page) throws Exception {
        return page.getData().getContent().length() == 0;
    }

    @Override
    public SecureOperation getSecureOperation() {
        return new SecureTestOperation();
    }


    public static void registerListener(TestEventListener listener) {
        eventListeners.add(listener);
    }

    public void addToResponse(String output) {
        if (!isClosed()) {
            response.add(output);
        }
    }

    synchronized boolean isClosed() {
        return isClosed;
    }

    synchronized void setClosed() {
        isClosed = true;
    }

    void closeHtmlResponse(int exitCode) throws Exception {
        if (!isClosed()) {
            setClosed();
            response.closeChunks();
            response.addTrailingHeader("Exit-Code", String.valueOf(exitCode));
            response.closeTrailer();
            response.close();
        }
    }

    boolean isRemoteDebug() {
        return remoteDebug;
    }

    public Response getResponse() {
        return response;
    }

    public static class HistoryWriterFactory implements XmlFormatter.WriterFactory {
        private static final Logger logger = LoggerFactory.getLogger(HistoryWriterFactory.class);

        private final File testHistoryDirectory;

        public HistoryWriterFactory(File testHistoryDirectory) {
            this.testHistoryDirectory = testHistoryDirectory;
        }

        @Override
        public Writer getWriter(WikiPage page, TestSummary counts, long time) throws IOException {
            File resultPath = new File(PageHistory.makePageHistoryFileName(testHistoryDirectory, page, counts, time));
            File resultDirectory = new File(resultPath.getParent());
            resultDirectory.mkdirs();
            File resultFile = new File(resultDirectory, resultPath.getName());
            logger.info("Creating test result file: resultFile=" + resultFile.getAbsolutePath());
            return new FileWriter(resultFile);
        }
    }

    public HtmlPageFactory getHtmlPageFactory() {
        return htmlPageFactory;
    }
}
