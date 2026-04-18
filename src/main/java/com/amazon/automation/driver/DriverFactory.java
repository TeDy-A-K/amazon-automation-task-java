package com.amazon.automation.driver;

import com.amazon.automation.config.FrameworkConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

@Log4j2
public final class DriverFactory {

    public static final DriverFactory INSTANCE = new DriverFactory();

    private static final ThreadLocal<WebDriver> DRIVER_HOLDER = new ThreadLocal<>();

    private DriverFactory() {
        // Singleton - private constructor
    }

    public WebDriver createDriver(FrameworkConfig config) {
        String browser = config.getBrowser().toLowerCase();
        WebDriver driver = switch (browser) {
            case "chrome" -> createChrome(config);
            case "firefox" -> createFirefox(config);
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };

        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().window().maximize();
        DRIVER_HOLDER.set(driver);
        log.info("Created {} driver with implicit wait set to 0", browser);
        return driver;
    }

    public WebDriver getDriver() {
        WebDriver driver = DRIVER_HOLDER.get();
        if (driver == null) {
            throw new IllegalStateException("Driver is not initialized for current thread");
        }
        return driver;
    }

    public void quitDriver() {
        WebDriver driver = DRIVER_HOLDER.get();
        if (driver != null) {
            driver.quit();
            DRIVER_HOLDER.remove();
            log.info("Closed browser session");
        }
    }

    private WebDriver createChrome(FrameworkConfig config) {
        if (config.useWebDriverManager()) {
            WebDriverManager.chromedriver().setup();
            log.info("Using WebDriverManager for Chrome");
        }
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        if (config.isHeadless()) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return new ChromeDriver(options);
    }

    private WebDriver createFirefox(FrameworkConfig config) {
        if (config.useWebDriverManager()) {
            WebDriverManager.firefoxdriver().setup();
            log.info("Using WebDriverManager for Firefox");
        }
        FirefoxOptions options = new FirefoxOptions();
        if (config.isHeadless()) {
            options.addArguments("-headless");
        }
        return new FirefoxDriver(options);
    }
}
