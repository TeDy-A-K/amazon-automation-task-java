package com.amazon.automation.pages;

import com.amazon.automation.model.BookSnapshot;
import com.amazon.automation.ui.UiActions;
import com.amazon.automation.waits.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductDetailsPage extends BasePage {

    private static final By PAGE_MARKER = By.id("productTitle");
    private static final By TITLE = By.id("productTitle");
    private static final By BADGE = By.cssSelector("#zeitgeistBadge_feature_div .a-badge-text, #acBadge_feature_div .a-badge-text");
    private static final By PRICE = By.cssSelector("#tmmSwatches .swatchElement.selected .slot-price .a-color-price");
    private static final By TYPE = By.cssSelector("#tmmSwatches .swatchElement.selected .slot-title span[aria-label$='Format:']");
    private static final By ADD_TO_BASKET = By.id("add-to-cart-button");
    private static final By ADDED_TO_BASKET_MESSAGE = By.id("NATC_SMART_WAGON_CONF_MSG_SUCCESS");
    private static final By ADDED_TO_BASKET_TITLE = By.cssSelector("#NATC_SMART_WAGON_CONF_MSG_SUCCESS h1.sw-atc-text");
    private static final By GO_TO_BASKET = By.xpath("//a[normalize-space()='Go to basket' or normalize-space()='Go to Basket']");
    private static final By HEADER_BASKET = By.id("nav-cart");
    private static final By HEADER_BASKET_COUNT = By.id("nav-cart-count");

    public ProductDetailsPage(WebDriver driver, UiActions actions, Waits waits) {
        super(driver, actions, waits);
    }

    @Override
    protected By pageMarker() {
        return PAGE_MARKER;
    }

    public BookSnapshot detailsSnapshot() {
        return BookSnapshot.builder()
                .title(readRequiredText(TITLE))
                .badge(readOptionalText(BADGE))
                .selectedType(readRequiredText(TYPE))
                .unitPrice(readRequiredText(PRICE))
                .build();
    }

    public void addToBasket() {
        actions.click(ADD_TO_BASKET);
    }

    public boolean isAddedToBasketMessageShown() {
        return waits.until(driver -> !driver.findElements(ADDED_TO_BASKET_MESSAGE).isEmpty());
    }

    public String addedToBasketMessageTitle() {
        return actions.getText(ADDED_TO_BASKET_TITLE);
    }

    public int basketItemCount() {
        return Integer.parseInt(actions.getText(HEADER_BASKET_COUNT).trim());
    }

    public BasketPage openBasketEditor() {
        By basketControl = waits.until(driver -> {
            if (!driver.findElements(GO_TO_BASKET).isEmpty()) {
                return GO_TO_BASKET;
            }
            if (!driver.findElements(HEADER_BASKET).isEmpty()) {
                return HEADER_BASKET;
            }
            return null;
        });

        actions.click(basketControl);
        return new BasketPage(driver, actions, waits);
    }
}
