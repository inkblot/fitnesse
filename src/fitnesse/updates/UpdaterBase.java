package fitnesse.updates;

import fitnesse.FitNesseContext;
import fitnesse.Updater;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

public class UpdaterBase implements Updater {
    private static final Logger logger = LoggerFactory.getLogger(UpdaterBase.class);
    public FitNesseContext context;
    public Properties rootProperties;
    public Update[] updates;

    public UpdaterBase(FitNesseContext context) throws IOException {
        this.context = context;
        rootProperties = loadProperties();
    }

    public Properties getProperties() {
        return rootProperties;
    }

    public Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        File propFile = getPropertiesFile();
        if (propFile.exists()) {
            InputStream is = null;
            try {
                is = new FileInputStream(propFile);
                properties.load(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return properties;
    }

    private File getPropertiesFile() {
        String filename = context.rootPagePath + "/properties";
        return new File(filename);
    }

    public void saveProperties() throws IOException {
        OutputStream os = null;
        File propFile = null;
        try {
            propFile = getPropertiesFile();
            os = new FileOutputStream(propFile);
            writeProperties(os);
        } catch (RuntimeException e) {
            String fileName = (propFile != null) ? propFile.getAbsolutePath() : "<unknown>";
            logger.error("Failed to save properties file: \"" + fileName + "\"", e);
            throw e;
        } catch (IOException e) {
            String fileName = (propFile != null) ? propFile.getAbsolutePath() : "<unknown>";
            logger.error("Failed to save properties file: \"" + fileName + "\"", e);
            throw e;
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private void writeProperties(final OutputStream OutputStream)
            throws IOException {
        BufferedWriter awriter;
        awriter = new BufferedWriter(new OutputStreamWriter(OutputStream, "8859_1"));
        awriter.write("#FitNesse properties");
        awriter.newLine();
        Object[] keys = rootProperties.keySet().toArray(new Object[rootProperties.keySet().size()]);
        Arrays.sort(keys);
        for (Enumeration<Object> enumeration = rootProperties.keys(); enumeration
                .hasMoreElements(); ) {
            String key = (String) enumeration.nextElement();
            String val = (String) rootProperties.get(key);
            awriter.write(key + "=" + val);
            awriter.newLine();
        }
        awriter.flush();
    }

    public void update() throws IOException {
        Update[] updates = getUpdates();
        for (Update update : updates) {
            if (update.shouldBeApplied())
                performUpdate(update);
        }
        saveProperties();
    }

    private void performUpdate(Update update) {
        try {
            logger.info(update.getMessage());
            update.doUpdate();
        } catch (Exception e) {
            logger.error("Could not perform update", e);
        }
    }

    private Update[] getUpdates() {
        return updates;
    }

}
