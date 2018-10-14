package com.mycompany.app;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
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

import java.util.logging.Level;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class BrowserTest {

    private static WebDriver driver;
    private static HelloWorldApplication app;

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

        app = new HelloWorldApplication();
        app.run(new String[]{"server", "/example.yml"});
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
        new WebDriverWait(driver, 20).until(presenceOfElementLocated(By.cssSelector(".App")));

        // assert
        WebElement link = driver.findElement(By.cssSelector("a"));
        Assert.assertEquals(link.getText(), "Learn React2");
    }
}
