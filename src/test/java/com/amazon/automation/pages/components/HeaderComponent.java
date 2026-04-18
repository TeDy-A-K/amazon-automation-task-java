package com.amazon.automation.pages.components;

import com.amazon.automation.ui.UiActions;
import com.amazon.automation.waits.Waits;
import org.openqa.selenium.By;

public class HeaderComponent {

    private final UiActions actions;
    private final Waits waits;

    private static final By SEARCH_INPUT = By.id("twotabsearchtextbox");
    private static final By SEARCH_SUBMIT = By.id("nav-search-submit-button");
    private static final By SEARCH_DEPARTMENT = By.id("searchDropdownBox");
    private static final By BASKET_COUNT = By.id("nav-cart-count");

    public HeaderComponent(UiActions actions, Waits waits) {
        this.actions = actions;
        this.waits = waits;
        waits.visible(SEARCH_INPUT);
    }

    public void searchWithinDepartment(String department, String keyword) {
        actions.selectByVisibleText(SEARCH_DEPARTMENT, department);
        actions.type(SEARCH_INPUT, keyword);
        actions.click(SEARCH_SUBMIT);
    }

    public String basketCount() {
        return actions.getText(BASKET_COUNT).trim();
    }
}
