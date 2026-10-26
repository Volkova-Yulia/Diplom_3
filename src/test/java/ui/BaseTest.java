package ui;

import driver.YandexDriverResolver;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.UUID;

public class BaseTest {

    protected WebDriver driver;

    private static final String BROWSER = System.getProperty("browser", "chrome");

    private static final String YANDEX_BINARY =
            System.getProperty("yandex.binary", defaultYandexBinary());

    @Before
    public void setUp() {
        if ("chrome".equalsIgnoreCase(BROWSER)) {
            driver = new ChromeDriver();
        } else if ("yandex".equalsIgnoreCase(BROWSER)) {
            driver = createYandexDriver();
        } else {
            throw new IllegalArgumentException("Неизвестный браузер: " + BROWSER);
        }
        driver.manage().window().maximize();
    }

    private WebDriver createYandexDriver() {
        File yandex = new File(YANDEX_BINARY);

        Assume.assumeTrue("Яндекс Браузер не найден по пути: " + YANDEX_BINARY,
                yandex.exists() && yandex.canExecute());

        String yandexDriver = YandexDriverResolver.resolve(YANDEX_BINARY);
        Assume.assumeTrue("Не найден chromedriver, совместимый с Яндекс Браузером. "
                        + "Установите его скриптом scripts/install-yandex-chromedriver.sh "
                        + "или укажите вручную: -Dyandex.driver=/path/to/chromedriver",
                yandexDriver != null);

        ChromeOptions options = new ChromeOptions();
        options.setBinary(yandex.getAbsolutePath());

        String previousDriver = System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY);
        try {
            System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, yandexDriver);
            return new ChromeDriver(options);
        } finally {
            if (previousDriver == null) {
                System.clearProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY);
            } else {
                System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, previousDriver);
            }
        }
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    protected String generateRandomEmail() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8) + "@yandex.ru";
    }

    private static String defaultYandexBinary() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            String baseDirectory = localAppData == null || localAppData.isEmpty()
                    ? "C:/Users/User/AppData/Local"
                    : localAppData;
            return baseDirectory + "/Yandex/YandexBrowser/Application/browser.exe";
        }
        if (os.contains("mac")) {
            return "/Applications/Yandex.app/Contents/MacOS/Yandex";
        }
        return "/usr/bin/yandex-browser";
    }
}
