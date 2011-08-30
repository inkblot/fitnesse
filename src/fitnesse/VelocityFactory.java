package fitnesse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;

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

    public static String translateTemplate(VelocityContext velocityContext, String templateFileName) throws Exception {
        Template template = getVelocityEngine().getTemplate(templateFileName);
        StringWriter writer = new StringWriter();
        template.merge(velocityContext, writer);
        return writer.toString();
    }
}
