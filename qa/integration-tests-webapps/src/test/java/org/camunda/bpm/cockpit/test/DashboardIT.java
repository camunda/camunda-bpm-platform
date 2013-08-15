package org.camunda.bpm.cockpit.test;


import org.camunda.bpm.TestProperties;
import org.camunda.bpm.util.SeleniumScreenshotRule;
import org.camunda.bpm.util.TestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class DashboardIT {

  protected static WebDriver driver = new FirefoxDriver();
  protected String appUrl;

  protected TestProperties testProperties;

  private TestUtil testUtil;

  @Rule
  public SeleniumScreenshotRule screenshotRule = new SeleniumScreenshotRule(driver);

  @Before
  public void before() throws Exception {
    testProperties = new TestProperties(48080);
    appUrl = testProperties.getApplicationPath("/camunda/app/cockpit");

    testUtil = new TestUtil(testProperties);

//    testUtil.createInitialUser("admin", "admin", "Mr.", "Admin");
  }

  @Test
  public void testLogin() {
    driver.get(appUrl+"/#/login");

    WebDriverWait wait = new WebDriverWait(driver, 10);

    WebElement user = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"text\"]")));
    user.sendKeys("demo");

    WebElement password= wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"password\"]")));
    password.sendKeys("demo");

    WebElement submit = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type=\"submit\"]")));
    submit.submit();

    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.tile")));
    element.click();
    Boolean found = wait.until(ExpectedConditions.textToBePresentInElement(By.tagName("h1"), "invoice receipt"));
  }

  @After
  public void after() {
//    testUtil.deleteUser("admin");
    testUtil.destroy();
  }

  @AfterClass
  public static void cleanup() {
    driver.close();
  }
}
