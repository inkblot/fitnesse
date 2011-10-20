package fitnesse.responders.testHistory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import util.Clock;
import util.FileUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PurgeHistoryResponder implements SecureResponder {
    private Date todaysDate;
    private final HtmlPageFactory htmlPageFactory;
    private final Clock clock;
    private final File testHistoryDirectory;

    @Inject
    public PurgeHistoryResponder(HtmlPageFactory htmlPageFactory, Clock clock, @Named(FitNesseModule.TEST_RESULTS_PATH) String testResultsPath) {
        this.htmlPageFactory = htmlPageFactory;
        this.clock = clock;
        this.testHistoryDirectory = new File(testResultsPath);
    }

    public Response makeResponse(Request request) throws Exception {
        todaysDate = clock.currentClockDate();
        if (hasValidInputs(request)) {
            purgeHistory(request);
            return makeValidResponse();
        } else {
            return makeErrorResponse(request);
        }
    }

    private SimpleResponse makeValidResponse() throws Exception {
        SimpleResponse response = new SimpleResponse();
        response.redirect("?testHistory");
        return response;
    }

    private void purgeHistory(Request request) throws ParseException {
        int days = getDaysInput(request);
        deleteTestHistoryOlderThanDays(days);
    }

    private Integer getDaysInput(Request request) {
        String daysInput = request.getInput("days").toString();
        return parseInt(daysInput);
    }

    private Integer parseInt(String daysInput) {
        try {
            return Integer.parseInt(daysInput);
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean hasValidInputs(Request request) {
        return request.getInput("days") != null && getDaysInput(request) >= 0;

    }

    private Response makeErrorResponse(Request request) throws Exception {
        return new ErrorResponder("Invalid Amount Of Days", htmlPageFactory).makeResponse(request);
    }

    public void setTodaysDate(Date date) {
        todaysDate = date;
    }

    public void deleteTestHistoryOlderThanDays(int days) throws ParseException {
        Date expirationDate = getDateDaysAgo(days);
        File[] files = FileUtil.getDirectoryListing(testHistoryDirectory);
        deleteExpiredFiles(files, expirationDate);
    }

    private void deleteExpiredFiles(File[] files, Date expirationDate) {
        for (File file : files)
            deleteIfExpired(file, expirationDate);
    }

    public Date getDateDaysAgo(int days) {
        long now = todaysDate.getTime();
        long millisecondsPerDay = 1000L * 60L * 60L * 24L;
        return new Date(now - (millisecondsPerDay * days));
    }

    private void deleteIfExpired(File file, Date expirationDate) {
        if (file.isDirectory()) {
            deleteDirectoryIfExpired(file, expirationDate);
        } else
            deleteFileIfExpired(file, expirationDate);
    }

    private void deleteDirectoryIfExpired(File file, Date expirationDate) {
        File[] files = FileUtil.getDirectoryListing(file);
        deleteExpiredFiles(files, expirationDate);
        if (file.list().length == 0)
            FileUtil.deleteFileSystemDirectory(file);
    }

    private void deleteFileIfExpired(File file, Date purgeOlder) {
        String name = file.getName();
        Date date = getDateFromPageHistoryFileName(name);
        if (date.getTime() < purgeOlder.getTime())
            FileUtil.deleteFile(file);
    }

    private Date getDateFromPageHistoryFileName(String name) {
        try {
            return tryExtractDateFromTestHistoryName(name);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Date tryExtractDateFromTestHistoryName(String testHistoryName) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
        String dateString = testHistoryName.split("_")[0];
        return dateFormat.parse(dateString);
    }


    public SecureOperation getSecureOperation() {
        return new AlwaysSecureOperation();
    }
}
