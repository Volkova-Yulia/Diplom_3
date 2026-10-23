package ui;

import api.ApiClient;
import io.qameta.allure.Feature;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pages.LoginPage;
import pages.RegisterPage;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@Feature("Регистрация")
public class RegistrationTest extends BaseTest {

    private static final String MINIMAL_PASSWORD = "123456";

    private final Map<String, String> createdUsers = new LinkedHashMap<>();

    public RegistrationTest(String browser) {
        super(browser);
    }

    @After
    public void deleteCreatedUsers() {
        createdUsers.forEach((email, password) -> {
            if (!ApiClient.deleteUserIfExists(email, password)) {
                System.err.println("Не удалось удалить тестового пользователя: " + email);
            }
        });
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    public void testSuccessfulRegistration() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.openUrl("register");

        String email = generateRandomEmail();
        String password = "password123";
        createdUsers.put(email, password);

        registerPage.register("ТестИван", email, password);

        LoginPage loginPage = new LoginPage(driver);
        assertTrue("Не произошло перенаправление на страницу входа",
                loginPage.waitForLoginPageDisplayed());
    }

    @Test
    @DisplayName("Отображение ошибки при вводе некорректного короткого пароля (< 6 символов)")
    public void testRegistrationWithShortPasswordShowsError() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.openUrl("register");

        registerPage.register("ТестИван", generateRandomEmail(), "12345");

        assertTrue("Сообщение об ошибке некорректного пароля не появилось",
                registerPage.waitForPasswordErrorDisplayed());
    }

    @Test
    @DisplayName("Успешная регистрация с минимальным паролем (6 символов)")
    public void testRegistrationWithMinimalPasswordSucceeds() {
        RegisterPage registerPage = new RegisterPage(driver);
        registerPage.openUrl("register");

        String email = generateRandomEmail();
        createdUsers.put(email, MINIMAL_PASSWORD);

        registerPage.register("ТестИван", email, MINIMAL_PASSWORD);

        LoginPage loginPage = new LoginPage(driver);
        assertTrue("Регистрация с минимальным паролем не удалась",
                loginPage.waitForLoginPageDisplayed());
    }
}
