package ru.gamble.stepdefs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.ru.Когда;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import net.minidev.json.JSONValue;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.datajack.Stash;
import ru.sbtqa.tag.datajack.exceptions.DataException;
import ru.sbtqa.tag.pagefactory.Page;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.exceptions.PageException;
import ru.sbtqa.tag.pagefactory.exceptions.PageInitializationException;
import ru.sbtqa.tag.qautils.errors.AutotestError;
import ru.sbtqa.tag.stepdefs.GenericStepDefs;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.openqa.selenium.By.xpath;
import static ru.gamble.stepdefs.Constants.STARTING_URL;


public class CommonStepDefs extends GenericStepDefs {
    private static final Logger LOG = LoggerFactory.getLogger(CommonStepDefs.class);
    static WebDriver driver = PageFactory.getDriver();
    private static final String sep = File.separator;
//private static StringBuilder tmp=new StringBuilder();

    public static String getSMSCode(String phone){
        String currentHandle = driver.getWindowHandle();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String registrationUrl = "";

        try {
            registrationUrl =  JsonLoader.getData().get(STARTING_URL).get("REGISTRATION_URL").getValue();
        } catch (DataException e) {
            LOG.error(e.getMessage());
        }

        js.executeScript("registration_window = window.open('" + registrationUrl + "')");

        Set<String> windows = driver.getWindowHandles();
        windows.remove(currentHandle);
        String newWindow = windows.toArray()[0].toString();

        driver.switchTo().window(newWindow);

        String xpath = "//li/a[contains(text(),'" + phone + "')]";
        WebElement numberSring = null;
        int x = 0;

        LOG.info("Пытаемся найти код подтверждения телефона");
        for(int y = 0; y < 5; y++) {

            try {
                LOG.info("Ожидаем 2 сек. для сервера TEST_INT");
                Thread.sleep(2000);
                new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'Статус регистрации пользователя')]")));
                if (driver.findElements(By.xpath(xpath)).isEmpty()){
                    driver.navigate().refresh();
                }
                else {
                    numberSring = driver.findElements(By.xpath(xpath)).get(0);
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
        }

        if(numberSring != null && !numberSring.getAttribute("innerText").isEmpty()) {
            String code = numberSring.getAttribute("innerText").split(" - ")[1];
            driver.switchTo().window(currentHandle);
            js.executeScript("registration_window.close()");
            return code;
        }else {
            throw new AutotestError("Ошибка! SMS-код не найден.[" + x + "] раз обновили страницу [" + driver.getCurrentUrl() + "] не найдя номер[" +  phone + "]");
        }
    }

    public static void pressButton(String param) {
        Page page;
        WebElement button;
        try {
            page = PageFactory.getInstance().getCurrentPage();
            button = page.getElementByTitle(param);
            button.click();
        } catch (PageInitializationException e) {
            e.printStackTrace();
        } catch (PageException e) {
            throw new AutotestError("Ошибка! Не удалось нажать на копку [" + param + "]\n" + e.getMessage());
        }

    }

    @Когда("^ждем некоторое время \"([^\"]*)\"$")
    public void waiting(String sec) throws InterruptedException {
        int seconds;
        if (sec.matches("^[0-9]+")) {
            seconds = Integer.parseInt(sec);
        }
        else
        {
            seconds = Integer.parseInt(Stash.getValue(sec));
        }
        Thread.sleep(seconds*1000);
    }

    @Когда("^запрашиваем дату-время и сохраняем в память$")
    public static void requestAndSaveToMamory(DataTable dataTable) {
        List<String> data = dataTable.asList(String.class);
        String key, value, date;
        key = data.get(0);
        value = data.get(1);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (value.equals("Current")) {
            date = formatter.format(System.currentTimeMillis());
            Stash.put(key, date);
            LOG.info(key + "<==[" + date + "]");
        }
    }


    // Ожидание появления элемента на странице
    public static void waitShowElement(By by) {
        WebDriverWait driverWait = new WebDriverWait(driver, 6, 500);
        try {
            driverWait.until(ExpectedConditions.visibilityOfElementLocated(by));
            List<WebElement> preloaders = driver.findElements(by);
            LOG.info("Найдено прелоадеров [" + preloaders.size() + "]");
            driverWait.until(ExpectedConditions.invisibilityOfAllElements(preloaders));
            LOG.info("Прелоадеры закрылись");
        } catch (Exception e) {
        }
    }

    @Когда("^разлогиниваем пользователя$")
    public void logOut() throws AWTException {
        LOG.info("Переход на главную страницу");
        goToMainPage("site");
        cleanCookies();
        descktopSiteLogOut(driver);

    }


    private void descktopSiteLogOut(WebDriver driver){
        WebDriverWait wait = new WebDriverWait(driver,20);
        LOG.info("Ищем наличие кнопки с силуетом пользователя.");
        List<WebElement> userMan = driver.findElements(By.id("user-icon"));
        List<WebElement> continueRegistartion = driver.findElements(By.id("continue-registration"));//возможно на сате залогинен пользователь , не окончивший регистрацию. тогда балнса и икони у него не будет,  абудт кнопка "продолжить регу"
        if (!userMan.isEmpty()){
            LOG.info("Нажимаем на кнопку с силуетом пользователя.");
            userMan.get(0).click();
            wait
                    .withMessage("Нажали на значок пользователя, но не появилась кнопка для выхода")
                    .until(ExpectedConditions.attributeContains(By.xpath("//div[contains(@class,'subMenuArea user-menu')]"),"class","active"));
            LOG.info("Ищем кнопку 'Выход' и нажимаем");
            int indexX = driver.findElement(By.id("log-out-button")).getLocation().getX() + driver.findElement(By.id("log-out-button")).getSize().getWidth()/2;
            int indexY = driver.findElement(By.id("log-out-button")).getLocation().getY() + driver.findElement(By.id("log-out-button")).getSize().getHeight()/2;
//            new Actions(driver).moveByOffset(indexX,indexY).click().build().perform();
            try {
                Robot r = new Robot();
                r.mouseMove(indexX,indexY);
                r.mousePress(InputEvent.BUTTON1_MASK);
                r.mouseRelease(InputEvent.BUTTON1_MASK);
            } catch (AWTException e) {
                e.printStackTrace();
            }


            // LOG.info("COORDINATE:" + MouseInfo.getPointerInfo().getLocation() + driver.findElement(By.id("log-out-button")).getLocation());
            //   new Actions(driver).moveToElement(driver.findElement(By.id("log-out-button")),10,0).click().build().perform();

//            driver.findElement(By.id("log-out-button")).click();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.navigate().refresh();
            LOG.info("Обновили страницу на всякий случай");
            wait
                    .withMessage("Разлогинивали-разлогинивали, да не ралогинили. На сайте все еще кто-то авторизован")
                    .until(ExpectedConditions.numberOfElementsToBe(By.id("user-icon"),0));
            wait
                    .withMessage("Разлогинивали-разлогинивали, да не ралогинили. На сайте все еще отображается баланс")
                    .until(ExpectedConditions.numberOfElementsToBe(By.id("topPanelWalletBalance"),0));
        }
        else if (!continueRegistartion.isEmpty()){
            driver.findElement(By.id("terminate-registration-logout-button")).click();
            wait
                    .withMessage("Разлогинивали-разлогинивали, да не ралогинили. На сайте все еще кто-то авторизован")
                    .until(ExpectedConditions.numberOfElementsToBe(By.id("continue-registration"),0));
        }
        else{
            LOG.info("На сайте никто не авторизован");
        }
    }

    private void mobileSiteLogOut(WebDriver driver) {
        try {
            LOG.info("Ищем наличие ссылки депозита.");
            new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@href='/private/balance/deposit']")));
            WebElement menuSwitchButton = driver.findElement(By.xpath("//label[@class='header__button header__menu-switch']"));
            LOG.info("Нажимаем на кнопку 'MenuSwitch'");
            menuSwitchButton.click();
            Thread.sleep(500);
            LOG.info("Ищем кнопку 'Выход' нажимаем и обновляем страницу");
            driver.findElement(By.xpath("//span[contains(.,'Выход')]")).click();
            driver.navigate().refresh();
        } catch (Exception e) {
            LOG.info("На сайте никто не авторизован");
        }
    }


    // Метод перехода на главную страницу
    @Когда("^переходит на главную страницу$")
    public static void goToMainPage() {
        goToMainPage("site");
    }

    @Когда("^переходит в админку$")
    public static void goToAdminPage() {
        goToMainPage("admin");
    }


    /**
     * Метод параметризованного перехода на страницу по siteUrl
     * или по-умолчанию указанную в файле application.properties
     *
     * @param siteUrl - URL страницы
     */
    @Когда("^переходит на страницу '(.+)'$")
    public static void goToMainPage(String siteUrl) {
        String currentUrl;
        try {
            switch (siteUrl) {
                case "site":
                    currentUrl = Stash.getValue("MAIN_URL");
                    break;
                case "admin":
                    currentUrl = JsonLoader.getData().get(STARTING_URL).get("ADMIN_URL").getValue();
                    break;
                case "registr":
                    currentUrl = JsonLoader.getData().get(STARTING_URL).get("REGISTRATION_URL").getValue();
                    break;
                default:
                    currentUrl = siteUrl;
                    break;
            }
            driver.get(currentUrl);
            LOG.info("Перешли на страницу [" + currentUrl + "]");
        } catch (DataException e) {
            LOG.error(e.getMessage());
        }

    }

    /**
     * Генератор e-mail
     *
     * @param key - ключ по которому сохраняем е-mail в памяти.
     */
    @Когда("^генерим email в \"([^\"]*)\"$")
    public static void generateEmailAndSave(String key) {
        String value = "testregistrator" + System.currentTimeMillis() + "@mailinator.com";
        LOG.info("Сохраняем в память key[" + key + "] <== value[" + value + "]");
        Stash.put(key, value);
    }



    /**
     * Инициализируйте страницу с соответствующим заголовком (определенным через
     * {@link ru.sbtqa.tag.pagefactory.annotations.PageEntry} аннотация)
     * Пользователь|Он ключевые слова являются необязательными
     *
     * @param title название страницы для инициализации
     * @throws PageInitializationException при неудачной инициализации страницы
     */
    public void openPage(String title) throws PageInitializationException {
        for (String windowHandle : driver.getWindowHandles()) {
            driver.switchTo().window(windowHandle);
        }
        PageFactory.getInstance().getPage(title);
    }


    /**
     * ожидание пока аттрибут без учета регистра будет содержать подстроку
     *
     * @param locator
     * @param attribute
     * @param value
     * @return
     */
    public static ExpectedCondition<Boolean> attributeContainsLowerCase(final By locator,
                                                                        final String attribute,
                                                                        final String value) {
        return new ExpectedCondition<Boolean>() {
            private String currentValue = "";

            @Override
            public Boolean apply(WebDriver driver) {
                return driver.findElement(locator).getAttribute(attribute).toLowerCase().contains(value.toLowerCase());
            }

            @Override
            public String toString() {
                return String.format("value to contain \"%s\". Current value: \"%s\"", value, currentValue);
            }
        };
    }


    public static ExpectedCondition<Boolean> attributeContainsLowerCase(final WebElement element,
                                                                        final String attribute,
                                                                        final String value) {
        return new ExpectedCondition<Boolean>() {
            private String currentValue = "";

            @Override
            public Boolean apply(WebDriver driver) {
                return element.getAttribute(attribute).toLowerCase().contains(value.toLowerCase());
            }

            @Override
            public String toString() {
                return String.format("value to contain \"%s\". Current value: \"%s\"", value, currentValue);
            }
        };
    }


    public static void waitOfPreloader() {
        waitOfPreloader(60);
    }

    public static void waitOfPreloader(int num) {
        LOG.debug("Проверка на наличие бесконечных прелоадеров");
        List<WebElement> list = driver.findElements(By.cssSelector("div.preloader__container"));
        int count = num;
        try {
            do {
                System.out.println(count);
                LOG.debug("List size is " + list.size());
                for (WebElement preloader : list) {
                    if (preloader.isDisplayed()) {
                        LOG.debug("Данный прелоадер виден");
                        count--;
                        Thread.sleep(500);
                        count--;
                        continue;
                    } else {
                        LOG.debug("Данный прелоадер не виден");
                        list.remove(preloader);
                    }
                    if (list.isEmpty()) {
                        LOG.debug("List is empty");
                    }
                    break;
                }
            } while (!list.isEmpty() && count > 0);
        } catch (org.openqa.selenium.StaleElementReferenceException e) {
            LOG.error("" + e);
        } catch (InterruptedException ie) {
            ie.getMessage();
        }
        if (count <= 0) {
            LOG.error("Количество попыток исчерпано. Прелоадер всё ещё виден");
            throw new AssertionError();
        }
        LOG.debug("Проверка успешно выполнена");
    }


    /**
     * Провкрутка страницы на х и y
     *
     * @param x прокрутка по горизонтали
     * @param y прокрутка по вертикали
     */
    public static void scrollPage(int x, int y) {
        ((JavascriptExecutor) driver).executeScript("window.scroll(" + x + ","
                + y + ");");
    }

    /**
     * Преобразовывает название игры к виду "team1 - team2".
     *
     * @param oldName - название игры, которое удем преобразовывать
     */
    public static String stringParse(String oldName) {
        String nameGame;
        Pattern p = Pattern.compile("(?u)[^а-яА-Ясa-zA-Z]");
        Matcher m = p.matcher(oldName);
        nameGame = m.replaceAll("");
        return nameGame;
    }


    /**
     * проверка что из Ближвйших трансляци переход на правильную игру
     * сравнивает на совпадение название спорта, команд и првоеряет есть ли видео если страница Лайв
     *
     * @return - возвращет true если все ОК, и false если что-то не совпадает с ожиданиями
     */
    public void checkLinkToGame() {

        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.id("menu-toggler")));
        boolean flag = true;
        boolean haveButton = Stash.getValue("haveButtonKey");
        String team1 = Stash.getValue("team1BTkey");
        String team2 = Stash.getValue("team2BTkey");
        String sportName = Stash.getValue("sportKey");

        if (haveButton) {
//            String sportis = driver.findElement(By.xpath("//div[@class='live-game-summary']/div[1]/div[1]/div[1]/div[contains(@class,'game-info')]")).getAttribute("class").replace("game-info game-info_", "");
            String sportis = driver.getCurrentUrl().split("sport=")[1].split("&")[0];
            String team1name = driver.findElement(By.xpath("//div[@class='live-game-summary']//div[contains(@class,'game-info')]/ng-include[1]//div[contains(@class,'team-1')]//p")).getAttribute("title").trim();
            String team2name = driver.findElement(By.xpath("//div[@class='live-game-summary']//div[contains(@class,'game-info')]/ng-include[1]//div[contains(@class,'team-2')]//p")).getAttribute("title").trim();
            LOG.info("Перешли на игру. Ее название в линии: " + team1name + " - " + team2name + ". Спорт: " + sportis);
            if (!team1.equals(team1name) || !team2.equals(team2name)) {
                Assertions.fail("Из Ближайших трансляций переход на неправильную игру. Вместо " + team1 + " " + team2 + "перешли на " + team1name + " " + team2name);
            }
            if (!(sportName.toLowerCase()).equals(sportis.toLowerCase())) {
                Assertions.fail("Из Ближайших трансляций переход на неправильный спорт. Игра " + stringParse(team1 + team2) + "Вместо " + sportName.toLowerCase() + " перешли в " + sportis.toLowerCase());
            }
            if (driver.findElements(By.xpath("//div[contains(@class,'left-menu__list-item-games-row') and contains(@class,'active')]")).isEmpty()){
                Assertions.fail("страница открылась, но никака игра не выделена активной в ЛМ!");
            }
            if (driver.findElement(By.xpath("//div[contains(@class,'left-menu__list-item-games-row') and contains(@class,'active')]//div[contains(@class,'icon icon-video-tv')]")).getAttribute("class").contains("js-hide")) {
                Assertions.fail("Для игры, у который в виджете Блжайшие трансляции есть кнопка %смотреть% не оказалось видео. Игра " + stringParse(team1 + team2));
            }
            LOG.info("У игры, у которой на виджете БТ есть кнопка Смотреть действительно есть видео. Проверка Успешна");
        } else {
            String gameName = driver.findElement(By.xpath("//div[contains(@class,'game-center-container__prematch-title')]")).getAttribute("innerText");
            LOG.info("Перешли на игру. Ее название в линии: " + gameName);
            if (!stringParse(gameName).equals(stringParse(team1 + team2))) {
                Assertions.fail("Из виджета переход на неправильную игру. Вместо " + stringParse(team1 + team2) + "перешли на " + stringParse(gameName));
            }
            LOG.info("Название игры в линии совпадает с тем, что ыбло на виджете БТ. Переход прошел успешно");
        }
    }

    @Когда("^(?:пользователь |он |)(?:осуществляет переход в) \"([^\"]*)\"$")
    public void changeFocusOnPage(String title) throws PageInitializationException {
        super.openPage(title);
    }


    public static void addStash(String key, String value) {
        List<String> values = new ArrayList<>();
        if (Stash.asMap().containsKey(key)) {
            values = Stash.getValue(key);
            values.add(value);
            Stash.asMap().replace(key, values);
        } else {
            values.add(value);
            Stash.put(key, values);
        }
    }

    /**
     * Проверка что при нажатии на ссылку открывается нужная страница. Проверка идет по url, причем эти url очищаются от всех символов, кроме букв и цифр. т.е. слеши собого значения тут не имеют
     *
     * @param element - на какой элемент жмакать чтобы открылась ссылка
     * @param pattern - ссылка или ее часть, которая должна открыться
     * @return true - если все ок.
     */

    public static boolean goLink(WebElement element, String pattern) {
        boolean flag = true;
        LOG.info("Проверяем что откроется правильная ссылка " + pattern);
        pattern = stringParse(pattern);
        int CountWind = driver.getWindowHandles().size();
        if (element.findElements(xpath("ancestor-or-self::*[@target='_blank']")).isEmpty()) {

            ((JavascriptExecutor) driver)//открываем ссылку в новой вкладке
                    .executeScript("window.open(arguments[0])", element);
        } else element.click();
        driver.switchTo().window(driver.getWindowHandles().toArray()[driver.getWindowHandles().size() - 1].toString());
        if ((CountWind + 1) != driver.getWindowHandles().size()) {
            LOG.error("Не открылась ссылка");
            return false;
        }
        LOG.info("Ссылка открылась");
        driver.switchTo().window(driver.getWindowHandles().toArray()[CountWind].toString());
        String siteUrl = stringParse(driver.getCurrentUrl());
        if (!siteUrl.contains(pattern)) {
            flag = false;
            LOG.error("Ссылка открылась, но не то, что надо. Вместо " + pattern + " открылось " + siteUrl);
        }
        driver.close();
        driver.switchTo().window(driver.getWindowHandles().toArray()[CountWind - 1].toString()); //мы знаем что поле открытия ссылки на скачивание количесвто ссылок будет на  больше, незачем переопрелеть CountWind.
        return flag;
    }

    @Когда("^(пользователь |он) очищает cookies$")
    public static void cleanCookies() {
        try {
            if (driver.manage().getCookies().size() > 0) {
                LOG.info("Удаляем Cookies");
                driver.manage().deleteAllCookies();
            }
        } catch (Exception e) {
            LOG.error("Cookies не было!");
        }
    }

    /**
     * прелоадер должен обязательно появиться, если его не было - значит способ пополнения как бы и не выбран. поэтому эта ункция ждет чтобы прелоадер точно был,
     * но чтобы был не бесконечен
     */
    public static void waitToPreloader() {
        int count = 20;
        try {
            while (count > 0) {
                if (driver.findElement(By.cssSelector("div.preloader__container")).isDisplayed()) {
                    waitOfPreloader();
                    break;
                }
                Thread.sleep(500);
                count--;
                if (count == 0) {
                    Assertions.fail("Прелоадер так и не появился!");
                }
            }
        } catch (Exception e) {
            LOG.error("" + e);
        }
    }

    /**
     * функиця, которая ждет пока элмент станет доступным. ждет, но не кликает
     *
     * @param element
     * @throws Exception
     */
    public static void waitEnabled(WebElement element) {
        int count = 20;
        try {
            while (count > 0) {
                if (element.isEnabled()) break;
                Thread.sleep(500);
                count--;
                if (count == 0) {
                    Assertions.fail("За 10 секунд элемент " + element + " так и не стал доступным");
                }
            }
        } catch (StaleElementReferenceException | InterruptedException e) {
            LOG.error("" + e);
        }
    }



    @Когда("^выбираем одну дату из \"([^\"]*)\" и сохраняем в \"([^\"]*)\" а id_user в \"([^\"]*)\"$")
    public void selectOneDateInResponce(String keyResponce, String keyDate, String keyId) throws ParseException {
        SimpleDateFormat oldFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm");
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        String actual = ((Response) Stash.getValue(keyResponce)).getBody().asString();
        actual = actual.replace("{\"code\":0,\"data\":", "").replace("}", "");
        String[] linesResponce = actual.split("swarmUserId");
        int i = 1 + new Random().nextInt(linesResponce.length - 1);
        String idUser = actual.split("swarmUserId")[i].replace("\":", "").split(",")[0];
        String dateForUser = null;
        if (actual.contains("videoIdentDate")) {
            dateForUser = actual.split("videoIdentDate")[i].replace("\":", "").split(",")[0].replaceAll("\"", "");
            oldFormat = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
            newFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        }
        if (actual.contains("skypeSendDate")) {
            dateForUser = actual.split("skypeSendDate")[i].replace("\":", "").split(",")[0].replaceAll("\"", "");
            oldFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm");
            newFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        }

        LOG.info("Выбранная дата: " + dateForUser + ", для юзера с id = " + idUser);

        LOG.info("Отнимем от даты одну минуту");
        Calendar newDateTime = new GregorianCalendar();

        newDateTime.setTime(oldFormat.parse(dateForUser));
        newDateTime.add(Calendar.MINUTE, -1);
        LOG.info("Теперь переведем дату в нужны формат");
        dateForUser = newFormat.format(newDateTime.getTime()).replace(" ", "T") + ":00";

        Stash.put(keyDate, dateForUser);
        Stash.put(keyId, idUser);

        LOG.info("Новая дата: " + dateForUser);
    }


    @Когда("^проверка что в ответе \"([^\"]*)\" нет юзера с \"([^\"]*)\"$")
    public void checkResponceNotConains(String keyResponce, String keyId) {
        String actual = ((Response) Stash.getValue(keyResponce)).getBody().asString();
        String userId = Stash.getValue(keyId);
        Assert.assertFalse("В ответе есть пользователь " + userId + ", хотя он не вписывается в заданные ts и ts_end:" + Stash.getValue("PARAMS"),
                actual.contains("\"swarmUserId\":" + userId));
        LOG.info("В ответе действительно теперь нет записи о пользователе с id=" + userId);
    }

    @Когда("^проверка что в ответе \"([^\"]*)\" верные даты  \"([^\"]*)\":$")
    public void checkResponceAPIgoodDate(String keyStash, String keyParams) throws ParseException {
        String actual = ((Response) Stash.getValue(keyStash)).getBody().asString();
        String params = Stash.getValue(keyParams).toString();
        SimpleDateFormat formatTS = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat formatResponse = new SimpleDateFormat("dd.MM.yyy hh:mm");
        String ts = params.split("ts=")[1].substring(0, 16).replace("T", " ");
        Date tsDate = formatTS.parse(ts);
        Date tsEndDate = new Date();
        String ts_end = null;
        if (params.contains("ts_end")) {
            ts_end = params.split("ts_end=")[1].substring(0, 16).replace("T", " ");
            tsEndDate = formatTS.parse(ts_end);
        }

        String dateInResponceString = new String();

        Date dateInResponce = new Date();
        boolean a;
        for (int i = 1; i < actual.split("\"skypeSendDate" + "\":").length; i++) {
            dateInResponceString = actual.split("\"skypeSendDate" + "\":")[i].split("}")[0].replaceAll("\"", "");
            dateInResponce = formatResponse.parse(dateInResponceString);
            Assert.assertTrue("В ответе есть результаты, выходящие за пределы ts-ts_end (" + ts + "   ---   " + ts_end + ")"
                            + ":\n" + dateInResponceString,
                    dateInResponce.after(tsDate) && dateInResponce.before(tsEndDate));
        }
        LOG.info("Да, все результаты запроса вписываются по времени в значения ts и ts_end");
    }



    @Когда("^проверим что время \"([^\"]*)\" уменьшилось в \"([^\"]*)\"$")
    public void checkTimebecomeLes(String keyTime, String keyResponse){
        int oldTime = Stash.getValue(keyTime);
        fingingAndSave(keyTime,keyResponse);
        int newTime = Stash.getValue(keyTime);
        Assert.assertTrue("Время " + keyTime + " должно было уменьшиться в этом запросе, но " + newTime + ">=" + oldTime,
                newTime<oldTime);
    }

    @Когда("^находим \"([^\"]*)\" и сохраняем \"([^\"]*)\" из вложенного \"([^\"]*)\"$")
    public void fingingAndSave3(String keyWhat,String keyWhere, String sourceString) {
        fingingAndSave2(keyWhat.split("-")[0],keyWhat,sourceString);
        List<HashMap> bonusMap = Stash.getValue(keyWhat);
        String bonuses = bonusMap.get(0).get(keyWhat.split("-")[1]).toString();
        Stash.put(keyWhere,bonuses);
    }

    @Когда("^находим и сохраняем \"([^\"]*)\" из \"([^\"]*)\"$")
    public void fingingAndSave(String keyFingingParams, String sourceString) {
        fingingAndSave2(keyFingingParams,keyFingingParams,sourceString);
    }

    @Когда("^находим \"([^\"]*)\" и сохраняем \"([^\"]*)\" из \"([^\"]*)\"$")
    public void fingingAndSave2(String param,String keyFingingParams, String sourceString) {
        String tmp;
        Object valueFingingParams, retMap = null;
        ObjectMapper mapper = new ObjectMapper();
        String resp;
        if (Stash.getValue(sourceString).getClass().getName().contains("List")){
            List<Object> list = Stash.getValue(sourceString);
            resp=list.get(list.size()-1).toString();
        }
        else {
            Response response = Stash.getValue(sourceString);
            resp=response.getBody().asString();
        }

        //Преобразуем в строку JSON-объект в зависимости от его структуры
        if (JSONValue.isValidJson(resp)) {
            tmp = resp;
        } else {
            tmp = JSONValue.toJSONString(Stash.getValue(sourceString));
        }

        TypeReference<LinkedHashMap<String, Object>> typeRef = new TypeReference<LinkedHashMap<String, Object>>() {
        };

        try {
            retMap = mapper.readValue(tmp, typeRef);
        } catch (IOException e) {
            TypeReference<ArrayList<Object>> typeRef1 = new TypeReference<ArrayList<Object>>() {
            };
            try {
                retMap = mapper.readValue(tmp, typeRef1);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.getMessage();
        }
        valueFingingParams = JsonLoader.hashMapper(retMap, param);
        LOG.info("Достаем значение [" + param + "] и записываем в память [" + JSONValue.toJSONString(valueFingingParams) + "]");
        Stash.put(keyFingingParams, valueFingingParams);
    }




    @After()
    public void getLoglal(Scenario scenario) {
        getLogg(scenario);
    }









    @Когда("^пользователь открывает новое окно с url \"([^\"]*)\"$")
    public void userOpenNewUrl2(String url) {
        WebDriver driver2 = new ChromeDriver();
        driver2.get(url);
        new WebDriverWait(driver2, 10).until(ExpectedConditions.urlToBe(url));
        Stash.put("driver", driver2);
    }

    public static void closingCurrtWin(String title) {
        PageFactory.getWebDriver().close();
        for (String windowHandle : PageFactory.getWebDriver().getWindowHandles()) {
            PageFactory.getWebDriver().switchTo().window(windowHandle);
            if (PageFactory.getWebDriver().getTitle().equals(title)) {
                return;
            }
        }
        throw new AutotestError("Unable to return to the previously opened page: " + title);
    }


    @Before()
    public void titleTest(Scenario scenario) {
        LOG.info("<================START...TEST================>");
        LOG.info("NAME: " + scenario.getName());
        LOG.info("TAGS: " + scenario.getSourceTagNames());
        LOG.info("ID: " + scenario.getId().replaceAll("\\D+", ""));
        String mainUrl;

        try {
            if (scenario.getSourceTagNames().contains("@mobile")) {
                mainUrl = JsonLoader.getData().get(STARTING_URL).get("MOBILE_URL").getValue();
            } else {
                mainUrl = JsonLoader.getData().get(STARTING_URL).get("MAIN_URL").getValue();
            }
            Stash.put("MAIN_URL", mainUrl);
            LOG.info("Сохранили в память key [MAIN_URL] <== value [" + mainUrl + "]");
        } catch (DataException e) {
            throw new AutotestError("Ошибка! Что-то не так с URL");
        }
    }


    @Когда("^вычленяем из названия игры одно слово \"([^\"]*)\" \"([^\"]*)\"$")
    public void oneWordSearch(String keySearch, String type) {
        LOG.info(Stash.getValue("nameGameKey") + " время начала ");
        List<String> types = Stash.getValue("typeGameKey");
        int index = types.indexOf(type);
        List<String> names = Stash.getValue("nameGameKey");
        for (String str : names.get(index).split(" ")) {
            if (str.length() > 3) {
                Stash.put(keySearch, str);
                LOG.info(keySearch + ": " + str);
                break;
            }
        }
        if (Stash.getValue(keySearch)==null){
            LOG.info("Название игры не содержит слова, длиной больше 3 символов. значит будем пытаться искать по полному названию " + names.get(index));
            Stash.put(keySearch, names.get(index));
        }
    }








    @Когда("^нажимает кнопку НАЗАД$")
    public void backToPage() {
        driver.navigate().back();
    }


    @Когда("^проверяем, совпадает ли дата и время игры с ожидаемыми \"([^\"]*)\" \"([^\"]*)\"$")
    public void checkDateTimeGame(String keyData, String typeGamekey) {
        String fullDateTime = Stash.getValue(keyData).toString().replace("\n", " ");
        String typeGame = Stash.getValue(typeGamekey);
        switch (typeGame) {
            case "live":
                LOG.info("Судя по времени, указанному на баннере, игра должна быть лайвовской. Проверять будем только что раздел соответствует ЛАЙВу " + fullDateTime);
                Assert.assertTrue(
                        "Раздел ЛАЙВ не активен",
                        driver.findElement(By.xpath("//*[@id='live']/..")).getAttribute("class").contains("active"));
                break;
            case "prematch":
                LOG.info("Судя по времени, указанному на баннере, игра должна быть прематчевской. Проверять будем и дату, и время игры в ПРЕМАТЧе " + fullDateTime);
                Assert.assertTrue(
                        "Раздел ПРЕМАТЧ не активен",
                        driver.findElement(By.xpath("//*[@id='prematch']/..")).getAttribute("class").contains("active"));
                String timeGameOnP = driver.findElement(By.xpath("//div[contains(@class,'bets-block_single-row') and contains(@class,'active')]//*[contains(@class,'bets-block__header-left-info')]")).getAttribute("innerText");
                String dateGameOnP = driver.findElement(By.xpath("//div[contains(@class,'bets-block_single-row') and contains(@class,'active')]/ancestor-or-self::div[contains(@class,'prematch-competition')]//div[contains(@class,'prematch-competition__header-date')]")).getAttribute("innerText");
                String dateTimeGameOnP = timeGameOnP + " - " + dateGameOnP;
                SimpleDateFormat formatPrematch = new SimpleDateFormat("hh:mm - dd MMM yyyy");
                Calendar datePrematch = Calendar.getInstance();
                Calendar dateTimeGame = Stash.getValue(keyData);

                try {
                    datePrematch.setTime(formatPrematch.parse(dateTimeGameOnP));
                    Assert.assertTrue(
                            "Время игры на баннере и на странице ПРЕМАТЧ не свопадает. На баннере: " + fullDateTime + ", в прематче: " + dateTimeGameOnP,
                            datePrematch.equals(dateTimeGame));
                } catch (ParseException e) {
                    LOG.info("Не удалось распарсить дату и время игры, указанные на баннере. Возможно не совпадает формат: " + dateTimeGameOnP + ". Формат: " + formatPrematch);
                    e.printStackTrace();
                }
                break;
        }

    }


    @Когда("^проверяем, совпадают ли коэффициенты на странице с теми, что на баннере \"([^\"]*)\"$")
    public void checkAllCoefs(String keyCoefs) {
        LOG.info("Формируем список коэффициентов для маркета ИСход на странице игры");
        List<String> coefsOnPage =
                driver.findElements(By.xpath("//div[contains(@class,'game-center-container__live')]//div[contains(@class,'bets-block__bet-cell_active')]/..//span[contains(@class,'bets-block__bet-cell-content-price')]"))
                        .stream().map(el -> el.getAttribute("innerText") + "%").collect(Collectors.toList());
        if (coefsOnPage.size()==2){
            coefsOnPage.add(1,"—%");
        }
        LOG.info(coefsOnPage + "\n% - это просто разделитель. все норм");
        LOG.info("Теперь сравним этот список,с тем что было на баннере");
        List<String> coefsOnBanners = Stash.getValue(keyCoefs);
        Assert.assertTrue(
                "Коэффициенты на странице и на баннере не совпадают. \n" + coefsOnPage + " вместо; \n" + coefsOnBanners,
                coefsOnBanners.equals(coefsOnPage)
        );
        LOG.info("Коэффициенты на беннере и на странице совпадают");
    }




    public static void getLogg(Scenario scenario){
        LOG.info("fail? " + scenario.isFailed());
        LOG.info("АФТЕРРР");
        File file = new File("src" + sep + "test" + sep + "resources" + sep + "logger.txt");
        if (scenario.isFailed()) {
            try (InputStream is = Files.newInputStream(Paths.get("src" + sep + "test" + sep + "resources" + sep + "logger.txt"))) {
                Allure.addAttachment("LOG", is);
                FileWriter nfile = new FileWriter("src" + sep + "test" + sep + "resources" + sep + "logger.txt", false);
                nfile.write("");
                nfile.close();
                file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            FileWriter nfile = null;
            try {
                nfile = new FileWriter("src" + sep + "test" + sep + "resources" + sep + "logger.txt", false);
                nfile.write("");
                nfile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            file.delete();
        }
    }




    public static ExpectedCondition<Boolean> elementIsOnPage(final By locator, final String messages) {
        return new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver driver) {
                return (!driver.findElements(locator).isEmpty());
            }

            @Override
            public String toString() {
                return messages;
            }
        };
    }



}

