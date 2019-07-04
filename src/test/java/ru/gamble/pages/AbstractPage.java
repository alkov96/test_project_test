package ru.gamble.pages;

import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
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
import ru.sbtqa.tag.pagefactory.Page;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.annotations.ActionTitle;
import ru.sbtqa.tag.pagefactory.annotations.ElementTitle;
import ru.sbtqa.tag.pagefactory.exceptions.PageException;
import ru.sbtqa.tag.qautils.errors.AutotestError;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeContains;
import static ru.sbtqa.tag.pagefactory.PageFactory.getWebDriver;


public abstract class AbstractPage extends Page{
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPage.class);

    public static By xpathListBets = xpath("//div[contains(@class,'coupon-bet') and not(contains(@class,'coupon-bet_offer'))]/ul");

    public static By preloaderOnPage = By.xpath("//div[contains(@class,'preloader__container') and not(contains(@class,'hide'))]");
    static WebDriver driver = PageFactory.getDriver();


    @ElementTitle("Вход")
    @FindBy(id = "log-in")
    private WebElement enterButton;

    @ElementTitle("На главную")
    @FindBy(id = "main-logo")
    protected WebElement onMainPageButton;

    @ElementTitle("Иконка юзера")
    @FindBy(id = "user-icon")
    protected WebElement userIconButton;

    @ElementTitle("Бургер")
    @FindBy(id = "service-list")
    protected WebElement burgerBottom;

    protected String fieldDay = "//*[@class='inpD']";

    protected String fieldMonth = "//div[contains(@class,'dateInput')]/div[@class='inpM']";

    protected String fieldYear = "//*[@class='inpY']";

    @ElementTitle("Настройки")
    @FindBy(id = "preferences")
    protected WebElement preferences;

    @ElementTitle("Активация Быстрой ставки")
    @FindBy(xpath = "//div[@class='coupon__toggler']/label")
    protected WebElement quickButton;
    //для ставок экспресс, быстрой ставки - т.е. там где 1 поле для ставки


    @ElementTitle("Флаг активности быстрой ставки")
    @FindBy(xpath = "//div[@class='coupon__toggler']/input")
    protected WebElement quickBetFlag;

    @ElementTitle("Очистить всё")
    @FindBy(xpath = "//div[contains(@class,'coupon__button-group_bet')]//button[normalize-space(text())='Очистить купон']")
    protected static WebElement clearCoupon;

    public static By pathToclearCoupon = By.xpath("//div[contains(@class,'coupon__button-group_bet')]//button[normalize-space(text())='Очистить купон']");

    @ElementTitle("Сервисное сообщение")
    @FindBy(xpath = "//div[contains(@class,'tech-msg active')]")
    private WebElement serviceMessage;

    @ElementTitle("Иконка закрытия сервисного сообщения")
    @FindBy(xpath = "//span[contains(@class,'tech-msg__close')]")
    private WebElement closeServiceMessage;

    @ElementTitle("Прематч")
    @FindBy(id = "prematch")
    private WebElement prematchBottom;

    @ElementTitle("Азбука беттинга")
    @FindBy (xpath = "//a[@href='/azbuka-bettinga']")
    private WebElement azbuka;

    @ElementTitle("Подвал")
    @FindBy (xpath = "//div[@class='footer__pin']")
    private WebElement podval;

    @FindBy(xpath = "//div[contains(@class,'menu-toggler')]")
    private WebElement expandCollapseMenusButton;


    @ActionTitle("открывает Избранное")
    public static void openFavourite() {
        LOG.info("vot");
        WebDriver driver = PageFactory.getDriver();
        driver.findElement(By.id("elected")).click();//нажали на кнопку избранного
    }

    @ActionTitle("нажимает кнопку")
    public static void pressButtonAP(String param){
        CommonStepDefs.pressButton(param);
        LOG.info("Нажали на [" + param + "]");
    }

    @ActionTitle("открывает/закрывает Мои пари")
    public void openMyBets(){
        PageFactory.getDriver().findElement(By.xpath("//span[@class='icon-svg__my-bets_selected']")).click();
    }
    @ActionTitle("stop")
    public static void stop() {
        LOG.info("STOP");
    }

    /**
     * Метод который по имени WebElement находит его на текущей странице,
     * достаёт его ссылку и переходит по ней
     *
     * @param param - имя WebElement
     */
    @ActionTitle("переходит по ссылке")
    public static void goesByReference(String param) throws PageException {
        Page page;
        page = PageFactory.getInstance().getCurrentPage();
        String link = page.getElementByTitle(param).getAttribute("href");
        PageFactory.getWebDriver().get(link);
        LOG.info("Получили и перешли по ссылке::" + link);
    }

    public static void clickElement(final WebElement element) {
        WebElement myDynamicElement = (new WebDriverWait(PageFactory.getWebDriver(), 10))
                .until(ExpectedConditions.elementToBeClickable(element));
        myDynamicElement.click();
    }


    public void tryingLoadPage(By by, int count, int waitSeconds) {
        WebDriver driver = PageFactory.getWebDriver();
        LOG.info("Ищем элемент [" + by + "] на странице::" + driver.getCurrentUrl());

        for (int j = 0; j < count; j++) {
            try {
                new WebDriverWait(PageFactory.getDriver(), waitSeconds ).until(ExpectedConditions.visibilityOfElementLocated(by));
                break;
            } catch (Exception e) {
                driver.navigate().refresh();
            }
            if (j >= count - 1) {
                throw new AutotestError("Ошибка! Не нашли элемент после " + j + " попыток перезагрузки страницы");
            }
        }
    }

    public void tryingLoadPage(WebElement element, int count, int waitSeconds) {
        WebDriver driver = PageFactory.getWebDriver();
        LOG.info("Ищем элемент [" + element + "] на странице::" + driver.getCurrentUrl());

        for (int j = 0; j < count; j++) {
            try {
                new WebDriverWait(PageFactory.getDriver(), waitSeconds ).until(ExpectedConditions.visibilityOf(element));
                break;
            } catch (Exception e) {
                driver.navigate().refresh();
            }
            if (j >= count - 1) {
                throw new AutotestError("Ошибка! Не нашли элемент после " + j + " попыток перезагрузки страницы");
            }
        }
    }



    @ActionTitle("завершает регистрацию перейдя по ссылке для БД")
    public static void endRegistrationByEmailLinkDB(){

        String url = Stash.getValue("MAIN_URL");
        String code = Stash.getValue("CODEEMAIL");
        String userId = Stash.getValue("userIdKey");
        driver.get(url + "/registration/email/verify?code=" + code + "_" + userId);
        LOG.info("Закрываем уведомление об успешном подтверждении почты");
        driver.findElement(By.cssSelector("a.modal__closeBtn.closeBtn")).click();
    }
    /**
     * Открывает выпадающий список и выбирает оттуда пункт случайным образом
     *
     * @param element - поле где ждем выпадающий список.
     * @param select - выбираемый пункт меню.
     */
    protected void selectMenu(WebElement element, int select) {
        new WebDriverWait(driver,10)
                .until(ExpectedConditions.elementToBeClickable(element.findElement(By.xpath("custom-select"))));
        element.findElement(By.xpath("custom-select")).click();
        new WebDriverWait(driver,10)
                .until(ExpectedConditions.elementToBeClickable(element.findElement(By.xpath("custom-select/div[2]/div[contains(.,'" + select + "')]"))));
        element.findElement(By.xpath("custom-select/div[2]/div[contains(.,'" + select + "')]")).click();
    }
    protected void selectMenu(WebElement element) {
        selectMenu(element, 0);
    }

    protected String enterDate(String value,String nameDate) {
        StringBuilder date = new StringBuilder();
        String day, month, year;
            String[] tmp = value.split("-");
            LOG.info("Вводим дату");
            selectMenu(driver.findElement(By.xpath("//label[text()='" + nameDate + "']//ancestor-or-self::tr" + fieldYear)), Integer.parseInt(tmp[0]));
            selectMenu(driver.findElement(By.xpath("//label[text()='" + nameDate + "']//ancestor-or-self::tr" + fieldMonth )), Integer.parseInt(tmp[1]));
            selectMenu(driver.findElement(By.xpath("//label[text()='" + nameDate + "']//ancestor-or-self::tr" + fieldDay)), Integer.parseInt(tmp[2]));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        day = driver.findElement(By.xpath("//label[text()='" + nameDate + "']//ancestor-or-self::tr" + fieldDay)).getAttribute("innerText");
        month = driver.findElement(By.xpath("//label[text()='" + nameDate + "']//ancestor-or-self::tr" + fieldMonth)).getAttribute("innerText");
        year = driver.findElement(By.xpath("//label[text()='" + nameDate + "']//ancestor-or-self::tr" + fieldYear)).getAttribute("innerText");
        return date.append(year).append("-").append(month).append("-").append(day).toString();
    }





    @ActionTitle("проверяет присутствие текста")
    public void checksPresenceOfText(String text) {
        List<WebElement> list = PageFactory.getWebDriver().findElements(By.xpath("//*[text()='" + text + "']"))
                .stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
        assertThat(!list.isEmpty()).as("Ошибка.Не найден::[" + text + " ]").isTrue();
    }

    @ActionTitle("проверяет наличие сообщения с текстом")
    public void checkServiceMessageTrue(String param) throws InterruptedException {
        MatcherAssert.assertThat(true, equalTo(checkServiceMessage(param)));
    }

    @ActionTitle("проверяет отсутствие сообщения с текстом")
    public void checkServiceMessageFalse() throws InterruptedException {
        MatcherAssert.assertThat(false, equalTo(checkServiceMessage(null)));
    }

    @ActionTitle("проверяет отсутствие сообщения с текстом после закрытия")
    public void checkServiceMessageFalseAfterClose() {
        MatcherAssert.assertThat(false, equalTo(checkCloseServiceMessage(serviceMessage)));
    }

    @ActionTitle("проверяет наличие иконки закрытия")
    public void chectCloseServiceMessageTrue(){
        MatcherAssert.assertThat(true, equalTo(checkCloseServiceMessage(closeServiceMessage)));
    }

    @ActionTitle("проверяет отсутствие иконки закрытия")
    public void chectCloseServiceMessageFalse(){
        MatcherAssert.assertThat(false, equalTo(checkCloseServiceMessage(closeServiceMessage)));
    }

    public void fillCouponFinal(int count, String ifForExperss) throws InterruptedException {
        String findCoeffs = "//div[contains(@class,'livecal-table__coefficient') and not(contains(@class,'no-link'))]";
        if (ifForExperss.equals("correct")) {
            List<WebElement> eventsInCoupon;
            List<WebElement> correctMarkets;
            Thread.sleep(3000);
            waitForElementPresent(By.xpath(findCoeffs), 10);
            correctMarkets = getWebDriver().findElements(By.xpath(findCoeffs))
                    .stream().filter(e -> e.isDisplayed() && Double.parseDouble(e.getAttribute("innerText")) >= 1.260)
                    .limit(count + 10).collect(Collectors.toList());
            for (WebElement coefficient : correctMarkets) {
                tryToClick(coefficient);
                eventsInCoupon = PageFactory.getWebDriver().findElements(xpathListBets);
                        //PageFactory.getWebDriver().findElements(By.xpath("//li[@class='coupon-bet-list__item']"));
                LOG.info("коэф: " + coefficient.getAttribute("innerText"));
                if (eventsInCoupon.size() == count) {
                    break;
                }
            }
        }
        if (ifForExperss.equals("incorrect")) {
            List<WebElement> eventsInCoupon;
            List<WebElement> inCorrectMarkets = null;
            waitForElementPresent(By.xpath(findCoeffs), 10);
            List<WebElement> allDaysPages = PageFactory.getWebDriver().findElements(By.cssSelector("span.livecal-days__weekday.ng-binding"));
            int tryPage = 0;
            int counter = 10;
            do {
                try {
                    inCorrectMarkets = getWebDriver().findElements(By.xpath(findCoeffs))
                            .stream().filter(e -> e.isDisplayed() && !e.getAttribute("innerText").contains("-") && Double.parseDouble(e.getAttribute("innerText")) < 1.25)
                            .limit(count + 3).collect(Collectors.toList());
                } catch (StaleElementReferenceException e) {
                    tryPage++;
                    allDaysPages.get(tryPage).click();
                }
                counter--;
                Assert.assertNotEquals("Не нашли достаточное количество некорректных событий.", 0, counter);
            } while (Objects.requireNonNull(inCorrectMarkets).size() < count && tryPage < allDaysPages.size() - 1);
            for (WebElement coefficient : inCorrectMarkets) {
                clickElement(coefficient);
                eventsInCoupon = PageFactory.getWebDriver().findElements(xpathListBets);
                LOG.info("коэф: " + coefficient.getAttribute("innerText"));
                if (eventsInCoupon.size() == count) {
                    break;
                }
            }
        }
        WebDriverWait wait = new WebDriverWait(PageFactory.getWebDriver(),10);
        wait.withMessage("Попытались добавить " + count + " событий в купон. Но добавилось только " + getWebDriver().findElements(xpathListBets).size()
                + "\nВероятно, просто нет подходящих событий");
        wait.until(ExpectedConditions.numberOfElementsToBe(xpathListBets,count));
    }

    public void waitForElementPresent(final By by, int timeout) {
        WebDriverWait wait = (WebDriverWait) new WebDriverWait(PageFactory.getWebDriver(), timeout)
                .ignoring(StaleElementReferenceException.class);
        wait.withMessage("Элемент " + by + " так и не появился за " + timeout + " секунд");
        wait.until((ExpectedCondition<Boolean>) webDriver -> {
            WebElement element = Objects.requireNonNull(webDriver).findElement(by);
            return element != null && element.isDisplayed();
        });
    }

    public void tryToClick(WebElement element) {

       for(int count = 0; count < 10; count ++){
        try {
            element.click();
            break;
        } catch (StaleElementReferenceException e) {
            tryToClick(element);
        }
        }
    }

    @ActionTitle("ждёт мс")
    public void whait(String ms) throws InterruptedException {
        int time = Integer.parseInt(ms);
        Thread.sleep(time);
    }

    @ActionTitle("перезагружает страницу")
    public void refresh(){
        PageFactory.getWebDriver().navigate().refresh();
    }


    private boolean checkCloseServiceMessage(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e){
            return false;
        }
    }

    private boolean checkServiceMessage(String text) throws InterruptedException {
        int count = 0;
        while (count < 5) {
            try {
                serviceMessage.isDisplayed();
                MatcherAssert.assertThat(true, equalTo(serviceMessage.findElement(By.xpath("//div[contains(@class,'tech-msg__content')]")).getAttribute("innerText").equals(text)));
                return true;
            } catch (Exception e){
                count++;
                Thread.sleep(1000);
                PageFactory.getWebDriver().navigate().refresh();
            }
        }
        return false;
    }

    @ActionTitle("ждет некоторое время")
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

    @ActionTitle("очищает купон")
    public static void clearCoupon(){
//        if (clearCoupon.isDisplayed()){
//            clearCoupon.click();
//        }

        if (!getWebDriver().findElements(pathToclearCoupon).isEmpty() && clearCoupon.isDisplayed()){
            clearCoupon.click();
        }
        WebDriverWait wait = new WebDriverWait(PageFactory.getWebDriver(),10);
        wait.withMessage("Очистить купон. Но остались события " + getWebDriver().findElements(xpathListBets).size());
        wait.until(ExpectedConditions.numberOfElementsToBe(xpathListBets,0));
    }

    protected void waitingForPreloaderToDisappear(int timeInSeconds){
        WebDriver driver = PageFactory.getWebDriver();
        try {
            new WebDriverWait(driver, timeInSeconds).until(ExpectedConditions.invisibilityOfElementLocated(xpath("//*[contains(@class,'preloader__container')]")));
        }catch (Exception e){
            throw new AutotestError("Ошибка! Прелоадер не исчез в течение [" + timeInSeconds + "] сек.");
        }
    }

    @ActionTitle("закрывает всплывающее окно 'Перейти в ЦУПИС'")
    public void closePopUpWindowGoToTSUPIS(){
        WebDriver driver = PageFactory.getWebDriver();
        String xpathGoToTSUPIS = "//div[contains (@class,'after-reg')]/a[contains(@class,'btn_important')]";
        try{
            LOG.info("Ждём появление всплывающего окна.");
            new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathGoToTSUPIS)));
            LOG.info("Появилось окно c кнопкой [" + driver.findElement(By.xpath(xpathGoToTSUPIS)).getAttribute("innerText") + "]");
            driver.findElements(By.xpath("//div/a[@class='modal__closeBtn closeBtn']")).stream().filter(WebElement::isDisplayed).findFirst().get().click();
            LOG.info("Закрыли всплывающего окно");
        }catch (Exception e){
            LOG.info("Окно не появилось.");
        }
    }

    @ActionTitle("проверяет, что присутствует сообщение")
    public void checksThatMessageIsPresent(String message){
        try {
            new WebDriverWait(PageFactory.getWebDriver(),3).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(.,'" + message + "')]")));
        }catch (Exception e){
            throw new AutotestError("Ошибка! Текст [" + message + "] не появился");
        }
    }

    @ActionTitle("закрываем окно 'Перейти в ЦУПИС' если выскочит")
    public void closePopUpWindowGoToTSUPISIfOpened(){
        WebDriver driver = PageFactory.getWebDriver();
        try{
            new WebDriverWait(driver,10).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@href,'https://1cupis.ru/auth')]")));
            LOG.info("Открылось окно 'Перейти в ЦУПИС' - закрываем");
            driver.findElements(By.xpath("//a[contains(@class,'modal__closeBtn closeBtn')]")).stream().filter(WebElement::isDisplayed).findFirst().get().click();
        }catch (Exception e){
            LOG.info("Окно 'Перейти в ЦУПИС' не появилось");
        }
    }



    public void clickIfVisible(WebElement element){
        checkMenuIsOpen(true);
        new WebDriverWait(PageFactory.getDriver(),10)
                .withMessage("Левое меню не развернулось")
                .until(ExpectedConditions.attributeContains(expandCollapseMenusButton,"class","collapsed"));
        element.click();
    }

    /**
     * открытие/закрытие левого меню
     * @param openorclose = true - если нужно открыть. false - если нужно закрыть
     */
    public void checkMenuIsOpen(boolean openorclose){
        if(expandCollapseMenusButton.getAttribute("class").contains("collapsed")!=openorclose){
            expandCollapseMenusButton.click();
        }
    }

    /**
     * сворачивание или разворачивание левого меню
     * false - свернуть
     * true - развернуть
     */
    public static void setExpandCollapseMenusButton(boolean collapsOrNot){
        WebDriver driver = PageFactory.getDriver();
        WebDriverWait wait =  new WebDriverWait(driver,10);
        WebElement menu = driver.findElement(By.id("menu-toggler"));
        if (menu.getAttribute("class").contains("collapsed")!=collapsOrNot){
            menu.click();
//            if (!driver.findElements(preloaderOnPage).isEmpty()){
//                driver.navigate().refresh();
//                CommonStepDefs.workWithPreloader();
//            }
        }

        if (collapsOrNot) {
            wait.withMessage("Не удалось развернуть левое меню");
            wait.until(attributeContains(By.id("menu-toggler"), "class", "collapsed"));
        }
        else {
            wait.withMessage("Не удалось свернуть левое меню");
            wait.until(ExpectedConditions.not(attributeContains(By.id("menu-toggler"), "class", "collapsed")));
        }
    }

    protected void goToThisPage(String peaceURL){
        WebDriver driver = PageFactory.getWebDriver();
        Set<String> windows = driver.getWindowHandles();
        for(String windowHandle: windows) {
            driver.switchTo().window(windowHandle);
            if (driver.getCurrentUrl().contains(peaceURL)) {
                break;
            }
        }
    }

    /**
     * Метод ввода по символу с задержкой чтобы JS-маски успевали обрабатывать
     * @inputField поле ввода
     * @text текст, который нужно ввести
     * @delay задержка между вводом каждого символа в миллисекундах
     */
    protected void slowFillField(WebElement inputField, String text, int delay){
        inputField.clear();
        char[] tmp = text.toCharArray();
        for(int i = 0; i < tmp.length; i++) {
            inputField.sendKeys(String.valueOf(tmp[i]));
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

