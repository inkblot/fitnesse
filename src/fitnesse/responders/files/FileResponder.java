package fitnesse.responders.files;

import fitnesse.Responder;
import fitnesse.http.InputStreamResponse;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import util.Clock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 10/12/11
 * Time: 9:06 PM
 */
public abstract class FileResponder implements Responder {
    private static final FileNameMap fileNameMap = URLConnection.getFileNameMap();
    protected final Clock clock;

    public FileResponder(Clock clock) {
        this.clock = clock;
    }

    public abstract InputStream getFileStream(Request request) throws IOException;

    public String getContentType(Request request) {
        return FileResponder.getContentType(FileResponder.getFileName(request.getResource()));
    }

    public abstract Integer getContentLength(Request request);

    public abstract Date getLastModifiedDate(Request request);

    private Response createNotModifiedResponse(Request request) {
        Response response = new SimpleResponse();
        response.setStatus(304);
        response.addHeader("Date", SimpleResponse.makeStandardHttpDateFormat().format(clock.currentClockDate()));
        response.addHeader("Cache-Control", "private");
        response.setLastModifiedHeader(SimpleResponse.makeStandardHttpDateFormat().format(getLastModifiedDate(request)));
        return response;
    }

    public Response makeResponse(Request request) throws Exception {
        InputStreamResponse response = new InputStreamResponse();

        if (isNotModified(request))
            return createNotModifiedResponse(request);
        else {
            response.setBody(getFileStream(request), getContentLength(request));
            response.setContentType(getContentType(request));
            response.setLastModifiedHeader(SimpleResponse.makeStandardHttpDateFormat().format(getLastModifiedDate(request)));
        }
        return response;
    }

    private boolean isNotModified(Request request) {
        if (request.hasHeader("If-Modified-Since")) {
            String queryDateString = (String) request.getHeader("If-Modified-Since");
            try {
                Date queryDate = SimpleResponse.makeStandardHttpDateFormat().parse(queryDateString);
                if (!queryDate.before(getLastModifiedDate(request)))
                    return true;
            } catch (ParseException e) {
                //Some browsers use local date formats that we can't parse.
                //So just ignore this exception if we can't parse the date.
            }
        }
        return false;
    }

    public static String getFileName(String resource) {
        return resource.substring(resource.lastIndexOf(File.separator) + 1);
    }

    public static String getContentType(String filename) {
        if (fileNameMap.getContentTypeFor(filename) != null)
            return fileNameMap.getContentTypeFor(filename);
        else if (filename.endsWith(".css"))
            return "text/css";
        else if (filename.endsWith(".jar"))
            return "application/x-java-archive";
        else
            return "text/plain";
    }
}
