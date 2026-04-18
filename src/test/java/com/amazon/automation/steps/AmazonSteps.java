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
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class AmazonSteps {

    private static final String AMAZON_UK_DOMAIN = "amazon.co.uk";
    private static final String AMAZON_TITLE = "Amazon";
    private static final String BOOKS_DEPARTMENT = "Books";
    private static final String ADDED_TO_BASKET_TEXT = "Added to Basket";
    private static final String SINGLE_ITEM_QUANTITY = "1";

    private final ScenarioContext context;

    private HomePage homePage;
    private SearchResultsPage searchResultsPage;
    private ProductDetailsPage productDetailsPage;
    private BasketPage basketPage;

    private BookSnapshot searchSnapshot;
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
        searchSnapshot = searchResultsPage.firstResultSnapshot();
    }

    @Then("the first search result should contain title fragment {string} and type {string}")
    public void theFirstSearchResultShouldContainTitleFragmentAndType(String expectedTitle, String expectedType) {
        assertThat(searchSnapshot.title()).contains(expectedTitle);
        assertThat(searchSnapshot.selectedType()).containsIgnoringCase(expectedType);
        assertThat(searchSnapshot.unitPrice()).isNotBlank();
    }

    @When("the user opens the first search result details")
    public void theUserOpensTheFirstSearchResultDetails() {
        productDetailsPage = searchResultsPage.openFirstResult();
        detailsSnapshot = productDetailsPage.detailsSnapshot();
    }

    @Then("the book details page should match the selected search result title fragment {string}, type {string}, and unit price")
    public void theBookDetailsPageShouldMatchTheSelectedSearchResultTitleFragmentTypeAndUnitPrice(String expectedTitle, String expectedType) {
        String expectedUnitPrice = normalizePrice(searchSnapshot.unitPrice());

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

    @Then("basket details should match the selected search result title fragment {string}, type {string}, unit price, and quantity {string}")
    public void basketDetailsShouldMatchTheSelectedSearchResultTitleFragmentTypeUnitPriceAndQuantity(String expectedTitle, String expectedType, String expectedQuantity) {
        String expectedUnitPrice = normalizePrice(searchSnapshot.unitPrice());

        assertThat(basketItems)
                .anySatisfy(item -> assertBasketItemMatches(item, expectedTitle, expectedType, expectedUnitPrice, expectedQuantity));

        assertThat(basketSubtotal).isNotBlank();

        if (SINGLE_ITEM_QUANTITY.equals(expectedQuantity)) {
            assertThat(normalizePrice(basketSubtotal)).isEqualTo(expectedUnitPrice);
        }
    }

    private void assertBasketItemMatches(BookSnapshot item, String expectedTitle, String expectedType, String expectedUnitPrice, String expectedQuantity) {
        SoftAssertions softly = new SoftAssertions();
        assertSnapshotMatchesTitleTypeAndUnitPrice(softly, item, expectedTitle, expectedType, expectedUnitPrice);
        softly.assertThat(item.quantity()).contains(expectedQuantity);
        softly.assertAll();
    }

    private void assertSnapshotMatchesTitleTypeAndUnitPrice(BookSnapshot snapshot, String expectedTitle, String expectedType, String expectedUnitPrice) {
        SoftAssertions softly = new SoftAssertions();
        assertSnapshotMatchesTitleTypeAndUnitPrice(softly, snapshot, expectedTitle, expectedType, expectedUnitPrice);
        softly.assertAll();
    }

    private void assertSnapshotMatchesTitleTypeAndUnitPrice(SoftAssertions softly, BookSnapshot snapshot, String expectedTitle, String expectedType, String expectedUnitPrice) {
        softly.assertThat(snapshot.title()).contains(expectedTitle);
        softly.assertThat(snapshot.selectedType()).containsIgnoringCase(expectedType);
        softly.assertThat(normalizePrice(snapshot.unitPrice())).isEqualTo(expectedUnitPrice);
    }

    private String normalizePrice(String value) {
        return value.replace('\u00A0', ' ')
                .replaceAll("\\s+", "")
                .trim();
    }
}
