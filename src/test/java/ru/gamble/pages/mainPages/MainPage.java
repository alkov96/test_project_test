package ru.gamble.pages.mainPages;


import cucumber.api.DataTable;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gamble.pages.AbstractPage;
import ru.gamble.stepdefs.CommonStepDefs;
import ru.sbtqa.tag.datajack.Stash;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.annotations.ActionTitle;
import ru.sbtqa.tag.pagefactory.annotations.ElementTitle;
import ru.sbtqa.tag.pagefactory.annotations.PageEntry;
import ru.sbtqa.tag.qautils.errors.AutotestError;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementDecorator;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementLocatorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@PageEntry(title = "Главная страница")
public class MainPage extends AbstractPage {
    private static final Logger LOG = LoggerFactory.getLogger(MainPage.class);
    static WebDriver driver = PageFactory.getDriver();

    @FindBy(xpath = "//div[contains(@class,'main-slider__wrapper')]")
    private WebElement slider;

    @ElementTitle("Регистрация")
    @FindBy(id = "register")
    private WebElement registrationButton;

    @ElementTitle("Прематч")
    @FindBy(id = "prematch")
    private WebElement prematchButton;

    @ElementTitle("Лайв")
    @FindBy(id = "live")
    private WebElement liveButton;

    @ElementTitle("Настройки")
    @FindBy(id = "preferences")
    public WebElement preferences;

    // Блок новостей
    @ElementTitle("Стрелка-вправо")
    @FindBy(xpath = "//*[contains(@class,'news-widget') and contains(@style,'visible')]/button[contains(@class,' next')]")
    private WebElement arrowRightButton;

    @ElementTitle("Стрелка-влево")
    @FindBy(xpath = "//*[contains(@class,'news-widget') and contains(@style,'visible')]/button[contains(@class,'previous')]")
    private WebElement arrowLeftButton;

    @ElementTitle("Новости")
    @FindBy(xpath = "//a[contains(@class,'bets-widget') and contains(.,'Новости')]")
    private WebElement newsButton;

    @ElementTitle("Анонсы")
    @FindBy(xpath = "//a[contains(@class,'bets-widget') and contains(.,'Анонсы')]")
    private WebElement announceButton;

    public MainPage() {
        PageFactory.initElements(new HtmlElementDecorator(new HtmlElementLocatorFactory(driver)), this);
        tryingLoadPage(By.xpath("//div[contains(@class,'main-slider__wrapper')]"),3, 5);
    }

    @ActionTitle("переключение видов спорта")
    public void checkChangeSport(String widget) {
        String path;
        switch (widget) {
            case "Горячие ставки":
                path = "//div[@class='bets-widget lastMinutesBets']";
                break;
            default:
                path = "//div[@class='bets-widget nearestBroadcasts']";
                break;
        }

        WebDriverWait wait = new WebDriverWait(driver,10);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        CommonStepDefs.waitOfPreloader();
        LOG.info("Смотрим что страницы в виджете переключаются и содержимое контейнера соответсвует выбранному виду спорта");
        String sportName;
        //    List<WebElement> allSport = driver.findElements(By.xpath("//div[contains(@class,'nearestBroadcasts')]//li[contains(@class,'sport-tabs__item') and not(contains(@class,'no-link'))]"));
        List<WebElement> allSport = driver.findElements(By.xpath(path + "//li[contains(@class,'sport-tabs__item') and not(contains(@class,'no-link'))]"));

        for (WebElement selectSport : allSport) {
            selectSport.click();
            sportName = selectSport.findElement(By.xpath("i")).getAttribute("class").replace("sport-tabs__icon sport-icon icon-", "");
            LOG.info(sportName);
            wait.until(CommonStepDefs.attributeContainsLowerCase(
                    By.xpath(path + "//div[contains(@class,'bets-widget-table__inner')]"),"class",sportName));

            if (driver.findElements(By.xpath(path + "//div[contains(@class,'bets-widget-table__inner')]/table[1]/tbody/tr")).size() == 1) {
                LOG.error("В ближайших трансляциях есть вкладка спорта " + sportName + ", но список для него пустой");
            }
        }

    }

    /**
     * поиск на виджете Ближайшие трансляции игры с (без) кнопки Смотреть. Если игры по кнопк не найдено, то берется просто первая попавшаяся
     * @param param - параметр, указывающий ищем ли мы игру с кнопкой, или без
     * в Stash сохраняет найденную игру. ключ - "gameBT"
     */
    @ActionTitle("ищет игру на БТ")
    public  void searchVideoGameBT(String param){
        boolean haveButton = param.equals("с кнопкой Смотреть");
        String ngclick;
        List<WebElement> games;
        if (haveButton) {
            ngclick = "button";
        } else {
            {
                ngclick = "command";
            }
        }
        CommonStepDefs.waitOfPreloader();
        int x = driver.findElement(By.xpath("//div[contains(@class,'nearestBroadcasts')]//li[contains(@class,'sport-tabs__item')]/../li[last()]")).getLocation().getX();
        int y = driver.findElement(By.xpath("//div[contains(@class,'nearestBroadcasts')]//li[contains(@class,'sport-tabs__item')]/../li[last()]")).getLocation().getY();
        CommonStepDefs.scrollPage(x, y);
        List<WebElement> allSport = driver.findElements(By.xpath("//div[contains(@class,'nearestBroadcasts')]//li[contains(@class,'sport-tabs__item')]"));//все вид спортов на виджете БТ
        int number = 0;
        do {
            games = driver.findElements(By.xpath("//div[@class='bets-widget nearestBroadcasts']/div[2]/div[1]/table[1]/tbody/tr/td[position()=1 and contains(@ng-click,'" + ngclick + "')]"));
            if (!games.isEmpty()) {
                break;
            }
            if(allSport.get(0).getAttribute("innerText").contains("Подходящих игр не найдено")){throw new AutotestError("Ошибка! Подходящих игр не найденою");}
            allSport.get(number).click();


            number++;
        } while (number <= allSport.size() - 1);
        if (games.isEmpty()){
            LOG.info("Подходящей игры не найдено. Придется брать просто первую попавшуюся из БТ");
            games = driver.findElements(By.xpath("//div[@class='bets-widget nearestBroadcasts']/div[2]/div[1]/table[1]/tbody/tr/td[position()=1 and @ng-click]"));
            haveButton = !haveButton;
        }else {
            LOG.info("Игра " + param + " найдена: " + games.get(0).getAttribute("innerText") + games.get(0).findElement(By.xpath("ancestor::tr")).getAttribute("inenrText"));
        }
        Stash.put("gameBT",games.get(0).findElement(By.xpath("ancestor::tr")));
        Stash.put("haveButtonKey",haveButton);
    }

    //переходит на игру нажатием на название первой команды в виджете
    @ActionTitle("переходит на игру из виджета БТ")
    public void lala(){
        WebElement selectGame = Stash.getValue("gameBT");
        //запоминаем названия команд
        String team1 = selectGame.findElement(By.xpath("td[contains(@class,'bets-item_who1')]/div[1]")).getAttribute("title").trim();
        String team2 = selectGame.findElement(By.xpath("td[contains(@class,'bets-item_who2')]/div[1]")).getAttribute("title").trim();
        String sportName = selectGame.findElement(By.xpath("ancestor::div[contains(@class,'bets-widget-table__inner active')]")).getAttribute("class").split("active-")[1].toLowerCase();
        LOG.info("Игра, на которой будем проверять переход из виджета БТ: " + team1 + " - " + team2 + ". Спорт - " + sportName);
        selectGame.findElement(By.xpath("td[contains(@class,'bets-item_who1')]")).click();
        Stash.put("team1BTkey",team1);
        Stash.put("team2BTkey",team2);
        Stash.put("sportKey",sportName);
    }


    @ActionTitle("проверяет что переход удался")
    public void openGame(){
        CommonStepDefs commonStepDefs = new CommonStepDefs();
        commonStepDefs.checkLinkToGame();
    }

    //добавление коэфа победы первой команды в виджете БТ
    @ActionTitle("добавляет коэф с виджета в купон и сохраняет название команд, коэф и исход")
    public void addToCouponFromBT(String widget, DataTable dataTable){
        List<String> data = dataTable.asList(String.class);
        String team1key = data.get(0);
        String team2key = data.get(1);
        String ishodKey = data.get(2);
        String coefKey = data.get(3);
        String path;
        switch (widget) {
            case "Горячие ставки":
                path = "//div[contains(@class,'lastMinutesBets')]";
                break;
            default:
                path = "//div[contains(@class,'nearestBroadcasts')]";
                break;
        }
        List<WebElement> games;
        List<WebElement> allSport = driver.findElements(By.xpath(path + "//li[contains(@class,'sport-tabs__item')]"));//все вид спортов на виджете
        int number = 0;
        do {
            games = driver.findElements(By.xpath(path + "/div[2]/div[1]/table[1]/tbody/tr/td[contains(@class,'bets-item_k1')]/div[not(contains(@class,'blocked'))]"));
            if (!games.isEmpty()) {
                break;
            }
            if(allSport.get(0).getAttribute("innerText").contains("Подходящих игр не найдено")) {
             throw new AutotestError("Ошибка! Подходящих игр не найдено");
            }

            allSport.get(number).click();


            number++;

        } while (number <= allSport.size() - 1);

        WebElement selectGame = games.get(0).findElement(By.xpath("ancestor::tr"));
        String bannerTeam1 = selectGame.findElement(By.xpath("td[contains(@class,'bets-item_who1')]/div[1]")).getAttribute("title").trim();
        String bannerTeam2 = selectGame.findElement(By.xpath("td[contains(@class,'bets-item_who2')]/div[1]")).getAttribute("title").trim();
        float p1 = Float.valueOf(selectGame.findElement(By.xpath("td[contains(@class,'bets-item_k1')]/div[1]/span")).getAttribute("innerText"));
        LOG.info("Игра, на которой будем проверять добавление в купон из виджета БТ: " + bannerTeam1 + " - " + bannerTeam2);
        LOG.info("Коэффициент победы первой команды = " + p1);
        selectGame.findElement(By.xpath("td[contains(@class,'bets-item_k1')]/div[1]/span")).click();
        new WebDriverWait(driver,10).until(ExpectedConditions.elementToBeClickable(By.id("menu-toggler")));
        Stash.put(team1key,bannerTeam1);
        Stash.put(team2key,bannerTeam2);
        Stash.put(ishodKey,bannerTeam1);//мы выбирали победу первой команды, поэтому и в купоне название ихода должно совпадать с первой командой
        Stash.put(coefKey,p1);
    }

    @ActionTitle("осуществляет переход на страницу, проверяет, что открылась нужная страница")
    public void widgetsOnMain(){
        List<WebElement> attr = driver.findElements(By.xpath("//div[@class='benef__item']/a"));
        for (WebElement element : attr) {
            String link = element.getAttribute("href");
            CommonStepDefs.goLink(element, link);
            LOG.info("Ссылка " + link + " открылась");
        }
    }

    @ActionTitle("проверяет что дайджест новостей не пустой")
    public void verifiesThatNewsDigestsNotEmpty() {
        List<WebElement> digestList = PageFactory.getWebDriver().findElements(By.xpath("//a[@class='news-widget__item-inner']"))
                .stream().filter(element -> !element.getAttribute("innerText").isEmpty()).collect(Collectors.toList());
        assertThat(!digestList.isEmpty()).as("Ошибка! Не найден ни один дайджест в блоке новостей").isTrue();

        for(WebElement element: digestList){
          LOG.info("Найдена новость::" + element.getAttribute("innerText").replaceAll("\n", " ").replaceAll("\\?","\""));
        }
    }

    @ActionTitle("ищет доступные коэффиценты на Главной")
    public void findAvailableCoef() {
        List<WebElement> coeff = driver.findElements(By.cssSelector("div.bets-widget-table__link"));
            if (coeff.size() == 0) {
                LOG.error("Нет доступных коэффициентов в разделе 'Горячие ставки'");
                LOG.info("Переходим в прематч");
                prematchButton.click();
                coeff = driver.findElements(By.cssSelector("div.bets-block__bet-cell"));
                if (coeff.size() == 0) {
                    Assertions.fail("Нет доступных коэффициентов");
                }
            }else{
                Stash.put("coeffKey", coeff.get(0));
            }
    }
    @ActionTitle("переходит в настройки и меняет коэффицент на Главной")
    public void changePreferencesCoeff() throws InterruptedException {
        LOG.info("переходит в настройки и меняет коэффицент");
        preferences.click();
        String previous;
        List<WebElement> list = driver.findElements(By.xpath("//li[contains(@class,'prefs__li_opt')]/span[contains(@class,'prefs__val')]/preceding-sibling::span[contains(@class,'prefs__key')]"));
        WebElement coeff = Stash.getValue("coeffKey");
        for (int i = list.size()-1; i >0; i--) {
            previous = coeff.getAttribute("innerText");
            LOG.info("Переключаемся на '" + list.get(i).getAttribute("innerText") + "' формат отображения"); // рандомно берёт 1 тип из 6
            list.get(i).click();
            LOG.info("Текущее значение коэффициента : " + coeff.getAttribute("innerText"));
            Thread.sleep(350);
            if (previous.equals(coeff.getAttribute("innerText"))){
                LOG.error("Формат отображения коэффициентов не изменился");
                Assertions.fail("Формат отображения коэффициентов не изменился: " + previous +" " + coeff.getAttribute("innerText"));
            }
        }
       // list.get(0).click();
        LOG.info("Смена форматов отображения коэффицентов прошла успешно");
    }




    @ActionTitle("ищет подходящий спорт в Горячих ставках")
    public void findSportHB(){
        WebDriverWait wait = new WebDriverWait(driver,15);
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

        List<String> sportsLanding = new ArrayList<>();
        driver.findElements(By.xpath("//div[@class='footer6__block footer6__block-forecast']//a[@class='f_menu-link']")).forEach(element->
        sportsLanding.add(element.getAttribute("innerText").replace("На ","")));

        //смотрим список всех спортов. ищем там нужный нам
        List<WebElement> allSport = driver.findElements(By.xpath("//div[@class='bets-widget lastMinutesBets']//li[contains(@class,'sport-tabs__item') and not(contains(@class,'no-link'))]"));
        int index = -1;
        for (WebElement sport : allSport){
            //index = sportsLanding.indexOf(sport.getAttribute("title").toLowerCase().trim());
            for (String subSportName: sport.getAttribute("title").toLowerCase().trim().split(" ")){
                index = sportsLanding.indexOf(subSportName);
                if (index!=-1) break;
            }
            if (index>0){
                sport.click(); //кликаем на нужный спорт в Гоячих ставках чтобы убедиться что там есть записи
                wait.until(CommonStepDefs.attributeContainsLowerCase(
                        By.xpath("//div[@class='bets-widget lastMinutesBets']//div[contains(@class,'bets-widget-table__inner')]"),"class","active"));
                if (driver.findElements(By.xpath("//div[@class='bets-widget lastMinutesBets']//div[contains(@class,'bets-widget-table__inner')]/table[1]/tbody/tr")).size() == 1) {
                    LOG.error("В горячих ставках есть вкладка спорта " + sport + ", но список для него пустой");
                }
                else break;
            }
        }
        assertTrue("В горячих ставках нет походящих видов спорта",index>=0);
        LOG.info("index = " + index);
        Stash.put("indexLandingSportKey",index);
    }

    @ActionTitle("переходит на лендинг вида спорта")
    public void openLandingSport(){
        WebDriverWait wait = new WebDriverWait(driver,10);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        int index=0;
        if (Stash.asMap().containsKey("indexLandingSportKey")){
            index= Stash.getValue("indexLandingSportKey");
        }
        LOG.info(driver.findElements(By.xpath("//div[@class='footer6__block footer6__block-forecast']//a[@class='f_menu-link']")).get(index).getAttribute("innerText"));
        String sport = driver.findElements(By.xpath("//div[@class='footer6__block footer6__block-forecast']//a[@class='f_menu-link']")).get(index).getAttribute("href").substring(1);
        driver.findElements(By.xpath("//div[@class='footer6__block footer6__block-forecast']//a[@class='f_menu-link']")).get(index).click();
        wait.until(ExpectedConditions.urlContains(sport));
        waitForElementPresent(By.cssSelector("h1.landing-sports-section__h"),10);
        Stash.put("sportKey",sport);
    }





    @ActionTitle("переходит на игру из Горячих ставок со ставкой Исход и запоминает id игры")
    public void goToGameFromHBandRememberID(String keyGameId){
        String path = "//div[contains(@class,'lastMinutesBets')]";
        List<WebElement> games;
        List<WebElement> allSport = driver.findElements(By.xpath(path + "//li[contains(@class,'sport-tabs__item')]"));//все вид спортов на виджете
        int number = 0;
        do {
            if(allSport.get(0).getAttribute("innerText").contains("Подходящих игр не найдено")) {
                throw new AutotestError("Ошибка! Подходящих игр не найдено");
            }
            games = driver.findElements(By.xpath(path + "//td[contains(@class,'bets-item_k1')]//span[contains(@class,'bets-widget-table__price')]"));
            if (!games.isEmpty()) {
                break;
            }
            allSport.get(number).click();


            number++;

        } while (number <= allSport.size() - 1);

        WebElement selectGame = games.get(0).findElement(By.xpath("ancestor::tr"));

        LOG.info("Переход на игру из Горячих ставок " + selectGame.getAttribute("innerText"));
        selectGame.click();
        new WebDriverWait(driver,10).until(ExpectedConditions.elementToBeClickable(By.id("menu-toggler")));


        String game = driver.getCurrentUrl().split("game=")[1];
        Stash.put(keyGameId,game);
        driver.findElement(By.id("main-logo")).click();

    }


}
