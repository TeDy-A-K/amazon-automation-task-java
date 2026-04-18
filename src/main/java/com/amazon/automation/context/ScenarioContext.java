package com.amazon.automation.context;

import com.amazon.automation.config.FrameworkConfig;
import com.amazon.automation.driver.DriverFactory;
import com.amazon.automation.ui.UiActions;
import com.amazon.automation.waits.Waits;
import lombok.Getter;

import org.jspecify.annotations.Nullable;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ScenarioContext {

    private final FrameworkConfig config;
    private final DriverFactory driverFactory;
    private WebDriver driver;
    private Waits waits;
    private UiActions uiActions;
    private final Map<String, Object> data = new HashMap<>();

    public ScenarioContext() {
        this.config = FrameworkConfig.INSTANCE;
        this.driverFactory = DriverFactory.INSTANCE;
    }

    public <T> void put(String key, T value) {
        data.put(key, value);
    }

    public <T> @Nullable T get(String key, Class<T> type) {
        Object value = data.get(key);
        return value == null ? null : type.cast(value);
    }

    public void clearScenarioData() {
        data.clear();
    }

    public void startSession() {
        this.driver = driverFactory.createDriver(config);
        this.waits = new Waits(driver, config.explicitTimeout(), config.pollingInterval());
        this.uiActions = new UiActions(driver, waits);
        clearScenarioData();
    }

    public void stopSession() {
        clearScenarioData();
        driverFactory.quitDriver();
    }
}
