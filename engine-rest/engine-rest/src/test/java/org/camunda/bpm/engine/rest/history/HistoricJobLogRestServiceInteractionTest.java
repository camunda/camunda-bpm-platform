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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricJobLogRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_JOB_LOG_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/job-log";
  protected static final String SINGLE_HISTORIC_JOB_LOG_RESOURCE_URL = HISTORIC_JOB_LOG_RESOURCE_URL + "/{id}";
  protected static final String HISTORIC_JOB_LOG_RESOURCE_GET_STACKTRACE_URL = SINGLE_HISTORIC_JOB_LOG_RESOURCE_URL + "/stacktrace";

  protected ProcessEngine namedProcessEngine;
  protected HistoryService mockHistoryService;

  protected HistoricJobLogQuery mockQuery;

  @Before
  public void setUpRuntimeData() {
    mockQuery = mock(HistoricJobLogQuery.class);

    HistoricJobLog mockedHistoricJobLog = MockProvider.createMockHistoricJobLog();

    when(mockQuery.singleResult()).thenReturn(mockedHistoricJobLog);
    when(mockQuery.logId(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ID)).thenReturn(mockQuery);

    mockHistoryService = mock(HistoryService.class);
    when(mockHistoryService.createHistoricJobLogQuery()).thenReturn(mockQuery);

    namedProcessEngine = getProcessEngine(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME);
    when(namedProcessEngine.getHistoryService()).thenReturn(mockHistoryService);
  }

  @Test
  public void testSimpleHistoricJobLogGet() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ID))
        .body("timestamp", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_TIMESTAMP))
        .body("removalTime", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_REMOVAL_TIME))
        .body("jobId", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_JOB_ID))
        .body("jobDueDate", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_JOB_DUE_DATE))
        .body("jobRetries", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_JOB_RETRIES))
        .body("jobPriority", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_JOB_PRIORITY))
        .body("jobExceptionMessage", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_JOB_EXCEPTION_MSG))
        .body("jobDefinitionId", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_JOB_DEF_ID))
        .body("jobDefinitionType", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_JOB_DEF_TYPE))
        .body("jobDefinitionConfiguration", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_JOB_DEF_CONFIG))
        .body("activityId", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ACTIVITY_ID))
        .body("failedActivityId", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_FAILED_ACTIVITY_ID))
        .body("executionId", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_EXECUTION_ID))
        .body("processInstanceId", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_PROC_INST_ID))
        .body("processDefinitionId", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_PROC_DEF_ID))
        .body("processDefinitionKey", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_PROC_DEF_KEY))
        .body("deploymentId", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_DEPLOYMENT_ID))
        .body("tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
        .body("hostname", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_HOSTNAME))
        .body("rootProcessInstanceId", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ROOT_PROC_INST_ID))
        .body("creationLog", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_IS_CREATION_LOG))
        .body("failureLog", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_IS_FAILURE_LOG))
        .body("successLog", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_IS_SUCCESS_LOG))
        .body("deletionLog", equalTo(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_IS_DELETION_LOG))
    .when()
      .get(SINGLE_HISTORIC_JOB_LOG_RESOURCE_URL);

    InOrder inOrder = inOrder(mockQuery);
    inOrder.verify(mockQuery).logId(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ID);
    inOrder.verify(mockQuery).singleResult();
  }

  @Test
  public void testHistoricJobLogGetIdDoesntExist() {
    String id = "nonExistingId";

    HistoricJobLogQuery invalidQueryNonExistingHistoricJobLog = mock(HistoricJobLogQuery.class);
    when(mockHistoryService.createHistoricJobLogQuery().logId(id)).thenReturn(invalidQueryNonExistingHistoricJobLog);
    when(invalidQueryNonExistingHistoricJobLog.singleResult()).thenReturn(null);

    given()
      .pathParam("id", id)
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Historic job log with id " + id + " does not exist"))
    .when()
      .get(SINGLE_HISTORIC_JOB_LOG_RESOURCE_URL);
  }

  @Test
  public void testGetStacktrace() {
    String stacktrace = "aStacktrace";
    when(mockHistoryService.getHistoricJobLogExceptionStacktrace(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ID)).thenReturn(stacktrace);

    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.TEXT)
    .when()
      .get(HISTORIC_JOB_LOG_RESOURCE_GET_STACKTRACE_URL);

    String content = response.asString();
    Assert.assertEquals(stacktrace, content);
  }

  @Test
  public void testGetStacktraceJobNotFound() {
    String exceptionMessage = "historic job log not found";
    doThrow(new ProcessEngineException(exceptionMessage)).when(mockHistoryService).getHistoricJobLogExceptionStacktrace(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ID);

    given()
      .pathParam("id", MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ID)
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo(exceptionMessage))
    .when()
      .get(HISTORIC_JOB_LOG_RESOURCE_GET_STACKTRACE_URL);
  }

  @Test
  public void testGetStacktraceThrowsAuthorizationException() {
    String exceptionMessage = "expected exception";
    doThrow(new AuthorizationException(exceptionMessage)).when(mockHistoryService).getHistoricJobLogExceptionStacktrace(MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ID);

    given()
      .pathParam("id", MockProvider.EXAMPLE_HISTORIC_JOB_LOG_ID)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(exceptionMessage))
    .when()
      .get(HISTORIC_JOB_LOG_RESOURCE_GET_STACKTRACE_URL);
  }
}
