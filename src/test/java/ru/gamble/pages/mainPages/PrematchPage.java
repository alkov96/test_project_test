package ru.gamble.pages.mainPages;

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

/**
 * @author p.sivak.
 * @since 04.05.2018.
 */
@PageEntry(title = "Прематч")
public class PrematchPage extends AbstractPage {
    private static final Logger LOG = LoggerFactory.getLogger(PrematchPage.class);
    static WebDriver driver = PageFactory.getDriver();

    @FindBy(xpath = "//a[@id='live-calendar']")
    private WebElement menu;

    @ElementTitle("Просмотр событий")
    @FindBy(id = "prematch-events")
    private WebElement prematchEventsBottom;

    @ElementTitle("Лайв-календарь")
    @FindBy(id = "live-calendar")
    private WebElement liveCalendarBottom;

    @ElementTitle("Результаты")
    @FindBy(id = "results")
    private WebElement resultsBottom;

    public PrematchPage() {
        PageFactory.initElements(new HtmlElementDecorator(new HtmlElementLocatorFactory(driver)), this);
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOf(menu));
    }
}
