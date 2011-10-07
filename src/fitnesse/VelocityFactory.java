package fitnesse;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import util.TodoException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class VelocityFactory {

    private static final VelocityEngine venEngine;

    static {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(VelocityEngine.RESOURCE_LOADER, "class");
        engine.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
        engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        try {
            engine.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        venEngine = engine;
    }

    public static VelocityEngine getVelocityEngine() {
        return venEngine;
    }

    public static String translateTemplate(VelocityContext velocityContext, String templateFileName) throws IOException {
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            mergeTemplate(writer, velocityContext, templateFileName);
            return writer.toString();
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public static void mergeTemplate(Writer writer, VelocityContext velocityContext, String templateName) throws IOException {
        try {
            Template template = getVelocityEngine().getTemplate(templateName);
            template.merge(velocityContext, writer);
        } catch (ResourceNotFoundException e) {
            throw new IllegalArgumentException("The specified template does not exist: " + templateName, e);
        } catch (ParseErrorException e) {
            throw new TodoException(e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new TodoException(e);
        }
    }
}
