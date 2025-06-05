package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final Properties props;

    static {
        props = new Properties();
        try {
            FileInputStream fis = new FileInputStream("config.properties");
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Error loading config: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}