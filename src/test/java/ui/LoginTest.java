package ui;

import api.ApiClient;
import io.qameta.allure.Feature;
import io.qameta.allure.junit4.DisplayName;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pages.ForgotPasswordPage;
import pages.LoginPage;
import pages.MainPage;
import pages.RegisterPage;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@Feature("Вход в систему")
public class LoginTest extends BaseTest {

    private static final String USER_NAME = "ТестИван";
    private static final String USER_PASSWORD = "password123";

    private static String email;

    public LoginTest(String browser) {
        super(browser);
    }

    @BeforeClass
    public static void createTestUser() {
        email = "login_user_" + UUID.randomUUID().toString().substring(0, 8) + "@yandex.ru";
        ApiClient.registerUser(USER_NAME, email, USER_PASSWORD);
    }

    @AfterClass
    public static void deleteTestUser() {
        if (!ApiClient.deleteUserIfExists(email, USER_PASSWORD)) {
            System.err.println("Не удалось удалить тестового пользователя: " + email);
        }
    }

    @Test
    @DisplayName("Вход по кнопке 'Войти в аккаунт' на главной")
    public void testLoginViaMainPageButton() {
        MainPage mainPage = new MainPage(driver);
        mainPage.openUrl("");
        mainPage.clickLoginButton();

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(email, USER_PASSWORD);

        assertTrue("Авторизация не удалась: кнопка 'Оформить заказ' не появилась",
                mainPage.waitForOrderButtonDisplayed());
    }

    @Test
    @DisplayName("Вход через кнопку 'Личный кабинет'")
    public void testLoginViaPersonalCabinetButton() {
        MainPage mainPage = new MainPage(driver);
        mainPage.openUrl("");
        mainPage.clickPersonalCabinetLink();

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(email, USER_PASSWORD);

        assertTrue("Авторизация через личный кабинет не удалась",
                mainPage.waitForOrderButtonDisplayed());
    }

    @Test
    @DisplayName("Вход через кнопку в форме регистрации")
    public void testLoginViaRegistrationForm() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.openUrl("register");
        registerPage.clickLoginLink();

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(email, USER_PASSWORD);

        MainPage mainPage = new MainPage(driver);
        assertTrue("Авторизация через форму регистрации не удалась",
                mainPage.waitForOrderButtonDisplayed());
    }

    @Test
    @DisplayName("Вход через кнопку в форме восстановления пароля")
    public void testLoginViaForgotPasswordForm() {
        ForgotPasswordPage forgotPasswordPage = new ForgotPasswordPage(driver);
        forgotPasswordPage.openUrl("forgot-password");
        forgotPasswordPage.clickLoginLink();

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(email, USER_PASSWORD);

        MainPage mainPage = new MainPage(driver);
        assertTrue("Авторизация через форму восстановления не удалась",
                mainPage.waitForOrderButtonDisplayed());
    }
}
