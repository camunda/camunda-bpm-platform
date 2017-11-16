package org.camunda.bpm.engine.rest.history;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.history.HistoricDecisionInputInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDecisionOutputInstanceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class HistoricDecisionInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_DECISION_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/decision-instance";
  protected static final String HISTORIC_DECISION_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_DECISION_INSTANCE_RESOURCE_URL + "/count";

  protected HistoricDecisionInstanceQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockHistoricDecisionInstanceQuery(MockProvider.createMockHistoricDecisionInstances());
  }

  protected HistoricDecisionInstanceQuery setUpMockHistoricDecisionInstanceQuery(List<HistoricDecisionInstance> mockedHistoricDecisionInstances) {
    HistoricDecisionInstanceQuery mockedHistoricDecisionInstanceQuery = mock(HistoricDecisionInstanceQuery.class);
    when(mockedHistoricDecisionInstanceQuery.list()).thenReturn(mockedHistoricDecisionInstances);
    when(mockedHistoricDecisionInstanceQuery.count()).thenReturn((long) mockedHistoricDecisionInstances.size());

    when(processEngine.getHistoryService().createHistoricDecisionInstanceQuery()).thenReturn(mockedHistoricDecisionInstanceQuery);

    return mockedHistoricDecisionInstanceQuery;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("caseDefinitionKey", queryKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

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
      .queryParam("sortBy", "evaluationTime")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);
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
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("evaluationTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByEvaluationTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("evaluationTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByEvaluationTime();
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
  public void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

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
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

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
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_DECISION_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testSimpleHistoricDecisionInstanceQuery() {
    String decisionDefinitionId = MockProvider.EXAMPLE_DECISION_DEFINITION_ID;

    Response response = given()
        .queryParam("decisionDefinitionId", decisionDefinitionId)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).decisionDefinitionId(decisionDefinitionId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    assertEquals(1, instances.size());
    Assert.assertNotNull(instances.get(0));

    String returnedHistoricDecisionInstanceId = from(content).getString("[0].id");
    String returnedDecisionDefinitionId = from(content).getString("[0].decisionDefinitionId");
    String returnedDecisionDefinitionKey = from(content).getString("[0].decisionDefinitionKey");
    String returnedDecisionDefinitionName = from(content).getString("[0].decisionDefinitionName");
    String returnedEvaluationTime = from(content).getString("[0].evaluationTime");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("[0].processDefinitionKey");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedCaseDefinitionKey = from(content).getString("[0].caseDefinitionKey");
    String returnedCaseInstanceId = from(content).getString("[0].caseInstanceId");
    String returnedActivityId = from(content).getString("[0].activityId");
    String returnedActivityInstanceId = from(content).getString("[0].activityInstanceId");
    List<HistoricDecisionInputInstanceDto> returnedInputs = from(content).getList("[0].inputs");
    List<HistoricDecisionOutputInstanceDto> returnedOutputs = from(content).getList("[0].outputs");
    Double returnedCollectResultValue = from(content).getDouble("[0].collectResultValue");
    String returnedTenantId = from(content).getString("[0].tenantId");
    String returnedRootDecisionInstanceId = from(content).getString("[0].rootDecisionInstanceId");
    String returnedDecisionRequirementsDefinitionId = from(content).getString("[0].decisionRequirementsDefinitionId");
    String returnedDecisionRequirementsDefinitionKey = from(content).getString("[0].decisionRequirementsDefinitionKey");

    assertThat(returnedHistoricDecisionInstanceId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID));
    assertThat(returnedDecisionDefinitionId, is(MockProvider.EXAMPLE_DECISION_DEFINITION_ID));
    assertThat(returnedDecisionDefinitionKey, is(MockProvider.EXAMPLE_DECISION_DEFINITION_KEY));
    assertThat(returnedDecisionDefinitionName, is(MockProvider.EXAMPLE_DECISION_DEFINITION_NAME));
    assertThat(returnedEvaluationTime, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_EVALUATION_TIME));
    assertThat(returnedProcessDefinitionId, is(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    assertThat(returnedProcessDefinitionKey, is(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY));
    assertThat(returnedProcessInstanceId, is(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID));
    assertThat(returnedCaseDefinitionId, is(MockProvider.EXAMPLE_CASE_DEFINITION_ID));
    assertThat(returnedCaseDefinitionKey, is(MockProvider.EXAMPLE_CASE_DEFINITION_KEY));
    assertThat(returnedCaseInstanceId, is(MockProvider.EXAMPLE_CASE_INSTANCE_ID));
    assertThat(returnedActivityId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_ID));
    assertThat(returnedActivityInstanceId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_INSTANCE_ID));
    assertThat(returnedInputs, is(nullValue()));
    assertThat(returnedOutputs, is(nullValue()));
    assertThat(returnedCollectResultValue, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_COLLECT_RESULT_VALUE));
    assertThat(returnedTenantId, is(MockProvider.EXAMPLE_TENANT_ID));
    assertThat(returnedRootDecisionInstanceId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID));
    assertThat(returnedDecisionRequirementsDefinitionId, is(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID));
    assertThat(returnedDecisionRequirementsDefinitionKey, is(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY));
  }

  @Test
  public void testAdditionalParameters() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .queryParams(stringQueryParameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  @Test
  public void testIncludeInputs() {
    mockedQuery = setUpMockHistoricDecisionInstanceQuery(Collections.singletonList(MockProvider.createMockHistoricDecisionInstanceWithInputs()));

    String decisionDefinitionId = MockProvider.EXAMPLE_DECISION_DEFINITION_ID;

    Response response = given()
        .queryParam("decisionDefinitionId", decisionDefinitionId)
        .queryParam("includeInputs", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).decisionDefinitionId(decisionDefinitionId);
    inOrder.verify(mockedQuery).includeInputs();
    inOrder.verify(mockedQuery, never()).includeOutputs();
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    assertEquals(1, instances.size());
    Assert.assertNotNull(instances.get(0));

    List<Map<String, Object>> returnedInputs = from(content).getList("[0].inputs");
    List<Map<String, Object>> returnedOutputs = from(content).getList("[0].outputs");

    assertThat(returnedInputs, is(notNullValue()));
    assertThat(returnedOutputs, is(nullValue()));

    verifyHistoricDecisionInputInstances(returnedInputs);
  }

  @Test
  public void testIncludeOutputs() {
    mockedQuery = setUpMockHistoricDecisionInstanceQuery(Collections.singletonList(MockProvider.createMockHistoricDecisionInstanceWithOutputs()));

    String decisionDefinitionId = MockProvider.EXAMPLE_DECISION_DEFINITION_ID;

    Response response = given()
        .queryParam("decisionDefinitionId", decisionDefinitionId)
        .queryParam("includeOutputs", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).decisionDefinitionId(decisionDefinitionId);
    inOrder.verify(mockedQuery, never()).includeInputs();
    inOrder.verify(mockedQuery).includeOutputs();
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    assertEquals(1, instances.size());
    Assert.assertNotNull(instances.get(0));

    List<Map<String, Object>> returnedInputs = from(content).getList("[0].inputs");
    List<Map<String, Object>> returnedOutputs = from(content).getList("[0].outputs");

    assertThat(returnedInputs, is(nullValue()));
    assertThat(returnedOutputs, is(notNullValue()));

    verifyHistoricDecisionOutputInstances(returnedOutputs);
  }

  @Test
  public void testIncludeInputsAndOutputs() {
    mockedQuery = setUpMockHistoricDecisionInstanceQuery(Collections.singletonList(MockProvider.createMockHistoricDecisionInstanceWithInputsAndOutputs()));

    String decisionDefinitionId = MockProvider.EXAMPLE_DECISION_DEFINITION_ID;

    Response response = given()
        .queryParam("decisionDefinitionId", decisionDefinitionId)
        .queryParam("includeInputs", true)
        .queryParam("includeOutputs", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).decisionDefinitionId(decisionDefinitionId);
    inOrder.verify(mockedQuery).includeInputs();
    inOrder.verify(mockedQuery).includeOutputs();
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    assertEquals(1, instances.size());
    Assert.assertNotNull(instances.get(0));

    List<Map<String, Object>> returnedInputs = from(content).getList("[0].inputs");
    List<Map<String, Object>> returnedOutputs = from(content).getList("[0].outputs");

    assertThat(returnedInputs, is(notNullValue()));
    assertThat(returnedOutputs, is(notNullValue()));

    verifyHistoricDecisionInputInstances(returnedInputs);
    verifyHistoricDecisionOutputInstances(returnedOutputs);
  }

  @Test
  public void testDefaultBinaryFetching() {
    String decisionDefinitionId = MockProvider.EXAMPLE_DECISION_DEFINITION_ID;

    given()
        .queryParam("decisionDefinitionId", decisionDefinitionId)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).decisionDefinitionId(decisionDefinitionId);
    inOrder.verify(mockedQuery, never()).disableBinaryFetching();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testDisableBinaryFetching() {
    String decisionDefinitionId = MockProvider.EXAMPLE_DECISION_DEFINITION_ID;

    given()
        .queryParam("decisionDefinitionId", decisionDefinitionId)
        .queryParam("disableBinaryFetching", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).decisionDefinitionId(decisionDefinitionId);
    inOrder.verify(mockedQuery).disableBinaryFetching();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testDefaultCustomObjectDeserialization() {
    String decisionDefinitionId = MockProvider.EXAMPLE_DECISION_DEFINITION_ID;

    given()
        .queryParam("decisionDefinitionId", decisionDefinitionId)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).decisionDefinitionId(decisionDefinitionId);
    inOrder.verify(mockedQuery, never()).disableCustomObjectDeserialization();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testDisableCustomObjectDeserialization() {
    String decisionDefinitionId = MockProvider.EXAMPLE_DECISION_DEFINITION_ID;

    given()
        .queryParam("decisionDefinitionId", decisionDefinitionId)
        .queryParam("disableCustomObjectDeserialization", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).decisionDefinitionId(decisionDefinitionId);
    inOrder.verify(mockedQuery).disableCustomObjectDeserialization();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testRootDecisionInstancesOnly() {

    given()
        .queryParam("rootDecisionInstancesOnly", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).rootDecisionInstancesOnly();
    verify(mockedQuery).list();
  }

  @Test
  public void testTenantIdListParameter() {
    mockedQuery = setUpMockHistoricDecisionInstanceQuery(createMockHistoricDecisionInstancesTwoTenants());

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> historicDecisionInstances = from(content).getList("");
    assertThat(historicDecisionInstances).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  private List<HistoricDecisionInstance> createMockHistoricDecisionInstancesTwoTenants() {
    return Arrays.asList(
        MockProvider.createMockHistoricDecisionInstanceBase(MockProvider.EXAMPLE_TENANT_ID),
        MockProvider.createMockHistoricDecisionInstanceBase(MockProvider.ANOTHER_EXAMPLE_TENANT_ID));
  }

  protected Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("decisionInstanceId", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    parameters.put("decisionInstanceIdIn", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID_IN);
    parameters.put("decisionDefinitionId", MockProvider.EXAMPLE_DECISION_DEFINITION_ID);
    parameters.put("decisionDefinitionIdIn", MockProvider.EXAMPLE_DECISION_DEFINITION_ID_IN);
    parameters.put("decisionDefinitionKey", MockProvider.EXAMPLE_DECISION_DEFINITION_KEY);
    parameters.put("decisionDefinitionKeyIn", MockProvider.EXAMPLE_DECISION_DEFINITION_KEY_IN);
    parameters.put("decisionDefinitionName", MockProvider.EXAMPLE_DECISION_DEFINITION_NAME);
    parameters.put("decisionDefinitionNameLike", MockProvider.EXAMPLE_DECISION_DEFINITION_NAME_LIKE);
    parameters.put("processDefinitionId", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    parameters.put("processDefinitionKey", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY);
    parameters.put("processInstanceId", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    parameters.put("caseDefinitionId", MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    parameters.put("caseDefinitionKey", MockProvider.EXAMPLE_CASE_DEFINITION_KEY);
    parameters.put("caseInstanceId", MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    parameters.put("activityIdIn", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_ID_IN);
    parameters.put("activityInstanceIdIn", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ACTIVITY_INSTANCE_ID_IN);
    parameters.put("evaluatedBefore", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_EVALUATED_BEFORE);
    parameters.put("evaluatedAfter", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_EVALUATED_AFTER);
    parameters.put("userId", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_USER_ID);
    parameters.put("rootDecisionInstanceId", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    parameters.put("decisionRequirementsDefinitionId", MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
    parameters.put("decisionRequirementsDefinitionKey", MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY);

    return parameters;
  }

  protected void verifyStringParameterQueryInvocations() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();
    StringArrayConverter stringArrayConverter = new StringArrayConverter();

    verify(mockedQuery).decisionInstanceId(stringQueryParameters.get("decisionInstanceId"));
    verify(mockedQuery).decisionInstanceIdIn(stringArrayConverter.convertQueryParameterToType(stringQueryParameters.get("decisionInstanceIdIn")));
    verify(mockedQuery).decisionDefinitionId(stringQueryParameters.get("decisionDefinitionId"));
    verify(mockedQuery).decisionDefinitionIdIn(stringArrayConverter.convertQueryParameterToType(stringQueryParameters.get("decisionDefinitionIdIn")));
    verify(mockedQuery).decisionDefinitionKey(stringQueryParameters.get("decisionDefinitionKey"));
    verify(mockedQuery).decisionDefinitionKeyIn(stringArrayConverter.convertQueryParameterToType(stringQueryParameters.get("decisionDefinitionKeyIn")));
    verify(mockedQuery).decisionDefinitionName(stringQueryParameters.get("decisionDefinitionName"));
    verify(mockedQuery).decisionDefinitionNameLike(stringQueryParameters.get("decisionDefinitionNameLike"));
    verify(mockedQuery).processDefinitionId(stringQueryParameters.get("processDefinitionId"));
    verify(mockedQuery).processDefinitionKey(stringQueryParameters.get("processDefinitionKey"));
    verify(mockedQuery).processInstanceId(stringQueryParameters.get("processInstanceId"));
    verify(mockedQuery).caseDefinitionId(stringQueryParameters.get("caseDefinitionId"));
    verify(mockedQuery).caseDefinitionKey(stringQueryParameters.get("caseDefinitionKey"));
    verify(mockedQuery).caseInstanceId(stringQueryParameters.get("caseInstanceId"));
    verify(mockedQuery).activityIdIn(stringArrayConverter.convertQueryParameterToType(stringQueryParameters.get("activityIdIn")));
    verify(mockedQuery).activityInstanceIdIn(stringArrayConverter.convertQueryParameterToType(stringQueryParameters.get("activityInstanceIdIn")));
    verify(mockedQuery).evaluatedBefore(DateTimeUtil.parseDate(stringQueryParameters.get("evaluatedBefore")));
    verify(mockedQuery).evaluatedAfter(DateTimeUtil.parseDate(stringQueryParameters.get("evaluatedAfter")));
    verify(mockedQuery).userId(stringQueryParameters.get("userId"));
    verify(mockedQuery).rootDecisionInstanceId(stringQueryParameters.get("rootDecisionInstanceId"));
    verify(mockedQuery).decisionRequirementsDefinitionId(stringQueryParameters.get("decisionRequirementsDefinitionId"));
    verify(mockedQuery).decisionRequirementsDefinitionKey(stringQueryParameters.get("decisionRequirementsDefinitionKey"));

    verify(mockedQuery).list();
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
        .queryParam("sortBy", sortBy)
        .queryParam("sortOrder", sortOrder)
      .then().expect()
        .statusCode(expectedStatus.getStatusCode())
      .when()
        .get(HISTORIC_DECISION_INSTANCE_RESOURCE_URL);
  }

  protected void verifyHistoricDecisionInputInstances(List<Map<String, Object>> returnedInputs) {
    assertThat(returnedInputs, hasSize(3));

    // verify common properties
    for (Map<String, Object> returnedInput : returnedInputs) {
      assertThat(returnedInput, hasEntry("id", (Object) MockProvider.EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_ID));
      assertThat(returnedInput, hasEntry("decisionInstanceId", (Object) MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID));
      assertThat(returnedInput, hasEntry("clauseId", (Object) MockProvider.EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_CLAUSE_ID));
      assertThat(returnedInput, hasEntry("clauseName", (Object) MockProvider.EXAMPLE_HISTORIC_DECISION_INPUT_INSTANCE_CLAUSE_NAME));
      assertThat(returnedInput, hasEntry("errorMessage", null));
    }

    verifyStringValue(returnedInputs.get(0));
    verifyByteArrayValue(returnedInputs.get(1));
    verifySerializedValue(returnedInputs.get(2));

  }

  protected void verifyHistoricDecisionOutputInstances(List<Map<String, Object>> returnedOutputs) {
    assertThat(returnedOutputs, hasSize(3));

    // verify common properties
    for (Map<String, Object> returnedOutput : returnedOutputs) {
      assertThat(returnedOutput, hasEntry("id", (Object) MockProvider.EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_ID));
      assertThat(returnedOutput, hasEntry("decisionInstanceId", (Object) MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID));
      assertThat(returnedOutput, hasEntry("clauseId", (Object) MockProvider.EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_CLAUSE_ID));
      assertThat(returnedOutput, hasEntry("clauseName", (Object) MockProvider.EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_CLAUSE_NAME));
      assertThat(returnedOutput, hasEntry("ruleId", (Object) MockProvider.EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_RULE_ID));
      assertThat(returnedOutput, hasEntry("ruleOrder", (Object) MockProvider.EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_RULE_ORDER));
      assertThat(returnedOutput, hasEntry("variableName", (Object) MockProvider.EXAMPLE_HISTORIC_DECISION_OUTPUT_INSTANCE_VARIABLE_NAME));
      assertThat(returnedOutput, hasEntry("errorMessage", null));
    }

    verifyStringValue(returnedOutputs.get(0));
    verifyByteArrayValue(returnedOutputs.get(1));
    verifySerializedValue(returnedOutputs.get(2));
  }

  protected void verifyStringValue(Map<String, Object> stringValue) {
    StringValue exampleValue = MockProvider.EXAMPLE_HISTORIC_DECISION_STRING_VALUE;
    assertThat(stringValue, hasEntry("type", (Object) VariableValueDto.toRestApiTypeName(exampleValue.getType().getName())));
    assertThat(stringValue, hasEntry("value", (Object) exampleValue.getValue()));
    assertThat(stringValue, hasEntry("valueInfo", (Object) Collections.emptyMap()));
  }

  protected void verifyByteArrayValue(Map<String, Object> byteArrayValue) {
    BytesValue exampleValue = MockProvider.EXAMPLE_HISTORIC_DECISION_BYTE_ARRAY_VALUE;
    assertThat(byteArrayValue, hasEntry("type", (Object) VariableValueDto.toRestApiTypeName(exampleValue.getType().getName())));
    String byteString = Base64.encodeBase64String(exampleValue.getValue()).trim();
    assertThat(byteArrayValue, hasEntry("value", (Object) byteString));
    assertThat(byteArrayValue, hasEntry("valueInfo", (Object) Collections.emptyMap()));
  }

  @SuppressWarnings("unchecked")
  protected void verifySerializedValue(Map<String, Object> serializedValue) {
    ObjectValue exampleValue = MockProvider.EXAMPLE_HISTORIC_DECISION_SERIALIZED_VALUE;
    assertThat(serializedValue, hasEntry("type", (Object) VariableValueDto.toRestApiTypeName(exampleValue.getType().getName())));
    assertThat(serializedValue, hasEntry("value", exampleValue.getValue()));
    Map<String, String> valueInfo = (Map<String, String>) serializedValue.get("valueInfo");
    assertThat(valueInfo, hasEntry("serializationDataFormat", exampleValue.getSerializationDataFormat()));
    assertThat(valueInfo, hasEntry("objectTypeName", exampleValue.getObjectTypeName()));

  }

}
