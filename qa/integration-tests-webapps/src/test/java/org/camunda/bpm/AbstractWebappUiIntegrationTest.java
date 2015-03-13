package org.camunda.bpm;

import org.camunda.bpm.util.SeleniumScreenshotRule;
import org.camunda.bpm.util.TestUtil;
import org.junit.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.net.URI;
import java.net.URISyntaxException;

public class AbstractWebappUiIntegrationTest {

  protected static WebDriver driver;

  protected String appUrl;
  protected TestProperties testProperties;
  protected TestUtil testUtil;
  protected String contextPath;

  @Rule
  public SeleniumScreenshotRule screenshotRule = new SeleniumScreenshotRule(driver);

  public AbstractWebappUiIntegrationTest(String contextPath) {
    this.contextPath = contextPath;
  }

  @BeforeClass
  public static void createDriver() {
    driver = new ChromeDriver();
  }

  @Before
  public void before() throws Exception {
    testProperties = new TestProperties(48080);
    appUrl = testProperties.getApplicationPath(contextPath);

    testUtil = new TestUtil(testProperties);
  }

  public static ExpectedCondition<Boolean> currentURIIs(final URI pageURI) {

    return new ExpectedCondition<Boolean>() {
      @Override
      public Boolean apply(WebDriver webDriver) {
        try {
          return new URI(webDriver.getCurrentUrl()).equals(pageURI);
        } catch (URISyntaxException e) {
          return false;
        }
      }
    };

  }

  @After
  public void after() {
    testUtil.destroy();
  }

  @AfterClass
  public static void quitDriver() {
    driver.quit();
  }
}
