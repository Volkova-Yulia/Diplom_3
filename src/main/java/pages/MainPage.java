package pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MainPage extends BasePage {

    private static final By LOGIN_ACCOUNT_BUTTON = By.xpath(".//button[text()='Войти в аккаунт']");
    private static final By PERSONAL_CABINET_LINK =
            By.xpath(".//header//a[@href='/account'][.//p[text()='Личный Кабинет']]");
    private static final By ORDER_BUTTON = By.xpath(".//button[text()='Оформить заказ']");

    private static final By BUNS_TAB = By.xpath(".//span[text()='Булки']/..");
    private static final By SAUCES_TAB = By.xpath(".//span[text()='Соусы']/..");
    private static final By FILLINGS_TAB = By.xpath(".//span[text()='Начинки']/..");

    private static final String ACTIVE_TAB_CLASS_PART = "tab_tab_type_current";

    public MainPage(WebDriver driver) {
        super(driver);
    }

    @Step("Кликнуть по кнопке 'Войти в аккаунт' на главной странице")
    public void clickLoginButton() {
        click(LOGIN_ACCOUNT_BUTTON);
    }

    @Step("Кликнуть по ссылке 'Личный кабинет' в шапке сайта")
    public void clickPersonalCabinetLink() {
        click(PERSONAL_CABINET_LINK);
    }

    @Step("Перейти к разделу 'Булки'")
    public void clickBunsTab() {
        click(BUNS_TAB);
    }

    @Step("Перейти к разделу 'Соусы'")
    public void clickSaucesTab() {
        click(SAUCES_TAB);
    }

    @Step("Перейти к разделу 'Начинки'")
    public void clickFillingsTab() {
        click(FILLINGS_TAB);
    }

    @Step("Проверить, что активен раздел 'Булки'")
    public boolean isBunsTabActive() {
        return waitForActiveTab(BUNS_TAB);
    }

    @Step("Проверить, что активен раздел 'Соусы'")
    public boolean isSaucesTabActive() {
        return waitForActiveTab(SAUCES_TAB);
    }

    @Step("Проверить, что активен раздел 'Начинки'")
    public boolean isFillingsTabActive() {
        return waitForActiveTab(FILLINGS_TAB);
    }

    private boolean waitForActiveTab(By tabLocator) {
        try {
            return wait.until(ExpectedConditions.attributeContains(
                    tabLocator, "class", ACTIVE_TAB_CLASS_PART));
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Step("Дождаться появления кнопки 'Оформить заказ'")
    public boolean waitForOrderButtonDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(ORDER_BUTTON)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
}
