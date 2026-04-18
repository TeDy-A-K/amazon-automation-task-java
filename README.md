# amazon-automation-task-java

Java 25 LTS + Maven BDD UI automation framework for Amazon UK using Cucumber, Selenium, PicoContainer DI, AssertJ, Log4j2, and Lombok.

## Scenario currently automated
- Navigate to amazon.co.uk 
  - Verify that the page is correct and opened 
- In section books search for `Harry Potter and the Cursed Child`
  - Verify that the first item has the title: `Harry Potter and the Cursed Child - Parts One & Two`
  - Verify if it has a badge if any 
  - Verify the selected type 
  - Verify the price 
- Then navigate to the book details 
  - Verify the title 
  - Verify the badge if any 
  - Verify the price 
  - Verify that type is `Paperback ` 
- Add the book to the basket 
  - Verify that the notification is shown 
  - With the title `Added to Basket` 
  - There is `one` item in the basket 
-  Click on edit the basket 
  - Verify that the book is shown on the list 
  - Verify that the title, type of print is the same as on the search page, price is the same as on the search page, quantity is `1`, and total price

## Run

```bash
mvn clean test
```

Generated report after execution:
- Cucumber HTML report: `target/cucumber/cucumber.html`

Default Cucumber tag filter: `@e2e` via `src/test/resources/cucumber.properties`

Override the default tag filter from the command line:

```bash
mvn test "-Dcucumber.filter.tags=@books"
```

## Config

Default config file: `src/test/resources/config/framework.properties`

Cucumber execution defaults are configured in `src/test/resources/cucumber.properties` and can be overridden with Java system properties passed to Maven.

Override priority:
1. Environment variables
2. Property file values