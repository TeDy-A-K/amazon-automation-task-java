package com.amazon.automation.hooks;

import com.amazon.automation.context.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

@RequiredArgsConstructor
public class Hooks {

    private final ScenarioContext context;

    @Before(order = 0)
    public void beforeScenario() {
        context.startSession();
    }

    @After(order = 1)
    public void attachFailureEvidence(Scenario scenario) {
        if (context.getConfig().screenshotOnFailure() && scenario.isFailed()) {
            byte[] screenshot = ((TakesScreenshot) context.getDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "failure-screenshot");
        }
    }

    @After(order = 0)
    public void afterScenario() {
        context.stopSession();
    }
}
