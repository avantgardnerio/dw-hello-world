package com.mycompany.app;

import com.google.inject.AbstractModule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule;
import ru.vyarus.dropwizard.guice.test.GuiceyConfigurationRule;

import java.util.logging.Level;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class BrowserTest {

    private static WebDriver driver;
    private static HelloWorldApplication app;

    static DropwizardAppRule RULE = new DropwizardAppRule<>(HelloWorldApplication.class, "/example.yml");

    @ClassRule
    public static RuleChain chain = RuleChain
            .outerRule(new GuiceyConfigurationRule((builder) -> builder.modulesOverride(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(JobsRepo.class).to(JobsRepoMock.class);
                }
            })))
            .around(RULE);

    @BeforeClass
    public static void setup() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--enable-logging");
        options.addArguments("--v=1");
        options.addArguments("--enable-logging=stderr");

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        caps.setCapability(ChromeOptions.CAPABILITY, options);

        driver = new ChromeDriver(caps);

        RULE.getApplication().run("server", "/example.yml");
    }

    @AfterClass
    public static void teardown() throws Exception {
        driver.close();
        driver = null;

        app.stop();
        app = null;
    }

    @Test
    public void pageShouldRender() {
        // setup & exercise
        driver.get("http://127.0.0.1:8080/index.html");
        new WebDriverWait(driver, 5).until(presenceOfElementLocated(By.cssSelector(".App")));

        // assert
        WebElement link = driver.findElement(By.cssSelector("h1"));
        Assert.assertEquals(link.getText(), "Jobs");
    }
}
