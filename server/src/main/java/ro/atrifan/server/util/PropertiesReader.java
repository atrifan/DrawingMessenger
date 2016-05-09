package ro.atrifan.server.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by alexandru.trifan on 24.04.2016.
 */
public class PropertiesReader {

    private static PropertiesReader instance = null;
    public static PropertiesReader load() throws IOException {
        if(instance == null) {
            Properties properties = new Properties();
            properties.load(PropertiesReader.class.getClassLoader().getResourceAsStream("config.properties"));
            instance = new PropertiesReader(properties);
        }

        return instance;
    }

    private Properties properties;

    private PropertiesReader() {}

    private PropertiesReader(Properties properties) {
        this.properties = properties;
    }

    public String getBrainHost() {
        return properties.getProperty("brain.host");
    }

    public String getBraingUserPath() {
        return properties.getProperty("brain.user.path");
    }

    public String getBrainGroupPath() {
        return properties.getProperty("brain.group.path");
    }

    public String getBrainDashBoardPath() {
        return properties.getProperty("brain.dashboard.path");
    }

    public String getMessageQueuHost() {
        return properties.getProperty("queue.host");
    }

    public String getMessageQueueQueueName() {
        return properties.getProperty("queue.name");
    }
}
