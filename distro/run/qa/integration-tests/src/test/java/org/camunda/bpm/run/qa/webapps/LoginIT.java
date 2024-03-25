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
package org.camunda.bpm.run.qa.webapps;

import org.camunda.bpm.run.qa.util.SpringBootManagedContainer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.AfterParam;
import org.junit.runners.Parameterized.BeforeParam;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

/**
 * NOTE:
 * copied from
 * <a href="https://github.com/camunda/camunda-bpm-platform/blob/master/qa/integration-tests-webapps/integration-tests/src/main/java/org/camunda/bpm/LoginIT.java">platform</a>
 * then added <code>@BeforeParam</code> and <code>@AfterParam</code> methods for container setup
 * and <code>@Parameters</code> for different setups, might be removed with https://jira.camunda.com/browse/CAM-11379
 */
@RunWith(Parameterized.class)
public class LoginIT extends AbstractWebappUiIT {

  @Parameter
  public String[] commands;

  @Parameters
  public static Collection<Object[]> commands() {
    return Arrays.asList(new Object[][] {
      { new String[0] },
      { new String[]{"--rest", "--webapps"} },
      { new String[]{"--webapps"} }
    });
  }

  @Rule
  public TestName name = new TestName();

  protected static SpringBootManagedContainer container;

  protected WebDriverWait wait;

  @BeforeParam
  public static void runStartScript(String[] commands) {
    container = new SpringBootManagedContainer(commands);
    try {
      container.start();
    } catch (Exception e) {
      throw new RuntimeException("Cannot start managed Spring Boot application!", e);
    }
  }

  @AfterParam
  public static void stopApp() {
    try {
      if (container != null) {
        container.stop();
      }
    } catch (Exception e) {
      throw new RuntimeException("Cannot stop managed Spring Boot application!", e);
    } finally {
      container = null;
    }
  }

  public void login(String appName) {
    driver.manage().deleteAllCookies();

    driver.get(appUrl + "app/" + appName + "/default/");

    wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    WebElement userNameInput = wait.until(visibilityOfElementLocated(By.cssSelector("input[type=\"text\"]")));
    sendKeys(userNameInput, "demo");

    WebElement passwordInput = wait.until(visibilityOfElementLocated(By.cssSelector("input[type=\"password\"]")));
    sendKeys(passwordInput, "demo");

    wait.until(visibilityOfElementLocated(By.cssSelector("button[type=\"submit\"]")))
        .submit();
  }

  public void sendKeys(WebElement element, String keys)  {

    // fix for CAM-13548
    Arrays.stream(keys.split("")).forEach(c -> element.sendKeys(c));
  }

  @Test
  public void shouldLoginToCockpit() throws URISyntaxException {
    try {
      loginToCockpit();
    } catch (WebDriverException e) {
      loginToCockpit();
    }
  }

  public void loginToCockpit() throws URISyntaxException {
    String appName = "cockpit";
    login(appName);
    wait.until(textToBePresentInElementLocated(
        By.cssSelector(".deployed .processes .stats-label"),
        "Process Definitions"));

    wait.until(currentURIIs(new URI(appUrl + "app/"
        + appName + "/default/#/dashboard")));
  }

  @Test
  public void shouldLoginToTasklist() {
    try {
      loginToTasklist();
    } catch (WebDriverException e) {
      loginToTasklist();
    }
  }

  public void loginToTasklist() {
    String appName = "tasklist";
    login(appName);
    wait.until(textToBePresentInElementLocated(
        By.cssSelector(".start-process-action view a"),
        "Start process"));

    wait.until(containsCurrentUrl(appUrl + "app/"
        + appName + "/default/#/?searchQuery="));
  }

  @Test
  public void shouldLoginToAdmin() throws URISyntaxException {
    try {
      loginToAdmin();
    } catch (WebDriverException e) {
      loginToAdmin();
    }
  }

  public void loginToAdmin() throws URISyntaxException {
    String appName = "admin";
    login(appName);
    wait.until(textToBePresentInElementLocated(
        By.cssSelector("[ng-class=\"activeClass('#/authorization')\"] a"),
        "Authorizations"));

    wait.until(currentURIIs(new URI(appUrl
        + "app/" + appName + "/default/#/")));
  }

  @Test
  public void shouldLoginToWelcome() throws URISyntaxException {
    try {
      loginToWelcome();
    } catch (WebDriverException e) {
      loginToWelcome();
    }
  }

  public void loginToWelcome() throws URISyntaxException {
    String appName = "welcome";
    login(appName);
    wait.until(textToBePresentInElementLocated(
        By.cssSelector(".webapps .section-title"),
        "Applications"));

    wait.until(currentURIIs(new URI(appUrl
        + "app/" + appName + "/default/#!/welcome")));
  }

}
