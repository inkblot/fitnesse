package fitnesse.responders.run.formatters;

import fitnesse.responders.run.TestSummary;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import util.FileUtil;
import util.TimeMeasurement;

public class PageInProgressFormatter extends NullFormatter {

    public PageInProgressFormatter(final WikiPage page) {
        super();
        this.page = page;
    }

    public String getLockFileName(WikiPage test) throws Exception {
        //PageData data = page.getData();
        PageData data = test.getData();
        return "FitNesseRoot/files/testProgress/" + data.getVariable("PAGE_PATH") + "." + data.getVariable("PAGE_NAME");
    }

    public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) throws Exception {
        FileUtil.createFile(getLockFileName(test), "");
    }

    @Override
    public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws Exception {
        FileUtil.deleteFile(getLockFileName(test));
    }

    /*@Override
    public void errorOccurred() {
      FileUtil.deleteFile(getLockFileName());
    };*/
}

