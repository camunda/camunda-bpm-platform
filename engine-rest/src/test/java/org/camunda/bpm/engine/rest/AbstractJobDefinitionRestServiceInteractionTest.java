package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.jayway.restassured.http.ContentType;

public abstract class AbstractJobDefinitionRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String JOB_DEFINITION_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/job-definition";
  protected static final String SINGLE_JOB_DEFINITION_RESOURCE_URL = JOB_DEFINITION_RESOURCE_URL + "/{id}";
  protected static final String SINGLE_JOB_DEFINITION_SUSPENDED_URL = SINGLE_JOB_DEFINITION_RESOURCE_URL + "/suspended";
  protected static final String JOB_DEFINITION_SUSPENDED_URL = JOB_DEFINITION_RESOURCE_URL + "/suspended";
  protected static final String JOB_DEFINITION_RETRIES_URL = SINGLE_JOB_DEFINITION_RESOURCE_URL + "/retries";

  private ProcessEngine namedProcessEngine;
  private ManagementService mockManagementService;
  private JobDefinitionQuery mockQuery;

  @Before
  public void setUpRuntimeData() {

    mockQuery = mock(JobDefinitionQuery.class);

    JobDefinition mockedJobDefinition = MockProvider.createMockJobDefinition();

    when(mockQuery.singleResult()).thenReturn(mockedJobDefinition);
    when(mockQuery.jobDefinitionId(MockProvider.EXAMPLE_JOB_DEFINITION_ID)).thenReturn(mockQuery);

    mockManagementService = mock(ManagementService.class);
    when(mockManagementService.createJobDefinitionQuery()).thenReturn(mockQuery);

    namedProcessEngine = getProcessEngine(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME);
    when(namedProcessEngine.getManagementService()).thenReturn(mockManagementService);
  }

  @Test
  public void testSimpleJobDefinitionGet() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_DEFINITION_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(MockProvider.EXAMPLE_JOB_DEFINITION_ID))
        .body("processDefinitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
        .body("processDefinitionKey", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY))
        .body("jobType", equalTo(MockProvider.EXAMPLE_JOB_TYPE))
        .body("jobConfiguration", equalTo(MockProvider.EXAMPLE_JOB_CONFIG))
        .body("activityId", equalTo(MockProvider.EXAMPLE_ACTIVITY_ID))
        .body("suspended", equalTo(MockProvider.EXAMPLE_JOB_DEFINITION_IS_SUSPENDED))
    .when()
      .get(SINGLE_JOB_DEFINITION_RESOURCE_URL);

    InOrder inOrder = inOrder(mockQuery);
    inOrder.verify(mockQuery).jobDefinitionId(MockProvider.EXAMPLE_JOB_DEFINITION_ID);
    inOrder.verify(mockQuery).singleResult();
  }

  @Test
  public void testJobDefinitionGetIdDoesntExist() {
    JobDefinitionQuery invalidQueryNonExistingJobDefinition;
    invalidQueryNonExistingJobDefinition = mock(JobDefinitionQuery.class);

    when(mockManagementService.createJobDefinitionQuery()
        .jobDefinitionId(MockProvider.NON_EXISTING_JOB_DEFINITION_ID))
        .thenReturn(invalidQueryNonExistingJobDefinition);

    when(invalidQueryNonExistingJobDefinition.singleResult()).thenReturn(null);

    String jobDefinitionId = MockProvider.NON_EXISTING_JOB_DEFINITION_ID;

    given()
      .pathParam("id", jobDefinitionId)
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Job Definition with id " + jobDefinitionId + " does not exist"))
    .when()
      .get(SINGLE_JOB_DEFINITION_RESOURCE_URL);
  }

  @Test
  public void testActivateJobDefinitionExcludingInstances() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("includeJobs", false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionById(MockProvider.EXAMPLE_JOB_DEFINITION_ID, false, null);
  }

  @Test
  public void testDelayedActivateJobDefinitionExcludingInstances() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("includeJobs", false);
    params.put("executionDate", MockProvider.EXAMPLE_JOB_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_JOB_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionById(MockProvider.EXAMPLE_JOB_DEFINITION_ID, false, executionDate);
  }

  @Test
  public void testActivateJobDefinitionIncludingInstances() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("includeJobs", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionById(MockProvider.EXAMPLE_JOB_DEFINITION_ID, true, null);
  }

  @Test
  public void testDelayedActivateJobDefinitionIncludingInstances() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("includeJobs", true);
    params.put("executionDate", MockProvider.EXAMPLE_JOB_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_JOB_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionById(MockProvider.EXAMPLE_JOB_DEFINITION_ID, true, executionDate);
  }

  @Test
  public void testActivateThrowProcessEngineException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("includeJobs", false);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(mockManagementService)
      .activateJobDefinitionById(eq(MockProvider.NON_EXISTING_JOB_DEFINITION_ID), eq(false), isNull(Date.class));

    given()
      .pathParam("id", MockProvider.NON_EXISTING_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedMessage))
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateNonParseableDateFormat() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("includeJobs", false);
    params.put("executionDate", "a");

    String expectedMessage = "Invalid format: \"a\"";
    String exceptionMessage = "The suspension state of Job Definition with id " + MockProvider.NON_EXISTING_JOB_DEFINITION_ID + " could not be updated due to: " + expectedMessage;

    given()
      .pathParam("id", MockProvider.NON_EXISTING_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(exceptionMessage))
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobDefinitionExcludingInstances() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("includeJobs", false);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionById(MockProvider.EXAMPLE_JOB_DEFINITION_ID, false, null);
  }

  @Test
  public void testDelayedSuspendJobDefinitionExcludingInstances() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("includeJobs", false);
    params.put("executionDate", MockProvider.EXAMPLE_JOB_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_JOB_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionById(MockProvider.EXAMPLE_JOB_DEFINITION_ID, false, executionDate);
  }

  @Test
  public void testSuspendJobDefinitionIncludingInstances() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("includeJobs", true);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionById(MockProvider.EXAMPLE_JOB_DEFINITION_ID, true, null);
  }

  @Test
  public void testDelayedSuspendJobDefinitionIncludingInstances() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("includeJobs", true);
    params.put("executionDate", MockProvider.EXAMPLE_JOB_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_JOB_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionById(MockProvider.EXAMPLE_JOB_DEFINITION_ID, true, executionDate);
  }

  @Test
  public void testSuspendThrowsProcessEngineException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("includeJobs", false);

    String expectedMessage = "expectedMessage";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(mockManagementService)
      .suspendJobDefinitionById(eq(MockProvider.NON_EXISTING_JOB_DEFINITION_ID), eq(false), isNull(Date.class));

    given()
      .pathParam("id", MockProvider.NON_EXISTING_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedMessage))
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendNonParseableDateFormat() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("includeJobs", false);
    params.put("executionDate", "a");

    String expectedMessage = "Invalid format: \"a\"";
    String exceptionMessage = "The suspension state of Job Definition with id " + MockProvider.NON_EXISTING_JOB_DEFINITION_ID + " could not be updated due to: " + expectedMessage;

    given()
      .pathParam("id", MockProvider.NON_EXISTING_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(exceptionMessage))
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendWithMultipleByParameters() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String message = "Only one of jobDefinitionId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(SINGLE_JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobDefinitionByProcessDefinitionKey() {
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
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, null);
  }

  @Test
  public void testActivateJobDefinitionByProcessDefinitionKeyIncludingInstaces() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("includeJobs", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, true, null);
  }

  @Test
  public void testDelayedActivateJobDefinitionByProcessDefinitionKey() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, executionDate);
  }

  @Test
  public void testDelayedActivateJobDefinitionByProcessDefinitionKeyIncludingInstaces() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("includeJobs", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, true, executionDate);
  }

  @Test
  public void testActivateJobDefinitionByProcessDefinitionKeyWithUnparseableDate() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", "a");

    String message = "Could not update the suspension state of Job Definitions due to: Invalid format: \"a\"";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobDefinitionByProcessDefinitionKeyWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockManagementService)
      .activateJobDefinitionByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, null);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobDefinitionByProcessDefinitionKey() {
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
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, null);
  }

  @Test
  public void testSuspendJobDefinitionByProcessDefinitionKeyIncludingInstaces() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("includeJobs", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, true, null);
  }

  @Test
  public void testDelayedSuspendJobDefinitionByProcessDefinitionKey() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, executionDate);
  }

  @Test
  public void testDelayedSuspendJobDefinitionByProcessDefinitionKeyIncludingInstaces() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("includeJobs", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, true, executionDate);
  }

  @Test
  public void testSuspendJobDefinitionByProcessDefinitionKeyWithUnparseableDate() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    params.put("executionDate", "a");

    String message = "Could not update the suspension state of Job Definitions due to: Invalid format: \"a\"";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobDefinitionByProcessDefinitionKeyWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockManagementService)
      .suspendJobDefinitionByProcessDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, false, null);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobDefinitionByProcessDefinitionId() {
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
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);
  }

  @Test
  public void testActivateJobDefinitionByProcessDefinitionIdIncludingInstaces() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("includeJobs", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, null);
  }

  @Test
  public void testDelayedActivateJobDefinitionByProcessDefinitionId() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, executionDate);
  }

  @Test
  public void testDelayedActivateJobDefinitionByProcessDefinitionIdIncludingInstaces() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("includeJobs", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).activateJobDefinitionByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, executionDate);
  }

  @Test
  public void testActivateJobDefinitionByProcessDefinitionIdWithUnparseableDate() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    params.put("executionDate", "a");

    String message = "Could not update the suspension state of Job Definitions due to: Invalid format: \"a\"";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobDefinitionByProcessDefinitionIdWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockManagementService)
      .activateJobDefinitionByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobDefinitionByProcessDefinitionId() {
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
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);
  }

  @Test
  public void testSuspendJobDefinitionByProcessDefinitionIdIncludingInstaces() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("includeJobs", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, null);
  }

  @Test
  public void testDelayedSuspendJobDefinitionByProcessDefinitionId() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, executionDate);
  }

  @Test
  public void testDelayedSuspendJobDefinitionByProcessDefinitionIdIncludingInstaces() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("includeJobs", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    params.put("executionDate", MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION);

    Date executionDate = DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION).toDate();

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);

    verify(mockManagementService).suspendJobDefinitionByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, true, executionDate);
  }

  @Test
  public void testSuspendJobDefinitionByProcessDefinitionIdWithUnparseableDate() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    params.put("executionDate", "a");

    String message = "Could not update the suspension state of Job Definitions due to: Invalid format: \"a\"";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobDefinitionByProcessDefinitionIdWithException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);

    String expectedException = "expectedException";
    doThrow(new ProcessEngineException(expectedException))
      .when(mockManagementService)
      .suspendJobDefinitionByProcessDefinitionId(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, false, null);

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", is(ProcessEngineException.class.getSimpleName()))
        .body("message", is(expectedException))
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testActivateJobDefinitionByIdShouldThrowException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", false);
    params.put("jobDefinitionId", MockProvider.EXAMPLE_JOB_DEFINITION_ID);

    String message = "Either processDefinitionId or processDefinitionKey can be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobDefinitionByIdShouldThrowException() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);
    params.put("jobDefinitionId", MockProvider.EXAMPLE_JOB_DEFINITION_ID);

    String message = "Either processDefinitionId or processDefinitionKey can be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSuspendJobDefinitionByNothing() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("suspended", true);

    String message = "Either jobDefinitionId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";

    given()
      .contentType(ContentType.JSON)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is(message))
      .when()
        .put(JOB_DEFINITION_SUSPENDED_URL);
  }

  @Test
  public void testSetJobRetries() {
    Map<String, Object> retriesVariableJson = new HashMap<String, Object>();
    retriesVariableJson.put("retries", MockProvider.EXAMPLE_JOB_RETRIES);

    given()
      .pathParam("id", MockProvider.EXAMPLE_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(retriesVariableJson)
    .then()
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(JOB_DEFINITION_RETRIES_URL);

    verify(mockManagementService).setJobRetriesByJobDefinitionId(MockProvider.EXAMPLE_JOB_DEFINITION_ID, MockProvider.EXAMPLE_JOB_RETRIES);
  }

  @Test
  public void testSetJobRetriesExceptionExpected() {
    String expectedMessage = "expected exception message";

    doThrow(new ProcessEngineException(expectedMessage))
      .when(mockManagementService)
        .setJobRetriesByJobDefinitionId(MockProvider.NON_EXISTING_JOB_DEFINITION_ID, MockProvider.EXAMPLE_JOB_RETRIES);

    Map<String, Object> retriesVariableJson = new HashMap<String, Object>();
    retriesVariableJson.put("retries", MockProvider.EXAMPLE_JOB_RETRIES);

    given()
      .pathParam("id", MockProvider.NON_EXISTING_JOB_DEFINITION_ID)
      .contentType(ContentType.JSON)
      .body(retriesVariableJson)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo(expectedMessage))
    .when()
      .put(JOB_DEFINITION_RETRIES_URL);

    verify(mockManagementService).setJobRetriesByJobDefinitionId(MockProvider.NON_EXISTING_JOB_DEFINITION_ID, MockProvider.EXAMPLE_JOB_RETRIES);
  }

}
