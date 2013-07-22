package org.camunda.bpm.tasklist.test;


import org.camunda.bpm.TestProperties;
import org.camunda.bpm.util.TestUtil;
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

  protected WebDriver driver;
  protected String appUrl;

  protected TestProperties testProperties;
  private TestUtil testUtil;

  @Before
  public void before() throws Exception {
    testProperties = new TestProperties(48080);
    appUrl = testProperties.getApplicationPath("/camunda/app/tasklist");
    driver = new FirefoxDriver();

    testUtil = new TestUtil(testProperties);

    testUtil.createUser("demo", "demo", "Mr.", "Admin");
  }

  @Test
  public void testLogin() {
    driver.get(appUrl + "/#/login");

    WebDriverWait wait = new WebDriverWait(driver, 10);

    WebElement user = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"text\"]")));
    user.sendKeys("demo");

    WebElement password= wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"password\"]")));
    password.sendKeys("demo");

    WebElement submit = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type=\"submit\"]")));
    submit.submit();

    boolean found = wait.until(ExpectedConditions.textToBePresentInElement(By.cssSelector("td"), "Assign Approver"));
  }

  @After
  public void after() {
    testUtil.deleteUser("demo");
    testUtil.destroy();

    driver.close();
  }
}
