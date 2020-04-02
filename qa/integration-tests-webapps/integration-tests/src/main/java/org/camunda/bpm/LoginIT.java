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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;

public class LoginIT extends AbstractWebappUiIntegrationTest {

  @Rule
  public TestName name = new TestName();

  protected WebDriverWait wait;
  protected String appName;

  @Before
  public void login() throws InterruptedException {
    appName = name.getMethodName()
        .replace("shouldLoginTo", "")
        .toLowerCase();

    driver.get(appUrl + "app/" + appName + "/default/");

    wait = new WebDriverWait(driver, 10);

    Thread.sleep(200);

    wait.until(presenceOfElementLocated(By.cssSelector("input[type=\"text\"]")))
        .sendKeys("demo");

    wait.until(presenceOfElementLocated(By.cssSelector("input[type=\"password\"]")))
        .sendKeys("demo");

    wait.until(presenceOfElementLocated(By.cssSelector("button[type=\"submit\"]")))
        .submit();
  }

  @After
  public void logout() {
    wait.until(presenceOfElementLocated(By.cssSelector(".account .dropdown-toggle")))
        .click();

    wait.until(presenceOfElementLocated(By.cssSelector(".logout")))
        .click();
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
