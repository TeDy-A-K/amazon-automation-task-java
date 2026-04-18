package com.amazon.automation.ui;

import com.amazon.automation.waits.Waits;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Log4j2
@RequiredArgsConstructor
public final class UiActions {

    private final WebDriver driver;
    private final Waits waits;

    public void navigateTo(String url) {
        try {
            driver.navigate().to(url);
        } catch (Exception e) {
            log.error("Failed to navigate to URL: {}", url, e);
            throw e;
        }
    }
    
    public void click(By locator) {
        try {
            WebElement element = waits.clickable(locator);
            scrollIntoView(element);
            element.click();
        } catch (Exception e) {
            log.error("Failed to click on element: {}", locator, e);
            throw e;
        }
    }

    public void type(By locator, CharSequence... text) {
        try {
            WebElement element = waits.visible(locator);
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            log.error("Failed to type in element: {}", locator, e);
            throw e;
        }
    }

    public void selectByVisibleText(By locator, String value) {
        try {
            WebElement element = waits.until(driver -> driver.findElement(locator));
            String optionValue = element.findElements(By.tagName("option")).stream()
                    .filter(option -> value.equals(option.getText().trim()))
                    .map(option -> option.getAttribute("value"))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Could not find option '" + value + "' for dropdown " + locator));

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];"
                            + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
                            + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    element,
                    optionValue);
        } catch (Exception e) {
            log.warn("Failed to select '{}' from dropdown {}: {}", value, locator, e.getMessage());
            throw e;
        }
    }

    public String getText(By locator) {
        try {
            return waits.visible(locator).getText();
        } catch (Exception e) {
            log.error("Failed to get text from element: {}", locator, e);
            throw e;
        }
    }

    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        } catch (Exception e) {
            log.warn("Failed to scroll element into view", e);
        }
    }
}
