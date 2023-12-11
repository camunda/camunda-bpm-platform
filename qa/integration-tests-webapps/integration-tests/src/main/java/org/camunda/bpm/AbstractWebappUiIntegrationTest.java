/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.camunda.bpm.util.SeleniumScreenshotRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class AbstractWebappUiIntegrationTest extends AbstractWebIntegrationTest {

  protected static WebDriver driver;

  @Rule
  public SeleniumScreenshotRule screenshotRule = new SeleniumScreenshotRule(driver);

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

    ChromeOptions chromeOptions = new ChromeOptions()
        .addArguments("--headless=new")
        .addArguments("--window-size=1920,1200")
        .addArguments("--disable-gpu")
        .addArguments("--no-sandbox")
        .addArguments("--disable-dev-shm-usage")
        .addArguments("--remote-allow-origins=*");

    driver = new ChromeDriver(chromeDriverService, chromeOptions);
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

  public static ExpectedCondition<Boolean> containsCurrentUrl(final String url) {

    return new ExpectedCondition<Boolean>() {
      @Override
      public Boolean apply(WebDriver webDriver) {
        return webDriver.getCurrentUrl().contains(url);
      }
    };

  }

  @Before
  public void createClient() throws Exception {
    preventRaceConditions();
    createClient(getWebappCtxPath());
    appUrl = testProperties.getApplicationPath("/" + getWebappCtxPath());
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
