package org.camunda.bpm.engine.rest.history;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response.Status;
import javax.xml.registry.InvalidRequestException;

import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractHistoricProcessInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String HISTORIC_PROCESS_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/process-instance";
  protected static final String HISTORIC_PROCESS_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_PROCESS_INSTANCE_RESOURCE_URL + "/count";

  protected HistoricProcessInstanceQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockHistoricProcessInstanceQuery(MockProvider.createMockHistoricProcessInstances());
  }

  private HistoricProcessInstanceQuery setUpMockHistoricProcessInstanceQuery(List<HistoricProcessInstance> mockedHistoricProcessInstances) {
    HistoricProcessInstanceQuery mockedhistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
    when(mockedhistoricProcessInstanceQuery.list()).thenReturn(mockedHistoricProcessInstances);
    when(mockedhistoricProcessInstanceQuery.count()).thenReturn((long) mockedHistoricProcessInstances.size());

    when(processEngine.getHistoryService().createHistoricProcessInstanceQuery()).thenReturn(mockedhistoricProcessInstanceQuery);

    return mockedhistoricProcessInstanceQuery;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("processDefinitionKey", queryKey)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testNoParametersQueryAsPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidVariableRequests() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_" + invalidComparator + "_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Invalid variable comparator specified: " + invalidComparator))
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    // invalid format
    queryValue = "invalidFormattedVariableQuery";

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("variable query parameter has to have format KEY_OPERATOR_VALUE"))
      .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("definitionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "definitionId")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
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
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("instanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("instanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
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
    executeAndVerifySorting("businessKey", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceBusinessKey();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("businessKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceBusinessKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("startTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceStartTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("startTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceStartTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceEndTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceEndTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceDuration();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceDuration();
    inOrder.verify(mockedQuery).desc();
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
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

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
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

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
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_COUNT_RESOURCE_URL);

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
        .post(HISTORIC_PROCESS_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testSimpleHistoricProcessQuery() {
    String processInstanceId = MockProvider.EXAMPLE_PROCESS_INSTANCE_ID;

    Response response = given()
        .queryParam("processInstanceId", processInstanceId)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).processInstanceId(processInstanceId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one process instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned process instance should not be null.", instances.get(0));

    String returnedProcessInstanceId = from(content).getString("[0].id");
    String returnedProcessInstanceBusinessKey = from(content).getString("[0].businessKey");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedStartTime = from(content).getString("[0].startTime");
    String returnedEndTime = from(content).getString("[0].endTime");
    long returnedDurationInMillis = from(content).getLong("[0].durationInMillis");
    String returnedStartUserId = from(content).getString("[0].startUserId");
    String returnedStartActivityId = from(content).getString("[0].startActivityId");
    String returnedDeleteReason = from(content).getString("[0].deleteReason");
    String returnedSuperProcessInstanceId = from(content).getString("[0].superProcessInstanceId");
    String returnedCaseInstanceId = from(content).getString("[0].caseInstanceId");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY, returnedProcessInstanceBusinessKey);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME.toString(), returnedStartTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_END_TIME.toString(), returnedEndTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS, returnedDurationInMillis);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_USER_ID, returnedStartUserId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_ACTIVITY_ID, returnedStartActivityId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON, returnedDeleteReason);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_PROCESS_INSTANCE_ID, returnedSuperProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_CASE_INSTANCE_ID, returnedCaseInstanceId);
  }

  @Test
  public void testAdditionalParametersExcludingProcesses() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .queryParams(stringQueryParameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  @Test
  public void testAdditionalParametersExcludingProcessesAsPost() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(stringQueryParameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  private Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    parameters.put("processInstanceBusinessKey", MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    parameters.put("processInstanceBusinessKeyLike", MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY_LIKE);
    parameters.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    parameters.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    parameters.put("processDefinitionName", MockProvider.EXAMPLE_PROCESS_DEFINITION_NAME);
    parameters.put("processDefinitionNameLike", MockProvider.EXAMPLE_PROCESS_DEFINITION_NAME_LIKE);
    parameters.put("startedBy", "startedBySomeone");
    parameters.put("superProcessInstanceId", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_PROCESS_INSTANCE_ID);
    parameters.put("subProcessInstanceId", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUB_PROCESS_INSTANCE_ID);
    parameters.put("caseInstanceId", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_CASE_INSTANCE_ID);

    return parameters;
  }

  private void verifyStringParameterQueryInvocations() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    verify(mockedQuery).processInstanceId(stringQueryParameters.get("processInstanceId"));
    verify(mockedQuery).processInstanceBusinessKey(stringQueryParameters.get("processInstanceBusinessKey"));
    verify(mockedQuery).processInstanceBusinessKeyLike(stringQueryParameters.get("processInstanceBusinessKeyLike"));
    verify(mockedQuery).processDefinitionId(stringQueryParameters.get("processDefinitionId"));
    verify(mockedQuery).processDefinitionKey(stringQueryParameters.get("processDefinitionKey"));
    verify(mockedQuery).processDefinitionName(stringQueryParameters.get("processDefinitionName"));
    verify(mockedQuery).processDefinitionNameLike(stringQueryParameters.get("processDefinitionNameLike"));
    verify(mockedQuery).startedBy(stringQueryParameters.get("startedBy"));
    verify(mockedQuery).superProcessInstanceId(stringQueryParameters.get("superProcessInstanceId"));
    verify(mockedQuery).subProcessInstanceId(stringQueryParameters.get("subProcessInstanceId"));
    verify(mockedQuery).caseInstanceId(stringQueryParameters.get("caseInstanceId"));

    verify(mockedQuery).list();
  }

  @Test
  public void testHistoricBeforeAndAfterStartTimeQuery() {
    given()
      .queryParam("startedBefore", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE)
      .queryParam("startedAfter", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

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
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyStartParameterQueryInvocations();
  }

  @Test
  public void testHistoricBeforeAndAfterStartTimeAsStringQueryAsPost() {
    Map<String, String> parameters = getCompleteStartDateAsStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyStringStartParameterQueryInvocations();
  }

  private Map<String, Date> getCompleteStartDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<String, Date>();

    parameters.put("startedAfter", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER));
    parameters.put("startedBefore", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE));

    return parameters;
  }

  private Map<String, String> getCompleteStartDateAsStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("startedAfter", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER);
    parameters.put("startedBefore", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE);

    return parameters;
  }

  private void verifyStartParameterQueryInvocations() {
    Map<String, Date> startDateParameters = getCompleteStartDateQueryParameters();

    verify(mockedQuery).startedBefore(startDateParameters.get("startedBefore"));
    verify(mockedQuery).startedAfter(startDateParameters.get("startedAfter"));

    verify(mockedQuery).list();
  }

  private void verifyStringStartParameterQueryInvocations() {
    Map<String, String> startDateParameters = getCompleteStartDateAsStringQueryParameters();

    verify(mockedQuery).startedBefore(DateTimeUtil.parseDate(startDateParameters.get("startedBefore")));
    verify(mockedQuery).startedAfter(DateTimeUtil.parseDate(startDateParameters.get("startedAfter")));

    verify(mockedQuery).list();
  }

  @Test
  public void testHistoricAfterAndBeforeFinishTimeQuery() {
    given()
      .queryParam("finishedAfter", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_AFTER)
      .queryParam("finishedBefore", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_BEFORE)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

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
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyFinishedParameterQueryInvocations();
  }

  @Test
  public void testHistoricAfterAndBeforeFinishTimeAsStringQueryAsPost() {
    Map<String, String> parameters = getCompleteFinishedDateAsStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyStringFinishedParameterQueryInvocations();
  }

  private Map<String, Date> getCompleteFinishedDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<String, Date>();

    parameters.put("finishedAfter", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_AFTER));
    parameters.put("finishedBefore", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_BEFORE));

    return parameters;
  }

  private Map<String, String> getCompleteFinishedDateAsStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("finishedAfter", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_AFTER);
    parameters.put("finishedBefore", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_BEFORE);

    return parameters;
  }

  private void verifyFinishedParameterQueryInvocations() {
    Map<String, Date> finishedDateParameters = getCompleteFinishedDateQueryParameters();

    verify(mockedQuery).finishedAfter(finishedDateParameters.get("finishedAfter"));
    verify(mockedQuery).finishedBefore(finishedDateParameters.get("finishedBefore"));

    verify(mockedQuery).list();
  }

  private void verifyStringFinishedParameterQueryInvocations() {
    Map<String, String> finishedDateParameters = getCompleteFinishedDateAsStringQueryParameters();

    verify(mockedQuery).finishedAfter(DateTimeUtil.parseDate(finishedDateParameters.get("finishedAfter")));
    verify(mockedQuery).finishedBefore(DateTimeUtil.parseDate(finishedDateParameters.get("finishedBefore")));

    verify(mockedQuery).list();
  }

  @Test
  public void testProcessQueryFinished() {
    given()
      .queryParam("finished", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).finished();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testProcessQueryFinishedAsPost() {
    Map<String, Boolean> body = new HashMap<String, Boolean>();
    body.put("finished", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).finished();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testProcessQueryUnfinished() {
    List<HistoricProcessInstance> mockedHistoricProcessInstances = MockProvider.createMockRunningHistoricProcessInstances();
    HistoricProcessInstanceQuery mockedhistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
    when(mockedhistoricProcessInstanceQuery.list()).thenReturn(mockedHistoricProcessInstances);
    when(processEngine.getHistoryService().createHistoricProcessInstanceQuery()).thenReturn(mockedhistoricProcessInstanceQuery);

    Response response = given()
        .queryParam("unfinished", true)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedhistoricProcessInstanceQuery);
    inOrder.verify(mockedhistoricProcessInstanceQuery).unfinished();
    inOrder.verify(mockedhistoricProcessInstanceQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one process instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned process instance should not be null.", instances.get(0));

    String returnedProcessInstanceId = from(content).getString("[0].id");
    String returnedEndTime = from(content).getString("[0].endTime");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(null, returnedEndTime);
  }

  @Test
  public void testProcessQueryUnfinishedAsPost() {
    List<HistoricProcessInstance> mockedHistoricProcessInstances = MockProvider.createMockRunningHistoricProcessInstances();
    HistoricProcessInstanceQuery mockedhistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
    when(mockedhistoricProcessInstanceQuery.list()).thenReturn(mockedHistoricProcessInstances);
    when(processEngine.getHistoryService().createHistoricProcessInstanceQuery()).thenReturn(mockedhistoricProcessInstanceQuery);

    Map<String, Boolean> body = new HashMap<String, Boolean>();
    body.put("unfinished", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(body)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedhistoricProcessInstanceQuery);
    inOrder.verify(mockedhistoricProcessInstanceQuery).unfinished();
    inOrder.verify(mockedhistoricProcessInstanceQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one process instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned process instance should not be null.", instances.get(0));

    String returnedProcessInstanceId = from(content).getString("[0].id");
    String returnedEndTime = from(content).getString("[0].endTime");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(null, returnedEndTime);
  }

  @Test
  public void testQueryByProcessInstanceIds() {
    given()
      .queryParam("processInstanceIds", "firstProcessInstanceId,secondProcessInstanceId")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyProcessInstanceIdSetInvovation();
  }

  @Test
  public void testQueryByProcessInstanceIdsAsPost() {
    Map<String, Set<String>> parameters = getCompleteProcessInstanceIdSetQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyProcessInstanceIdSetInvovation();
  }

  private Map<String, Set<String>> getCompleteProcessInstanceIdSetQueryParameters() {
    Map<String, Set<String>> parameters = new HashMap<String, Set<String>>();

    Set<String> processInstanceIds = new HashSet<String>();
    processInstanceIds.add("firstProcessInstanceId");
    processInstanceIds.add("secondProcessInstanceId");

    parameters.put("processInstanceIds", processInstanceIds);

    return parameters;
  }

  private void verifyProcessInstanceIdSetInvovation() {
    Map<String, Set<String>> parameters = getCompleteProcessInstanceIdSetQueryParameters();

    verify(mockedQuery).processInstanceIds(parameters.get("processInstanceIds"));
    verify(mockedQuery).list();
  }

  @Test
  public void testQueryByProcessDefinitionKeyNotIn() {
    given()
      .queryParam("processDefinitionKeyNotIn", "firstProcessInstanceKey,secondProcessInstanceKey")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyProcessDefinitionKeyNotInListInvovation();
  }

  @Test
  public void testQueryByProcessDefinitionKeyNotInAsPost() {
    Map<String, List<String>> parameters = getCompleteProcessDefinitionKeyNotInListQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyProcessDefinitionKeyNotInListInvovation();
  }

  private Map<String, List<String>> getCompleteProcessDefinitionKeyNotInListQueryParameters() {
    Map<String, List<String>> parameters = new HashMap<String, List<String>>();

    List<String> processInstanceIds = new ArrayList<String>();
    processInstanceIds.add("firstProcessInstanceKey");
    processInstanceIds.add("secondProcessInstanceKey");

    parameters.put("processDefinitionKeyNotIn", processInstanceIds);

    return parameters;
  }

  private void verifyProcessDefinitionKeyNotInListInvovation() {
    Map<String, List<String>> parameters = getCompleteProcessDefinitionKeyNotInListQueryParameters();

    verify(mockedQuery).processDefinitionKeyNotIn(parameters.get("processDefinitionKeyNotIn"));
    verify(mockedQuery).list();
  }

  @Test
  public void testVariableParameters() {
    // equals
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);

    // greater then
    queryValue = variableName + "_gt_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueGreaterThan(variableName, variableValue);

    // greater then equals
    queryValue = variableName + "_gteq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueGreaterThanOrEqual(variableName, variableValue);

    // lower then
    queryValue = variableName + "_lt_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLessThan(variableName, variableValue);

    // lower then equals
    queryValue = variableName + "_lteq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLessThanOrEqual(variableName, variableValue);

    // like
    queryValue = variableName + "_like_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLike(variableName, variableValue);

    // not equals
    queryValue = variableName + "_neq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
  }

  @Test
  public void testMultipleVariableParameters() {
    String variableName1 = "varName";
    String variableValue1 = "varValue";
    String variableParameter1 = variableName1 + "_eq_" + variableValue1;

    String variableName2 = "anotherVarName";
    String variableValue2 = "anotherVarValue";
    String variableParameter2 = variableName2 + "_neq_" + variableValue2;

    String queryValue = variableParameter1 + "," + variableParameter2;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueEquals(variableName1, variableValue1);
    verify(mockedQuery).variableValueNotEquals(variableName2, variableValue2);
  }

  @Test
  public void testMultipleVariableParametersAsPost() {
    String variableName = "varName";
    String variableValue = "varValue";
    String anotherVariableName = "anotherVarName";
    Integer anotherVariableValue = 30;

    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", variableName);
    variableJson.put("operator", "eq");
    variableJson.put("value", variableValue);

    Map<String, Object> anotherVariableJson = new HashMap<String, Object>();
    anotherVariableJson.put("name", anotherVariableName);
    anotherVariableJson.put("operator", "neq");
    anotherVariableJson.put("value", anotherVariableValue);

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);
    variables.add(anotherVariableJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).variableValueNotEquals(anotherVariableName, anotherVariableValue);
  }

}
