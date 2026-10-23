package ui;

import io.qameta.allure.Feature;
import io.qameta.allure.junit4.DisplayName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pages.MainPage;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@Feature("Раздел 'Конструктор'")
public class ConstructorTest extends BaseTest {

    public ConstructorTest(String browser) {
        super(browser);
    }

    @Test
    @DisplayName("Переход к разделу 'Булки'")
    public void testGoToBunsSection() {
        MainPage mainPage = new MainPage(driver);
        mainPage.openUrl("");

        mainPage.clickSaucesTab();
        mainPage.clickBunsTab();

        assertTrue("Раздел 'Булки' не стал активным", mainPage.isBunsTabActive());
    }

    @Test
    @DisplayName("Переход к разделу 'Соусы'")
    public void testGoToSaucesSection() {
        MainPage mainPage = new MainPage(driver);
        mainPage.openUrl("");

        mainPage.clickSaucesTab();

        assertTrue("Раздел 'Соусы' не стал активным", mainPage.isSaucesTabActive());
    }

    @Test
    @DisplayName("Переход к разделу 'Начинки'")
    public void testGoToFillingsSection() {
        MainPage mainPage = new MainPage(driver);
        mainPage.openUrl("");

        mainPage.clickFillingsTab();

        assertTrue("Раздел 'Начинки' не стал активным", mainPage.isFillingsTabActive());
    }
}
