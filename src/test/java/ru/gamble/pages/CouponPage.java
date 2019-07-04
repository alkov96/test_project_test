package ru.gamble.pages;


import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gamble.stepdefs.CommonStepDefs;
import ru.sbtqa.tag.datajack.Stash;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.annotations.ActionTitle;
import ru.sbtqa.tag.pagefactory.annotations.ElementTitle;
import ru.sbtqa.tag.pagefactory.annotations.PageEntry;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementDecorator;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementLocatorFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.By.xpath;

/**
 * @author p.sivak.
 * @since 11.05.2018.
 */
@PageEntry(title = "Купон")
public class CouponPage extends AbstractPage {
    private static final Logger LOG = LoggerFactory.getLogger(CouponPage.class);
    static WebDriver driver = PageFactory.getDriver();

    @FindBy(xpath = "//div[contains(@class,'coupon__types')]")
    private WebElement coupon;

    @ElementTitle("Активация Быстрой ставки")
    @FindBy(xpath = "//div[@class='coupon__toggler']/label")
    protected WebElement quickButton;
    //для ставок экспресс, быстрой ставки - т.е. там где 1 поле для ставки


    @ElementTitle("Флаг активности быстрой ставки")
    @FindBy(xpath = "//div[@class='coupon__toggler']/input")
    protected WebElement quickBetFlag;

    @ElementTitle("Очистить всё")
    @FindBy(xpath = "//span[@class='btn btn_full-width']")
    public static WebElement clearCoupon;

    @ElementTitle("Тип купона")
    @FindBy(xpath = "//div[contains(@class,'coupon__types')]//li[contains(@class,'selected')]")
    private WebElement couponType;

    static By expressBonusText = xpath("//div[contains(@class,'coupon-bet_offer')]//span[contains(@class,'coupon-bet__text')]");
    static By expressBonusLink = xpath("//div[contains(@class,'coupon-bet_offer')]//a[contains(@class,'coupon-bet__link')]");

    static By currentExpressBonus = xpath("//div[@class='coupon__bottom-block']//span[contains(@class,'coupon__sum orange')]");

    @ElementTitle("параметры в купоне")
    @FindBy(xpath = "//i[@class='icon icon-settings-old coupon-tabs__item-icon']")
    private WebElement button_of_param_in_coupon;

    @ElementTitle("поле суммы ставки Ординар")
    @FindBy(xpath = "//input[contains(@class,'input coupon__input') and not(@id='bet-input')]")
    public static WebElement couponInputOrdinar;

    @ElementTitle("поле суммы ставки типа Система")
    @FindBy(xpath = "//input[contains(@class,'input coupon__input') and @id='bet-input']")
    private WebElement couponInputSystem;

    @ElementTitle("кнопка Заключить пари для Экспресса и Системы")
    //@FindBy(id="place-bet-button")
    @FindBy(xpath = "//button[contains(@class,'btn_coupon') and normalize-space(text())='Заключить пари']")
    private WebElement buttonBet;

    @ElementTitle("переключатель ставки на бонусы")
    @FindBy(xpath = "//div[@class='coupon__button-group']//label[contains(@class,'coupon-btn_b')]")
    private WebElement bonusBet;


    static By xpathFreeBet = xpath("//div[@class='coupon__button-group']//label[contains(@class,'coupon-btn_f')]");


    @FindBy(className = "coupon__banners") //баннеры в купоне
    private WebElement bannersInCoupon;

    @ElementTitle("текущий тип системы")
    @FindBy(xpath = "//div[contains(@class,'coupon__system-select')]//div[contains(@class,'custom-select__placeholder')]/span")
    private WebElement current_type_of_system;

    public CouponPage() {
        WebDriver driver = PageFactory.getDriver();
        PageFactory.initElements(new HtmlElementDecorator(new HtmlElementLocatorFactory(driver)), this);
        tryingLoadPage(coupon,10, 5);
    }

    @ActionTitle("убирает события из купона, пока их не станет")
    public void removeEventsFromCoupon(String param) {
        int count = Integer.parseInt(param);
        while (PageFactory.getWebDriver().findElements(xpathListBets).size() > count) {
            PageFactory.getWebDriver().findElement(xpath("//span[@class='coupon-bet__close-btn']")).click();
        }
    }

    @ActionTitle("проверяет отсутствие ссылки О бонусах к экспрессу и текста о бонусе")
    public void checkBonusFalse() {
        checkExpressBonus(false);
    }

    @ActionTitle("проверяет корректность ссылки О бонусах к экспрессу и текста о бонусе")
    public void checkBonusTrue() {
        checkExpressBonus(true);
    }

    @ActionTitle("заполняет сумму для ставки")
    public void fillSumm(String param1, String param2) {
        if (param1.equals("Экспресс")) {
            driver.findElement(xpath("//input[@id='express-bet-input']")).clear();
            driver.findElement(xpath("//input[@id='express-bet-input']")).sendKeys(param2);
        }
    }

    @ActionTitle("проверяет наличие бонуса к возможному выигрышу")
    public void checkBonusPresent() {
        checkBonus(true);
    }

    @ActionTitle("проверяет отсутствие бонуса к возможному выйгрышу")
    public void checkBonusNotPresent() {
        checkBonus(false);
    }


    public void checkBonus(boolean expect) {
        List<WebElement> listBets = driver.findElements(xpathListBets);
        //driver.findElements(xpath("//div[contains(@class,'coupon-bet') and not(contains(@class,'coupon-bet_offer'))]/ul"));
        if (!expect) {
            assertTrue(
                    "Есть эспресс-бонус!!! " + driver.findElements(currentExpressBonus).size(),
                    driver.findElements(currentExpressBonus).isEmpty());
        } else {
            assertTrue(
                    "Неправильный размер экспресс-бонуса (или его вообще нет)   ||| " + driver.findElement(currentExpressBonus).getAttribute("innerText") + " |||",
                    driver.findElement(currentExpressBonus).getAttribute("innerText").contains(listBets.size() + "%")); // проверка корректности текста
        }

    }

    public void checkExpressBonus(boolean expect) {
        List<WebElement> listBets = driver.findElements(xpathListBets);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!expect) {
            assertTrue(
                    "Есть экспресс-бонус!!! " + driver.findElements(expressBonusText).size(),
                    driver.findElements(expressBonusText).isEmpty());
            assertTrue(
                    "Есть ссылка на описание экспресс-бонуса в правилах",
                    driver.findElements(expressBonusLink).isEmpty());
        } else {
            assertFalse(
                    "Нет текста про экспресс-бонус!!! ",
                    driver.findElements(expressBonusText).isEmpty());
            assertTrue(
                    "Неправильная текст в описании экспресс-бонуса. Или его вообще нет   ||| " + driver.findElement(expressBonusText).getAttribute("innerText") + " |||",
                    driver.findElement(expressBonusText).getAttribute("innerText").contains("+" + (listBets.size() + 1) + "% к выигрышу")); // проверка корректности текста
            assertTrue(
                    "Неправильная ссылка на описание экспресс-бонуса. Или ссылки вообще нет  ||| " + driver.findElement(expressBonusLink).getAttribute("href") + " |||",
                    driver.findElement(expressBonusLink).getAttribute("href").contains("/rules/express-bonus")); // проверка корректности ссылки

        }

    }


    @ActionTitle("проверяет, добавилось ли событие в купон")
    public void checkListOfCoupon() {
        List<WebElement> couponList = driver.findElements(xpath("//li[@class='coupon-bet__row']/span[@class='coupon-bet__title']"));
        if (couponList.isEmpty()) {
            Assertions.fail("События не добавлиись в купон.");
        } else LOG.info("Событие " + couponList.size());
    }

    @ActionTitle("проверяет, совпадают ли события в купоне с ожидаемыми из")
    public void bannerAndTeams(String team1key, String team2key) {
        String couponGame = driver.findElement(xpath("//li[@class='coupon-bet__row']/span[@class='coupon-bet__title']")).getAttribute("innerText");//cuponGame - наше добавленные события в купоне.
        String team1 = Stash.getValue(team1key);
        String team2 = Stash.getValue(team2key);
        if (team1==null && team2==null){
            LOG.info("В памяти не хранятся названия команд. сверять не с чем");
            return;
        }
        if (CommonStepDefs.stringParse(team1 + team2).equals(CommonStepDefs.stringParse(couponGame))) {
            LOG.info("Названия команд в купоне совпадают с ожидаемыми: [" + team1 + "] - [" + team2 + "] <=> [" + couponGame + "]");
        } else
            Assertions.fail("Названия команд в купоне не совпадают с ожидаемыми: [" + team1 + "] - [" + team2 + "] <=> [" + couponGame + "]");
    }


    @ActionTitle("проверяет, совпадает ли исход в купоне с ожидаемым")
    public void checkIshod(String ishodKey) {
        String ishodName = Stash.getValue(ishodKey);//ожидаемое название исхода
        if (ishodName==null){
            LOG.info("В памяти не хранится название команды, на которую поставили. сверять не с чем");
            return;
        }
        String ishod = driver.findElement(xpath("//ul[@class='coupon-bet__content']/li[2]/div")).getAttribute("innerText").split("\n")[1].trim();
        if(ishod.matches("^[П].*[1].*"))
        {ishod = driver.findElement(By.xpath("//span[contains(@class,'coupon-bet__title')]")).getAttribute("innerText").split("–")[0].trim();
        } else if (ishod.matches("^[П].*[2].*")){
            ishod = driver.findElement(By.xpath("//span[contains(@class,'coupon-bet__title')]")).getAttribute("innerText").split("–")[1].trim();
        }


        if (CommonStepDefs.stringParse(ishod).equals(CommonStepDefs.stringParse(ishodName))) {
            LOG.info("Выбранных исход в купоне совпадает с ожидаемым: " + ishod + " <=> " + ishodName);
        } else Assertions.fail("Выбранный исход в купоне не совпадает с ожидаемым: " + ishod + " - " + ishodName);

    }

    @ActionTitle("сравнивает коэфиценты")
    public void compareCoef(String keyOutcome) {
        String coefString = Stash.getValue(keyOutcome).toString();
        float coef = Float.valueOf(coefString);
        float coefCoupon = Float.valueOf(driver.findElement(xpath("//div[@class='coupon-bet__coeffs']/span[2]")).getAttribute("innerText"));//Кэфицент в купоне
        WebElement oldWebElement = driver.findElement(xpath("//span[contains(@class,'coupon-bet__coeff-strikeout')]"));
        float oldCoef = oldWebElement.isDisplayed() ? Float.valueOf(oldWebElement.getAttribute("innerText").trim()) : coefCoupon;
        if (coef != coefCoupon && coef != oldCoef) {
            Assertions.fail("Коэфицент в купоне не совпадает с коэфицентом в событии: " + coefCoupon + coef);
        } else LOG.info("Коэфицент в купоне совпадает с коэфицентом в событии: " + coefCoupon + " <=> " + coef);
    }

    @ActionTitle("устанавливает условие для принятия коэфицентов как 'Никогда'")
    public void neverAccept() {
        button_of_param_in_coupon.click();
        driver.findElement(xpath("//label[@for='betset_none']")).click();
        LOG.info("Установили условие 'Никогда'");
        driver.findElement(xpath("//ul[@class='coupon-tabs coupon-tabs_black']/li[1]")).click();//возращаемся обратно в купон
        //button_of_param_in_coupon.click();
    }


        ExpectedCondition<Boolean> pageLoadCondition = new
                ExpectedCondition<Boolean>() {
                    @Nullable
                    @Override
                    public Boolean apply(@Nullable WebDriver webDriver) {
                        return null;
                    }

                    public Boolean apply(WebDriver driver, By by) {
                        return driver.findElement(by).isDisplayed();
                    }
                };




    @ActionTitle("проверяет, что после изменения условий на 'Никогда' в купоне появляется кнопка 'Принять' и информационное сообщение")
    public void buttonAndMessageIsDisplayed() throws InterruptedException {
        By by = xpath("//div[@class='coupon-bet__coeffs']/span[contains(@class,'coupon-bet__coeff-strikeout') and not (contains (@class, 'ng-hide'))]");
        WebDriverWait wait = new WebDriverWait(PageFactory.getWebDriver(),70);
        wait.withMessage("Не удалось найти события, где меняется коэфицент");
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(by, 0));
        List<WebElement> oldCoef = driver.findElements(by).stream().filter(element -> element.isDisplayed()).collect(Collectors.toList());
        if (oldCoef.size() == 0){
            Assertions.fail("Коэфицент не поменялся!");
        }
        Thread.sleep(500);
        WebElement error_message = driver.findElement(xpath("//span[@class='coupon__message-fragment']"));
        if (oldCoef.size() > 0 && !error_message.isDisplayed()) {
            Assertions.fail("Коэф изменился, однако сообщение не отображается.");
        }
        //LOG.info("Изменился коэф и появилось сообщение о принятии коэфиценита");
        Thread.sleep(5000);
        WebElement btn_accept = driver.findElement(xpath("//span[@class='btn btn_full-width' and @ng-click = 'acceptChanges()']"));
        if (!error_message.isDisplayed()
                || !btn_accept.isDisplayed()) {
            Assertions.fail("При изменении условий ставки не появилось сообщение или кнопка о принятии изменений.");
        }
        LOG.info("Появилось сообщение о принятии коэфицента и кнопка");

        LOG.info("Проверка на принятие условия 'Никогда' в купоне прошла успешно.");

    }

    /**
     * функция проверяет, как изменился коэф для конкретной ставки
     *
     * @param param порядковый номер ставки в купоне
     * @return возращает 0, если коэф не изменился, >0, если увеличился, и <0, если уменьшился
     */
    public float compareCoef(int param) {
        List<WebElement> allBets = driver.findElements(xpath("//ul[@class='coupon-bet__content']"));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        for (int i = 3; i > 0; i--) {
            List<WebElement> oldCoef = driver.findElements(xpath("//div[@class='coupon-bet__coeffs']/span[contains(@class,'coupon-bet__coeff-strikeout') and not (contains (@class, 'ng-hide'))]"))
                    .stream().filter(element -> element.isDisplayed()).collect(Collectors.toList());
            if (!oldCoef.isEmpty()) break;
        }
        float coefCoupon = Float.valueOf(allBets.get(param).findElement(xpath(".//div[@class='coupon-bet__coeffs']/span[2]")).getAttribute("innerText"));
        String oldString = allBets.get(param).findElement(xpath(".//div[@class='coupon-bet__coeffs']/span[1]")).getAttribute("class");
        float coefOld;
        coefOld = oldString.contains("ng-hide") ? coefCoupon : Float.valueOf(allBets.get(param).findElement(xpath(".//div[@class='coupon-bet__coeffs']/span[1]")).getAttribute("innerText"));
        LOG.info("Старый коэф: " + coefOld);
        LOG.info("Текущий коэф: " + coefCoupon);
        return coefCoupon - coefOld;
    }





}

