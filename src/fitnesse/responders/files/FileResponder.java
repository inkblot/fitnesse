package fitnesse.responders.files;

import fitnesse.Responder;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import util.Clock;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 10/12/11
 * Time: 9:06 PM
 */
public abstract class FileResponder implements Responder {
    protected final Clock clock;
    protected String lastModifiedDateString;

    public FileResponder(Clock clock) {
        this.clock = clock;
    }

    protected Response createNotModifiedResponse() {
        Response response = new SimpleResponse();
        response.setStatus(304);
        response.addHeader("Date", SimpleResponse.makeStandardHttpDateFormat().format(clock.currentClockDate()));
        response.addHeader("Cache-Control", "private");
        response.setLastModifiedHeader(lastModifiedDateString);
        return response;
    }
}
