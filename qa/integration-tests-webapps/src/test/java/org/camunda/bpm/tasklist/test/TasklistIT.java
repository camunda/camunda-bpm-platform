package org.camunda.bpm.tasklist.test;

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

/**
 * @author drobisch
 */
public class TasklistIT {
  WebDriver driver;
  private String appUrl;

  @Before
  public void before() throws Exception {

    TestProperties testProperties = new TestProperties(48080);

    appUrl = testProperties.getApplicationPath("/tasklist");
    driver = new FirefoxDriver();
  }

  @Test
  public void testLogin() throws InterruptedException {
    driver.get(appUrl+"/app/#/login");

    WebDriverWait wait = new WebDriverWait(driver, 10);

    WebElement user = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"text\"]")));
    user.sendKeys("demo");

    WebElement password= wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"password\"]")));
    password.sendKeys("demo");

    WebElement submit = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".form-signin")));
    submit.submit();

    wait.until(ExpectedConditions.textToBePresentInElement(By.cssSelector("td"), "Assign Approver"));
  }

  @After
  public void after() {
    driver.close();
  }
}
