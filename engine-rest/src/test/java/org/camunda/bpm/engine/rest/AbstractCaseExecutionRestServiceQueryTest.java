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

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.camunda.bpm.engine.rest.util.OrderingBuilder;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;


/**
*
* @author Roman Smirnov
*
*/
public abstract class AbstractCaseExecutionRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String CASE_EXECUTION_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/case-execution";
  protected static final String CASE_EXECUTION_COUNT_QUERY_URL = CASE_EXECUTION_QUERY_URL + "/count";

  private CaseExecutionQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockCaseExecutionQuery(MockProvider.createMockCaseExecutions());
  }

  private CaseExecutionQuery setUpMockCaseExecutionQuery(List<CaseExecution> mockedCaseExecutions) {
    CaseExecutionQuery query = mock(CaseExecutionQuery.class);

    when(processEngine.getCaseService().createCaseExecutionQuery()).thenReturn(query);

    when(query.list()).thenReturn(mockedCaseExecutions);
    when(query.count()).thenReturn((long) mockedCaseExecutions.size());

    return query;
  }

  @Test
  public void testEmptyQuery() {
    String queryCaseExecutionId = "";

    given()
      .queryParam("caseExecutionId", queryCaseExecutionId)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).list();
  }

  @Test
  public void testEmptyQueryAsPost() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("caseExecutionId", "");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
          .statusCode(Status.OK.getStatusCode())
     .when()
       .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).list();
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(CASE_EXECUTION_QUERY_URL);

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
      .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("caseExecutionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
    .when()
      .get(CASE_EXECUTION_QUERY_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "caseExecutionId")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(CASE_EXECUTION_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(CASE_EXECUTION_QUERY_URL);
  }

  @Test
  public void testSortingParameters() {
    // asc
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseExecutionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseExecutionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseDefinitionKey", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionKey();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseDefinitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionId();
    inOrder.verify(mockedQuery).asc();

    // desc
    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseExecutionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseExecutionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseDefinitionKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseDefinitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionId();
    inOrder.verify(mockedQuery).desc();
  }

  @Test
  public void testSecondarySortingAsPost() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("sorting", OrderingBuilder.create()
      .orderBy("caseExecutionId").desc()
      .orderBy("caseDefinitionId").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(CASE_EXECUTION_QUERY_URL);

    inOrder.verify(mockedQuery).orderByCaseExecutionId();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByCaseDefinitionId();
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
      .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  /**
   * If parameter "firstResult" is missing, we expect 0 as default.
   */
  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  /**
   * If parameter "maxResults" is missing, we expect Integer.MAX_VALUE as default.
   */
  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testCaseExecutionRetrieval() {
    String queryCaseExecutionId = "aCaseExecutionId";

    Response response = given()
        .queryParam("caseExecutionId", queryCaseExecutionId)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).caseExecutionId(queryCaseExecutionId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, String>> caseExecutions = from(content).getList("");

    assertThat(caseExecutions).hasSize(1);
    assertThat(caseExecutions.get(0)).isNotNull();

    String returnedId = from(content).getString("[0].id");
    String returnedCaseInstanceId = from(content).getString("[0].caseInstanceId");
    String returnedParentId = from(content).getString("[0].parentId");
    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedActivityId = from(content).getString("[0].activityId");
    String returnedActivityName = from(content).getString("[0].activityName");
    String returnedActivityType = from(content).getString("[0].activityType");
    String returnedActivityDescription = from(content).getString("[0].activityDescription");
    boolean returnedRequired = from(content).getBoolean("[0].required");
    boolean returnedActiveState = from(content).getBoolean("[0].active");
    boolean returnedEnabledState = from(content).getBoolean("[0].enabled");
    boolean returnedDisabledState = from(content).getBoolean("[0].disabled");

    assertThat(returnedId).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_ID);
    assertThat(returnedCaseInstanceId).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_CASE_INSTANCE_ID);
    assertThat(returnedParentId).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_PARENT_ID);
    assertThat(returnedCaseDefinitionId).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_CASE_DEFINITION_ID);
    assertThat(returnedActivityId).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_ACTIVITY_ID);
    assertThat(returnedActivityName).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_ACTIVITY_NAME);
    assertThat(returnedActivityType).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_ACTIVITY_TYPE);
    assertThat(returnedActivityDescription).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_ACTIVITY_DESCRIPTION);
    assertThat(returnedRequired).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_IS_REQUIRED);
    assertThat(returnedEnabledState).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_IS_ENABLED);
    assertThat(returnedActiveState).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_IS_ACTIVE);
    assertThat(returnedDisabledState).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_IS_DISABLED);
  }

  @Test
  public void testCaseExecutionRetrievalAsPost() {
    String queryCaseExecutionId = "aCaseExecutionId";

    Map<String, String> queryParameter = new HashMap<String, String>();
    queryParameter.put("caseExecutionId", queryCaseExecutionId);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameter)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).caseExecutionId(queryCaseExecutionId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, String>> caseExecutions = from(content).getList("");

    assertThat(caseExecutions).hasSize(1);
    assertThat(caseExecutions.get(0)).isNotNull();

    String returnedId = from(content).getString("[0].id");
    String returnedCaseInstanceId = from(content).getString("[0].caseInstanceId");
    String returnedParentId = from(content).getString("[0].parentId");
    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedActivityId = from(content).getString("[0].activityId");
    String returnedActivityName = from(content).getString("[0].activityName");
    String returnedActivityType = from(content).getString("[0].activityType");
    String returnedActivityDescription = from(content).getString("[0].activityDescription");
    boolean returnedRequired = from(content).getBoolean("[0].required");
    boolean returnedActiveState = from(content).getBoolean("[0].active");
    boolean returnedEnabledState = from(content).getBoolean("[0].enabled");
    boolean returnedDisabledState = from(content).getBoolean("[0].disabled");

    assertThat(returnedId).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_ID);
    assertThat(returnedCaseInstanceId).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_CASE_INSTANCE_ID);
    assertThat(returnedParentId).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_PARENT_ID);
    assertThat(returnedCaseDefinitionId).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_CASE_DEFINITION_ID);
    assertThat(returnedActivityId).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_ACTIVITY_ID);
    assertThat(returnedActivityName).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_ACTIVITY_NAME);
    assertThat(returnedActivityType).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_ACTIVITY_TYPE);
    assertThat(returnedActivityDescription).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_ACTIVITY_DESCRIPTION);
    assertThat(returnedRequired).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_IS_REQUIRED);
    assertThat(returnedEnabledState).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_IS_ENABLED);
    assertThat(returnedActiveState).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_IS_ACTIVE);
    assertThat(returnedDisabledState).isEqualTo(MockProvider.EXAMPLE_CASE_EXECUTION_IS_DISABLED);
  }

  @Test
  public void testMultipleParameters() {
    Map<String, String> queryParameters = new HashMap<String, String>();

    queryParameters.put("caseExecutionId", "aCaseExecutionId");
    queryParameters.put("caseDefinitionKey", "aCaseDefId");
    queryParameters.put("caseDefinitionId", "aCaseDefId");
    queryParameters.put("caseInstanceId", "aCaseInstanceId");
    queryParameters.put("businessKey", "aBusinessKey");
    queryParameters.put("activityId", "anActivityId");
    queryParameters.put("required", "true");
    queryParameters.put("active", "true");
    queryParameters.put("enabled", "true");
    queryParameters.put("disabled", "true");

    given()
      .queryParams(queryParameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseExecutionId(queryParameters.get("caseExecutionId"));
    verify(mockedQuery).caseDefinitionKey(queryParameters.get("caseDefinitionKey"));
    verify(mockedQuery).caseDefinitionId(queryParameters.get("caseDefinitionId"));
    verify(mockedQuery).caseInstanceId(queryParameters.get("caseInstanceId"));
    verify(mockedQuery).caseInstanceBusinessKey(queryParameters.get("businessKey"));
    verify(mockedQuery).activityId(queryParameters.get("activityId"));
    verify(mockedQuery).required();
    verify(mockedQuery).active();
    verify(mockedQuery).enabled();
    verify(mockedQuery).disabled();
    verify(mockedQuery).list();
  }

  @Test
  public void testMultipleParametersAsPost() {
    String aCaseExecutionId = "aCaseExecutionId";
    String aCaseDefKey = "aCaseDefKey";
    String aCaseDefId = "aCaseDefId";
    String aCaseInstanceId = "aCaseInstanceId";
    String aBusinessKey = "aBusinessKey";
    String anActivityId = "anActivityId";

    Map<String, Object> queryParameters = new HashMap<String, Object>();

    queryParameters.put("caseExecutionId", aCaseExecutionId);
    queryParameters.put("caseDefinitionKey", aCaseDefKey);
    queryParameters.put("caseDefinitionId", aCaseDefId);
    queryParameters.put("caseInstanceId", aCaseInstanceId);
    queryParameters.put("businessKey", aBusinessKey);
    queryParameters.put("activityId", anActivityId);
    queryParameters.put("required", "true");
    queryParameters.put("repeatable", "true");
    queryParameters.put("repetition", "true");
    queryParameters.put("active", "true");
    queryParameters.put("enabled", "true");
    queryParameters.put("disabled", "true");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(queryParameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseExecutionId(aCaseExecutionId);
    verify(mockedQuery).caseDefinitionKey(aCaseDefKey);
    verify(mockedQuery).caseDefinitionId(aCaseDefId);
    verify(mockedQuery).caseInstanceId(aCaseInstanceId);
    verify(mockedQuery).caseInstanceBusinessKey(aBusinessKey);
    verify(mockedQuery).activityId(anActivityId);
    verify(mockedQuery).required();
    verify(mockedQuery).active();
    verify(mockedQuery).enabled();
    verify(mockedQuery).disabled();
    verify(mockedQuery).list();
  }

  @Test
  public void testCaseInstanceVariableParameters() {
    // equals
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueEquals(variableName, variableValue);

    // greater then
    queryValue = variableName + "_gt_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueGreaterThan(variableName, variableValue);

    // greater then equals
    queryValue = variableName + "_gteq_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueGreaterThanOrEqual(variableName, variableValue);

    // lower then
    queryValue = variableName + "_lt_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueLessThan(variableName, variableValue);

    // lower then equals
    queryValue = variableName + "_lteq_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueLessThanOrEqual(variableName, variableValue);

    // like
    queryValue = variableName + "_like_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueLike(variableName, variableValue);

    // not equals
    queryValue = variableName + "_neq_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueNotEquals(variableName, variableValue);
  }

  @Test
  public void testCaseInstanceVariableParametersAsPost() {
    // equals
    String variableName = "varName";
    String variableValue = "varValue";

    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", variableName);
    variableJson.put("operator", "eq");
    variableJson.put("value", variableValue);

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("caseInstanceVariables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueEquals(variableName, variableValue);

    // greater then
    variableJson.put("operator", "gt");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueGreaterThan(variableName, variableValue);

    // greater then equals
    variableJson.put("operator", "gteq");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueGreaterThanOrEqual(variableName, variableValue);

    // lower then
    variableJson.put("operator", "lt");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueLessThan(variableName, variableValue);

    // lower then equals
    variableJson.put("operator", "lteq");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueLessThanOrEqual(variableName, variableValue);

    // like
    variableJson.put("operator", "like");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueLike(variableName, variableValue);

    // not equals
    variableJson.put("operator", "neq");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueNotEquals(variableName, variableValue);
  }

  @Test
  public void testMultipleCaseInstanceVariableParameters() {
    String variableName1 = "varName";
    String variableValue1 = "varValue";
    String variableParameter1 = variableName1 + "_eq_" + variableValue1;

    String variableName2 = "anotherVarName";
    String variableValue2 = "anotherVarValue";
    String variableParameter2 = variableName2 + "_neq_" + variableValue2;

    String queryValue = variableParameter1 + "," + variableParameter2;

    given()
      .queryParam("caseInstanceVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueEquals(variableName1, variableValue1);
    verify(mockedQuery).caseInstanceVariableValueNotEquals(variableName2, variableValue2);
  }

  @Test
  public void testMultipleCaseInstanceVariableParametersAsPost() {
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
    json.put("caseInstanceVariables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).caseInstanceVariableValueEquals(variableName, variableValue);
    verify(mockedQuery).caseInstanceVariableValueNotEquals(eq(anotherVariableName), argThat(EqualsPrimitiveValue.numberValue(anotherVariableValue)));
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
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);

    // greater then
    queryValue = variableName + "_gt_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueGreaterThan(variableName, variableValue);

    // greater then equals
    queryValue = variableName + "_gteq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueGreaterThanOrEqual(variableName, variableValue);

    // lower then
    queryValue = variableName + "_lt_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueLessThan(variableName, variableValue);

    // lower then equals
    queryValue = variableName + "_lteq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueLessThanOrEqual(variableName, variableValue);

    // like
    queryValue = variableName + "_like_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueLike(variableName, variableValue);

    // not equals
    queryValue = variableName + "_neq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
  }

  @Test
  public void testVariableParametersAsPost() {
    // equals
    String variableName = "varName";
    String variableValue = "varValue";

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
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);

    // greater then
    variableJson.put("operator", "gt");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueGreaterThan(variableName, variableValue);

    // greater then equals
    variableJson.put("operator", "gteq");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueGreaterThanOrEqual(variableName, variableValue);

    // lower then
    variableJson.put("operator", "lt");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueLessThan(variableName, variableValue);

    // lower then equals
    variableJson.put("operator", "lteq");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueLessThanOrEqual(variableName, variableValue);

    // like
    variableJson.put("operator", "like");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueLike(variableName, variableValue);

    // not equals
    variableJson.put("operator", "neq");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_EXECUTION_QUERY_URL);

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
        .get(CASE_EXECUTION_QUERY_URL);

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
        .post(CASE_EXECUTION_QUERY_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).variableValueNotEquals(eq(anotherVariableName), argThat(EqualsPrimitiveValue.numberValue(anotherVariableValue)));
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(CASE_EXECUTION_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testQueryCountAsPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .body("count", equalTo(1))
    .when()
      .post(CASE_EXECUTION_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

}
