package pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {

    private static final By EMAIL_INPUT = By.xpath(".//label[text()='Email']/../input");
    private static final By PASSWORD_INPUT = By.xpath(".//label[text()='Пароль']/../input");
    private static final By LOGIN_SUBMIT_BUTTON = By.xpath(".//button[text()='Войти']");
    private static final By LOGIN_PAGE_TITLE = By.xpath(".//h2[text()='Вход']");
    private static final By REGISTER_LINK = By.xpath(".//a[@href='/register']");
    private static final By FORGOT_PASSWORD_LINK = By.xpath(".//a[@href='/forgot-password']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Step("Авторизоваться с email: {email}")
    public void login(String email, String password) {

        wait.until(ExpectedConditions.presenceOfElementLocated(LOGIN_PAGE_TITLE));
        type(EMAIL_INPUT, email);
        type(PASSWORD_INPUT, password);
        click(LOGIN_SUBMIT_BUTTON);
    }

    @Step("Дождаться страницы входа")
    public boolean waitForLoginPageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_PAGE_TITLE)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Step("Кликнуть по ссылке 'Зарегистрироваться'")
    public void clickRegisterLink() {
        click(REGISTER_LINK);
    }

    @Step("Кликнуть по ссылке 'Восстановить пароль'")
    public void clickForgotPasswordLink() {
        click(FORGOT_PASSWORD_LINK);
    }
}
