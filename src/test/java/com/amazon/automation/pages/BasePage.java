package com.amazon.automation.pages;

import com.amazon.automation.ui.UiActions;
import com.amazon.automation.waits.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final UiActions actions;
    protected final Waits waits;

    protected BasePage(WebDriver driver, UiActions actions, Waits waits) {
        this.driver = driver;
        this.actions = actions;
        this.waits = waits;
        waits.visible(pageMarker()); // Waits for page marker to be visible -> the page has loaded
    }

    protected abstract By pageMarker();

    protected String readRequiredText(By locator) {
        String text = readOptionalText(locator);
        if (text.isEmpty()) {
            throw new IllegalStateException("Expected text for locator " + locator + " on " + getClass().getSimpleName());
        }
        return text;
    }

    protected String readOptionalText(By locator) {
        if (driver.findElements(locator).isEmpty()) {
            return "";
        }

        return normalizeText(actions.getText(locator));
    }

    protected String readRequiredTextWithin(WebElement root, By locator) {
        String text = readOptionalTextWithin(root, locator);
        if (text.isEmpty()) {
            throw new IllegalStateException("Expected text for locator " + locator + " within " + getClass().getSimpleName());
        }
        return text;
    }

    protected String readOptionalTextWithin(WebElement root, By locator) {
        return root.findElements(locator).stream()
                .findFirst()
                .map(WebElement::getText)
                .map(this::normalizeText)
                .orElse("");
    }

    protected String readRequiredDomTextWithin(WebElement root, By locator) {
        String text = root.findElements(locator).stream()
                .findFirst()
                .map(element -> normalizeText(element.getDomProperty("textContent")))
                .orElse("");

        if (text.isEmpty()) {
            throw new IllegalStateException("Expected DOM text for locator " + locator + " within " + getClass().getSimpleName());
        }

        return text;
    }

    protected String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        return value.replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}
