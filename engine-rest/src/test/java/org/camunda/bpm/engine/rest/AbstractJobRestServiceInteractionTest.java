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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.rest.dto.runtime.JobSuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.MockJobBuilder;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractJobRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String JOB_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/job";
  protected static final String SINGLE_JOB_RESOURCE_URL = JOB_RESOURCE_URL + "/{id}";
  protected static final String JOB_RESOURCE_SET_RETRIES_URL = SINGLE_JOB_RESOURCE_URL + "/retries";
  protected static final String JOB_RESOURCE_SET_PRIORITY_URL = SINGLE_JOB_RESOURCE_URL + "/priority";
  protected static final String JOB_RESOURCE_EXECUTE_JOB_URL = SINGLE_JOB_RESOURCE_URL + "/execute";
  protected static final String JOB_RESOURCE_GET_STACKTRACE_URL = SINGLE_JOB_RESOURCE_URL + "/stacktrace";
  protected static final String JOB_RESOURCE_SET_DUEDATE_URL = SINGLE_JOB_RESOURCE_URL + "/duedate";
  protected static final String SINGLE_JOB_SUSPENDED_URL = SINGLE_JOB_RESOURCE_URL + "/suspended";
  protected static final String JOB_SUSPENDED_URL = JOB_RESOURCE_URL + "/suspended";

  private ProcessEngine namedProcessEngine;
  private ManagementService mockManagementService;

  private JobQuery mockQuery;

  @Before
  public void setUpRuntimeData() {

    mockQuery = mock(JobQuery.class);
    Job mockedJob = new MockJobBuilder()
      .id(MockProvider.EXAMPLE_JOB_ID)
      .processInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .executionId(MockProvider.EXAMPLE_EXECUTION_ID)
      .retries(MockProvider.EXAMPLE_JOB_RETRIES)
      .exceptionMessage(MockProvider.EXAMPLE_JOB_NO_EXCEPTION_MESSAGE)
      .dueDate(new Date())
      .priority(MockProvider.EXAMPLE_JOB_PRIORITY)
      .build();

    when(mockQuery.singleResult()).thenReturn(mockedJob);
    when(mockQuery.jobId(MockProvider.EXAMPLE_JOB_ID)).thenReturn(mockQuery);

    mockManagementService = mock(ManagementService.class);
    when(mockManagementService.createJobQuery()).thenReturn(mockQuery);

    namedProcessEngine = getProcessEngine(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME);
    when(namedProcessEngine.getManagementService()).thenReturn(mockManagementService);
  }

  @Test
  public void testSetJobRetries() {
    Map<String, Object> retriesVariableJson = new HashMap<String, Object>();
    retriesVariableJson.put("retries", MockProvider.EXAMPLE_JOB_RETRIES);

    given().pathParam("id", MockProvider.EXAMPLE_JOB_ID).contentType(ContentType.JSON).body(retriesVariableJson).then().expect()
    .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().put(JOB_RESOURCE_SET_RETRIES_URL);

    verify(mockManagementService).setJobRetries(MockProvider.EXAMPLE_JOB_ID, MockProvider.EXAMPLE_JOB_RETRIES);
  }

  @Test
  public void testSetJobRetriesNonExistentJob() {
    String expectedMessage = "No job found with id '" + MockProvider.NON_EXISTING_JOB_ID + "'.";

    doThrow(new ProcessEngineException(expectedMessage)).when(mockManagementService).setJobRetries(MockProvider.NON_EXISTING_JOB_ID,
        MockProvider.EXAMPLE_JOB_RETRIES);

    Map<String, Object> retriesVariableJson = new HashMap<String, Object>();
    retriesVariableJson.put("retries", MockProvider.EXAMPLE_JOB_RETRIES);

    given().pathParam("id", MockProvider.NON_EXISTING_JOB_ID).contentType(ContentType.JSON)
    .body(retriesVariableJson).then().expect()
    .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo(expectedMessage))
    .when().put(JOB_RESOURCE_SET_RETRIES_URL);

    verify(mockManagementService).setJobRetries(MockProvider.NON_EXISTING_JOB_ID, MockProvider.EXAMPLE_JOB_RETRIES);
  }

  @Test
  public void testSetJobRetriesNegativeRetries() {

    String expectedMessage = "The number of job retries must be a non-negative Integer, but '" + MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES
        + "' has been provided.";

    doThrow(new ProcessEngineException(expectedMessage)).when(mockManagementService).setJobRetries(MockProvider.EXAMPLE_JOB_ID,
        MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);

    Map<String, Object> retriesVariableJson = new HashMap<String, Object>();
    retriesVariableJson.put("retries", MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);

    given().pathParam("id", MockProvider.EXAMPLE_JOB_ID).contentType(ContentType.JSON).body(retriesVariableJson).then().then().expect()
    .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo(expectedMessage))
    .when().put(JOB_RESOURCE_SET_RETRIES_URL);

    verify(mockManagementService).setJobRetries(MockProvider.EXAMPLE_JOB_ID, MockProvider.EXAMPLE_NEGATIVE_JOB_RETRIES);
  }

  @Test
  public void testSetJobRetriesThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(mockManagementService).setJobRetries(anyString(), anyInt());

    Map<String, Object> retriesVariableJson = new HashMap<String, Object>();
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
  }

  @Test
  public void testSimpleJobGet() {
    given().pathParam("id", MockProvider.EXAMPLE_JOB_ID).then().expect().statusCode(Status.OK.getStatusCode())
    .body("id", equalTo(MockProvider.EXAMPLE_JOB_ID))
    .body("processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
    .body("executionId", equalTo(MockProvider.EXAMPLE_EXECUTION_ID))
    .body("exceptionMessage", equalTo(MockProvider.EXAMPLE_JOB_NO_EXCEPTION_MESSAGE))
    .body("priority", equalTo(MockProvider.EXAMPLE_JOB_PRIORITY))
    .when().get(SINGLE_JOB_RESOURCE_URL);

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

    given().pathParam("id", jobId).then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("Job with id " + jobId + " does not exist")).when()
    .get(SINGLE_JOB_RESOURCE_URL);
  }

  @Test
  public void testExecuteJob() {
    given().pathParam("id", MockProvider.EXAMPLE_JOB_ID)
    .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(JOB_RESOURCE_EXECUTE_JOB_URL);

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
    Map<String, Object> duedateVariableJson = new HashMap<String, Object>();
    duedateVariableJson.put("duedate", newDuedate);

    given().pathParam("id", MockProvider.EXAMPLE_JOB_ID).contentType(ContentType.JSON).body(duedateVariableJson).then().expect()
    .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().put(JOB_RESOURCE_SET_DUEDATE_URL);

    verify(mockManagementService).setJobDuedate(MockProvider.EXAMPLE_JOB_ID, newDuedate);

  }

  @Test
  public void testSetJobDuedateNull() {
    Map<String, Object> duedateVariableJson = new HashMap<String, Object>();
    duedateVariableJson.put("duedate", null);

    given().pathParam("id", MockProvider.EXAMPLE_JOB_ID).contentType(ContentType.JSON).body(duedateVariableJson).then().expect()
    .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().put(JOB_RESOURCE_SET_DUEDATE_URL);

    verify(mockManagementService).setJobDuedate(MockProvider.EXAMPLE_JOB_ID, null);

  }

  @Test
  public void testSetJobDuedateNonExistentJob() {
    Date newDuedate = MockProvider.createMockDuedate();
    String expectedMessage = "No job found with id '" + MockProvider.NON_EXISTING_JOB_ID + "'.";

    doThrow(new ProcessEngineException(expectedMessage)).when(mockManagementService).setJobDuedate(MockProvider.NON_EXISTING_JOB_ID,
        newDuedate);

    Map<String, Object> duedateVariableJson = new HashMap<String, Object>();
    duedateVariableJson.put("duedate", newDuedate);

    given().pathParam("id", MockProvider.NON_EXISTING_JOB_ID).contentType(ContentType.JSON)
    .body(duedateVariableJson).then().expect()
    .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo(expectedMessage))
    .when().put(JOB_RESOURCE_SET_DUEDATE_URL);

    verify(mockManagementService).setJobDuedate(MockProvider.NON_EXISTING_JOB_ID, newDuedate);
  }

  @Test
  public void testSetJobDuedateThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(mockManagementService).setJobDuedate(anyString(), any(Date.class));

    Date newDuedate = MockProvider.createMockDuedate();
    Map<String, Object> duedateVariableJson = new HashMap<String, Object>();
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

    verify(mockManagementService).activateJobById(MockProvider.EXAMPLE_JOB_ID);
  }

  @Test
  public void testActivateThrowsProcessEngineException() {
    JobSuspensionStateDto dto = new JobSuspensionStateDto();
    dto.setSuspended(false);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(mockManagementService)
      .activateJobById(eq(MockProvider.NON_EXISTING_JOB_ID));

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
      .when(mockManagementService)
      .activateJobById(eq(MockProvider.EXAMPLE_JOB_ID));

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

    verify(mockManagementService).suspendJobById(MockProvider.EXAMPLE_JOB_ID);
  }

  @Test
  public void testSuspendedThrowsProcessEngineException() {
    JobSuspensionStateDto dto = new JobSuspensionStateDto();
    dto.setSuspended(true);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(mockManagementService)
      .suspendJobById(eq(MockProvider.NON_EXISTING_JOB_ID));

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
    Map<String, Object> params = new HashMap<String, Object>();
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
      .when(mockManagementService)
      .suspendJobById(eq(MockProvider.EXAMPLE_JOB_ID));

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
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(mockManagementService).activateJobByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
  }

  @Test
  public void testActivateJobByProcessDefinitionKeyWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockManagementService)
      .activateJobByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

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
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockManagementService)
      .activateJobByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

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
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(mockManagementService).suspendJobByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
  }

  @Test
  public void testSuspendJobByProcessDefinitionKeyWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockManagementService)
      .suspendJobByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

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
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockManagementService)
      .suspendJobByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

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
  public void testActivateJobByProcessDefinitionId() {
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(mockManagementService).activateJobByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }

  @Test
  public void testActivateJobByProcessDefinitionIdWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockManagementService)
      .activateJobByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

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
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockManagementService)
      .activateJobByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

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
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(mockManagementService).suspendJobByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }

  @Test
  public void testSuspendJobByProcessDefinitionIdWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockManagementService)
      .suspendJobByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

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
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockManagementService)
      .suspendJobByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

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
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(mockManagementService).activateJobByProcessInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
  }

  @Test
  public void testActivateJobByProcessInstanceIdWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockManagementService)
      .activateJobByProcessInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

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
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockManagementService)
      .activateJobByProcessInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

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
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(mockManagementService).suspendJobByProcessInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
  }

  @Test
  public void testSuspendJobByProcessInstanceIdWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockManagementService)
      .suspendJobByProcessInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

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
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockManagementService)
      .suspendJobByProcessInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

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
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(mockManagementService).activateJobByJobDefinitionId(MockProvider.EXAMPLE_JOB_DEFINITION_ID);
  }

  @Test
  public void testActivateJobByJobDefinitionIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("jobDefinitionId", MockProvider.EXAMPLE_JOB_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockManagementService)
      .activateJobByJobDefinitionId(MockProvider.EXAMPLE_JOB_DEFINITION_ID);

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
    Map<String, Object> params = new HashMap<String, Object>();
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

    verify(mockManagementService).suspendJobByJobDefinitionId(MockProvider.EXAMPLE_JOB_DEFINITION_ID);
  }

  @Test
  public void testSuspendJobByJobDefinitionIdThrowsAuthorizationException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("jobDefinitionId", MockProvider.EXAMPLE_JOB_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new AuthorizationException(expectedException))
      .when(mockManagementService)
      .suspendJobByJobDefinitionId(MockProvider.EXAMPLE_JOB_DEFINITION_ID);

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
    Map<String, Object> params = new HashMap<String, Object>();
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
    Map<String, Object> params = new HashMap<String, Object>();
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
    Map<String, Object> params = new HashMap<String, Object>();
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
    Map<String, Object> priorityJson = new HashMap<String, Object>();
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
  public void testSetJobPriorityNonExistentJob() {
    String expectedMessage = "No job found with id '" + MockProvider.NON_EXISTING_JOB_ID + "'.";

    doThrow(new NotFoundException(expectedMessage))
      .when(mockManagementService).setJobPriority(MockProvider.NON_EXISTING_JOB_ID, MockProvider.EXAMPLE_JOB_PRIORITY);

    Map<String, Object> priorityJson = new HashMap<String, Object>();
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

    Map<String, Object> priorityJson = new HashMap<String, Object>();
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
  public void testSetJobPriorityThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message))
      .when(mockManagementService).setJobPriority(anyString(), anyInt());

    Map<String, Object> priorityJson = new HashMap<String, Object>();
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

}
