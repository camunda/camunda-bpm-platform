package org.camunda.bpm.engine.rest.history;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.OrderingBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class HistoricActivityInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/activity-instance";

  protected static final String HISTORIC_ACTIVITY_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL + "/count";

  protected HistoricActivityInstanceQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockHistoricActivityInstanceQuery(MockProvider.createMockHistoricActivityInstances());
  }

  private HistoricActivityInstanceQuery setUpMockHistoricActivityInstanceQuery(List<HistoricActivityInstance> mockedHistoricActivityInstances) {
    HistoricActivityInstanceQuery mockedhistoricActivityInstanceQuery = mock(HistoricActivityInstanceQuery.class);
    when(mockedhistoricActivityInstanceQuery.list()).thenReturn(mockedHistoricActivityInstances);
    when(mockedhistoricActivityInstanceQuery.count()).thenReturn((long) mockedHistoricActivityInstances.size());

    when(processEngine.getHistoryService().createHistoricActivityInstanceQuery()).thenReturn(mockedhistoricActivityInstanceQuery);

    return mockedhistoricActivityInstanceQuery;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("processInstanceId", queryKey)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testNoParametersQueryAsPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("instanceId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
      .when()
        .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "instanceId")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when()
        .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
    .queryParam("sortOrder", "asc")
  .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("instanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("instanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("executionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByExecutionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("executionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByExecutionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityName", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityName();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityName", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityName();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityType", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityType();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityType", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityType();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("startTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceStartTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("startTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceStartTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceEndTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceEndTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceDuration();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceDuration();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("occurrence", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderPartiallyByOccurrence();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("occurrence", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderPartiallyByOccurrence();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).asc();
  }

  @Test
  public void testSecondarySortingAsPost() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("sorting", OrderingBuilder.create()
      .orderBy("definitionId").desc()
      .orderBy("instanceId").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).asc();
  }

  @Test
  public void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_ACTIVITY_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testQueryCountForPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .body("count", equalTo(1))
      .when()
        .post(HISTORIC_ACTIVITY_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testSimpleHistoricActivityQuery() {
    String processInstanceId = MockProvider.EXAMPLE_PROCESS_INSTANCE_ID;

    Response response = given()
        .queryParam("processInstanceId", processInstanceId)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).processInstanceId(processInstanceId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one activity instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned activity instance should not be null.", instances.get(0));

    String returnedId = from(content).getString("[0].id");
    String returnedParentActivityInstanceId = from(content).getString("[0].parentActivityInstanceId");
    String returnedActivityId = from(content).getString("[0].activityId");
    String returnedActivityName = from(content).getString("[0].activityName");
    String returnedActivityType = from(content).getString("[0].activityType");
    String returnedProcessDefinitionKey = from(content).getString("[0].processDefinitionKey");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedTaskId = from(content).getString("[0].taskId");
    String returnedCalledProcessInstanceId = from(content).getString("[0].calledProcessInstanceId");
    String returnedCalledCaseInstanceId = from(content).getString("[0].calledCaseInstanceId");
    String returnedAssignee = from(content).getString("[0].assignee");
    Date returnedStartTime = DateTimeUtil.parseDate(from(content).getString("[0].startTime"));
    Date returnedEndTime = DateTimeUtil.parseDate(from(content).getString("[0].endTime"));
    long returnedDurationInMillis = from(content).getLong("[0].durationInMillis");
    boolean canceled = from(content).getBoolean("[0].canceled");
    boolean completeScope = from(content).getBoolean("[0].completeScope");
    String returnedTenantId = from(content).getString("[0].tenantId");

    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_ID, returnedId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_PARENT_ACTIVITY_INSTANCE_ID, returnedParentActivityInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_ID, returnedActivityId);
    Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_NAME, returnedActivityName);
    Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_TYPE, returnedActivityType);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, returnedProcessDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_EXECUTION_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_ID, returnedTaskId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID, returnedCalledProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_CASE_INSTANCE_ID, returnedCalledCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME, returnedAssignee);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_START_TIME), returnedStartTime);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_END_TIME), returnedEndTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_DURATION, returnedDurationInMillis);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_CANCELED, canceled);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_COMPLETE_SCOPE, completeScope);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
  }

  @Test
  public void testAdditionalParameters() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .queryParams(stringQueryParameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  @Test
  public void testAdditionalParametersAsPost() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(stringQueryParameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  @Test
  public void testBooleanParameters() {
    Map<String, Boolean> params = getCompleteBooleanQueryParameters();

    given()
      .queryParams(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyBooleanParameterQueryInvocations();
  }

  @Test
  public void testBooleanParametersAsPost() {
    Map<String, Boolean> params = getCompleteBooleanQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyBooleanParameterQueryInvocations();
  }

  private Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("activityInstanceId", MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_ID);
    parameters.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    parameters.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    parameters.put("executionId", MockProvider.EXAMPLE_EXECUTION_ID);
    parameters.put("activityId", MockProvider.EXAMPLE_ACTIVITY_ID);
    parameters.put("activityName", MockProvider.EXAMPLE_ACTIVITY_NAME);
    parameters.put("activityType", MockProvider.EXAMPLE_ACTIVITY_TYPE);
    parameters.put("taskAssignee", MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME);

    return parameters;
  }

  private Map<String, Boolean> getCompleteBooleanQueryParameters() {
    Map<String, Boolean> parameters = new HashMap<String, Boolean>();

    parameters.put("canceled", MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_CANCELED);
    parameters.put("completeScope", MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_COMPLETE_SCOPE);

    return parameters;
  }

  private void verifyStringParameterQueryInvocations() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    verify(mockedQuery).activityInstanceId(stringQueryParameters.get("activityInstanceId"));
    verify(mockedQuery).processInstanceId(stringQueryParameters.get("processInstanceId"));
    verify(mockedQuery).processDefinitionId(stringQueryParameters.get("processDefinitionId"));
    verify(mockedQuery).executionId(stringQueryParameters.get("executionId"));
    verify(mockedQuery).activityId(stringQueryParameters.get("activityId"));
    verify(mockedQuery).activityName(stringQueryParameters.get("activityName"));
    verify(mockedQuery).activityType(stringQueryParameters.get("activityType"));
    verify(mockedQuery).taskAssignee(stringQueryParameters.get("taskAssignee"));

    verify(mockedQuery).list();
  }

  private void verifyBooleanParameterQueryInvocations() {
    Map<String, Boolean> booleanParams = getCompleteBooleanQueryParameters();
    Boolean canceled = booleanParams.get("canceled");
    Boolean completeScope = booleanParams.get("completeScope");

    if (canceled != null && canceled) {
      verify(mockedQuery).canceled();
    }
    if (completeScope != null && completeScope) {
      verify(mockedQuery).completeScope();
    }

    verify(mockedQuery).list();
  }

  @Test
  public void testFinishedHistoricActivityQuery() {
    Response response = given()
        .queryParam("finished", true)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).finished();
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one activity instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned activity instance should not be null.", instances.get(0));

    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedActivityEndTime = from(content).getString("[0].endTime");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_END_TIME, returnedActivityEndTime);
  }

  @Test
  public void testFinishedHistoricActivityQueryAsPost() {
    Map<String, Boolean> body = new HashMap<String, Boolean>();
    body.put("finished", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(body)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).finished();
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one activity instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned activity instance should not be null.", instances.get(0));

    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedActivityEndTime = from(content).getString("[0].endTime");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_END_TIME, returnedActivityEndTime);
  }

  @Test
  public void testUnfinishedHistoricActivityQuery() {
    List<HistoricActivityInstance> mockedHistoricActivityInstances = MockProvider.createMockRunningHistoricActivityInstances();
    HistoricActivityInstanceQuery mockedhistoricActivityInstanceQuery = mock(HistoricActivityInstanceQuery.class);
    when(mockedhistoricActivityInstanceQuery.list()).thenReturn(mockedHistoricActivityInstances);
    when(processEngine.getHistoryService().createHistoricActivityInstanceQuery()).thenReturn(mockedhistoricActivityInstanceQuery);

    Response response = given()
        .queryParam("unfinished", true)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedhistoricActivityInstanceQuery);
    inOrder.verify(mockedhistoricActivityInstanceQuery).unfinished();
    inOrder.verify(mockedhistoricActivityInstanceQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one activity instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned activity instance should not be null.", instances.get(0));

    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedActivityEndTime = from(content).getString("[0].endTime");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertNull(returnedActivityEndTime);
  }

  @Test
  public void testUnfinishedHistoricActivityQueryAsPost() {
    List<HistoricActivityInstance> mockedHistoricActivityInstances = MockProvider.createMockRunningHistoricActivityInstances();
    HistoricActivityInstanceQuery mockedhistoricActivityInstanceQuery = mock(HistoricActivityInstanceQuery.class);
    when(mockedhistoricActivityInstanceQuery.list()).thenReturn(mockedHistoricActivityInstances);
    when(processEngine.getHistoryService().createHistoricActivityInstanceQuery()).thenReturn(mockedhistoricActivityInstanceQuery);

    Map<String, Boolean> body = new HashMap<String, Boolean>();
    body.put("unfinished", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(body)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedhistoricActivityInstanceQuery);
    inOrder.verify(mockedhistoricActivityInstanceQuery).unfinished();
    inOrder.verify(mockedhistoricActivityInstanceQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one activity instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned activity instance should not be null.", instances.get(0));

    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedActivityEndTime = from(content).getString("[0].endTime");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertNull(returnedActivityEndTime);
  }

  @Test
  public void testHistoricBeforeAndAfterStartTimeQuery() {
    given()
      .queryParam("startedBefore", MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_STARTED_BEFORE)
      .queryParam("startedAfter", MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_STARTED_AFTER)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyStartParameterQueryInvocations();
  }

  @Test
  public void testHistoricBeforeAndAfterStartTimeQueryAsPost() {
    Map<String, Date> parameters = getCompleteStartDateQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyStartParameterQueryInvocations();
  }

  private Map<String, Date> getCompleteStartDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<String, Date>();

    parameters.put("startedAfter", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_STARTED_AFTER));
    parameters.put("startedBefore", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_STARTED_BEFORE));

    return parameters;
  }

  private void verifyStartParameterQueryInvocations() {
    Map<String, Date> startDateParameters = getCompleteStartDateQueryParameters();

    verify(mockedQuery).startedBefore(startDateParameters.get("startedBefore"));
    verify(mockedQuery).startedAfter(startDateParameters.get("startedAfter"));

    verify(mockedQuery).list();
  }

  @Test
  public void testHistoricAfterAndBeforeFinishTimeQuery() {
    given()
      .queryParam("finishedAfter", MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_FINISHED_AFTER)
      .queryParam("finishedBefore", MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_FINISHED_BEFORE)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyFinishedParameterQueryInvocations();
  }

  @Test
  public void testHistoricAfterAndBeforeFinishTimeQueryAsPost() {
    Map<String, Date> parameters = getCompleteFinishedDateQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verifyFinishedParameterQueryInvocations();
  }

  private Map<String, Date> getCompleteFinishedDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<String, Date>();

    parameters.put("finishedAfter", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_FINISHED_AFTER));
    parameters.put("finishedBefore", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_FINISHED_BEFORE));

    return parameters;
  }

  private void verifyFinishedParameterQueryInvocations() {
    Map<String, Date> finishedDateParameters = getCompleteFinishedDateQueryParameters();

    verify(mockedQuery).finishedAfter(finishedDateParameters.get("finishedAfter"));
    verify(mockedQuery).finishedBefore(finishedDateParameters.get("finishedBefore"));

    verify(mockedQuery).list();
  }

  @Test
  public void testTenantIdListParameter() {
    mockedQuery = setUpMockHistoricActivityInstanceQuery(createMockHistoricActivityInstancesTwoTenants());

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> executions = from(content).getList("");
    assertThat(executions).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testTenantIdListPostParameter() {
    mockedQuery = setUpMockHistoricActivityInstanceQuery(createMockHistoricActivityInstancesTwoTenants());

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST.split(","));

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_ACTIVITY_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> executions = from(content).getList("");
    assertThat(executions).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  private List<HistoricActivityInstance> createMockHistoricActivityInstancesTwoTenants() {
    return Arrays.asList(
        MockProvider.createMockHistoricActivityInstance(MockProvider.EXAMPLE_TENANT_ID),
        MockProvider.createMockHistoricActivityInstance(MockProvider.ANOTHER_EXAMPLE_TENANT_ID));
  }
}
