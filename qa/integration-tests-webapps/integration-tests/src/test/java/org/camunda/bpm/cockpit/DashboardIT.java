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
package org.camunda.bpm.cockpit;

import org.camunda.bpm.AbstractWebappUiIntegrationTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;

public class DashboardIT extends AbstractWebappUiIntegrationTest {

  @Test
  public void testLogin() throws URISyntaxException {
    driver.get(appUrl + "app/cockpit/#/login");

    WebDriverWait wait = new WebDriverWait(driver, 30);

    try {
      wait.until(ExpectedConditions.titleIs("Camunda Cockpit"));

    } catch (TimeoutException e) {
      driver.get(appUrl + "app/cockpit/#/login");

      wait.until(ExpectedConditions.titleIs("Camunda Cockpit"));

    }

    WebElement user = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"text\"]")));
    user.sendKeys("demo");

    WebElement password= wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type=\"password\"]")));
    password.sendKeys("demo");

    WebElement submit = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type=\"submit\"]")));
    submit.submit();

    wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".deployed .processes .value"), "1"));
    wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".deployed .processes .stats-label"), "Process Definitions"));

    wait.until(currentURIIs(new URI(appUrl + "app/cockpit/default/#/dashboard")));
  }

}
