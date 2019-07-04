package ru.gamble.pages.prematchPages;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gamble.pages.AbstractPage;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.annotations.ElementTitle;
import ru.sbtqa.tag.pagefactory.annotations.PageEntry;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementDecorator;
import ru.yandex.qatools.htmlelements.loader.decorator.HtmlElementLocatorFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@PageEntry(title = "Просмотр событий")
public class EventViewerPage extends AbstractPage {
    private static final Logger LOG = LoggerFactory.getLogger(EventViewerPage.class);
    static WebDriver driver = PageFactory.getDriver();

    @ElementTitle("Период времени")
    @FindBy(xpath = "//div[contains(@class,'periods__input')]")
    private WebElement selectPeriod;

    @ElementTitle("Кнопка МАКСБЕТ")
    @FindBy(xpath = "//label[contains(@class,'coupon-btn_max-bet')]/i[contains(@class,'icon-maxbet')]")
    private WebElement maxBet;

    private static By xpathForsportsPrematch = By.xpath("//*[@id='sports-list-container']//li[contains(@id,'sport') and not(contains(@id,'sport--'))]");
    private static By xpathSport = By.xpath("//li[contains(@class,'left-menu__list-item-sport') and not(contains(@id,'sport--')) and not(contains(@class,'favorite'))]");
    private static By xpathRegion = By.xpath(".//li[contains(@class,'left-menu__list-item-region')]");//путь, начиная от спорта. т.е. от li
    private static By xpathCompetition = By.xpath(".//div[contains(@class,'left-menu__list-item-region-compitition')]");//путь, начиная от спорта. т.е. от li


    public EventViewerPage() {
        PageFactory.initElements(new HtmlElementDecorator(new HtmlElementLocatorFactory(driver)), this);
        new WebDriverWait(PageFactory.getDriver(), 10).until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//div[contains(@class,'menu-toggler')]"))));
        checkMenuIsOpen(true);
    }




    /**
     * провекра что игры в первых 5 спортах удовлетворяют фильтру по времени
     * @param period
     */

    /**
     * проверка что все игры в первых 5 регионах спорта sport удовлетворяют периоду времени
     * @param sport
     * @param valuePeriod
     */
    private void checkRegionsInSport(int sport,String valuePeriod){
        WebDriverWait wait = new WebDriverWait(driver,10);
        int countRegions = driver.findElements(xpathSport).get(sport).findElements(xpathRegion).size(); //количество регионов
        countRegions = Math.min(countRegions,5);//будем смотреть толкьо первые 5 регионов
        WebElement region;
        for (int i=0;i<countRegions;i++){
            region = driver.findElements(xpathSport).get(sport).findElements(xpathRegion).get(i);
            if (!region.getAttribute("class").contains("active")) {
                LOG.info("разворачиваем регион " + region.getAttribute("innerText").split("\n")[0]);
                clickIfVisible(region);
                wait
                        .withMessage("Регион не раскрылся")
                        .until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath(xpathCompetition.toString().split(" ")[1].replace(".","")),0));
            }

            checkCompetitionsInRegion(sport,i,valuePeriod);
            checkMenuIsOpen(true);
            region = driver.findElements(xpathSport).get(sport).findElements(xpathRegion).get(i);
            if (region.getAttribute("class").contains("active")) {
                LOG.info("сворачиваем регион " + region.getAttribute("innerText").split("\n")[0]);
                clickIfVisible(region.findElement(By.xpath(".//div[contains(@class,'icon-arrow')]")));
            }
        }
    }

    /**
     * проверка что все игры в первых 5 соревнованиях выбранного региона удовлетворяют периоду времени
     * @param region
     * @param valuePeriod
     */
    private void checkCompetitionsInRegion(int sport, int region,String valuePeriod){
        WebElement competition;
        int countCompetit = driver.findElements(xpathSport).get(sport).findElements(xpathRegion).get(region).findElements(xpathCompetition).size(); //количество соревновани в выбранном регионе
        countCompetit = Math.min(countCompetit,5);
        for (int i=0;i<countCompetit;i++){
            checkMenuIsOpen(true);
            competition = driver.findElements(xpathSport).get(sport).findElements(xpathRegion).get(region).findElements(xpathCompetition).get(i);
            LOG.info("Выбираем сорвенование в этом регионе");
            clickIfVisible(competition);//кликнули на выбранное сорвенование в регионе. теперь в центральной части страниыв отображаются игры только этого соревнования
            setExpandCollapseMenusButton(false); //сворачиваем левое меню.чтобы ЦО была полностью видна
            checkGameOnCenter(valuePeriod);
        }
    }

    private void checkGameOnCenter(String valuePeriod){
        SimpleDateFormat formatterOnlyDate = new SimpleDateFormat("dd MMM yyyy", new Locale("ru"));
        Calendar today = Calendar.getInstance();
        String segodnya = formatterOnlyDate.format(today.getTime());
        today.add(Calendar.DAY_OF_YEAR,1);
        String zavtra = formatterOnlyDate.format(today.getTime());
        today.add(Calendar.DAY_OF_YEAR,1);
        String poslezavtra = formatterOnlyDate.format(today.getTime());
        By xpathToDate = By.xpath("../preceding-sibling::div[contains(@class,'prematch-competition__header')]/div[contains(@class,'prematch-competition__header-date')]");
        By xpathToTime = By.xpath(".//div[contains(@class,'bets-block__header-left-info')]");
//        LOG.info("Закроем ЛМ чтобы не мешалось");
//        checkMenuIsOpen(false);
        List <WebElement> games = driver.findElements(By.xpath("//div[@class='prematch-competition__games']/div[contains(@class,'bets-block_single-row')]"));
        LOG.info("В Центральной области страницы " + games.size() + " игр");
        List <String> dateGamesString = new ArrayList<>();
        List <String> nameGames = driver.findElements(By.xpath("//div[@class='bets-block__header-teams']")).stream().map(el->el.getAttribute("innerText")).collect(Collectors.toList());
        games.forEach(el->dateGamesString.add(
                el.findElement(xpathToDate).getAttribute("innerText")+
                " " + el.findElement(xpathToTime).getAttribute("innerText")));
        Calendar period = Calendar.getInstance();
        Calendar dateOneGame = Calendar.getInstance();
        int hoursPeriod = 0;
        if (!valuePeriod.equals("Выберите время")) {
            hoursPeriod = valuePeriod.contains("час") ? Integer.valueOf(valuePeriod.split(" ")[0]) : Integer.valueOf(valuePeriod.split(" ")[0]) * 24;
        }
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy k:mm");
        period.add(Calendar.HOUR,hoursPeriod);
        String dateGame=new String();
        for (int i=0;i<dateGamesString.size();i++){
            LOG.info("Проверяем игру " + nameGames.get(i));
            dateGame = dateGamesString.get(i);
            int ind = dateGame.indexOf(":");
            dateGame = dateGame.substring(0,ind-3);
            switch (dateGame){
                case "Сегодня":
                    dateGame=segodnya + " " + dateGamesString.get(i).substring(ind-2);
                    break;
                case "Завтра":
                    dateGame=zavtra + " " + dateGamesString.get(i).substring(ind-2);
                    break;
                case "Послезавтра":
                    dateGame=poslezavtra + " " + dateGamesString.get(i).substring(ind-2);
                    break;
                default:
                    dateGame = dateGamesString.get(i);
                    break;
            }
            try {
                dateOneGame.setTime(format.parse(dateGame));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Assert.assertTrue("Игра не соответсвтует выбранному периоду " + valuePeriod + ":: время игры " + dateGame,
                    dateOneGame.before(period));
            LOG.info("Игра соответсвует выбранному периоду");
            i++;
        }
    }








}


