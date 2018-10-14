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
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

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

        DesiredCapabilities dc = new DesiredCapabilities();
        dc.setJavascriptEnabled(true);
        dc.setCapability(ChromeOptions.CAPABILITY, options);

        driver = new ChromeDriver(dc);

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
