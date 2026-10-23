package config;

public final class TestConfig {

    public static final String BASE_URL = System.getProperty(
            "base.url", "https://qa-stellarburgers.education-services.ru");

    private TestConfig() {
    }
}
