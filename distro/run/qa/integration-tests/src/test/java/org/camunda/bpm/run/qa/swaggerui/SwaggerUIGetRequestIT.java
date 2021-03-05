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
package org.camunda.bpm.run.qa.swaggerui;

import org.camunda.bpm.run.qa.util.SpringBootManagedContainer;
import org.camunda.bpm.run.qa.webapps.AbstractWebappUiIT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
/**
 * NOTE:
 * This test is specific to RUN, as Swagger UI is exclusive to RUN.
 * Do not remove with https://jira.camunda.com/browse/CAM-11379
 */
public class SwaggerUIGetRequestIT extends AbstractWebappUiIT {
  private static SpringBootManagedContainer container;

  @Before
  public void runStartScript() {
    container = new SpringBootManagedContainer();
    try {
      container.start();
    } catch (Exception e) {
      throw new RuntimeException("Cannot start managed Spring Boot application!", e);
    }
  }

  @After
  public void stopApp() {
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

  @Test
  public void submitPostRequest() {
    driver.manage().deleteAllCookies();
    String path = testProperties.getApplicationPath("/swaggerui");
    driver.get(path);

    WebDriverWait wait = new WebDriverWait(driver, 10);
    // click on Batch resource
    wait.until(visibilityOfElementLocated(By.id("operations-tag-Batch"))).click();
    // click on get batches endpoint
    wait.until(visibilityOfElementLocated(By.cssSelector("#operations-Batch-getBatches > .opblock-summary"))).click();;
    // click on execute request
    wait.until(visibilityOfElementLocated(By.cssSelector(".execute"))).click();
    // there should be a response to the request
    wait.until(textToBePresentInElementLocated(By.tagName("h5"), "Response body"));
    // the response should have status 200
    wait.until(textToBePresentInElementLocated(
      By.cssSelector(".live-responses-table .response > .response-col_status"), "200"));

  }
}
