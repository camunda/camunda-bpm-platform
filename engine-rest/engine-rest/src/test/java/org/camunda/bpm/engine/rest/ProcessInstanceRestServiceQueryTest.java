package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.withTimezone;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.camunda.bpm.engine.rest.util.OrderingBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class ProcessInstanceRestServiceQueryTest extends
    AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String PROCESS_INSTANCE_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance";
  protected static final String PROCESS_INSTANCE_COUNT_QUERY_URL = PROCESS_INSTANCE_QUERY_URL + "/count";
  protected ProcessInstanceQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockInstanceQuery(createMockInstanceList());
  }

  private ProcessInstanceQuery setUpMockInstanceQuery(List<ProcessInstance> mockedInstances) {
    ProcessInstanceQuery sampleInstanceQuery = mock(ProcessInstanceQuery.class);
    when(sampleInstanceQuery.list()).thenReturn(mockedInstances);
    when(sampleInstanceQuery.count()).thenReturn((long) mockedInstances.size());
    when(processEngine.getRuntimeService().createProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    return sampleInstanceQuery;
  }

  private List<ProcessInstance> createMockInstanceList() {
    List<ProcessInstance> mocks = new ArrayList<ProcessInstance>();

    mocks.add(MockProvider.createMockInstance());
    return mocks;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given().queryParam("processDefinitionKey", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }

  @Test
  public void testInvalidVariableRequests() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_" + invalidComparator + "_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Invalid variable comparator specified: " + invalidComparator))
      .when().get(PROCESS_INSTANCE_QUERY_URL);

    // invalid format
    queryValue = "invalidFormattedVariableQuery";
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("variable query parameter has to have format KEY_OPERATOR_VALUE"))
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("definitionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
      .then().expect().statusCode(expectedStatus.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "definitionId")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
  }

  @Test
  public void testInstanceRetrieval() {
    String queryKey = "key";
    Response response = given().queryParam("processDefinitionKey", queryKey)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(PROCESS_INSTANCE_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).processDefinitionKey(queryKey);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one process definition returned.", 1, instances.size());
    Assert.assertNotNull("There should be one process definition returned", instances.get(0));

    String returnedInstanceId = from(content).getString("[0].id");
    Boolean returnedIsEnded = from(content).getBoolean("[0].ended");
    String returnedDefinitionId = from(content).getString("[0].definitionId");
    String returnedBusinessKey = from(content).getString("[0].businessKey");
    Boolean returnedIsSuspended = from(content).getBoolean("[0].suspended");
    String returnedCaseInstanceId = from(content).getString("[0].caseInstanceId");
    String returnedTenantId = from(content).getString("[0].tenantId");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED, returnedIsEnded);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY, returnedBusinessKey);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED, returnedIsSuspended);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
  }

  @Test
  public void testIncompleteProcessInstance() {
    setUpMockInstanceQuery(createIncompleteMockInstances());
    Response response = expect().statusCode(Status.OK.getStatusCode())
        .when().get(PROCESS_INSTANCE_QUERY_URL);

    String content = response.asString();
    String returnedBusinessKey = from(content).getString("[0].businessKey");
    Assert.assertNull("Should be null, as it is also null in the original process instance on the server.",
        returnedBusinessKey);
  }

  private List<ProcessInstance> createIncompleteMockInstances() {
    List<ProcessInstance> mocks = new ArrayList<ProcessInstance>();
    ProcessInstance mockInstance = mock(ProcessInstance.class);
    when(mockInstance.getId()).thenReturn(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);

    mocks.add(mockInstance);
    return mocks;
  }

  @Test
  public void testNoParametersQuery() {
    expect().statusCode(Status.OK.getStatusCode()).when().get(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testAdditionalParametersExcludingVariables() {
    Map<String, String> queryParameters = getCompleteQueryParameters();

    given().queryParams(queryParameters)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).caseInstanceId(queryParameters.get("caseInstanceId"));
    verify(mockedQuery).processInstanceBusinessKey(queryParameters.get("businessKey"));
    verify(mockedQuery).processInstanceBusinessKeyLike(queryParameters.get("businessKeyLike"));
    verify(mockedQuery).processDefinitionKey(queryParameters.get("processDefinitionKey"));
    verify(mockedQuery).processDefinitionId(queryParameters.get("processDefinitionId"));
    verify(mockedQuery).deploymentId(queryParameters.get("deploymentId"));
    verify(mockedQuery).superProcessInstanceId(queryParameters.get("superProcessInstance"));
    verify(mockedQuery).subProcessInstanceId(queryParameters.get("subProcessInstance"));
    verify(mockedQuery).superCaseInstanceId(queryParameters.get("superCaseInstance"));
    verify(mockedQuery).subCaseInstanceId(queryParameters.get("subCaseInstance"));
    verify(mockedQuery).suspended();
    verify(mockedQuery).active();
    verify(mockedQuery).incidentId(queryParameters.get("incidentId"));
    verify(mockedQuery).incidentMessage(queryParameters.get("incidentMessage"));
    verify(mockedQuery).incidentMessageLike(queryParameters.get("incidentMessageLike"));
    verify(mockedQuery).incidentType(queryParameters.get("incidentType"));
    verify(mockedQuery).list();
  }

  private Map<String, String> getCompleteQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("businessKey", "aBusinessKey");
    parameters.put("businessKeyLike", "aKeyLike");
    parameters.put("processDefinitionKey", "aProcDefKey");
    parameters.put("processDefinitionId", "aProcDefId");
    parameters.put("deploymentId", "deploymentId");
    parameters.put("superProcessInstance", "aSuperProcInstId");
    parameters.put("subProcessInstance", "aSubProcInstId");
    parameters.put("superCaseInstance", "aSuperCaseInstId");
    parameters.put("subCaseInstance", "aSubCaseInstId");
    parameters.put("suspended", "true");
    parameters.put("active", "true");
    parameters.put("incidentId", "incId");
    parameters.put("incidentMessage", "incMessage");
    parameters.put("incidentMessageLike", "incMessageLike");
    parameters.put("incidentType", "incType");
    parameters.put("caseInstanceId", "aCaseInstanceId");

    return parameters;
  }

  @Test
  public void testVariableParameters() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueEquals(variableName, variableValue);

    queryValue = variableName + "_gt_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueGreaterThan(variableName, variableValue);

    queryValue = variableName + "_gteq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueGreaterThanOrEqual(variableName, variableValue);

    queryValue = variableName + "_lt_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueLessThan(variableName, variableValue);

    queryValue = variableName + "_lteq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueLessThanOrEqual(variableName, variableValue);

    queryValue = variableName + "_like_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueLike(variableName, variableValue);

    queryValue = variableName + "_neq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);
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

    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);

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

    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).variableValueNotEquals(eq(anotherVariableName), argThat(EqualsPrimitiveValue.numberValue(anotherVariableValue)));

  }

  @Test
  public void testDateVariableParameter() {
    String variableName = "varName";
    String variableValue = withTimezone("2014-06-16T10:00:00");
    String queryValue = variableName + "_eq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(PROCESS_INSTANCE_QUERY_URL);

    Date date = DateTimeUtil.parseDate(variableValue);

    verify(mockedQuery).variableValueEquals(variableName, date);
  }

  @Test
  public void testDateVariableParameterAsPost() {
    String variableName = "varName";
    String variableValue = withTimezone("2014-06-16T10:00:00");

    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", variableName);
    variableJson.put("operator", "eq");
    variableJson.put("value", variableValue);

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(PROCESS_INSTANCE_QUERY_URL);

    Date date = DateTimeUtil.parseDate(variableValue);

    verify(mockedQuery).variableValueEquals(variableName, date);
  }

  @Test
  public void testCompletePostParameters() {
    Map<String, String> queryParameters = getCompleteQueryParameters();

    given().contentType(POST_JSON_CONTENT_TYPE).body(queryParameters)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().post(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).caseInstanceId(queryParameters.get("caseInstanceId"));
    verify(mockedQuery).processInstanceBusinessKey(queryParameters.get("businessKey"));
    verify(mockedQuery).processInstanceBusinessKeyLike(queryParameters.get("businessKeyLike"));
    verify(mockedQuery).processDefinitionKey(queryParameters.get("processDefinitionKey"));
    verify(mockedQuery).processDefinitionId(queryParameters.get("processDefinitionId"));
    verify(mockedQuery).deploymentId(queryParameters.get("deploymentId"));
    verify(mockedQuery).superProcessInstanceId(queryParameters.get("superProcessInstance"));
    verify(mockedQuery).subProcessInstanceId(queryParameters.get("subProcessInstance"));
    verify(mockedQuery).superCaseInstanceId(queryParameters.get("superCaseInstance"));
    verify(mockedQuery).subCaseInstanceId(queryParameters.get("subCaseInstance"));
    verify(mockedQuery).suspended();
    verify(mockedQuery).active();
    verify(mockedQuery).incidentId(queryParameters.get("incidentId"));
    verify(mockedQuery).incidentMessage(queryParameters.get("incidentMessage"));
    verify(mockedQuery).incidentMessageLike(queryParameters.get("incidentMessageLike"));
    verify(mockedQuery).incidentType(queryParameters.get("incidentType"));
    verify(mockedQuery).list();
  }

  @Test
  public void testTenantIdListParameter() {
    mockedQuery = setUpMockInstanceQuery(createMockProcessInstancesTwoTenants());

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    assertThat(instances).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  public void testWithoutTenantIdParameter() {
    mockedQuery = setUpMockInstanceQuery(Arrays.asList(MockProvider.createMockInstance(null)));

    Response response = given()
      .queryParam("withoutTenantId", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId1).isEqualTo(null);
  }

  @Test
  public void testTenantIdListPostParameter() {
    mockedQuery = setUpMockInstanceQuery(createMockProcessInstancesTwoTenants());

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST.split(","));

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(PROCESS_INSTANCE_QUERY_URL);

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
  public void testWithoutTenantIdPostParameter() {
    mockedQuery = setUpMockInstanceQuery(Arrays.asList(MockProvider.createMockInstance(null)));

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("withoutTenantId", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId1).isEqualTo(null);
  }

  private List<ProcessInstance> createMockProcessInstancesTwoTenants() {
    return Arrays.asList(
        MockProvider.createMockInstance(MockProvider.EXAMPLE_TENANT_ID),
        MockProvider.createMockInstance(MockProvider.ANOTHER_EXAMPLE_TENANT_ID));
  }

  @Test
  public void testActivityIdListParameter() {
    given()
      .queryParam("activityIdIn", MockProvider.EXAMPLE_ACTIVITY_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).activityIdIn(MockProvider.EXAMPLE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID);
    verify(mockedQuery).list();
  }

  @Test
  public void testActivityIdListPostParameter() {
    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("activityIdIn", MockProvider.EXAMPLE_ACTIVITY_ID_LIST.split(","));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(queryParameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).activityIdIn(MockProvider.EXAMPLE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID);
    verify(mockedQuery).list();
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("instanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("businessKey", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByBusinessKey();
    inOrder.verify(mockedQuery).asc();
  }

  @Test
  public void testSecondarySortingAsPost() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("sorting", OrderingBuilder.create()
      .orderBy("definitionKey").desc()
      .orderBy("definitionId").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(PROCESS_INSTANCE_QUERY_URL);

    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).asc();
  }

  @Test
  public void testSuccessfulPagination() {

    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  /**
   * If parameter "firstResult" is missing, we expect 0 as default.
   */
  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;
    given().queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  /**
   * If parameter "maxResults" is missing, we expect Integer.MAX_VALUE as default.
   */
  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;
    given().queryParam("firstResult", firstResult)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(PROCESS_INSTANCE_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testQueryCountForPost() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().post(PROCESS_INSTANCE_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testInstanceRetrievalByList() {
    List<ProcessInstance> mockProcessInstanceList = new ArrayList<ProcessInstance>();

    mockProcessInstanceList.add(MockProvider.createMockInstance());
    mockProcessInstanceList.add(MockProvider.createAnotherMockInstance());

    ProcessInstanceQuery instanceQuery = mock(ProcessInstanceQuery.class);

    when(processEngine.getRuntimeService().createProcessInstanceQuery()).thenReturn(instanceQuery);
    when(instanceQuery.list()).thenReturn(mockProcessInstanceList);

    Response response = given()
        .queryParam("processInstanceIds", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID_LIST)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(PROCESS_INSTANCE_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(instanceQuery);
    Set<String> expectedSet = MockProvider.createMockSetFromList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID_LIST);

    inOrder.verify(instanceQuery).processInstanceIds(expectedSet);
    inOrder.verify(instanceQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be two process definitions returned.", 2, instances.size());

    String returnedInstanceId1 = from(content).getString("[0].id");
    String returnedInstanceId2 = from(content).getString("[1].id");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedInstanceId1);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID, returnedInstanceId2);
  }

  @Test
  public void testInstanceRetrievalByListAsPost() {
    List<ProcessInstance> mockProcessInstanceList = new ArrayList<ProcessInstance>();

    mockProcessInstanceList.add(MockProvider.createMockInstance());
    mockProcessInstanceList.add(MockProvider.createAnotherMockInstance());

    ProcessInstanceQuery instanceQuery = mock(ProcessInstanceQuery.class);

    when(processEngine.getRuntimeService().createProcessInstanceQuery()).thenReturn(instanceQuery);
    when(instanceQuery.list()).thenReturn(mockProcessInstanceList);

    Map<String, Set<String>> params = new HashMap<String, Set<String>>();
    Set<String> processInstanceIds = MockProvider.createMockSetFromList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID_LIST);
    params.put("processInstanceIds", processInstanceIds);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(params)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(PROCESS_INSTANCE_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(instanceQuery);

    inOrder.verify(instanceQuery).processInstanceIds(processInstanceIds);
    inOrder.verify(instanceQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be two process definitions returned.", 2, instances.size());

    String returnedInstanceId1 = from(content).getString("[0].id");
    String returnedInstanceId2 = from(content).getString("[1].id");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedInstanceId1);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID, returnedInstanceId2);
  }

  @Test
  public void testInstanceRetrievalByListWithDuplicate() {
    List<ProcessInstance> mockProcessInstanceList = new ArrayList<ProcessInstance>();

    mockProcessInstanceList.add(MockProvider.createMockInstance());
    mockProcessInstanceList.add(MockProvider.createAnotherMockInstance());

    ProcessInstanceQuery instanceQuery = mock(ProcessInstanceQuery.class);

    when(instanceQuery.list()).thenReturn(mockProcessInstanceList);
    when(processEngine.getRuntimeService().createProcessInstanceQuery()).thenReturn(instanceQuery);

    Response response = given()
        .queryParam("processInstanceIds", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID_LIST_WITH_DUP)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(PROCESS_INSTANCE_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(instanceQuery);
    Set<String> expectedSet = MockProvider.createMockSetFromList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID_LIST);

    inOrder.verify(instanceQuery).processInstanceIds(expectedSet);
    inOrder.verify(instanceQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be two process definitions returned.", 2, instances.size());

    String returnedInstanceId1 = from(content).getString("[0].id");
    String returnedInstanceId2 = from(content).getString("[1].id");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedInstanceId1);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID, returnedInstanceId2);
  }

  @Test
  public void testInstanceRetrievalByListWithDuplicateAsPost() {
    List<ProcessInstance> mockProcessInstanceList = new ArrayList<ProcessInstance>();

    mockProcessInstanceList.add(MockProvider.createMockInstance());
    mockProcessInstanceList.add(MockProvider.createAnotherMockInstance());

    ProcessInstanceQuery instanceQuery = mock(ProcessInstanceQuery.class);

    when(instanceQuery.list()).thenReturn(mockProcessInstanceList);
    when(processEngine.getRuntimeService().createProcessInstanceQuery()).thenReturn(instanceQuery);

    Map<String, Set<String>> params = new HashMap<String, Set<String>>();
    Set<String> processInstanceIds = MockProvider.createMockSetFromList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID_LIST);
    params.put("processInstanceIds", processInstanceIds);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(params)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(PROCESS_INSTANCE_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(instanceQuery);
    inOrder.verify(instanceQuery).processInstanceIds(processInstanceIds);
    inOrder.verify(instanceQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be two process definitions returned.", 2, instances.size());

    String returnedInstanceId1 = from(content).getString("[0].id");
    String returnedInstanceId2 = from(content).getString("[1].id");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedInstanceId1);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID, returnedInstanceId2);
  }

  @Test
  public void testInstanceRetrievalByListWithEmpty() {
    ProcessInstanceQuery instanceQuery = mock(ProcessInstanceQuery.class);

    when(instanceQuery.list()).thenReturn(null);
    String expectedExceptionMessage = "Set of process instance ids is empty";
    doThrow(new ProcessEngineException(expectedExceptionMessage)).when(instanceQuery).processInstanceIds(anySetOf(String.class));
    when(processEngine.getRuntimeService().createProcessInstanceQuery()).thenReturn(instanceQuery);

    String emptyList = "";
    given()
      .queryParam("processInstanceIds", emptyList)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo(expectedExceptionMessage))
      .when()
        .get(PROCESS_INSTANCE_QUERY_URL);
  }

  @Test
  public void testInstanceRetrievalByListWithEmptyAsPost() {
    ProcessInstanceQuery instanceQuery = mock(ProcessInstanceQuery.class);

    when(instanceQuery.list()).thenReturn(null);
    String expectedExceptionMessage = "Set of process instance ids is empty";
    doThrow(new ProcessEngineException(expectedExceptionMessage)).when(instanceQuery).processInstanceIds(anySetOf(String.class));
    when(processEngine.getRuntimeService().createProcessInstanceQuery()).thenReturn(instanceQuery);

    Map<String, Set<String>> params = new HashMap<String, Set<String>>();
    params.put("processInstanceIds", new HashSet<String>());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo(expectedExceptionMessage))
      .when()
        .post(PROCESS_INSTANCE_QUERY_URL);
  }

  @Test
  public void testQueryRootProcessInstances() {
    given()
      .queryParam("rootProcessInstances", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).rootProcessInstances();
  }

  @Test
  public void testQueryRootProcessInstancesAsPost() {
    Map<String, Object> params =new HashMap<String, Object>();
    params.put("rootProcessInstances", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(PROCESS_INSTANCE_QUERY_URL);

    verify(mockedQuery).rootProcessInstances();
  }
}
