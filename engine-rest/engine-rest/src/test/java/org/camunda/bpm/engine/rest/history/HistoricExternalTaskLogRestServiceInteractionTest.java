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
package org.camunda.bpm.engine.rest.history;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricExternalTaskLogQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

import javax.ws.rs.core.Response.Status;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class HistoricExternalTaskLogRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/external-task-log";
  protected static final String SINGLE_HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL = HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL + "/{id}";
  protected static final String HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_GET_ERROR_DETAILS_URL = SINGLE_HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL + "/error-details";
  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();
  protected ProcessEngine namedProcessEngine;
  protected HistoryService mockHistoryService;

  protected HistoricExternalTaskLogQuery mockQuery;

  @Before
  public void setUpRuntimeData() {
    mockQuery = mock(HistoricExternalTaskLogQuery.class);

    HistoricExternalTaskLog mockedHistoricExternalTaskLog = MockProvider.createMockHistoricExternalTaskLog();

    when(mockQuery.singleResult()).thenReturn(mockedHistoricExternalTaskLog);
    when(mockQuery.logId(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID)).thenReturn(mockQuery);

    mockHistoryService = mock(HistoryService.class);
    when(mockHistoryService.createHistoricExternalTaskLogQuery()).thenReturn(mockQuery);

    namedProcessEngine = getProcessEngine(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME);
    when(namedProcessEngine.getHistoryService()).thenReturn(mockHistoryService);
  }

  @Test
  public void testSimpleHistoricExternalTaskLogGet() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID))
        .body("timestamp", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_TIMESTAMP))
        .body("removalTime", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_REMOVAL_TIME))
        .body("externalTaskId", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_EXTERNAL_TASK_ID))
        .body("topicName", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_TOPIC_NAME))
        .body("workerId", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_WORKER_ID))
        .body("retries", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_RETRIES))
        .body("priority", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PRIORITY))
        .body("errorMessage", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ERROR_MSG))
        .body("activityId", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ACTIVITY_ID))
        .body("activityInstanceId", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ACTIVITY_INSTANCE_ID))
        .body("executionId", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_EXECUTION_ID))
        .body("processInstanceId", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_INST_ID))
        .body("processDefinitionId", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_ID))
        .body("processDefinitionKey", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_PROC_DEF_KEY))
        .body("tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
        .body("rootProcessInstanceId", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ROOT_PROC_INST_ID))
        .body("creationLog", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_CREATION_LOG))
        .body("failureLog", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_FAILURE_LOG))
        .body("successLog", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_SUCCESS_LOG))
        .body("deletionLog", equalTo(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_IS_DELETION_LOG))
    .when()
      .get(SINGLE_HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);

    InOrder inOrder = inOrder(mockQuery);
    inOrder.verify(mockQuery).logId(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID);
    inOrder.verify(mockQuery).singleResult();
  }

  @Test
  public void testHistoricExternalTaskLogGetIdDoesntExist() {
    String id = "nonExistingId";

    HistoricExternalTaskLogQuery invalidQueryNonExistingHistoricExternalTaskLog = mock(HistoricExternalTaskLogQuery.class);
    when(mockHistoryService.createHistoricExternalTaskLogQuery().logId(id)).thenReturn(invalidQueryNonExistingHistoricExternalTaskLog);
    when(invalidQueryNonExistingHistoricExternalTaskLog.singleResult()).thenReturn(null);

    given()
      .pathParam("id", id)
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Historic external task log with id " + id + " does not exist"))
    .when()
      .get(SINGLE_HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_URL);
  }

  @Test
  public void testGetErrorDetails() {
    String errorDetails = "someErrorDetails";
    when(mockHistoryService.getHistoricExternalTaskLogErrorDetails(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID)).thenReturn(errorDetails);

    Response response =
      given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.TEXT)
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_GET_ERROR_DETAILS_URL);

    String content = response.asString();
    assertEquals(errorDetails, content);
  }

  @Test
  public void testGetErrorDetailsExternalTaskNotFound() {
    String exceptionMessage = "historic external task log not found";
    doThrow(new ProcessEngineException(exceptionMessage)).when(mockHistoryService).getHistoricExternalTaskLogErrorDetails(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID);

    given()
      .pathParam("id", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID)
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo(exceptionMessage))
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_GET_ERROR_DETAILS_URL);
  }

  @Test
  public void testGetErrorDetailsThrowsAuthorizationException() {
    String exceptionMessage = "expected exception";
    doThrow(new AuthorizationException(exceptionMessage)).when(mockHistoryService).getHistoricExternalTaskLogErrorDetails(MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID);

    given()
      .pathParam("id", MockProvider.EXAMPLE_HISTORIC_EXTERNAL_TASK_LOG_ID)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(exceptionMessage))
    .when()
      .get(HISTORIC_EXTERNAL_TASK_LOG_RESOURCE_GET_ERROR_DETAILS_URL);
  }

}
