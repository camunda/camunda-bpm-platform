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
package org.camunda.bpm.engine.rest;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TelemetryRestServiceTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String TELEMETRY_URL = TEST_RESOURCE_ROOT_PATH +  TelemetryRestService.PATH;
  protected static final String TELEMETRY_CONFIG_URL = TELEMETRY_URL + "/configuration";

  protected ManagementService managementServiceMock;


  @Before
  public void setupMocks() {
    managementServiceMock = mock(ManagementService.class);
    when(processEngine.getManagementService()).thenReturn(managementServiceMock);
  }
  
  @Test
  public void shouldDisableTelemetry() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("enableTelemetry", false);
    
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
    .post(TELEMETRY_CONFIG_URL);
    
    verify(managementServiceMock).toggleTelemetry(false);
  }

  @Test
  public void shouldEnableTelemetry() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("enableTelemetry", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(TELEMETRY_CONFIG_URL);

    verify(managementServiceMock).toggleTelemetry(true);
  }

  @Test
  public void shouldThrowAuthorizationExceptionOnEnablingTelemetry() {
    String message = "Required admin authenticated group or user.";
    doThrow(new AuthorizationException(message)).when(managementServiceMock).toggleTelemetry(anyBoolean());

    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("enableTelemetry", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
      .post(TELEMETRY_CONFIG_URL);
  }

  @Test
  public void shouldFetchEnabledTelemetryConfiguration() {
    when(managementServiceMock.isTelemetryEnabled()).thenReturn(true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("enableTelemetry", equalTo(true))
    .when()
      .get(TELEMETRY_CONFIG_URL);

    verify(managementServiceMock).isTelemetryEnabled();
  }

  @Test
  public void shouldFetchDisabledTelemetryConfiguration() {
    when(managementServiceMock.isTelemetryEnabled()).thenReturn(false);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("enableTelemetry", equalTo(false))
    .when()
      .get(TELEMETRY_CONFIG_URL);

    verify(managementServiceMock).isTelemetryEnabled();
  }

  @Test
  public void shouldFetchEmptyTelemetryConfiguration() {
    when(managementServiceMock.isTelemetryEnabled()).thenReturn(null);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("enableTelemetry", equalTo(null))
    .when()
      .get(TELEMETRY_CONFIG_URL);

    verify(managementServiceMock).isTelemetryEnabled();
  }

  @Test
  public void shouldThrowAuthorizationExceptionOnFetchingTelemetryConfig() {
    String message = "Required admin authenticated group or user.";
    doThrow(new AuthorizationException(message)).when(managementServiceMock).isTelemetryEnabled();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
      .get(TELEMETRY_CONFIG_URL);

    verify(managementServiceMock).isTelemetryEnabled();
  }

}
