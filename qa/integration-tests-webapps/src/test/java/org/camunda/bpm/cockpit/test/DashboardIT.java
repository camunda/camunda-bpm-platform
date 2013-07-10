package org.camunda.bpm.cockpit.test;

import org.camunda.bpm.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DashboardIT {

  WebDriver driver;
  private String appUrl;

  @Before
    public void before() throws Exception {

      TestProperties testProperties = new TestProperties(48080);

      appUrl = testProperties.getApplicationPath("/camunda/app/cockpit");
      driver = new FirefoxDriver();
    }

    @Test
    public void testDashboard() throws InterruptedException {
      driver.get(appUrl);

      WebDriverWait wait = new WebDriverWait(driver, 10);
      WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.tile")));
      element.click();
      Boolean found = wait.until(ExpectedConditions.textToBePresentInElement(By.tagName("h1"), "invoice receipt"));
    }

    @After
    public void after() {
      driver.close();
    }

}
