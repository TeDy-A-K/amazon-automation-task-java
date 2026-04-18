package com.amazon.automation.pages;

import com.amazon.automation.model.BookSnapshot;
import com.amazon.automation.ui.UiActions;
import com.amazon.automation.waits.Waits;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class BasketPage extends BasePage {

    private static final By PAGE_MARKER = By.cssSelector("#sc-active-cart, h1.sc-your-amazon-cart-is-empty");
    private static final By BASKET_ITEM = By.cssSelector("[data-name='Active Items'] .sc-list-item-content, .sc-list-item-content");
    private static final By ITEM_TITLE = By.cssSelector(".sc-product-title");
    private static final By ITEM_TYPE = By.cssSelector(".sc-product-binding");
    private static final By ITEM_UNIT_PRICE = By.cssSelector(".sc-product-price, .a-size-medium.a-color-base.sc-price.sc-white-space-nowrap");
    private static final By ITEM_QUANTITY = By.cssSelector(".a-dropdown-prompt, span[data-a-selector='value']");
    private static final By BASKET_SUBTOTAL = By.id("sc-subtotal-amount-activecart");

    public BasketPage(WebDriver driver, UiActions actions, Waits waits) {
        super(driver, actions, waits);
    }

    @Override
    protected By pageMarker() {
        return PAGE_MARKER;
    }

    public List<BookSnapshot> getItemSnapshots() {
        try {
            List<WebElement> items = waits.until(driver -> {
                List<WebElement> visibleItems = driver.findElements(BASKET_ITEM).stream()
                        .filter(WebElement::isDisplayed)
                        .toList();
                return visibleItems.isEmpty() ? null : visibleItems;
            });

            return items.stream()
                    .map(this::toItemSnapshot)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get basket item snapshots", e);
        }
    }

    public String basketSubtotal() {
        return readRequiredText(BASKET_SUBTOTAL);
    }

    private BookSnapshot toItemSnapshot(WebElement item) {
        return BookSnapshot.builder()
                .title(readRequiredTextWithin(item, ITEM_TITLE))
                .selectedType(readRequiredTextWithin(item, ITEM_TYPE))
                .unitPrice(readRequiredTextWithin(item, ITEM_UNIT_PRICE))
                .quantity(readRequiredTextWithin(item, ITEM_QUANTITY))
                .build();
    }
}