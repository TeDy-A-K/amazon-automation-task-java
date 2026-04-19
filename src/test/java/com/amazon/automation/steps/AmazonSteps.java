package com.amazon.automation.steps;

import com.amazon.automation.context.ScenarioContext;
import com.amazon.automation.pages.BasketPage;
import com.amazon.automation.model.BookSnapshot;
import com.amazon.automation.pages.HomePage;
import com.amazon.automation.pages.ProductDetailsPage;
import com.amazon.automation.pages.SearchResultsPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class AmazonSteps {

    private final ScenarioContext context;

    private HomePage homePage;
    private SearchResultsPage searchResultsPage;
    private ProductDetailsPage productDetailsPage;
    private BasketPage basketPage;

    private List<BookSnapshot> searchSnapshots = new ArrayList<>();
    private BookSnapshot detailsSnapshot;
    private List<BookSnapshot> basketItems;
    private String basketSubtotal;

    private static final String AMAZON_PAGE_TITLE = "Amazon";
    private static final String AMAZON_BOOKS_DEPARTMENT = "Books";
    private static final String AMAZON_ADDED_TO_BASKET_TEXT = "Added to Basket";

    @Given("the user is on the Amazon UK home page with accepted cookies")
    public void theUserIsOnTheAmazonUkHomePageWithAcceptedCookies() {
        context.getUiActions().navigateTo(context.getConfig().getBaseUrl());
        homePage = new HomePage(context.getDriver(), context.getUiActions(), context.getWaits());
        homePage.acceptCookiesIfPresent();

        validateUrlDomain(context.getDriver().getCurrentUrl(), context.getConfig().getBaseUrlDomain());
        assertThat(context.getDriver().getTitle())
                .as("Page title should be Amazon")
                .containsIgnoringCase(AMAZON_PAGE_TITLE);
    }

    @When("the user searches in Books for {string}")
    public void theUserSearchesInBooksFor(String keyword) {
        searchResultsPage = homePage.searchInSection(keyword, AMAZON_BOOKS_DEPARTMENT);
        searchSnapshots.add(searchResultsPage.firstResultSnapshot());
    }

    @Then("the first search result should contain title fragment {string} and type {string}")
    public void theFirstSearchResultShouldContainTitleFragmentAndType(String expectedTitle, String expectedType) {
        assertThat(searchSnapshots)
                .as("Search snapshots should not be empty")
                .isNotEmpty();

        BookSnapshot lastSearchSnapshot = getLastSearchSnapshot();
        assertThat(lastSearchSnapshot.title())
                .as("First search result title should contain: " + expectedTitle)
                .contains(expectedTitle);
        assertThat(lastSearchSnapshot.selectedType())
                .as("First search result type should be: " + expectedType)
                .containsIgnoringCase(expectedType);
        assertThat(lastSearchSnapshot.unitPrice())
                .as("First search result unit price should not be blank")
                .isNotBlank();
    }

    @When("the user opens the first search result details")
    public void theUserOpensTheFirstSearchResultDetails() {
        productDetailsPage = searchResultsPage.openFirstResult();
        detailsSnapshot = productDetailsPage.detailsSnapshot();
    }

    @Then("the book details page should match the selected search result title fragment {string}, type {string}, and unit price")
    public void theBookDetailsPageShouldMatchTheSelectedSearchResultTitleFragmentTypeAndUnitPrice(String expectedTitle,
            String expectedType) {
        assertThat(searchSnapshots).isNotEmpty();
        BookSnapshot lastSearchSnapshot = getLastSearchSnapshot();
        String expectedUnitPrice = lastSearchSnapshot.unitPrice();

        assertSnapshotMatchesTitleTypeAndUnitPrice(detailsSnapshot, expectedTitle, expectedType, expectedUnitPrice);
    }

    @When("the user adds the item to the basket")
    public void theUserAddsTheItemToTheBasket() {
        productDetailsPage.addToBasket();
    }

    @Then("the add to basket confirmation should be shown with {int} basket item")
    public void theAddToBasketConfirmationShouldBeShownWithOneBasketItem(int itemCount) {
        assertThat(productDetailsPage.isAddedToBasketMessageShown())
                .as("Add to basket confirmation message should be displayed")
                .isTrue();
        assertThat(productDetailsPage.addedToBasketMessageTitle())
                .as("Basket message title should contain 'Added to Basket'")
                .containsIgnoringCase(AMAZON_ADDED_TO_BASKET_TEXT);
        assertThat(productDetailsPage.basketItemCount())
                .as("Basket item count should be: " + itemCount)
                .isEqualTo(itemCount);
    }

    @When("the user opens the basket editor")
    public void theUserOpensTheBasketEditor() {
        basketPage = productDetailsPage.openBasketEditor();
        basketItems = basketPage.getItemSnapshots();
        basketSubtotal = basketPage.basketSubtotal();
    }

    @Then("basket details should contain the added books with title fragment, type, unit price, quantity, and total price")
    public void basketDetailsShouldContainTheAddedBooksWithTitleFragmentTypeUnitPriceQuantityAndTotalPrice() {
        assertThat(basketItems)
                .as("Basket items should not be empty")
                .isNotEmpty();
        assertThat(searchSnapshots)
                .as("Search snapshots should not be empty")
                .isNotEmpty();

        BigDecimal totalExpectedSubtotal = BigDecimal.ZERO;

        // Verify each added book exists in the basket with correct details
        for (BookSnapshot searchSnapshot : searchSnapshots) {
            String expectedTitle = searchSnapshot.title();
            String expectedType = searchSnapshot.selectedType();
            String expectedUnitPrice = searchSnapshot.unitPrice();

            BookSnapshot basketItem = basketItems.stream()
                    .filter(item -> item.title().contains(expectedTitle))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            "Expected book with title containing '" + expectedTitle + "' not found in basket"));

            assertSnapshotMatchesTitleTypeAndUnitPrice(basketItem, expectedTitle, expectedType, expectedUnitPrice);

            int quantity = extractQuantity(basketItem);

            BigDecimal unitPrice = new BigDecimal(expectedUnitPrice);
            BigDecimal itemSubtotal = unitPrice.multiply(new BigDecimal(quantity));
            totalExpectedSubtotal = totalExpectedSubtotal.add(itemSubtotal);
        }

        // Verify total basket subtotal matches sum of all items
        assertThat(basketSubtotal)
                .as("Basket subtotal should not be blank")
                .isNotBlank();
        BigDecimal actualSubtotal = new BigDecimal(basketSubtotal);
        assertThat(actualSubtotal)
                .as("Basket subtotal should match the sum of all item prices")
                .isEqualByComparingTo(totalExpectedSubtotal);
    }

    private void validateUrlDomain(String currentUrl, String expectedDomain) {
        try {
            URI uri = URI.create(currentUrl);
            String host = uri.getHost();
            assertThat(host)
                    .as("URL host should match expected domain: '" + expectedDomain + "'")
                    .isEqualTo(expectedDomain);
        } catch (Exception e) {
            throw new AssertionError("Failed to validate URL domain. URL: '" + currentUrl +
                    "', Expected domain: '" + expectedDomain + "'", e);
        }
    }

    private BookSnapshot getLastSearchSnapshot() {
        return searchSnapshots.get(searchSnapshots.size() - 1);
    }

    private int extractQuantity(BookSnapshot item) {
        assertThat(item.quantity())
                .as("Item quantity should not be blank")
                .isNotBlank();
        String quantityStr = item.quantity().replaceAll("\\D+", "").trim();

        assertThat(quantityStr)
                .as("Item quantity should contain at least one digit. Actual value: '" + item.quantity() + "'")
                .isNotBlank();

        try {
            return Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            throw new AssertionError("Failed to parse quantity as integer. Extracted value: '" + quantityStr +
                    "' from original: '" + item.quantity() + "'", e);
        }
    }

    private void assertSnapshotMatchesTitleTypeAndUnitPrice(
            BookSnapshot snapshot, String expectedTitle, String expectedType, String expectedUnitPrice) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(snapshot.title())
                .as("Book title should contain: " + expectedTitle)
                .contains(expectedTitle);
        softly.assertThat(snapshot.selectedType())
                .as("Book type should be: " + expectedType)
                .containsIgnoringCase(expectedType);
        softly.assertThat(snapshot.unitPrice())
                .as("Book unit price should be: " + expectedUnitPrice)
                .isEqualTo(expectedUnitPrice);
        softly.assertAll();
    }
}
