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
package org.camunda.bpm.run.qa;

import io.restassured.response.Response;
import org.camunda.bpm.run.qa.util.SpringBootManagedContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.AfterParam;
import org.junit.runners.Parameterized.BeforeParam;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

/**
 * Test cases for ensuring connectivity to REST API based on startup parameters
 */
@RunWith(Parameterized.class)
public class ComponentAvailabilityIT {

  @Parameter(0)
  public String[] commands;
  @Parameter(1)
  public boolean restAvailable;
  @Parameter(2)
  public boolean webappsAvailable;
  @Parameter(3)
  public boolean exampleAvailable;

  @Parameters(name = "Test instance: {index}. Rest: {1}, Webapps: {2}, Example: {3}")
  public static Collection<Object[]> commands() {
    return Arrays.asList(new Object[][] {
      { new String[0], true, true, true },
      { new String[]{"--rest"}, true, false, false },
      { new String[]{"--rest", "--webapps"}, true, true, false },
      { new String[]{"--rest", "--example"}, true, false, true },
      { new String[]{"--webapps"}, false, true, false },
      { new String[]{"--rest", "--webapps"}, true, true, false },
      { new String[]{"--rest", "--webapps", "--example"}, true, true, true },
      { new String[]{"--rest", "--webapps", "--example", "--oauth2"}, true, true, true }
    });
  }

  private static SpringBootManagedContainer container;

  @BeforeParam
  public static void runStartScript(String[] commands, boolean restAvailable, boolean webappsAvailable, boolean exampleAvailable) {
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

  @Test
  public void shouldFindEngineViaRestApiRequest() {
    Response response = when().get(container.getBaseUrl() + "/engine-rest/engine");
    if (restAvailable) {
      response.then()
        .body("size()", is(1))
        .body("name[0]", is("default"));
    } else {
      response.then()
        .statusCode(404);
    }
  }

  @Test
  public void shouldFindWelcomeApp() {
    Response response = when().get(container.getBaseUrl() + "/camunda/app/welcome/default");
    if (webappsAvailable) {
      response.then()
        .statusCode(200)
        .body("html.head.title", equalTo("Camunda Welcome"));
    } else {
      response.then()
        .statusCode(404);
    }
  }

  @Test
  public void shouldFindExample() {
    Response response = when().get(container.getBaseUrl() + "/engine-rest/process-definition");
    if (exampleAvailable && restAvailable) {
      response.then()
        .body("size()", is(3))
        .body("key[0]", is("ReviewInvoice"));
    } else if (restAvailable) {
      response.then()
        .body("size()", is(0));
    } else {
      response.then()
        .statusCode(404);
    }
  }
}
