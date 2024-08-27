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

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;
import java.io.IOException;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.run.qa.util.SpringBootManagedContainer;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;

import io.restassured.response.Response;

public class ProductionConfigurationIT {

  static SpringBootManagedContainer container;

  @AfterClass
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

  @BeforeClass
  public static void runStartScript() throws IOException {
    container = new SpringBootManagedContainer("--production");

    container.createConfigurationYml("configuration/production.yml",
        ProductionConfigurationIT.class.getClassLoader().getResourceAsStream("ProductionConfigurationIntegrationTest_production.yml"));

    try {
      container.start();
    } catch (Exception e) {
      throw new RuntimeException("Cannot start managed Spring Boot application!", e);
    }
  }

  @Test
  public void shouldStartWithProductionConfiguration() {
    // when
    Response engineResponse = when().get(container.getBaseUrl() + "/engine-rest/engine");

    // then
    engineResponse.then()
      .statusCode(Status.OK.getStatusCode())
      .body("size()", is(1))
      .body("[0].name", is("production"));
  }

  @Test
  public void shouldNotProvideExampleInProductionConfiguration() {
    // when
    Response response = when().get(container.getBaseUrl() + "/engine-rest/engine/production/process-definition");

    // then
    response.then()
      .body("size()", is(0));
  }
}
