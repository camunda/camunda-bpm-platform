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

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.EqualsVariableMap;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.variable.EqualsObjectValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsUntypedValue;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String EXTERNAL_TASK_URL = TEST_RESOURCE_ROOT_PATH + "/external-task";
  protected static final String FETCH_EXTERNAL_TASK_URL = EXTERNAL_TASK_URL + "/fetchAndLock";
  protected static final String SINGLE_EXTERNAL_TASK_URL = EXTERNAL_TASK_URL + "/{id}";
  protected static final String COMPLETE_EXTERNAL_TASK_URL = SINGLE_EXTERNAL_TASK_URL + "/complete";
  protected static final String HANDLE_EXTERNAL_TASK_FAILURE_URL = SINGLE_EXTERNAL_TASK_URL + "/failure";
  protected static final String HANDLE_EXTERNAL_TASK_BPMN_ERROR_URL = SINGLE_EXTERNAL_TASK_URL + "/bpmnError";
  protected static final String UNLOCK_EXTERNAL_TASK_URL = SINGLE_EXTERNAL_TASK_URL + "/unlock";
  protected static final String RETRIES_EXTERNAL_TASK_URL = SINGLE_EXTERNAL_TASK_URL + "/retries";
  protected static final String PRIORITY_EXTERNAL_TASK_URL = SINGLE_EXTERNAL_TASK_URL + "/priority";

  protected ExternalTaskService externalTaskService;

  protected LockedExternalTask lockedExternalTaskMock;
  protected ExternalTaskQueryTopicBuilder fetchTopicBuilder;

  protected ExternalTask externalTaskMock;
  protected ExternalTaskQuery externalTaskQueryMock;

  @Before
  public void setUpRuntimeData() {
    externalTaskService = mock(ExternalTaskService.class);
    when(processEngine.getExternalTaskService()).thenReturn(externalTaskService);

    // locked external task
    lockedExternalTaskMock = MockProvider.createMockLockedExternalTask();

    // fetching
    fetchTopicBuilder = mock(ExternalTaskQueryTopicBuilder.class);
    when(externalTaskService.fetchAndLock(anyInt(), any(String.class))).thenReturn(fetchTopicBuilder);
    when(externalTaskService.fetchAndLock(anyInt(), any(String.class), any(Boolean.class))).thenReturn(fetchTopicBuilder);
    when(fetchTopicBuilder.topic(any(String.class), anyLong())).thenReturn(fetchTopicBuilder);
    when(fetchTopicBuilder.variables(anyListOf(String.class))).thenReturn(fetchTopicBuilder);
    when(fetchTopicBuilder.variables(any(String[].class))).thenReturn(fetchTopicBuilder);
    when(fetchTopicBuilder.topic(any(String.class), anyLong())).thenReturn(fetchTopicBuilder);

    // querying
    externalTaskQueryMock = mock(ExternalTaskQuery.class);
    when(externalTaskQueryMock.externalTaskId(any(String.class))).thenReturn(externalTaskQueryMock);
    when(externalTaskService.createExternalTaskQuery()).thenReturn(externalTaskQueryMock);

    // external task
    externalTaskMock = MockProvider.createMockExternalTask();
  }

  @Test
  public void testFetchAndLock() {
    // given
    when(fetchTopicBuilder.execute()).thenReturn(Arrays.asList(lockedExternalTaskMock));

    // when
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("maxTasks", 5);
    parameters.put("workerId", "aWorkerId");
    parameters.put("usePriority", true);

    Map<String, Object> topicParameter = new HashMap<String, Object>();
    topicParameter.put("topicName", "aTopicName");
    topicParameter.put("lockDuration", 12354L);
    topicParameter.put("variables", Arrays.asList(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME));
    parameters.put("topics", Arrays.asList(topicParameter));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("[0].id", equalTo(MockProvider.EXTERNAL_TASK_ID))
      .body("[0].topicName", equalTo(MockProvider.EXTERNAL_TASK_TOPIC_NAME))
      .body("[0].workerId", equalTo(MockProvider.EXTERNAL_TASK_WORKER_ID))
      .body("[0].lockExpirationTime", equalTo(MockProvider.EXTERNAL_TASK_LOCK_EXPIRATION_TIME))
      .body("[0].processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("[0].executionId", equalTo(MockProvider.EXAMPLE_EXECUTION_ID))
      .body("[0].activityId", equalTo(MockProvider.EXAMPLE_ACTIVITY_ID))
      .body("[0].activityInstanceId", equalTo(MockProvider.EXAMPLE_ACTIVITY_INSTANCE_ID))
      .body("[0].processDefinitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("[0].processDefinitionKey", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY))
      .body("[0].tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
      .body("[0].retries", equalTo(MockProvider.EXTERNAL_TASK_RETRIES))
      .body("[0].errorMessage", equalTo(MockProvider.EXTERNAL_TASK_ERROR_MESSAGE))
      .body("[0].variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME,
          notNullValue())
      .body("[0].variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".value",
          equalTo(MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE.getValue()))
      .body("[0].variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".type",
          equalTo("String"))
      .when().post(FETCH_EXTERNAL_TASK_URL);

    InOrder inOrder = inOrder(fetchTopicBuilder, externalTaskService);
    inOrder.verify(externalTaskService).fetchAndLock(5, "aWorkerId", true);
    inOrder.verify(fetchTopicBuilder).topic("aTopicName", 12354L);
    inOrder.verify(fetchTopicBuilder).variables(Arrays.asList(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME));
    inOrder.verify(fetchTopicBuilder).execute();
    verifyNoMoreInteractions(fetchTopicBuilder, externalTaskService);
  }

  @Test
  public void testFetchWithoutVariables() {
    // given
    when(fetchTopicBuilder.execute()).thenReturn(Arrays.asList(lockedExternalTaskMock));

    // when
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("maxTasks", 5);
    parameters.put("workerId", "aWorkerId");

    Map<String, Object> topicParameter = new HashMap<String, Object>();
    topicParameter.put("topicName", "aTopicName");
    topicParameter.put("lockDuration", 12354L);
    parameters.put("topics", Arrays.asList(topicParameter));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("[0].id", equalTo(MockProvider.EXTERNAL_TASK_ID))
    .when()
      .post(FETCH_EXTERNAL_TASK_URL);

    InOrder inOrder = inOrder(fetchTopicBuilder, externalTaskService);
    inOrder.verify(externalTaskService).fetchAndLock(5, "aWorkerId", false);
    inOrder.verify(fetchTopicBuilder).topic("aTopicName", 12354L);
    inOrder.verify(fetchTopicBuilder).execute();
    verifyNoMoreInteractions(fetchTopicBuilder, externalTaskService);
  }

  @Test
  public void testComplete() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("workerId", "aWorkerId");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(COMPLETE_EXTERNAL_TASK_URL);

    verify(externalTaskService).complete("anExternalTaskId", "aWorkerId", null);
    verifyNoMoreInteractions(externalTaskService);
  }

  @Test
  public void testCompleteWithVariables() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("workerId", "aWorkerId");

    Map<String, Object> variables = VariablesBuilder
        .create()
        .variable("var1", "val1")
        .variable("var2", "val2", "String")
        .variable("var3", ValueType.OBJECT.getName(), "val3", "aFormat", "aRootType")
        .getVariables();
    parameters.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(COMPLETE_EXTERNAL_TASK_URL);

    verify(externalTaskService).complete(
        eq("anExternalTaskId"),
        eq("aWorkerId"),
        argThat(EqualsVariableMap.matches()
          .matcher("var1", EqualsUntypedValue.matcher().value("val1"))
          .matcher("var2", EqualsPrimitiveValue.stringValue("val2"))
          .matcher("var3",
              EqualsObjectValue.objectValueMatcher()
                .type(ValueType.OBJECT)
                .serializedValue("val3")
                .serializationFormat("aFormat")
                .objectTypeName("aRootType"))));

    verifyNoMoreInteractions(externalTaskService);
  }

  @Test
  public void testCompleteNonExistingTask() {
    doThrow(new NotFoundException())
      .when(externalTaskService)
      .complete(any(String.class), any(String.class), anyMapOf(String.class, Object.class));

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("workerId", "aWorkerId");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("External task with id anExternalTaskId does not exist"))
    .when()
      .post(COMPLETE_EXTERNAL_TASK_URL);
  }

  @Test
  public void testCompleteThrowsAuthorizationException() {
    doThrow(new AuthorizationException("aMessage"))
      .when(externalTaskService)
      .complete(any(String.class), any(String.class), anyMapOf(String.class, Object.class));

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("workerId", "aWorkerId");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo("aMessage"))
    .when()
      .post(COMPLETE_EXTERNAL_TASK_URL);
  }

  @Test
  public void testCompleteThrowsBadUserRequestException() {
    doThrow(new BadUserRequestException("aMessage"))
      .when(externalTaskService)
      .complete(any(String.class), any(String.class), anyMapOf(String.class, Object.class));

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("workerId", "aWorkerId");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("aMessage"))
    .when()
      .post(COMPLETE_EXTERNAL_TASK_URL);
  }

  @Test
  public void testUnlock() {
    given()
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(UNLOCK_EXTERNAL_TASK_URL);

    verify(externalTaskService).unlock("anExternalTaskId");
    verifyNoMoreInteractions(externalTaskService);
  }

  @Test
  public void testUnlockNonExistingTask() {
    doThrow(new NotFoundException()).when(externalTaskService).unlock(any(String.class));

    given()
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("External task with id anExternalTaskId does not exist"))
    .when()
      .post(UNLOCK_EXTERNAL_TASK_URL);

    verify(externalTaskService).unlock("anExternalTaskId");
    verifyNoMoreInteractions(externalTaskService);
  }

  @Test
  public void testUnlockThrowsAuthorizationException() {
    doThrow(new AuthorizationException("aMessage")).when(externalTaskService).unlock(any(String.class));

    given()
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo("aMessage"))
    .when()
      .post(UNLOCK_EXTERNAL_TASK_URL);

    verify(externalTaskService).unlock("anExternalTaskId");
    verifyNoMoreInteractions(externalTaskService);
  }

  @Test
  public void testHandleFailure() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("workerId", "aWorkerId");
    parameters.put("errorMessage", "anErrorMessage");
    parameters.put("retries", 5);
    parameters.put("retryTimeout", 12345);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(HANDLE_EXTERNAL_TASK_FAILURE_URL);

    verify(externalTaskService).handleFailure("anExternalTaskId", "aWorkerId", "anErrorMessage", 5, 12345);
    verifyNoMoreInteractions(externalTaskService);
  }

  @Test
  public void testHandleFailureNonExistingTask() {
    doThrow(new NotFoundException())
      .when(externalTaskService)
      .handleFailure(any(String.class), any(String.class), any(String.class), anyInt(), anyLong());

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("workerId", "aWorkerId");
    parameters.put("errorMessage", "anErrorMessage");
    parameters.put("retries", 5);
    parameters.put("retryTimeout", 12345);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("External task with id anExternalTaskId does not exist"))
    .when()
      .post(HANDLE_EXTERNAL_TASK_FAILURE_URL);
  }

  @Test
  public void testHandleFailureThrowsAuthorizationException() {
    doThrow(new AuthorizationException("aMessage"))
      .when(externalTaskService)
      .handleFailure(any(String.class), any(String.class), any(String.class), anyInt(), anyLong());

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("workerId", "aWorkerId");
    parameters.put("errorMessage", "anErrorMessage");
    parameters.put("retries", 5);
    parameters.put("retryTimeout", 12345);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo("aMessage"))
    .when()
      .post(HANDLE_EXTERNAL_TASK_FAILURE_URL);
  }

  @Test
  public void testHandleFailureThrowsBadUserRequestException() {
    doThrow(new BadUserRequestException("aMessage"))
      .when(externalTaskService)
      .handleFailure(any(String.class), any(String.class), any(String.class), anyInt(), anyLong());

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("workerId", "aWorkerId");
    parameters.put("errorMessage", "anErrorMessage");
    parameters.put("retries", 5);
    parameters.put("retryTimeout", 12345);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("aMessage"))
    .when()
      .post(HANDLE_EXTERNAL_TASK_FAILURE_URL);
  }

  
  
  @Test
  public void testHandleBpmnError() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("workerId", "aWorkerId");
    parameters.put("errorCode", "anErrorCode");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(HANDLE_EXTERNAL_TASK_BPMN_ERROR_URL);

    verify(externalTaskService).handleBpmnError("anExternalTaskId", "aWorkerId", "anErrorCode");
    verifyNoMoreInteractions(externalTaskService);
  }

  @Test
  public void testHandleBpmnErrorNonExistingTask() {
    doThrow(new NotFoundException())
      .when(externalTaskService)
      .handleBpmnError(any(String.class), any(String.class), any(String.class));

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("workerId", "aWorkerId");
    parameters.put("errorCode", "errorCode");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("External task with id anExternalTaskId does not exist"))
    .when()
      .post(HANDLE_EXTERNAL_TASK_BPMN_ERROR_URL);
  }

  @Test
  public void testHandleBpmnErrorThrowsAuthorizationException() {
    doThrow(new AuthorizationException("aMessage"))
      .when(externalTaskService)
      .handleBpmnError(any(String.class), any(String.class), any(String.class));

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("workerId", "aWorkerId");
    parameters.put("errorCode", "errorCode");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo("aMessage"))
    .when()
      .post(HANDLE_EXTERNAL_TASK_BPMN_ERROR_URL);
  }

  @Test
  public void testHandleBpmnErrorThrowsBadUserRequestException() {
    doThrow(new BadUserRequestException("aMessage"))
      .when(externalTaskService)
      .handleBpmnError(any(String.class), any(String.class), any(String.class));

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("workerId", "aWorkerId");
    parameters.put("errorCode", "errorCode");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("aMessage"))
    .when()
      .post(HANDLE_EXTERNAL_TASK_BPMN_ERROR_URL);
  }
  
  
  @Test
  public void testSetRetries() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("retries", "5");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(RETRIES_EXTERNAL_TASK_URL);

    verify(externalTaskService).setRetries("anExternalTaskId", 5);
    verifyNoMoreInteractions(externalTaskService);
  }

  @Test
  public void testSetRetriesNonExistingTask() {
    doThrow(new NotFoundException()).when(externalTaskService).setRetries(any(String.class), anyInt());

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("retries", "5");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("External task with id anExternalTaskId does not exist"))
    .when()
      .put(RETRIES_EXTERNAL_TASK_URL);
  }

  @Test
  public void testSetRetriesThrowsAuthorizationException() {
    doThrow(new AuthorizationException("aMessage")).when(externalTaskService).setRetries(any(String.class), anyInt());

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("retries", "5");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo("aMessage"))
    .when()
      .put(RETRIES_EXTERNAL_TASK_URL);
  }
  
  
  
  @Test
  public void testSetPriority() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("priority", "5");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(PRIORITY_EXTERNAL_TASK_URL);

    verify(externalTaskService).setPriority("anExternalTaskId", 5);
    verifyNoMoreInteractions(externalTaskService);
  }

  @Test
  public void testSetPriorityNonExistingTask() {
    doThrow(new NotFoundException()).when(externalTaskService).setPriority(any(String.class), anyInt());

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("priority", "5");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("External task with id anExternalTaskId does not exist"))
    .when()
      .put(PRIORITY_EXTERNAL_TASK_URL);
  }

  @Test
  public void testSetPriorityThrowsAuthorizationException() {
    doThrow(new AuthorizationException("aMessage")).when(externalTaskService).setPriority(any(String.class), anyInt());

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("priority", "5");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo("aMessage"))
    .when()
      .put(PRIORITY_EXTERNAL_TASK_URL);
  }

  @Test
  public void testGetSingleExternalTask() {
    when(externalTaskQueryMock.singleResult()).thenReturn(externalTaskMock);

    given()
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("activityId", equalTo(MockProvider.EXAMPLE_ACTIVITY_ID))
      .body("activityInstanceId", equalTo(MockProvider.EXAMPLE_ACTIVITY_INSTANCE_ID))
      .body("errorMessage", equalTo(MockProvider.EXTERNAL_TASK_ERROR_MESSAGE))
      .body("executionId", equalTo(MockProvider.EXAMPLE_EXECUTION_ID))
      .body("id", equalTo(MockProvider.EXTERNAL_TASK_ID))
      .body("lockExpirationTime", equalTo(MockProvider.EXTERNAL_TASK_LOCK_EXPIRATION_TIME))
      .body("processDefinitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("processDefinitionKey", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY))
      .body("processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("retries", equalTo(MockProvider.EXTERNAL_TASK_RETRIES))
      .body("suspended", equalTo(MockProvider.EXTERNAL_TASK_SUSPENDED))
      .body("topicName", equalTo(MockProvider.EXTERNAL_TASK_TOPIC_NAME))
      .body("workerId", equalTo(MockProvider.EXTERNAL_TASK_WORKER_ID))
      .body("tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
      .body("priority", equalTo(MockProvider.EXTERNAL_TASK_PRIORITY))
    .when()
      .get(SINGLE_EXTERNAL_TASK_URL);
  }

  @Test
  public void testGetNonExistingExternalTask() {
    when(externalTaskQueryMock.singleResult()).thenReturn(null);

    given()
      .pathParam("id", "anExternalTaskId")
    .then()
      .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("External task with id anExternalTaskId does not exist"))
    .when()
      .get(SINGLE_EXTERNAL_TASK_URL);
  }
}
