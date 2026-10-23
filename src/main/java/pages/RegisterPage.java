package pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class RegisterPage extends BasePage {

    private static final By NAME_INPUT = By.xpath(".//label[text()='Имя']/../input");
    private static final By EMAIL_INPUT = By.xpath(".//label[text()='Email']/../input");
    private static final By PASSWORD_INPUT = By.xpath(".//label[text()='Пароль']/../input");
    private static final By REGISTER_BUTTON = By.xpath(".//button[text()='Зарегистрироваться']");
    private static final By PASSWORD_ERROR = By.xpath(".//p[text()='Некорректный пароль']");
    private static final By LOGIN_LINK = By.xpath(".//a[@href='/login' and text()='Войти']");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    @Step("Зарегистрировать пользователя с именем: {name}, email: {email}")
    public void register(String name, String email, String password) {
        type(NAME_INPUT, name);
        type(EMAIL_INPUT, email);
        type(PASSWORD_INPUT, password);
        click(REGISTER_BUTTON);
    }

    @Step("Кликнуть по ссылке 'Войти' на странице регистрации")
    public void clickLoginLink() {
        click(LOGIN_LINK);
    }

    @Step("Дождаться сообщения об ошибке некорректного пароля")
    public boolean waitForPasswordErrorDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_ERROR)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
}
