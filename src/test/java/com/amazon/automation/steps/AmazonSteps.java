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
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class AmazonSteps {

    private static final String AMAZON_UK_DOMAIN = "amazon.co.uk";
    private static final String AMAZON_TITLE = "Amazon";
    private static final String BOOKS_DEPARTMENT = "Books";
    private static final String ADDED_TO_BASKET_TEXT = "Added to Basket";

    private final ScenarioContext context;

    private HomePage homePage;
    private SearchResultsPage searchResultsPage;
    private ProductDetailsPage productDetailsPage;
    private BasketPage basketPage;

    private List<BookSnapshot> searchSnapshots = new ArrayList<>();
    private BookSnapshot detailsSnapshot;
    private List<BookSnapshot> basketItems;
    private String basketSubtotal;

    @Given("the user is on the Amazon UK home page with accepted cookies")
    public void theUserIsOnTheAmazonUkHomePageWithAcceptedCookies() {
        context.getUiActions().navigateTo(context.getConfig().getBaseUrl());
        homePage = new HomePage(context.getDriver(), context.getUiActions(), context.getWaits());
        homePage.acceptCookiesIfPresent();
        assertThat(context.getDriver().getCurrentUrl()).contains(AMAZON_UK_DOMAIN);
        assertThat(context.getDriver().getTitle()).containsIgnoringCase(AMAZON_TITLE);
    }

    @When("the user searches in Books for {string}")
    public void theUserSearchesInBooksFor(String keyword) {
        searchResultsPage = homePage.searchInSection(keyword, BOOKS_DEPARTMENT);
        searchSnapshots.add(searchResultsPage.firstResultSnapshot());
    }

    @Then("the first search result should contain title fragment {string} and type {string}")
    public void theFirstSearchResultShouldContainTitleFragmentAndType(String expectedTitle, String expectedType) {
        assertThat(searchSnapshots).isNotEmpty();
        BookSnapshot lastSearchSnapshot = searchSnapshots.get(searchSnapshots.size() - 1);
        assertThat(lastSearchSnapshot.title()).contains(expectedTitle);
        assertThat(lastSearchSnapshot.selectedType()).containsIgnoringCase(expectedType);
        assertThat(lastSearchSnapshot.unitPrice()).isNotBlank();
    }

    @When("the user opens the first search result details")
    public void theUserOpensTheFirstSearchResultDetails() {
        productDetailsPage = searchResultsPage.openFirstResult();
        detailsSnapshot = productDetailsPage.detailsSnapshot();
    }

    @Then("the book details page should match the selected search result title fragment {string}, type {string}, and unit price")
    public void theBookDetailsPageShouldMatchTheSelectedSearchResultTitleFragmentTypeAndUnitPrice(String expectedTitle, String expectedType) {
        assertThat(searchSnapshots).isNotEmpty();
        BookSnapshot lastSearchSnapshot = searchSnapshots.get(searchSnapshots.size() - 1);
        String expectedUnitPrice = lastSearchSnapshot.unitPrice();

        assertSnapshotMatchesTitleTypeAndUnitPrice(detailsSnapshot, expectedTitle, expectedType, expectedUnitPrice);
    }

    @When("the user adds the item to the basket")
    public void theUserAddsTheItemToTheBasket() {
        productDetailsPage.addToBasket();
    }

    @Then("the add to basket confirmation should be shown with {int} basket item")
    public void theAddToBasketConfirmationShouldBeShownWithOneBasketItem(int itemCount) {
        assertThat(productDetailsPage.isAddedToBasketMessageShown()).isTrue();
        assertThat(productDetailsPage.addedToBasketMessageTitle()).containsIgnoringCase(ADDED_TO_BASKET_TEXT);
        assertThat(productDetailsPage.basketItemCount()).isEqualTo(itemCount);
    }

    @When("the user opens the basket editor")
    public void theUserOpensTheBasketEditor() {
        basketPage = productDetailsPage.openBasketEditor();
        basketItems = basketPage.getItemSnapshots();
        basketSubtotal = basketPage.basketSubtotal();
    }

    @Then("basket details should contain the added books with title fragment, type, unit price, quantity, and total price")
    public void basketDetailsShouldContainTheAddedBooksWithTitleFragmentTypeUnitPriceQuantityAndTotalPrice() {
        assertThat(basketItems).isNotEmpty();
        assertThat(searchSnapshots).isNotEmpty();

        BigDecimal totalExpectedSubtotal = BigDecimal.ZERO;

        // Verify each added book exists in the basket with correct details
        for (BookSnapshot searchSnapshot : searchSnapshots) {
            String expectedTitle = searchSnapshot.title();
            String expectedType = searchSnapshot.selectedType();
            String expectedUnitPrice = searchSnapshot.unitPrice();

            BookSnapshot basketItem = basketItems.stream()
                    .filter(item -> item.title().contains(expectedTitle))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Expected book with title containing '" + expectedTitle + "' not found in basket"));

            assertSnapshotMatchesTitleTypeAndUnitPrice(basketItem, expectedTitle, expectedType, expectedUnitPrice);

            int quantity = extractQuantity(basketItem);

            BigDecimal unitPrice = new BigDecimal(expectedUnitPrice);
            BigDecimal itemSubtotal = unitPrice.multiply(new BigDecimal(quantity));
            totalExpectedSubtotal = totalExpectedSubtotal.add(itemSubtotal);
        }

        // Verify total basket subtotal matches sum of all items
        assertThat(basketSubtotal).isNotBlank();
        BigDecimal actualSubtotal = new BigDecimal(basketSubtotal);
        assertThat(actualSubtotal).isEqualByComparingTo(totalExpectedSubtotal);
    }

    private int extractQuantity(BookSnapshot item) {
        assertThat(item.quantity()).isNotBlank();
        String quantityStr = item.quantity().replaceAll("\\D+", "").trim();
        return Integer.parseInt(quantityStr);
    }

    private void assertSnapshotMatchesTitleTypeAndUnitPrice(BookSnapshot snapshot, String expectedTitle, String expectedType, String expectedUnitPrice) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(snapshot.title()).contains(expectedTitle);
        softly.assertThat(snapshot.selectedType()).containsIgnoringCase(expectedType);
        softly.assertThat(snapshot.unitPrice()).isEqualTo(expectedUnitPrice);
        softly.assertAll();
    }
}
