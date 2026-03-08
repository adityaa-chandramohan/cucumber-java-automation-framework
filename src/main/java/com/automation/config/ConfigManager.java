package com.automation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration manager.
 * Loads environment-specific properties and supports system property overrides.
 *
 * Priority: System properties > env-specific config > default config
 */
public class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static ConfigManager instance;
    private final Properties properties = new Properties();

    private ConfigManager() {
        String env = System.getProperty("env", "dev");
        loadProperties("config/config.properties");
        loadProperties("config/config-" + env + ".properties");
        log.info("Configuration loaded for environment: {}", env);
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadProperties(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is != null) {
                properties.load(is);
                log.debug("Loaded properties from: {}", resourcePath);
            }
        } catch (IOException e) {
            log.warn("Could not load properties file: {}", resourcePath);
        }
    }

    public String get(String key) {
        // System properties override file-based config
        String value = System.getProperty(key, properties.getProperty(key));
        if (value == null) {
            throw new IllegalArgumentException("Configuration key not found: " + key);
        }
        return value;
    }

    public String get(String key, String defaultValue) {
        return System.getProperty(key, properties.getProperty(key, defaultValue));
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key, "false"));
    }

    public String getBaseUrl() {
        return get("api.base.url");
    }

    public int getConnectionTimeout() {
        return getInt("api.connection.timeout");
    }

    public int getReadTimeout() {
        return getInt("api.read.timeout");
    }
}
