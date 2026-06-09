package configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config instance = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Could not find config.properties");
            }
            properties.load(inputStream);
        } catch (IOException e){
            throw new RuntimeException("Fail ed to load config.properties", e);
        }
    }
    public static String getProperty(String key) {
        return instance.properties.getProperty(key);
    }
}
