package pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ForgotPasswordPage extends BasePage {

    private static final By LOGIN_LINK = By.xpath(".//a[@href='/login' and text()='Войти']");

    public ForgotPasswordPage(WebDriver driver) {
        super(driver);
    }

    @Step("Кликнуть по ссылке 'Войти' на странице восстановления пароля")
    public void clickLoginLink() {
        click(LOGIN_LINK);
    }
}
