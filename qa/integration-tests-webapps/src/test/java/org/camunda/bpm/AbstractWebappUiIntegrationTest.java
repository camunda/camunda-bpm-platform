/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm;

import org.camunda.bpm.util.SeleniumScreenshotRule;
import org.camunda.bpm.util.TestUtil;
import org.junit.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

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
    String chromeDriverExecutable = "chromedriver";
    if (System.getProperty( "os.name" ).toLowerCase(Locale.US).indexOf("windows") > -1) {
      chromeDriverExecutable += ".exe";
    }

    File chromeDriver = new File("target/chromedriver/" + chromeDriverExecutable);
    if (!chromeDriver.exists()) {
      throw new RuntimeException("chromedriver could not be located!");
    }

    ChromeDriverService chromeDriverService = new ChromeDriverService.Builder()
        .withVerbose(true)
        .usingAnyFreePort()
        .usingDriverExecutable(chromeDriver)
        .build();

    driver = new ChromeDriver(chromeDriverService);
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
