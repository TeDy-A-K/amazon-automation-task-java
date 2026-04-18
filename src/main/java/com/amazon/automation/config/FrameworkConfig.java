package com.amazon.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import lombok.Getter;

@Getter
public final class FrameworkConfig {

    public static final FrameworkConfig INSTANCE = new FrameworkConfig();

    private static final String CONFIG_PATH = "config/framework.properties";
    private final Properties properties;

    private FrameworkConfig() {
        this.properties = loadProperties();
    }

    public String getBaseUrl() {
        return get("base.url");
    }

    public String getBrowser() {
        return getOrDefault("browser", "chrome");
    }

    public boolean isHeadless() {
        return Boolean.parseBoolean(getOrDefault("headless", "false"));
    }

    public boolean useWebDriverManager() {
        return Boolean.parseBoolean(getOrDefault("use.webdriver.manager", "false"));
    }

    public Duration explicitTimeout() {
        return Duration.ofSeconds(Long.parseLong(getOrDefault("explicit.wait.timeout.seconds", "15")));
    }

    public Duration pollingInterval() {
        return Duration.ofMillis(Long.parseLong(getOrDefault("explicit.wait.polling.millis", "250")));
    }

    public boolean screenshotOnFailure() {
        return Boolean.parseBoolean(getOrDefault("screenshot.on.failure", "true"));
    }

    private String get(String key) {
        String value = System.getProperty(key, System.getenv(toEnvKey(key)));
        if (Objects.nonNull(value) && !value.isBlank()) {
            return value;
        }
        value = properties.getProperty(key);
        if (Objects.isNull(value) || value.isBlank()) {
            throw new IllegalStateException("Missing required config: " + key);
        }
        return value.trim();
    }

    private String getOrDefault(String key, String defaultValue) {
        String value = System.getProperty(key, System.getenv(toEnvKey(key)));
        if (Objects.nonNull(value) && !value.isBlank()) {
            return value.trim();
        }
        return properties.getProperty(key, defaultValue).trim();
    }

    private String toEnvKey(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_PATH)) {
            if (is == null) {
                throw new IllegalStateException("Unable to find " + CONFIG_PATH + " in classpath");
            }
            props.load(is);
            return props;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load framework configuration", ex);
        }
    }
}
