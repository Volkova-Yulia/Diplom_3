package pages;

import config.TestConfig;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {

    private static final By HEADER_FEED_LINK = By.xpath(".//header//a[@href='/feed']");

    private static final Duration ELEMENT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration APP_READY_TIMEOUT = Duration.ofSeconds(15);
    private static final int MAX_LOAD_ATTEMPTS = 3;

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;

        this.wait = new WebDriverWait(driver, ELEMENT_TIMEOUT);
    }

    @Step("Открыть страницу по относительному URL: {relativeUrl}")
    public void openUrl(String relativeUrl) {
        String path = relativeUrl == null ? "" : relativeUrl.trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        driver.get(TestConfig.BASE_URL + "/" + path);

        for (int attempt = 1; attempt <= MAX_LOAD_ATTEMPTS; attempt++) {
            if (waitForAppReady()) {
                return;
            }
            if (attempt < MAX_LOAD_ATTEMPTS) {
                driver.navigate().refresh();
            }
        }
        throw new TimeoutException("Приложение не отрисовалось: url=" + driver.getCurrentUrl()
                + ", title=" + driver.getTitle()
                + ", размер DOM=" + driver.getPageSource().length()
                + ", страница ошибки Chrome=" + isChromeErrorPage());
    }

    private boolean waitForAppReady() {
        try {
            new WebDriverWait(driver, APP_READY_TIMEOUT)
                    .until(ExpectedConditions.presenceOfElementLocated(HEADER_FEED_LINK));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private boolean isChromeErrorPage() {
        String title = driver.getTitle();
        if (title == null || title.isEmpty()) {
            return true;
        }
        String host = driver.getCurrentUrl().replaceFirst("^https?://", "").split("/")[0];
        return title.equals(host);
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {

        WebElement element = wait.until(driver -> {
            try {
                WebElement found = driver.findElement(locator);
                found.getTagName();
                return found;
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                return null;
            }
        });
        element.clear();
        element.sendKeys(text);
    }
}
