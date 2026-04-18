Feature: Amazon UK books - search to basket validation
  As a customer on Amazon UK
  I want to search for a specific book and add it to my basket
  So that I can validate key product details across pages

  @e2e @books
  Scenario Outline: Search and add a paperback book to basket
    Given the user is on the Amazon UK home page with accepted cookies
    When the user searches in Books for "<searchKeyword>"
    Then the first search result should contain title fragment "<expectedTitle>" and type "<expectedType>"
    When the user opens the first search result details
    Then the book details page should match the selected search result title fragment "<expectedTitle>", type "<expectedType>", and unit price
    When the user adds the item to the basket
    Then the add to basket confirmation should be shown with <expectedQuantity> basket item
    When the user opens the basket editor
    Then basket details should contain the added books with title fragment, type, unit price, quantity, and total price

    Examples:
      | searchKeyword                     | expectedTitle                                         | expectedType | expectedQuantity |
      | Harry Potter and the Cursed Child | Harry Potter and the Cursed Child - Parts One and Two | Paperback    |                1 |
