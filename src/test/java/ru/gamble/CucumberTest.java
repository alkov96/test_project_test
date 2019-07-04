package ru.gamble;

import cucumber.api.CucumberOptions;
import cucumber.api.Scenario;
import cucumber.api.junit.Cucumber;
import io.qameta.allure.Attachment;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gamble.stepdefs.CommonStepDefs;
import ru.sbtqa.tag.pagefactory.PageFactory;

import javax.mail.Session;
import java.util.logging.FileHandler;

import static java.rmi.server.RemoteServer.getLog;

//del /q C:\Workspace\autotests-888-m\allure-results
@RunWith(Cucumber.class)
@CucumberOptions(
        monochrome = true,
        glue = {"ru.gamble.stepdefs", "ru.sbtqa.tag.stepdefs.ru"},
        features = {"src/test/resources/features/"},
        plugin = {"io.qameta.allure.cucumber2jvm.AllureCucumber2Jvm","pretty"},
        tags = {"@smoke"})

public class CucumberTest {
    private static final Logger LOG = LoggerFactory.getLogger(CucumberTest.class);

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            screenshot();
        }

        @Attachment(value = "Page screenshot", type = "image/jpeg")
        public byte[] saveScreenshot(byte[] screenShot) {
            return screenShot;
        }

        public void screenshot() {
            if (PageFactory.getWebDriver() == null) {
                LOG.info("Driver for screenshot not found");
            }
        }
    };

    @AfterClass
    public static void afterScenario(){
        PageFactory.dispose();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}




