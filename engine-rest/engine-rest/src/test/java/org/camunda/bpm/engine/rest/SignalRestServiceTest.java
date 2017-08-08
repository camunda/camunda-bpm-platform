/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.SignalEventReceivedBuilderImpl;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Tassilo Weidner
 */
public class SignalRestServiceTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String SIGNAL_URL = TEST_RESOURCE_ROOT_PATH +  SignalRestService.PATH;

  private RuntimeService runtimeServiceMock;
  private SignalEventReceivedBuilder signalBuilderMock;

  @Before
  public void setupMocks() {
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);

    signalBuilderMock = mock(SignalEventReceivedBuilderImpl.class);
    when(runtimeServiceMock.createSignalEvent(anyString())).thenReturn(signalBuilderMock);
    when(signalBuilderMock.setVariables(Matchers.<Map<String,Object>>any())).thenReturn(signalBuilderMock);
    when(signalBuilderMock.executionId(anyString())).thenReturn(signalBuilderMock);
    when(signalBuilderMock.tenantId(anyString())).thenReturn(signalBuilderMock);
    when(signalBuilderMock.withoutTenantId()).thenReturn(signalBuilderMock);
  }

  @Test
  public void shouldBroadcast() {
    Map<String, String> requestBody = new HashMap<String, String>();
    requestBody.put("name", "aSignalName");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent(requestBody.get("name"));
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldBroadcastWithVariables() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");
    requestBody.put("variables",
      VariablesBuilder.create()
      .variable("total", 420)
      .variable("invoiceId", "ABC123")
      .getVariables());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent((String) requestBody.get("name"));

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("total", 420);
    expectedVariables.put("invoiceId", "ABC123");
    verify(signalBuilderMock).setVariables(expectedVariables);
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldBroadcastWithTenant() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");
    requestBody.put("tenantId", "aTenantId");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent((String) requestBody.get("name"));
    verify(signalBuilderMock).tenantId((String) requestBody.get("tenantId"));
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldBroadcastWithVariablesAndTenant() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");
    requestBody.put("variables",
      VariablesBuilder.create()
      .variable("total", 420)
      .variable("invoiceId", "ABC123")
      .getVariables());
    requestBody.put("tenantId", "aTenantId");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent((String) requestBody.get("name"));

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("total", 420);
    expectedVariables.put("invoiceId", "ABC123");
    verify(signalBuilderMock).setVariables(expectedVariables);
    verify(signalBuilderMock).tenantId((String) requestBody.get("tenantId"));
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldBroadcastWithoutTenant() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");
    requestBody.put("withoutTenantId", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent((String) requestBody.get("name"));
    verify(signalBuilderMock).withoutTenantId();
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldBroadcastWithoutTenantAndWithVariables() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");
    requestBody.put("variables",
      VariablesBuilder.create()
      .variable("total", 420)
      .variable("invoiceId", "ABC123")
      .getVariables());
    requestBody.put("withoutTenantId", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent((String) requestBody.get("name"));
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("total", 420);
    expectedVariables.put("invoiceId", "ABC123");
    verify(signalBuilderMock).setVariables(expectedVariables);
    verify(signalBuilderMock).withoutTenantId();
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldDeliverToSingleExecution() {
    Map<String, String> requestBody = new HashMap<String, String>();
    requestBody.put("name", "aSignalName");
    requestBody.put("executionId", "anExecutionId");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent(requestBody.get("name"));
    verify(signalBuilderMock).executionId(requestBody.get("executionId"));
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldDeliverToSingleExecutionWithVariables() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");
    requestBody.put("executionId", "anExecutionId");
    requestBody.put("variables",
      VariablesBuilder.create()
      .variable("total", 420)
      .variable("invoiceId", "ABC123")
      .getVariables());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent((String) requestBody.get("name"));
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("total", 420);
    expectedVariables.put("invoiceId", "ABC123");
    verify(signalBuilderMock).setVariables(expectedVariables);
    verify(signalBuilderMock).executionId((String) requestBody.get("executionId"));
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldDeliverToSingleExecutionWithTenant() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");
    requestBody.put("tenantId", "aTenantId");
    requestBody.put("executionId", "anExecutionId");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent((String) requestBody.get("name"));
    verify(signalBuilderMock).tenantId((String) requestBody.get("tenantId"));
    verify(signalBuilderMock).executionId((String) requestBody.get("executionId"));
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldDeliverToSingleExecutionWithVariablesAndTenant() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");
    requestBody.put("executionId", "anExecutionId");
    requestBody.put("variables",
      VariablesBuilder.create()
      .variable("total", 420)
      .variable("invoiceId", "ABC123")
      .getVariables());
    requestBody.put("tenantId", "aTenantId");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent((String) requestBody.get("name"));
    verify(signalBuilderMock).executionId((String) requestBody.get("executionId"));
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("total", 420);
    expectedVariables.put("invoiceId", "ABC123");
    verify(signalBuilderMock).setVariables(expectedVariables);
    verify(signalBuilderMock).tenantId((String) requestBody.get("tenantId"));
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldDeliverToSingleExecutionWithoutTenant() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");
    requestBody.put("executionId", "anExecutionId");
    requestBody.put("withoutTenantId", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent((String) requestBody.get("name"));
    verify(signalBuilderMock).executionId((String) requestBody.get("executionId"));
    verify(signalBuilderMock).withoutTenantId();
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldDeliverToSingleExecutionWithoutTenantAndWithVariables() {
    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");
    requestBody.put("executionId", "anExecutionId");
    requestBody.put("withoutTenantId", true);
    requestBody.put("variables",
      VariablesBuilder.create()
      .variable("total", 420)
      .variable("invoiceId", "ABC123")
      .getVariables());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SIGNAL_URL);

    verify(runtimeServiceMock).createSignalEvent((String) requestBody.get("name"));
    verify(signalBuilderMock).executionId((String) requestBody.get("executionId"));
    verify(signalBuilderMock).withoutTenantId();
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("total", 420);
    expectedVariables.put("invoiceId", "ABC123");
    verify(signalBuilderMock).setVariables(expectedVariables);
    verify(signalBuilderMock).send();
    verifyNoMoreInteractions(signalBuilderMock);
  }

  @Test
  public void shouldThrowExceptionByMissingName() {
    Map<String, Object> requestBody = new HashMap<String, Object>();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("No signal name given"))
    .when()
      .post(SIGNAL_URL);
  }

  @Test
  public void shouldThrowBadUserRequestException() {
    String message = "expected exception";
    doThrow(new BadUserRequestException(message)).when(signalBuilderMock).send();

    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(BadUserRequestException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
      .post(SIGNAL_URL);
  }

  @Test
  public void shouldThrowAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(signalBuilderMock).send();

    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
      .post(SIGNAL_URL);
  }

  @Test
  public void shouldThrowProcessEngineException() {
    String message = "expected exception";
    doThrow(new ProcessEngineException(message)).when(signalBuilderMock).send();

    Map<String, Object> requestBody = new HashMap<String, Object>();
    requestBody.put("name", "aSignalName");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(requestBody)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
      .post(SIGNAL_URL);
  }

}
