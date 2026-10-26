package api;

import config.TestConfig;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import pojo.User;

import static io.restassured.RestAssured.given;

public final class ApiClient {

    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String USER_PATH = "/api/auth/user";
    private static final int DELETE_ATTEMPTS = 3;

    private ApiClient() {
    }

    @Step("Зарегистрировать пользователя {email}")
    public static void registerUser(String name, String email, String password) {
        given()
                .baseUri(TestConfig.BASE_URL)
                .contentType(ContentType.JSON)
                .body(new User(name, email, password))
        .when()
                .post(REGISTER_PATH)
        .then()
                .statusCode(200);
    }

    @Step("Выполнить логин пользователя {email}")
    public static String login(String email, String password) {
        Response response = given()
                .baseUri(TestConfig.BASE_URL)
                .contentType(ContentType.JSON)
                .body(new User(null, email, password))
        .when()
                .post(LOGIN_PATH);

        if (response.statusCode() != 200) {
            return null;
        }
        return response.jsonPath().getString("accessToken");
    }

    @Step("Удалить пользователя {email}, если он существует")
    public static boolean deleteUserIfExists(String email, String password) {
        for (int attempt = 1; attempt <= DELETE_ATTEMPTS; attempt++) {
            try {
                String token = login(email, password);
                if (token == null || token.isEmpty()) {
                    return false;
                }

                int status = given()
                        .baseUri(TestConfig.BASE_URL)
                        .header("Authorization", normalizeToken(token))
                .when()
                        .delete(USER_PATH)
                        .statusCode();

                if (status == 200 || status == 202) {
                    return true;
                }
            } catch (Exception e) {

            }

            if (attempt < DELETE_ATTEMPTS) {
                sleepBeforeRetry();
            }
        }
        return false;
    }

    private static void sleepBeforeRetry() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String normalizeToken(String token) {
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
