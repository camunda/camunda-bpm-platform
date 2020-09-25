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
import org.junit.After;
import org.junit.Before;
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
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;

/**
 * NOTE:
 * copied from
 * <a href="https://github.com/camunda/camunda-bpm-platform/blob/master/qa/integration-tests-webapps/integration-tests/src/test/java/org/camunda/bpm/LoginIT.java">platform</a>
 * then added <code>@BeforeClass</code> and <code>@AfterClass</code> methods for container setup
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
  protected String appName;


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

  @Before
  public void login() throws InterruptedException {
    appName = name.getMethodName()
        .replace("shouldLoginTo", "")
        .toLowerCase();
    appName = appName.substring(0, appName.length() - 3);// added so the parameter is also removed from the name

    driver.get(appUrl + "app/" + appName + "/default/");

    wait = new WebDriverWait(driver, 10);

    Thread.sleep(200);

    wait.until(presenceOfElementLocated(By.cssSelector("input[type=\"text\"]")))
        .sendKeys("demo");

    wait.until(presenceOfElementLocated(By.cssSelector("input[type=\"password\"]")))
        .sendKeys("demo");

    wait.until(presenceOfElementLocated(By.cssSelector("button[type=\"submit\"]")))
        .submit();

    wait.until(presenceOfElementLocated(By.cssSelector(".modal-close")))
        .click();
  }

  @After
  public void logout() throws InterruptedException {
    Thread.sleep(200);

    if (appName.equals("cockpit")) {
      wait.until(presenceOfElementLocated(By.cssSelector(".UserInformation .user")))
          .click();

      wait.until(presenceOfElementLocated(By.xpath("//button[text()='Log out']")))
          .click();

    } else {
      wait.until(presenceOfElementLocated(By.cssSelector(".account .dropdown-toggle")))
          .click();

      wait.until(presenceOfElementLocated(By.cssSelector(".logout")))
          .click();

    }
  }

  @Test
  public void shouldLoginToCockpit() throws URISyntaxException {
    wait.until(textToBePresentInElementLocated(
        By.cssSelector(".deployed .processes .stats-label"),
        "Process Definitions"));

    wait.until(currentURIIs(new URI(appUrl + "app/"
        + appName + "/default/#/dashboard")));
  }

  @Test
  public void shouldLoginToTasklist() {
    wait.until(textToBePresentInElementLocated(
        By.cssSelector(".start-process-action view a"),
        "Start process"));

    wait.until(containsCurrentUrl(appUrl + "app/"
        + appName + "/default/#/?searchQuery="));
  }

  @Test
  public void shouldLoginToAdmin() throws URISyntaxException {
    wait.until(textToBePresentInElementLocated(
        By.cssSelector("[ng-class=\"activeClass('#/authorization')\"] a"),
        "Authorizations"));

    wait.until(currentURIIs(new URI(appUrl
        + "app/" + appName + "/default/#/")));
  }

  @Test
  public void shouldLoginToWelcome() throws URISyntaxException {
    wait.until(textToBePresentInElementLocated(
        By.cssSelector(".webapps .section-title"),
        "Applications"));

    wait.until(currentURIIs(new URI(appUrl
        + "app/" + appName + "/default/#!/welcome")));
  }

}
