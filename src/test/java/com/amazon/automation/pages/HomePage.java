package com.amazon.automation.pages;

import com.amazon.automation.pages.components.HeaderComponent;
import com.amazon.automation.ui.UiActions;
import com.amazon.automation.waits.Waits;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {

    private static final By PAGE_MARKER = By.id("nav-logo-sprites");
    private static final By COOKIE_BANNER = By.cssSelector("form#cos-banner[role='dialog']");
    private static final By ACCEPT_COOKIES = By.id("sp-cc-accept");
    private static final Duration COOKIE_BANNER_TIMEOUT = Duration.ofSeconds(1);
    private final HeaderComponent header;

    public HomePage(WebDriver driver, UiActions actions, Waits waits) {
        super(driver, actions, waits);
        this.header = new HeaderComponent(actions, waits);
    }

    @Override
    protected By pageMarker() {
        return PAGE_MARKER;
    }

    public SearchResultsPage searchInSection(String keyword, String department) {
        header.searchWithinDepartment(department, keyword);
        return new SearchResultsPage(driver, actions, waits);
    }

    public void acceptCookiesIfPresent() {
        boolean bannerPresent = waits.present(COOKIE_BANNER, COOKIE_BANNER_TIMEOUT)
                || waits.present(ACCEPT_COOKIES, COOKIE_BANNER_TIMEOUT);

        if (!bannerPresent) {
            return;
        }

        actions.click(ACCEPT_COOKIES);
        waits.invisible(COOKIE_BANNER);
    }
}
