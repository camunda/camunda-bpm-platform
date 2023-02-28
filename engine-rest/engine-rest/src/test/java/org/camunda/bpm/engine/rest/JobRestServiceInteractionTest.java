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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.management.SetJobRetriesBuilder;
import org.camunda.bpm.engine.management.SetJobRetriesByJobsAsyncBuilder;
import org.camunda.bpm.engine.management.UpdateJobSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.management.UpdateJobSuspensionStateTenantBuilder;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobSuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.MockJobBuilder;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.JsonPathUtil;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

public class JobRestServiceInteractionTest extends AbstractRestServiceTest {

  private static final String RETRIES = "retries";
  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String JOB_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/job";
  protected static final String SINGLE_JOB_RESOURCE_URL = JOB_RESOURCE_URL + "/{id}";
  protected static final String JOB_RESOURCE_SET_RETRIES_URL = SINGLE_JOB_RESOURCE_URL + "/retries";
  protected static final String JOBS_SET_RETRIES_URL = JOB_RESOURCE_URL + "/retries";
  protected static final String JOB_RESOURCE_SET_PRIORITY_URL = SINGLE_JOB_RESOURCE_URL + "/priority";
  protected static final String JOB_RESOURCE_EXECUTE_JOB_URL = SINGLE_JOB_RESOURCE_URL + "/execute";
  protected static final String JOB_RESOURCE_GET_STACKTRACE_URL = SINGLE_JOB_RESOURCE_URL + "/stacktrace";
  protected static final String JOB_RESOURCE_SET_DUEDATE_URL = SINGLE_JOB_RESOURCE_URL + "/duedate";
  protected static final String JOB_RESOURCE_RECALC_DUEDATE_URL = JOB_RESOURCE_SET_DUEDATE_URL + "/recalculate";
  protected static final String SINGLE_JOB_SUSPENDED_URL = SINGLE_JOB_RESOURCE_URL + "/suspended";
  protected static final String JOB_SUSPENDED_URL = JOB_RESOURCE_URL + "/suspended";

  private ProcessEngine namedProcessEngine;
  private ManagementService mockManagementService;

  private UpdateJobSuspensionStateTenantBuilder mockSuspensionStateBuilder;
  private UpdateJobSuspensionStateSelectBuilder mockSuspensionStateSelectBuilder;

  private JobQuery mockQuery;
  private SetJobRetriesByJobsAsyncBuilder mockSetJobRetriesByJobsAsyncBuilder;
  private SetJobRetriesBuilder mockSetJobRetriesBuilder;

  @Before
  public void setUpRuntimeData() {

    mockQuery = mock(JobQuery.class);
    Job mockedJob = new MockJobBuilder()
      .id(MockProvider.EXAMPLE_JOB_ID)
      .processInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .executionId(MockProvider.EXAMPLE_EXECUTION_ID)
      .retries(MockProvider.EXAMPLE_JOB_RETRIES)
      .exceptionMessage(MockProvider.EXAMPLE_JOB_NO_EXCEPTION_MESSAGE)
      .failedActivityId(MockProvider.EXAMPLE_JOB_FAILED_ACTIVITY_ID)
      .dueDate(new Date())
      .priority(MockProvider.EXAMPLE_JOB_PRIORITY)
      .jobDefinitionId(MockProvider.EXAMPLE_JOB_DEFINITION_ID)
      .tenantId(MockProvider.EXAMPLE_TENANT_ID)
      .createTime(DateTimeUtil.parseDate(MockProvider.EXAMPLE_JOB_CREATE_TIME))
      .build();

    when(mockQuery.singleResult()).thenReturn(mockedJob);
    when(mockQuery.jobId(MockProvider.EXAMPLE_JOB_ID)).thenReturn(mockQuery);

    mockManagementService = mock(ManagementService.class);
    when(mockManagementService.createJobQuery()).thenReturn(mockQuery);

    mockSetJobRetriesByJobsAsyncBuilder = MockProvider.createMockSetJobRetriesByJobsAsyncBuilder(mockManagementService);

    mockSetJobRetriesBuilder = MockProvider.createMockSetJobRetriesBuilder(mockManagementService);

    mockSuspensionStateSelectBuilder = mock(UpdateJobSuspensionStateSelectBuilder.class);
    when(mockManagementService.updateJobSuspensionState()).thenReturn(mockSuspensionStateSelectBuilder);

    mockSuspensionStateBuilder = mock(UpdateJobSuspensionStateTenantBuilder.class);
    when(mockSuspensionStateSelectBuilder.byJobId(anyString())).thenReturn(mockSuspensionStateBuilder);
    when(mockSuspensionStateSelectBuilder.byJobDefinitionId(anyString())).thenReturn(mockSuspensionStateBuilder);
    when(mockSuspensionStateSelectBuilder.byProcessInstanceId(anyString())).thenReturn(mockSuspensionStateBuilder);
    when(mockSuspensionStateSelectBuilder.byProcessDefinitionId(anyString())).thenReturn(mockSuspensionStateBuilder);
    when(mockSuspensionStateSelectBuilder.byProcessDefinitionKey(anyString())).thenReturn(mockSuspensionStateBuilder);

    namedProcessEngine = getProcessEngine(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME);
    when(namedProcessEngine.getManagementService()).thenReturn(mockManagementService);
  }

  @Test
  public void testSetRetries() {
    Map<String, Object> retriesVariableJson = new HashMap<>();
    retriesVariableJson.put("retries", MockProvider.EXAMPLE_JOB_RETRIES);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(retriesVariableJson)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(JOB_RESOURCE_SET_RETRIES_URL);

    verify(mockManagementService).setJobRetries(MockProvider.EXAMPLE_JOB_RETRIES);
    verify(mockSetJobRetriesBuilder).jobId(MockProvider.EXAMPLE_JOB_ID);
    verify(mockSetJobRetriesBuilder).execute();
    verifyNoMoreInteractions(mockSetJobRetriesBuilder);
  }

  @Test
  public void testSetRetriesWithDueDate() {
    Map<String, Object> retriesVariableJson = new HashMap<>();
    retriesVariableJson.put("retries", MockProvider.EXAMPLE_JOB_RETRIES);
    Date newDueDate = new Date(1675752840000L);
    retriesVariableJson.put("dueDate", newDueDate);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(retriesVariableJson).then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(JOB_RESOURCE_SET_RETRIES_URL);

    verify(mockManagementService).setJobRetries(MockProvider.EXAMPLE_JOB_RETRIES);
    verify(mockSetJobRetriesBuilder).jobId(MockProvider.EXAMPLE_JOB_ID);
    verify(mockSetJobRetriesBuilder).dueDate(newDueDate);
    verify(mockSetJobRetriesBuilder).execute();
    verifyNoMoreInteractions(mockSetJobRetriesBuilder);
  }

  @Test
  public void testSetRetriesWithNullDueDate() {
    Map<String, Object> retriesVariableJson = new HashMap<>();
    retriesVariableJson.put("retries", MockProvider.EXAMPLE_JOB_RETRIES);
    retriesVariableJson.put("dueDate", null);

    given()
    .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
    .contentType(ContentType.JSON)
    .body(retriesVariableJson).then().expect()
    .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
    .put(JOB_RESOURCE_SET_RETRIES_URL);

    verify(mockManagementService).setJobRetries(MockProvider.EXAMPLE_JOB_RETRIES);
    verify(mockSetJobRetriesBuilder).jobId(MockProvider.EXAMPLE_JOB_ID);
    verify(mockSetJobRetriesBuilder).dueDate(null);
    verify(mockSetJobRetriesBuilder).execute();
    verifyNoMoreInteractions(mockSetJobRetriesBuilder);
  }

  @Test
  public void testSetRetriesNonExistentJob() {
    String expectedMessage = "No job found with id '" + MockProvider.NON_EXISTING_JOB_ID + "'.";

    doThrow(new ProcessEngineException(expectedMessage)).when(mockSetJobRetriesBuilder).execute();

    Map<String, Object> retriesVariableJson = new HashMap<>();
    retriesVariableJson.put("retries", MockProvider.EXAMPLE_JOB_RETRIES);

    given()
      .pathParam("id", MockProvider.NON_EXISTING_JOB_ID)
      .contentType(ContentType.JSON)
      .body(retriesVariableJson)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo(expectedMessage))
    .when()
      .put(JOB_RESOURCE_SET_RETRIES_URL);

    verify(mockManagementService).setJobRetries(MockProvider.EXAMPLE_JOB_RETRIES);
    verify(mockSetJobRetriesBuilder).jobId(MockProvider.NON_EXISTING_JOB_ID);
    verify(mockSetJobRetriesBuilder).execute();
    verifyNoMoreInteractions(mockSetJobRetriesBuilder);
  }

  @Test
  public void testSetRetriesNegativeRetries() {

    String expectedMessage = "The number of job retries must be a non-negative Integer, but '" + MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES
        + "' has been provided.";

    doThrow(new ProcessEngineException(expectedMessage)).when(mockManagementService).setJobRetries(MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);

    Map<String, Object> retriesVariableJson = new HashMap<>();
    retriesVariableJson.put("retries", MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(retriesVariableJson)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo(expectedMessage))
    .when()
      .put(JOB_RESOURCE_SET_RETRIES_URL);

    verify(mockManagementService).setJobRetries(MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);
    verifyNoMoreInteractions(mockSetJobRetriesBuilder);
  }

  @Test
  public void testSetRetriesThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(mockManagementService).setJobRetries(anyInt());

    Map<String, Object> retriesVariableJson = new HashMap<>();
    retriesVariableJson.put("retries", MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON).body(retriesVariableJson)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .put(JOB_RESOURCE_SET_RETRIES_URL);

    verify(mockManagementService).setJobRetries(MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);
    verifyNoMoreInteractions(mockSetJobRetriesBuilder);
  }

  @Test
  public void testSimpleJobGet() {
    given().pathParam("id", MockProvider.EXAMPLE_JOB_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_JOB_ID))
      .body("processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("executionId", equalTo(MockProvider.EXAMPLE_EXECUTION_ID))
      .body("exceptionMessage", equalTo(MockProvider.EXAMPLE_JOB_NO_EXCEPTION_MESSAGE))
      .body("failedActivityId", equalTo(MockProvider.EXAMPLE_JOB_FAILED_ACTIVITY_ID))
      .body("priority", equalTo(MockProvider.EXAMPLE_JOB_PRIORITY))
      .body("jobDefinitionId", equalTo(MockProvider.EXAMPLE_JOB_DEFINITION_ID))
      .body("tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
      .body("createTime", equalTo(MockProvider.EXAMPLE_JOB_CREATE_TIME))
    .when()
      .get(SINGLE_JOB_RESOURCE_URL);

    InOrder inOrder = inOrder(mockQuery);
    inOrder.verify(mockQuery).jobId(MockProvider.EXAMPLE_JOB_ID);
    inOrder.verify(mockQuery).singleResult();
  }

  @Test
  public void testJobGetIdDoesntExist() {
    JobQuery invalidQueryNonExistingJob;
    invalidQueryNonExistingJob = mock(JobQuery.class);
    when(mockManagementService.createJobQuery().jobId(MockProvider.NON_EXISTING_JOB_ID)).thenReturn(invalidQueryNonExistingJob);
    when(invalidQueryNonExistingJob.singleResult()).thenReturn(null);

    String jobId = MockProvider.NON_EXISTING_JOB_ID;

    given()
      .pathParam("id", jobId)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Job with id " + jobId + " does not exist"))
    .when()
      .get(SINGLE_JOB_RESOURCE_URL);
  }

  @Test
  public void testExecuteJob() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(JOB_RESOURCE_EXECUTE_JOB_URL);

    verify(mockManagementService).executeJob(MockProvider.EXAMPLE_JOB_ID);
  }

  @Test
  public void testExecuteJobIdDoesntExist() {
    String jobId = MockProvider.NON_EXISTING_JOB_ID;

    String expectedMessage = "No job found with id '" + jobId + "'";

    doThrow(new ProcessEngineException(expectedMessage)).when(mockManagementService).executeJob(MockProvider.NON_EXISTING_JOB_ID);

    given().pathParam("id", jobId)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName())).body("message", equalTo(expectedMessage))
    .when().post(JOB_RESOURCE_EXECUTE_JOB_URL);
  }

  @Test
  public void testExecuteJobRuntimeException() {
    String jobId = MockProvider.EXAMPLE_JOB_ID;

    doThrow(new RuntimeException("Runtime exception")).when(mockManagementService).executeJob(jobId);

    given().pathParam("id", jobId)
    .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(RestException.class.getSimpleName())).body("message", equalTo("Runtime exception"))
    .when().post(JOB_RESOURCE_EXECUTE_JOB_URL);
  }

  @Test
  public void testExecuteJobThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(mockManagementService).executeJob(anyString());

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(JOB_RESOURCE_EXECUTE_JOB_URL);
  }

  @Test
  public void testGetStacktrace() {
    String stacktrace = "aStacktrace";
    when(mockManagementService.getJobExceptionStacktrace(MockProvider.EXAMPLE_JOB_ID)).thenReturn(stacktrace);

    Response response = given().pathParam("id", MockProvider.EXAMPLE_JOB_ID)
    .then().expect().statusCode(Status.OK.getStatusCode()).contentType(ContentType.TEXT)
    .when().get(JOB_RESOURCE_GET_STACKTRACE_URL);

    String content = response.asString();
    Assert.assertEquals(stacktrace, content);
  }

  @Test
  public void testGetStacktraceJobNotFound() {
    String exceptionMessage = "job not found";
    doThrow(new ProcessEngineException(exceptionMessage)).when(mockManagementService).getJobExceptionStacktrace(MockProvider.EXAMPLE_JOB_ID);

    given().pathParam("id", MockProvider.EXAMPLE_JOB_ID)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo(exceptionMessage))
    .when().get(JOB_RESOURCE_GET_STACKTRACE_URL);
  }

  @Test
  public void testGetStacktraceJobThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(mockManagementService).getJobExceptionStacktrace(MockProvider.EXAMPLE_JOB_ID);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(JOB_RESOURCE_GET_STACKTRACE_URL);
  }

  @Test
  public void testSetJobDuedate() {
    Date newDuedate = MockProvider.createMockDuedate();
    Map<String, Object> duedateVariableJson = new HashMap<>();
    duedateVariableJson.put("duedate", newDuedate);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(duedateVariableJson)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(JOB_RESOURCE_SET_DUEDATE_URL);

    verify(mockManagementService).setJobDuedate(MockProvider.EXAMPLE_JOB_ID, newDuedate, false);
  }

  @Test
  public void testSetJobDuedateNull() {
    Map<String, Object> duedateVariableJson = new HashMap<>();
    duedateVariableJson.put("duedate", null);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(duedateVariableJson)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(JOB_RESOURCE_SET_DUEDATE_URL);

    verify(mockManagementService).setJobDuedate(MockProvider.EXAMPLE_JOB_ID, null, false);
  }

  @Test
  public void testSetJobDuedateCascade() {
    Date newDuedate = MockProvider.createMockDuedate();
    Map<String, Object> duedateVariableJson = new HashMap<>();
    duedateVariableJson.put("duedate", newDuedate);
    duedateVariableJson.put("cascade", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(duedateVariableJson)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(JOB_RESOURCE_SET_DUEDATE_URL);

    verify(mockManagementService).setJobDuedate(MockProvider.EXAMPLE_JOB_ID, newDuedate, true);
  }

  @Test
  public void testSetJobDuedateNullCascade() {
    Map<String, Object> duedateVariableJson = new HashMap<>();
    duedateVariableJson.put("duedate", null);
    duedateVariableJson.put("cascade", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(duedateVariableJson)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(JOB_RESOURCE_SET_DUEDATE_URL);

    verify(mockManagementService).setJobDuedate(MockProvider.EXAMPLE_JOB_ID, null, true);
  }

  @Test
  public void testSetJobDuedateNonExistentJob() {
    Date newDuedate = MockProvider.createMockDuedate();
    String expectedMessage = "No job found with id '" + MockProvider.NON_EXISTING_JOB_ID + "'.";

    doThrow(new ProcessEngineException(expectedMessage)).when(mockManagementService).setJobDuedate(MockProvider.NON_EXISTING_JOB_ID,
        newDuedate, false);

    Map<String, Object> duedateVariableJson = new HashMap<>();
    duedateVariableJson.put("duedate", newDuedate);

    given().pathParam("id", MockProvider.NON_EXISTING_JOB_ID).contentType(ContentType.JSON)
    .body(duedateVariableJson).then().expect()
    .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo(expectedMessage))
    .when().put(JOB_RESOURCE_SET_DUEDATE_URL);

    verify(mockManagementService).setJobDuedate(MockProvider.NON_EXISTING_JOB_ID, newDuedate, false);
  }

  @Test
  public void testSetJobDuedateThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(mockManagementService).setJobDuedate(anyString(), any(Date.class), anyBoolean());

    Date newDuedate = MockProvider.createMockDuedate();
    Map<String, Object> duedateVariableJson = new HashMap<>();
    duedateVariableJson.put("duedate", newDuedate);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(duedateVariableJson)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .put(JOB_RESOURCE_SET_DUEDATE_URL);
  }

  @Test
  public void testActivateJob() {
    JobSuspensionStateDto dto = new JobSuspensionStateDto();
    dto.setSuspended(false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byJobId(MockProvider.EXAMPLE_JOB_ID);
    verify(mockSuspensionStateBuilder).activate();
  }

  @Test
  public void testActivateThrowsProcessEngineException() {
    JobSuspensionStateDto dto = new JobSuspensionStateDto();
    dto.setSuspended(false);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(mockSuspensionStateBuilder)
      .activate();

    given()
      .pathParam("id", MockProvider.NON_EXISTING_JOB_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedMessage))
      .when()
        .put(SINGLE_JOB_SUSPENDED_URL);
  }

  @Test
  public void testActivateThrowsAuthorizationException() {
    JobSuspensionStateDto dto = new JobSuspensionStateDto();
    dto.setSuspended(false);

    String expectedMessage = "expectedMessage";

    doThrow(new AuthorizationException(expectedMessage))
      .when(mockSuspensionStateBuilder)
      .activate();

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(expectedMessage))
      .when()
        .put(SINGLE_JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJob() {
    JobSuspensionStateDto dto = new JobSuspensionStateDto();
    dto.setSuspended(true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byJobId(MockProvider.EXAMPLE_JOB_ID);
    verify(mockSuspensionStateBuilder).suspend();
  }

  @Test
  public void testSuspendedThrowsProcessEngineException() {
    JobSuspensionStateDto dto = new JobSuspensionStateDto();
    dto.setSuspended(true);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(mockSuspensionStateBuilder)
      .suspend();

    given()
      .pathParam("id", MockProvider.NON_EXISTING_JOB_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedMessage))
      .when()
        .put(SINGLE_JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendWithMultipleByParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("jobDefinitionId", MockProvider.EXAMPLE_JOB_DEFINITION_ID);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String message = "Only one of jobId, jobDefinitionId, processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(SINGLE_JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendThrowsAuthorizationException() {
    JobSuspensionStateDto dto = new JobSuspensionStateDto();
    dto.setSuspended(true);

    String expectedMessage = "expectedMessage";

    doThrow(new AuthorizationException(expectedMessage))
      .when(mockSuspensionStateBuilder)
      .suspend();

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(dto)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(expectedMessage))
      .when()
        .put(SINGLE_JOB_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobByProcessDefinitionKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockSuspensionStateBuilder).activate();
  }

  @Test
  public void testActivateJobByProcessDefinitionKeyWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockSuspensionStateBuilder)
      .activate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobByProcessDefinitionKeyThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockSuspensionStateBuilder)
      .activate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobByProcessDefinitionKey() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockSuspensionStateBuilder).suspend();
  }

  @Test
  public void testSuspendJobByProcessDefinitionKeyWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockSuspensionStateBuilder)
      .suspend();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobByProcessDefinitionKeyThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockSuspensionStateBuilder)
      .suspend();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobByProcessDefinitionKeyAndTenantId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("processDefinitionTenantId", MockProvider.EXAMPLE_TENANT_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockSuspensionStateBuilder).processDefinitionTenantId(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockSuspensionStateBuilder).activate();
  }

  @Test
  public void testActivateJobByProcessDefinitionKeyWithoutTenantId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("processDefinitionWithoutTenantId", true);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockSuspensionStateBuilder).processDefinitionWithoutTenantId();
    verify(mockSuspensionStateBuilder).activate();
  }

  @Test
  public void testSuspendJobByProcessDefinitionKeyAndTenantId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("processDefinitionTenantId", MockProvider.EXAMPLE_TENANT_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockSuspensionStateBuilder).processDefinitionTenantId(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockSuspensionStateBuilder).suspend();
  }

  @Test
  public void testSuspendJobByProcessDefinitionKeyWithoutTenantId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("processDefinitionWithoutTenantId", true);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    verify(mockSuspensionStateBuilder).processDefinitionWithoutTenantId();
    verify(mockSuspensionStateBuilder).suspend();
  }

  @Test
  public void testActivateJobByProcessDefinitionId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    verify(mockSuspensionStateBuilder).activate();
  }

  @Test
  public void testActivateJobByProcessDefinitionIdWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockSuspensionStateBuilder)
      .activate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobByProcessDefinitionIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockSuspensionStateBuilder)
      .activate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobByProcessDefinitionId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    verify(mockSuspensionStateBuilder).suspend();
  }

  @Test
  public void testSuspendJobByProcessDefinitionIdWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockSuspensionStateBuilder)
      .suspend();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobByProcessDefinitionIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockSuspensionStateBuilder)
      .suspend();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobByProcessInstanceId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byProcessInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    verify(mockSuspensionStateBuilder).activate();
  }

  @Test
  public void testActivateJobByProcessInstanceIdWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockSuspensionStateBuilder)
      .activate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobByProcessInstanceIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockSuspensionStateBuilder)
      .activate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobByProcessInstanceId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byProcessInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    verify(mockSuspensionStateBuilder).suspend();
  }

  @Test
  public void testSuspendJobByProcessInstanceIdWithException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockSuspensionStateBuilder)
      .suspend();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobByProcessInstanceIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockSuspensionStateBuilder)
      .suspend();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobByJobDefinitionId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("jobDefinitionId", MockProvider.EXAMPLE_JOB_DEFINITION_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byJobDefinitionId(MockProvider.EXAMPLE_JOB_DEFINITION_ID);
    verify(mockSuspensionStateBuilder).activate();
  }

  @Test
  public void testActivateJobByJobDefinitionIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("jobDefinitionId", MockProvider.EXAMPLE_JOB_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockSuspensionStateBuilder)
      .activate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobByJobDefinitionId() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("jobDefinitionId", MockProvider.EXAMPLE_JOB_DEFINITION_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_SUSPENDED_URL);

    verify(mockSuspensionStateSelectBuilder).byJobDefinitionId(MockProvider.EXAMPLE_JOB_DEFINITION_ID);
    verify(mockSuspensionStateBuilder).suspend();
  }

  @Test
  public void testSuspendJobByJobDefinitionIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("jobDefinitionId", MockProvider.EXAMPLE_JOB_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockSuspensionStateBuilder)
      .suspend();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .body("type", is(AuthorizationException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobByIdShouldThrowException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", false);
    params.put("jobId", MockProvider.EXAMPLE_JOB_ID);

    String message = "Either jobDefinitionId, processInstanceId, processDefinitionId or processDefinitionKey can be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobByIdShouldThrowException() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);
    params.put("jobId", MockProvider.EXAMPLE_JOB_ID);

    String message = "Either jobDefinitionId, processInstanceId, processDefinitionId or processDefinitionKey can be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobByNothing() {
    Map<String, Object> params = new HashMap<>();
    params.put("suspended", true);

    String message = "Either jobId, jobDefinitionId, processInstanceId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(JOB_SUSPENDED_URL);
  }

  @Test
  public void testSetJobPriority() {
    Map<String, Object> priorityJson = new HashMap<>();
    priorityJson.put("priority", MockProvider.EXAMPLE_JOB_PRIORITY);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(priorityJson)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().put(JOB_RESOURCE_SET_PRIORITY_URL);

    verify(mockManagementService).setJobPriority(MockProvider.EXAMPLE_JOB_ID, MockProvider.EXAMPLE_JOB_PRIORITY);
  }

  @Test
  public void testSetJobPriorityToExtremeValue() {
    Map<String, Object> priorityJson = new HashMap<>();
    priorityJson.put("priority", Long.MAX_VALUE);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(priorityJson)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().put(JOB_RESOURCE_SET_PRIORITY_URL);

    verify(mockManagementService).setJobPriority(MockProvider.EXAMPLE_JOB_ID, Long.MAX_VALUE);
  }

  @Test
  public void testSetJobPriorityNonExistentJob() {
    String expectedMessage = "No job found with id '" + MockProvider.NON_EXISTING_JOB_ID + "'.";

    doThrow(new NotFoundException(expectedMessage))
      .when(mockManagementService).setJobPriority(MockProvider.NON_EXISTING_JOB_ID, MockProvider.EXAMPLE_JOB_PRIORITY);

    Map<String, Object> priorityJson = new HashMap<>();
    priorityJson.put("priority", MockProvider.EXAMPLE_JOB_PRIORITY);

    given()
      .pathParam("id", MockProvider.NON_EXISTING_JOB_ID)
      .contentType(ContentType.JSON)
      .body(priorityJson)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo(expectedMessage))
    .when().put(JOB_RESOURCE_SET_PRIORITY_URL);

    verify(mockManagementService).setJobPriority(MockProvider.NON_EXISTING_JOB_ID, MockProvider.EXAMPLE_JOB_PRIORITY);
  }

  @Test
  public void testSetJobPriorityFailure() {
    String expectedMessage = "No job found with id '" + MockProvider.EXAMPLE_JOB_ID + "'.";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(mockManagementService).setJobPriority(MockProvider.EXAMPLE_JOB_ID, MockProvider.EXAMPLE_JOB_PRIORITY);

    Map<String, Object> priorityJson = new HashMap<>();
    priorityJson.put("priority", MockProvider.EXAMPLE_JOB_PRIORITY);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(priorityJson)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo(expectedMessage))
    .when().put(JOB_RESOURCE_SET_PRIORITY_URL);

    verify(mockManagementService).setJobPriority(MockProvider.EXAMPLE_JOB_ID, MockProvider.EXAMPLE_JOB_PRIORITY);
  }

  @Test
  public void testSetNullJobPriorityFailure() {
    String expectedMessage = "Priority for job '" +  MockProvider.EXAMPLE_JOB_ID + "' cannot be null.";

    Map<String, Object> priorityJson = new HashMap<>();
    priorityJson.put("priority", null);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(priorityJson)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo(expectedMessage))
    .when().put(JOB_RESOURCE_SET_PRIORITY_URL);

    verifyNoMoreInteractions(mockManagementService);
  }

  @Test
  public void testSetJobPriorityThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message))
      .when(mockManagementService).setJobPriority(any(), anyLong());

    Map<String, Object> priorityJson = new HashMap<>();
    priorityJson.put("priority", MockProvider.EXAMPLE_JOB_PRIORITY);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .contentType(ContentType.JSON)
      .body(priorityJson)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .put(JOB_RESOURCE_SET_PRIORITY_URL);
  }

  @Test
  public void deleteJob() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_JOB_RESOURCE_URL);

    verify(mockManagementService).deleteJob(MockProvider.EXAMPLE_JOB_ID);
    verifyNoMoreInteractions(mockManagementService);
  }

  @Test
  public void deleteNotExistingJob() {
    String jobId = MockProvider.NON_EXISTING_JOB_ID;

    String expectedMessage = "No job found with id '" + jobId + "'.";

    doThrow(new NullValueException(expectedMessage))
      .when(mockManagementService).deleteJob(jobId);

    given()
      .pathParam("id", jobId)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo(expectedMessage))
    .when()
      .delete(SINGLE_JOB_RESOURCE_URL);

    verify(mockManagementService).deleteJob(jobId);
    verifyNoMoreInteractions(mockManagementService);
  }

  @Test
  public void deleteLockedJob() {
    String jobId = MockProvider.EXAMPLE_JOB_ID;

    String expectedMessage = "Cannot delete job when the job is being executed. Try again later.";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(mockManagementService).deleteJob(jobId);

    given()
      .pathParam("id", jobId)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo(expectedMessage))
    .when()
      .delete(SINGLE_JOB_RESOURCE_URL);

    verify(mockManagementService).deleteJob(jobId);
    verifyNoMoreInteractions(mockManagementService);
  }

  @Test
  public void deleteJobThrowAuthorizationException() {
    String jobId = MockProvider.EXAMPLE_JOB_ID;

    String expectedMessage = "Missing permissions";

    doThrow(new AuthorizationException(expectedMessage))
      .when(mockManagementService).deleteJob(jobId);

    given()
      .pathParam("id", jobId)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(expectedMessage))
    .when()
      .delete(SINGLE_JOB_RESOURCE_URL);

    verify(mockManagementService).deleteJob(jobId);
    verifyNoMoreInteractions(mockManagementService);
  }

  @Test
  public void testSetRetriesByJobsAsync() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("jobIds", ids);
    messageBodyJson.put(RETRIES, 5);

    Response response =
        given()
          .contentType(ContentType.JSON)
          .body(messageBodyJson)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(JOBS_SET_RETRIES_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByJobsAsync(5);
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobIds(eq(ids));
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobQuery(null);
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByJobsAsyncBuilder);
  }

  @Test
  public void testSetRetriesAsyncWithDueDate() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("jobIds", ids);
    Date newDueDate = new Date(1675752840000L);
    messageBodyJson.put("dueDate", newDueDate);
    messageBodyJson.put(RETRIES, 5);

    Response response =
        given()
          .contentType(ContentType.JSON)
          .body(messageBodyJson)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(JOBS_SET_RETRIES_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByJobsAsync(eq(5));
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobIds(eq(ids));
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobQuery(null);
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).dueDate(newDueDate);
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByJobsAsyncBuilder);
  }

  @Test
  public void testSetRetriesAsyncWithNullDueDate() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("jobIds", ids);
    messageBodyJson.put("dueDate", null);
    messageBodyJson.put(RETRIES, 5);

    Response response =
        given()
          .contentType(ContentType.JSON)
          .body(messageBodyJson)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(JOBS_SET_RETRIES_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByJobsAsync(eq(5));
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobIds(eq(ids));
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobQuery(null);
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).dueDate(null);
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByJobsAsyncBuilder);
  }
  @Test
  public void testSetRetriesAsyncWithQuery() {
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(RETRIES, 5);
    HistoricProcessInstanceQueryDto query = new HistoricProcessInstanceQueryDto();
    messageBodyJson.put("jobQuery", query);

    Response response =
        given()
          .contentType(ContentType.JSON)
          .body(messageBodyJson)
        .then().expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(JOBS_SET_RETRIES_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByJobsAsync(5);
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobIds(null);
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobQuery(any(JobQuery.class));
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByJobsAsyncBuilder);
  }


  @Test
  public void testSetRetriesAsyncWithCreateTimesQuery() {
    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    messageBodyJson.put(RETRIES, 5);
    Map<String, Object> condition = new HashMap<String, Object>();
    condition.put("operator", "lt");
    condition.put("value", "2022-12-15T10:45:00.000+0100");
    Map<String, Object> jobQueryDto = new HashMap<String, Object>();
    jobQueryDto.put("createTimes", Arrays.asList(condition));
    messageBodyJson.put("jobQuery", jobQueryDto);

    Response response = given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(JOBS_SET_RETRIES_URL);

    verifyBatchJson(response.asString());

    verify(mockManagementService, times(1)).setJobRetriesByJobsAsync(5);
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobIds(null);
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobQuery(any(JobQuery.class));
    verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).executeAsync();
    verifyNoMoreInteractions(mockSetJobRetriesByJobsAsyncBuilder);
  }


  @Test
  public void testSetRetriesAsyncWithDueDatesQuery() {
      Map<String, Object> messageBodyJson = new HashMap<String, Object>();
      messageBodyJson.put(RETRIES, 5);
      Map<String, Object> condition = new HashMap<String, Object>();
      condition.put("operator", "lt");
      condition.put("value", "2022-12-15T10:45:00.000+0100");
      Map<String, Object> jobQueryDto = new HashMap<String, Object>();
      jobQueryDto.put("dueDates", Arrays.asList(condition));
      messageBodyJson.put("jobQuery", jobQueryDto);

      Response response = given()
          .contentType(ContentType.JSON).body(messageBodyJson)
          .then().expect()
          .statusCode(Status.OK.getStatusCode())
          .when().post(JOBS_SET_RETRIES_URL);

      verifyBatchJson(response.asString());

      verify(mockManagementService, times(1)).setJobRetriesByJobsAsync(5);
      verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobIds(null);
      verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).jobQuery(any(JobQuery.class));
      verify(mockSetJobRetriesByJobsAsyncBuilder, times(1)).executeAsync();
      verifyNoMoreInteractions(mockSetJobRetriesByJobsAsyncBuilder);
  }


  @Test
  public void testSetRetriesWithBadRequestQuery() {
    doThrow(new BadUserRequestException("job ids are empty"))
        .when(mockSetJobRetriesByJobsAsyncBuilder).jobQuery(eq((JobQuery) null));

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(RETRIES, 5);

    given()
      .contentType(ContentType.JSON).body(messageBodyJson)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(JOBS_SET_RETRIES_URL);
  }

  @Test
  public void testSetRetriesWithoutBody() {
    given()
        .contentType(ContentType.JSON)
        .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .when().post(JOBS_SET_RETRIES_URL);

    verifyNoMoreInteractions(mockSetJobRetriesBuilder);
  }

  @Test
  public void testSetRetriesWithNegativeRetries() {
    doThrow(new BadUserRequestException("retries are negative"))
        .when(mockManagementService).setJobRetriesByJobsAsync(eq(MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES));

    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put(RETRIES, MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);
    JobQueryDto query = new JobQueryDto();
    messageBodyJson.put("jobQuery", query);

    given()
        .contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
      .when()
        .post(JOBS_SET_RETRIES_URL);
  }

  @Test
  public void testSetRetriesWithoutRetries() {
    Map<String, Object> messageBodyJson = new HashMap<>();
    messageBodyJson.put("jobIds", null);

    given()
      .contentType(ContentType.JSON)
      .body(messageBodyJson)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(JOBS_SET_RETRIES_URL);

    verifyNoMoreInteractions(mockSetJobRetriesBuilder);
  }

  @Test
  public void testRecalculateDuedateWithoutDateBase() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(JOB_RESOURCE_RECALC_DUEDATE_URL);

    verify(mockManagementService).recalculateJobDuedate(MockProvider.EXAMPLE_JOB_ID, true);
  }

  @Test
  public void testRecalculateDuedateCreationDateBased() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .queryParam("creationDateBased", true)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(JOB_RESOURCE_RECALC_DUEDATE_URL);

    verify(mockManagementService).recalculateJobDuedate(MockProvider.EXAMPLE_JOB_ID, true);
  }

  @Test
  public void testRecalculateDuedateCurrentDateBased() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_ID)
      .queryParam("creationDateBased", false)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(JOB_RESOURCE_RECALC_DUEDATE_URL);

    verify(mockManagementService).recalculateJobDuedate(MockProvider.EXAMPLE_JOB_ID, false);
  }

  @Test
  public void testRecalculateDuedateWithUnknownJobId() {
    String jobId = MockProvider.NON_EXISTING_JOB_ID;

    String expectedMessage = "No job found with id '" + jobId + "'.";

    doThrow(new NotFoundException(expectedMessage))
      .when(mockManagementService).recalculateJobDuedate(jobId, true);

    given()
      .pathParam("id", jobId)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", is(expectedMessage))
    .when().post(JOB_RESOURCE_RECALC_DUEDATE_URL);

    verify(mockManagementService).recalculateJobDuedate(jobId, true);
    verifyNoMoreInteractions(mockManagementService);
  }

  @Test
  public void testRecalculateDuedateUnauthorized() {
    String jobId = MockProvider.EXAMPLE_JOB_ID;

    String expectedMessage = "Missing permissions";

    doThrow(new AuthorizationException(expectedMessage))
      .when(mockManagementService).recalculateJobDuedate(jobId, true);

    given()
      .pathParam("id", jobId)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", is(AuthorizationException.class.getSimpleName()))
      .body("message", is(expectedMessage))
    .when().post(JOB_RESOURCE_RECALC_DUEDATE_URL);

    verify(mockManagementService).recalculateJobDuedate(jobId, true);
    verifyNoMoreInteractions(mockManagementService);
  }

  protected void verifyBatchJson(String batchJson) {
    BatchDto batch = JsonPathUtil.from(batchJson).getObject("", BatchDto.class);
    assertNotNull("The returned batch should not be null.", batch);
    assertEquals(MockProvider.EXAMPLE_BATCH_ID, batch.getId());
    assertEquals(MockProvider.EXAMPLE_BATCH_TYPE, batch.getType());
    assertEquals(MockProvider.EXAMPLE_BATCH_TOTAL_JOBS, batch.getTotalJobs());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOBS_PER_SEED, batch.getBatchJobsPerSeed());
    assertEquals(MockProvider.EXAMPLE_INVOCATIONS_PER_BATCH_JOB, batch.getInvocationsPerBatchJob());
    assertEquals(MockProvider.EXAMPLE_SEED_JOB_DEFINITION_ID, batch.getSeedJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_MONITOR_JOB_DEFINITION_ID, batch.getMonitorJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOB_DEFINITION_ID, batch.getBatchJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_TENANT_ID, batch.getTenantId());
  }

}
