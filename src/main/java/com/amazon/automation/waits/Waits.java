package com.amazon.automation.waits;

import java.time.Duration;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Waits {

    private final WebDriver driver;
    private final Duration timeout;
    private final Duration polling;

    public WebElement visible(By locator) {
        return new WebDriverWait(driver, timeout)
                .pollingEvery(polling)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement clickable(By locator) {
        return new WebDriverWait(driver, timeout)
                .pollingEvery(polling)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    public boolean invisible(By locator) {
        return new WebDriverWait(driver, timeout)
                .pollingEvery(polling)
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public <T> T until(Function<WebDriver, T> condition) {
        return until(timeout, condition);
    }

    public <T> T until(Duration customTimeout, Function<WebDriver, T> condition) {
        return new FluentWait<>(driver)
                .withTimeout(customTimeout)
                .pollingEvery(polling)
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .until(condition);
    }

    public boolean present(By locator, Duration customTimeout) {
        try {
            return until(customTimeout, driver -> driver.findElements(locator).isEmpty() ? null : Boolean.TRUE);
        } catch (TimeoutException exception) {
            return false;
        }
    }
}
