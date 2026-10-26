# Автотесты для UI веб-приложения Stellar Burgers

Стенд: `https://qa-stellarburgers.education-services.ru`

## Структура проекта

```text
src/main/java/
├── api/
│   └── ApiClient.java
├── config/
│   └── TestConfig.java
├── driver/
│   └── YandexDriverResolver.java
├── pages/
│   ├── BasePage.java
│   ├── MainPage.java
│   ├── LoginPage.java
│   ├── RegisterPage.java
│   └── ForgotPasswordPage.java
└── pojo/
    └── User.java

src/test/java/
└── ui/
    ├── BaseTest.java
    ├── LoginTest.java
    ├── RegistrationTest.java
    └── ConstructorTest.java

scripts/
└── install-yandex-chromedriver.sh
```

В `src/test/java` лежат только тесты и их базовый класс. Page Object, API-клиент,
конфигурация и вспомогательный код находятся в `src/main/java`.

Локаторы элементов находятся внутри соответствующих Page Object. Тесты не обращаются
к локаторам напрямую — только к методам страниц.

## Покрытие по заданию

| Требование | Тест |
|---|---|
| Успешная регистрация | `RegistrationTest.testSuccessfulRegistration` |
| Ошибка при некорректном пароле (< 6 символов) | `RegistrationTest.testRegistrationWithShortPasswordShowsError` |
| Минимальный пароль — 6 символов | `RegistrationTest.testRegistrationWithMinimalPasswordSucceeds` |
| Вход по кнопке «Войти в аккаунт» | `LoginTest.testLoginViaMainPageButton` |
| Вход через кнопку «Личный кабинет» | `LoginTest.testLoginViaPersonalCabinetButton` |
| Вход через кнопку в форме регистрации | `LoginTest.testLoginViaRegistrationForm` |
| Вход через кнопку в форме восстановления пароля | `LoginTest.testLoginViaForgotPasswordForm` |
| Переход к разделу «Булки» | `ConstructorTest.testGoToBunsSection` |
| Переход к разделу «Соусы» | `ConstructorTest.testGoToSaucesSection` |
| Переход к разделу «Начинки» | `ConstructorTest.testGoToFillingsSection` |

Все тестовые методы выполняются в браузере, заданном системным свойством `browser`
(по умолчанию — Google Chrome, для Яндекс Браузера — `-Dbrowser=yandex`).
Итого 10 тестовых методов.

Признак успешного входа — появление кнопки «Оформить заказ» вместо «Войти в аккаунт».

## Тестовые данные

- В `LoginTest` новый пользователь создаётся через API в `@Before` (перед каждым
  тестом) и удаляется через API в `@After`.
- В `RegistrationTest` регистрация выполняется через UI, потому что это и есть проверяемый
  сценарий. Все созданные пользователи запоминаются и удаляются через API в `@After`.
- `ApiClient` умеет регистрировать пользователя, логинить его и удалять
  (`DELETE /api/auth/user`). Тело запросов передаётся сериализацией объекта
  `pojo.User`.

## Запуск

Требуется Java 11 и Maven.

```bash
mvn test
```

Браузер задаётся системным свойством `browser` (по умолчанию — `chrome`):

```bash
mvn test -Dbrowser=yandex
```

Базовый URL можно переопределить:

```bash
mvn test -Dbase.url=https://qa-stellarburgers.education-services.ru
```

Отчёт Surefire: `target/surefire-reports`.

## Allure

Результаты прогона складываются в `target/allure-results`; свежие результаты
прогона в Chrome также приложены в корневом каталоге `allure-results/`.
HTML-отчёт генерируется командой:

```bash
mvn allure:report
open allure-report/index.html
```

В отчёте видны фичи (`@Feature`), названия тестов и все шаги
Page Object и API-клиента (`@Step`).

Шаги в IntelliJ отображаются только при установленном плагине Allure IntelliJ:
для Maven-агента (`aspectjweaver`) он подключается автоматически, а для запуска
из IDE нужно добавить `-javaagent` в настройки Run Configuration.

## Яндекс Браузер

Яндекс Браузер собран на Chromium, но его сборки нет в каталоге Chrome for Testing,
поэтому Selenium Manager не может подобрать совместимый `chromedriver` (нужна та же
мажорная версия Chromium, что и в браузере) и берёт несовместимый драйвер.

Установить подходящий драйвер в кэш Selenium Manager:

```bash
scripts/install-yandex-chromedriver.sh
```

Скрипт читает версию Chromium из `Info.plist` Яндекс Браузера и скачивает
`chromedriver` соответствующей мажорной версии. `YandexDriverResolver` затем
находит этот драйвер в кэше и подставляет его через `webdriver.chrome.driver`
(это свойство имеет приоритет над Selenium Manager).

Путь к драйверу и браузеру можно указать вручную:

```bash
mvn test -Dyandex.driver=/path/to/chromedriver -Dyandex.binary=/path/to/Yandex
```

Если версию Chromium не удалось определить автоматически, её можно задать явно:

```bash
mvn test -Dyandex.chromium.version=150
```

Если браузер или драйвер не найдены — тесты с `-Dbrowser=yandex` не падают,
а пропускаются с указанием причины.
