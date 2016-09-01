package org.camunda.bpm.engine.rest.history;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstanceQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
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

public class HistoricCaseInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_CASE_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/case-instance";
  protected static final String HISTORIC_CASE_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_CASE_INSTANCE_RESOURCE_URL + "/count";

  protected HistoricCaseInstanceQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockHistoricCaseInstanceQuery(MockProvider.createMockHistoricCaseInstances());
  }

  protected HistoricCaseInstanceQuery setUpMockHistoricCaseInstanceQuery(List<HistoricCaseInstance> mockedHistoricCaseInstances) {
    HistoricCaseInstanceQuery mockedHistoricCaseInstanceQuery = mock(HistoricCaseInstanceQuery.class);
    when(mockedHistoricCaseInstanceQuery.list()).thenReturn(mockedHistoricCaseInstances);
    when(mockedHistoricCaseInstanceQuery.count()).thenReturn((long) mockedHistoricCaseInstances.size());

    when(processEngine.getHistoryService().createHistoricCaseInstanceQuery()).thenReturn(mockedHistoricCaseInstanceQuery);

    return mockedHistoricCaseInstanceQuery;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("caseDefinitionKey", queryKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testNoParametersQueryAsPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("definitionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "definitionId")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("instanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("instanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("businessKey", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceBusinessKey();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("businessKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceBusinessKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("createTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceCreateTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("createTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceCreateTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("closeTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceCloseTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("closeTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceCloseTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceDuration();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceDuration();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).desc();
  }

  @Test
  public void testSecondarySortingAsPost() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("sorting", OrderingBuilder.create()
      .orderBy("businessKey").desc()
      .orderBy("closeTime").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    inOrder.verify(mockedQuery).orderByCaseInstanceBusinessKey();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByCaseInstanceCloseTime();
    inOrder.verify(mockedQuery).asc();
  }

  @Test
  public void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_CASE_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testQueryCountForPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .body("count", equalTo(1))
    .when()
      .post(HISTORIC_CASE_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testSimpleHistoricCaseQuery() {
    String caseInstanceId = MockProvider.EXAMPLE_CASE_INSTANCE_ID;

    Response response = given()
        .queryParam("caseInstanceId", caseInstanceId)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).caseInstanceId(caseInstanceId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals(1, instances.size());
    Assert.assertNotNull(instances.get(0));

    String returnedCaseInstanceId = from(content).getString("[0].id");
    String returnedCaseInstanceBusinessKey = from(content).getString("[0].businessKey");
    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedCaseDefinitionKey = from(content).getString("[0].caseDefinitionKey");
    String returnedCaseDefinitionName = from(content).getString("[0].caseDefinitionName");
    String returnedCreateTime = from(content).getString("[0].createTime");
    String returnedCloseTime = from(content).getString("[0].closeTime");
    long returnedDurationInMillis = from(content).getLong("[0].durationInMillis");
    String returnedCreateUserId = from(content).getString("[0].createUserId");
    String returnedSuperCaseInstanceId = from(content).getString("[0].superCaseInstanceId");
    String returnedSuperProcessInstanceId = from(content).getString("[0].superProcessInstanceId");
    String returnedTenantId = from(content).getString("[0].tenantId");
    boolean active = from(content).getBoolean("[0].active");
    boolean completed = from(content).getBoolean("[0].completed");
    boolean terminated = from(content).getBoolean("[0].terminated");
    boolean closed = from(content).getBoolean("[0].closed");

    Assert.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY, returnedCaseInstanceBusinessKey);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_ID, returnedCaseDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_KEY, returnedCaseDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_NAME, returnedCaseDefinitionName);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_TIME, returnedCreateTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSE_TIME, returnedCloseTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_DURATION_MILLIS, returnedDurationInMillis);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_USER_ID, returnedCreateUserId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_CASE_INSTANCE_ID, returnedSuperCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_PROCESS_INSTANCE_ID, returnedSuperProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_ACTIVE, active);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_COMPLETED, completed);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_TERMINATED, terminated);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_CLOSED, closed);
  }

  @Test
  public void testAdditionalParametersExcludingCases() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .queryParams(stringQueryParameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  @Test
  public void testAdditionalParametersExcludingCasesAsPost() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(stringQueryParameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  @Test
  public void testHistoricBeforeAndAfterCreateTimeQuery() {
    given()
      .queryParam("createdBefore", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_BEFORE)
      .queryParam("createdAfter", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_AFTER)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCreateParameterQueryInvocations();
  }

  @Test
  public void testHistoricBeforeAndAfterCreateTimeQueryAsPost() {
    Map<String, Date> parameters = getCompleteCreateDateQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCreateParameterQueryInvocations();
  }

  @Test
  public void testHistoricBeforeAndAfterCreateTimeAsStringQueryAsPost() {
    Map<String, String> parameters = getCompleteCreateDateAsStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyStringCreateParameterQueryInvocations();
  }

  @Test
  public void testHistoricAfterAndBeforeCloseTimeQuery() {
    given()
      .queryParam("closedAfter", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_AFTER)
      .queryParam("closedBefore", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_BEFORE)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyClosedParameterQueryInvocations();
  }

  @Test
  public void testHistoricAfterAndBeforeCloseTimeQueryAsPost() {
    Map<String, Date> parameters = getCompleteClosedDateQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyClosedParameterQueryInvocations();
  }

  @Test
  public void testHistoricAfterAndBeforeCloseTimeAsStringQueryAsPost() {
    Map<String, String> parameters = getCompleteClosedDateAsStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyStringClosedParameterQueryInvocations();
  }

  @Test
  public void testCaseActiveClosed() {
    given()
      .queryParam("active", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).active();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testCaseQueryActiveAsPost() {
    Map<String, Boolean> body = new HashMap<String, Boolean>();
    body.put("active", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).active();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testCaseQueryCompleted() {
    given()
      .queryParam("completed", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).completed();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testCaseQueryCompletedAsPost() {
    Map<String, Boolean> body = new HashMap<String, Boolean>();
    body.put("completed", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).completed();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testCaseQueryTerminated() {
    given()
      .queryParam("terminated", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).terminated();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testCaseQueryTerminatedAsPost() {
    Map<String, Boolean> body = new HashMap<String, Boolean>();
    body.put("terminated", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).terminated();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testCaseQueryClosed() {
    given()
      .queryParam("closed", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).closed();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testCaseQueryClosedAsPost() {
    Map<String, Boolean> body = new HashMap<String, Boolean>();
    body.put("closed", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).closed();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testCaseQueryNotClosed() {
    List<HistoricCaseInstance> mockedHistoricCaseInstances = MockProvider.createMockRunningHistoricCaseInstances();
    HistoricCaseInstanceQuery mockedHistoricCaseInstanceQuery = mock(HistoricCaseInstanceQuery.class);
    when(mockedHistoricCaseInstanceQuery.list()).thenReturn(mockedHistoricCaseInstances);
    when(processEngine.getHistoryService().createHistoricCaseInstanceQuery()).thenReturn(mockedHistoricCaseInstanceQuery);

    Response response = given()
        .queryParam("notClosed", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedHistoricCaseInstanceQuery);
    inOrder.verify(mockedHistoricCaseInstanceQuery).notClosed();
    inOrder.verify(mockedHistoricCaseInstanceQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals(1, instances.size());
    Assert.assertNotNull(instances.get(0));

    String returnedCaseInstanceId = from(content).getString("[0].id");
    String returnedCloseTime = from(content).getString("[0].closeTime");

    Assert.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assert.assertEquals(null, returnedCloseTime);
  }

  @Test
  public void testCaseQueryNotClosedAsPost() {
    List<HistoricCaseInstance> mockedHistoricCaseInstances = MockProvider.createMockRunningHistoricCaseInstances();
    HistoricCaseInstanceQuery mockedHistoricCaseInstanceQuery = mock(HistoricCaseInstanceQuery.class);
    when(mockedHistoricCaseInstanceQuery.list()).thenReturn(mockedHistoricCaseInstances);
    when(processEngine.getHistoryService().createHistoricCaseInstanceQuery()).thenReturn(mockedHistoricCaseInstanceQuery);

    Map<String, Boolean> body = new HashMap<String, Boolean>();
    body.put("notClosed", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(body)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedHistoricCaseInstanceQuery);
    inOrder.verify(mockedHistoricCaseInstanceQuery).notClosed();
    inOrder.verify(mockedHistoricCaseInstanceQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals(1, instances.size());
    Assert.assertNotNull(instances.get(0));

    String returnedCaseInstanceId = from(content).getString("[0].id");
    String returnedCloseTime = from(content).getString("[0].closeTime");

    Assert.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assert.assertEquals(null, returnedCloseTime);
  }

  @Test
  public void testQueryByCaseInstanceIds() {
    given()
      .queryParam("caseInstanceIds", "firstCaseInstanceId,secondCaseInstanceId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCaseInstanceIdSetInvocation();
  }

  @Test
  public void testQueryByCaseInstanceIdsAsPost() {
    Map<String, Set<String>> parameters = getCompleteCaseInstanceIdSetQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCaseInstanceIdSetInvocation();
  }

  @Test
  public void testQueryByCaseDefinitionKeyNotIn() {
    given()
      .queryParam("caseDefinitionKeyNotIn", "firstCaseInstanceKey,secondCaseInstanceKey")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCaseDefinitionKeyNotInListInvocation();
  }

  @Test
  public void testQueryByCaseDefinitionKeyNotInAsPost() {
    Map<String, List<String>> parameters = getCompleteCaseDefinitionKeyNotInListQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCaseDefinitionKeyNotInListInvocation();
  }

  @Test
  public void testVariableParameters() {
    // equals
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);

    // greater then
    queryValue = variableName + "_gt_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueGreaterThan(variableName, variableValue);

    // greater then equals
    queryValue = variableName + "_gteq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueGreaterThanOrEqual(variableName, variableValue);

    // lower then
    queryValue = variableName + "_lt_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLessThan(variableName, variableValue);

    // lower then equals
    queryValue = variableName + "_lteq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLessThanOrEqual(variableName, variableValue);

    // like
    queryValue = variableName + "_like_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLike(variableName, variableValue);

    // not equals
    queryValue = variableName + "_neq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

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
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

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
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).variableValueNotEquals(eq(anotherVariableName), argThat(EqualsPrimitiveValue.numberValue(anotherVariableValue)));
  }

  @Test
  public void testTenantIdListParameter() {
    mockedQuery = setUpMockHistoricCaseInstanceQuery(createMockHistoricCaseInstancesTwoTenants());

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> historicCaseInstances = from(content).getList("");
    assertThat(historicCaseInstances).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testTenantIdListPostParameter() {
    mockedQuery = setUpMockHistoricCaseInstanceQuery(createMockHistoricCaseInstancesTwoTenants());

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST.split(","));

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> historicCaseInstances = from(content).getList("");
    assertThat(historicCaseInstances).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testWithoutTenantIdParameter() {
    mockedQuery = setUpMockHistoricCaseInstanceQuery(Arrays.asList(MockProvider.createMockHistoricCaseInstance(null)));

    Response response = given()
      .queryParam("withoutTenantId", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId1).isEqualTo(null);
  }

  @Test
  public void testWithoutTenantIdPostParameter() {
    mockedQuery = setUpMockHistoricCaseInstanceQuery(Arrays.asList(MockProvider.createMockHistoricCaseInstance(null)));

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("withoutTenantId", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId1).isEqualTo(null);
  }

  private List<HistoricCaseInstance> createMockHistoricCaseInstancesTwoTenants() {
    return Arrays.asList(
        MockProvider.createMockHistoricCaseInstance(MockProvider.EXAMPLE_TENANT_ID),
        MockProvider.createMockHistoricCaseInstance(MockProvider.ANOTHER_EXAMPLE_TENANT_ID));
  }

  @Test
  public void testCaseActivityIdListParameter() {

    Response response = given()
      .queryParam("caseActivityIdIn", MockProvider.EXAMPLE_CASE_ACTIVITY_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseActivityIdIn(MockProvider.EXAMPLE_CASE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_CASE_ACTIVITY_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> historicCaseInstances = from(content).getList("");
    assertThat(historicCaseInstances).hasSize(1);
  }

  @Test
  public void testCaseActivityIdListPostParameter() {

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("caseActivityIdIn", MockProvider.EXAMPLE_CASE_ACTIVITY_ID_LIST.split(","));

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseActivityIdIn(MockProvider.EXAMPLE_CASE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_CASE_ACTIVITY_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> historicCaseInstances = from(content).getList("");
    assertThat(historicCaseInstances).hasSize(1);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
      .then().expect()
      .statusCode(expectedStatus.getStatusCode())
      .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
  }

  protected Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("caseInstanceId", MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    parameters.put("caseInstanceBusinessKey", MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);
    parameters.put("caseInstanceBusinessKeyLike", MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY_LIKE);
    parameters.put("caseDefinitionId", MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    parameters.put("caseDefinitionKey", MockProvider.EXAMPLE_CASE_DEFINITION_KEY);
    parameters.put("caseDefinitionName", MockProvider.EXAMPLE_CASE_DEFINITION_NAME);
    parameters.put("caseDefinitionNameLike", MockProvider.EXAMPLE_CASE_DEFINITION_NAME_LIKE);
    parameters.put("createdBy", "createdBySomeone");
    parameters.put("superCaseInstanceId", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_CASE_INSTANCE_ID);
    parameters.put("subCaseInstanceId", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUB_CASE_INSTANCE_ID);
    parameters.put("superProcessInstanceId", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_PROCESS_INSTANCE_ID);
    parameters.put("subProcessInstanceId", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUB_PROCESS_INSTANCE_ID);

    return parameters;
  }

  protected void verifyStringParameterQueryInvocations() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    verify(mockedQuery).caseInstanceId(stringQueryParameters.get("caseInstanceId"));
    verify(mockedQuery).caseInstanceBusinessKey(stringQueryParameters.get("caseInstanceBusinessKey"));
    verify(mockedQuery).caseInstanceBusinessKeyLike(stringQueryParameters.get("caseInstanceBusinessKeyLike"));
    verify(mockedQuery).caseDefinitionId(stringQueryParameters.get("caseDefinitionId"));
    verify(mockedQuery).caseDefinitionKey(stringQueryParameters.get("caseDefinitionKey"));
    verify(mockedQuery).caseDefinitionName(stringQueryParameters.get("caseDefinitionName"));
    verify(mockedQuery).caseDefinitionNameLike(stringQueryParameters.get("caseDefinitionNameLike"));
    verify(mockedQuery).createdBy(stringQueryParameters.get("createdBy"));
    verify(mockedQuery).superCaseInstanceId(stringQueryParameters.get("superCaseInstanceId"));
    verify(mockedQuery).subCaseInstanceId(stringQueryParameters.get("subCaseInstanceId"));
    verify(mockedQuery).superProcessInstanceId(stringQueryParameters.get("superProcessInstanceId"));
    verify(mockedQuery).subProcessInstanceId(stringQueryParameters.get("subProcessInstanceId"));
    verify(mockedQuery).caseInstanceId(stringQueryParameters.get("caseInstanceId"));

    verify(mockedQuery).list();
  }

  protected Map<String, Date> getCompleteCreateDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<String, Date>();

    parameters.put("createdAfter", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_AFTER));
    parameters.put("createdBefore", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_BEFORE));

    return parameters;
  }

  protected void verifyCreateParameterQueryInvocations() {
    Map<String, Date> createDateParameters = getCompleteCreateDateQueryParameters();

    verify(mockedQuery).createdBefore(createDateParameters.get("createdBefore"));
    verify(mockedQuery).createdAfter(createDateParameters.get("createdAfter"));

    verify(mockedQuery).list();
  }

  protected Map<String, String> getCompleteCreateDateAsStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("createdAfter", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_AFTER);
    parameters.put("createdBefore", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_BEFORE);

    return parameters;
  }

  protected void verifyStringCreateParameterQueryInvocations() {
    Map<String, String> createDateParameters = getCompleteCreateDateAsStringQueryParameters();

    verify(mockedQuery).createdBefore(DateTimeUtil.parseDate(createDateParameters.get("createdBefore")));
    verify(mockedQuery).createdAfter(DateTimeUtil.parseDate(createDateParameters.get("createdAfter")));

    verify(mockedQuery).list();
  }

  protected Map<String, Date> getCompleteClosedDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<String, Date>();

    parameters.put("closedAfter", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_AFTER));
    parameters.put("closedBefore", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_BEFORE));

    return parameters;
  }

  protected void verifyClosedParameterQueryInvocations() {
    Map<String, Date> closedDateParameters = getCompleteClosedDateQueryParameters();

    verify(mockedQuery).closedAfter(closedDateParameters.get("closedAfter"));
    verify(mockedQuery).closedBefore(closedDateParameters.get("closedBefore"));

    verify(mockedQuery).list();
  }

  protected Map<String, String> getCompleteClosedDateAsStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("closedAfter", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_AFTER);
    parameters.put("closedBefore", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_BEFORE);

    return parameters;
  }

  protected void verifyStringClosedParameterQueryInvocations() {
    Map<String, String> closedDateParameters = getCompleteClosedDateAsStringQueryParameters();

    verify(mockedQuery).closedAfter(DateTimeUtil.parseDate(closedDateParameters.get("closedAfter")));
    verify(mockedQuery).closedBefore(DateTimeUtil.parseDate(closedDateParameters.get("closedBefore")));

    verify(mockedQuery).list();
  }

  protected Map<String, Set<String>> getCompleteCaseInstanceIdSetQueryParameters() {
    Map<String, Set<String>> parameters = new HashMap<String, Set<String>>();

    Set<String> caseInstanceIds = new HashSet<String>();
    caseInstanceIds.add("firstCaseInstanceId");
    caseInstanceIds.add("secondCaseInstanceId");

    parameters.put("caseInstanceIds", caseInstanceIds);

    return parameters;
  }

  protected void verifyCaseInstanceIdSetInvocation() {
    Map<String, Set<String>> parameters = getCompleteCaseInstanceIdSetQueryParameters();

    verify(mockedQuery).caseInstanceIds(parameters.get("caseInstanceIds"));
    verify(mockedQuery).list();
  }

  protected Map<String, List<String>> getCompleteCaseDefinitionKeyNotInListQueryParameters() {
    Map<String, List<String>> parameters = new HashMap<String, List<String>>();

    List<String> caseInstanceIds = new ArrayList<String>();
    caseInstanceIds.add("firstCaseInstanceKey");
    caseInstanceIds.add("secondCaseInstanceKey");

    parameters.put("caseDefinitionKeyNotIn", caseInstanceIds);

    return parameters;
  }

  protected void verifyCaseDefinitionKeyNotInListInvocation() {
    Map<String, List<String>> parameters = getCompleteCaseDefinitionKeyNotInListQueryParameters();

    verify(mockedQuery).caseDefinitionKeyNotIn(parameters.get("caseDefinitionKeyNotIn"));
    verify(mockedQuery).list();
  }

}
