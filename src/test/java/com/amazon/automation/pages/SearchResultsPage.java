package com.amazon.automation.pages;

import com.amazon.automation.model.BookSnapshot;
import com.amazon.automation.ui.UiActions;
import com.amazon.automation.waits.Waits;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SearchResultsPage extends BasePage {

    private static final By PAGE_MARKER = By.cssSelector("[data-component-type='s-search-results']");
    private static final By RESULT_ITEMS = By.cssSelector("[data-component-type='s-search-result'][data-asin]:not([data-asin=''])");
    private static final By RESULT_TITLE = By.cssSelector("h2 span");
    private static final By RESULT_LINK = By.cssSelector("a.a-link-normal.s-line-clamp-2");
    private static final By RESULT_BADGE = By.cssSelector(".a-badge-text");
    private static final By RESULT_TYPE = By.cssSelector("a.a-size-base.a-link-normal.a-text-bold");
    private static final By RESULT_PRICE = By.cssSelector("a[aria-describedby='price-link'] .a-price .a-offscreen");

    public SearchResultsPage(WebDriver driver, UiActions actions, Waits waits) {
        super(driver, actions, waits);
    }

    @Override
    protected By pageMarker() {
        return PAGE_MARKER;
    }

    public BookSnapshot firstResultSnapshot() {
        return getItemSnapshotByIndex(0);
    }

    public BookSnapshot getItemSnapshotByIndex(int index) {
        WebElement result = resultByIndex(index);

        return BookSnapshot.builder()
                .title(readRequiredTextWithin(result, RESULT_TITLE))
                .badge(readOptionalTextWithin(result, RESULT_BADGE))
                .selectedType(readRequiredTextWithin(result, RESULT_TYPE))
                .unitPrice(readRequiredDomTextWithin(result, RESULT_PRICE))
                .build();
    }

    public ProductDetailsPage openFirstResult() {
        return openResultByIndex(0);
    }

    public ProductDetailsPage openResultByIndex(int index) {
        WebElement result = resultByIndex(index);
        result.findElement(RESULT_LINK).click();
        return new ProductDetailsPage(driver, actions, waits);
    }

    private WebElement resultByIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Result index must be zero or greater: " + index);
        }

        try {
            return waits.until(driver -> {
                List<WebElement> items = driver.findElements(RESULT_ITEMS).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(item -> item.findElements(RESULT_LINK).stream().anyMatch(WebElement::isDisplayed))
                    .toList();

                return items.size() > index ? items.get(index) : null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve search result at index " + index, e);
        }
    }
}
