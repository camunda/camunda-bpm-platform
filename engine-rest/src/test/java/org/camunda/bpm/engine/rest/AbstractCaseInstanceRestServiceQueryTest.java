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
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
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
public abstract class AbstractCaseInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String CASE_INSTANCE_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/case-instance";
  protected static final String CASE_INSTANCE_COUNT_QUERY_URL = CASE_INSTANCE_QUERY_URL + "/count";

  private CaseInstanceQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = setUpMockCaseInstanceQuery(MockProvider.createMockCaseInstances());
  }

  private CaseInstanceQuery setUpMockCaseInstanceQuery(List<CaseInstance> mockedCaseInstances) {
    CaseInstanceQuery query = mock(CaseInstanceQuery.class);

    when(query.list()).thenReturn(mockedCaseInstances);
    when(query.count()).thenReturn((long) mockedCaseInstances.size());
    when(processEngine.getCaseService().createCaseInstanceQuery()).thenReturn(query);

    return query;
  }

  @Test
  public void testEmptyQuery() {
    String queryCaseInstanceId = "";

    given()
      .queryParam("caseInstanceId", queryCaseInstanceId)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).list();
  }

  @Test
  public void testEmptyQueryAsPost() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("caseInstanceId", "");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
          .statusCode(Status.OK.getStatusCode())
     .when()
       .post(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).list();
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(CASE_INSTANCE_QUERY_URL);

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
      .post(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("caseInstanceId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
    .when()
      .get(CASE_INSTANCE_QUERY_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "caseInstanceId")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(CASE_INSTANCE_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(CASE_INSTANCE_QUERY_URL);
  }

  @Test
  public void testSortingParameters() {
    // asc
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("caseInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceId();
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
    executeAndVerifySorting("caseInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceId();
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
      .orderBy("caseInstanceId").desc()
      .orderBy("caseDefinitionId").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(CASE_INSTANCE_QUERY_URL);

    inOrder.verify(mockedQuery).orderByCaseInstanceId();
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
      .get(CASE_INSTANCE_QUERY_URL);

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
      .get(CASE_INSTANCE_QUERY_URL);

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
      .get(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testCaseInstanceRetrieval() {
    String queryCaseInstanceId = "aCaseInstanceId";

    Response response = given()
        .queryParam("caseInstanceId", queryCaseInstanceId)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_INSTANCE_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).caseInstanceId(queryCaseInstanceId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, String>> caseInstances = from(content).getList("");

    assertThat(caseInstances).hasSize(1);
    assertThat(caseInstances.get(0)).isNotNull();

    String returnedId = from(content).getString("[0].id");
    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedBusinessKeyKey = from(content).getString("[0].businessKey");
    boolean returnedActiveState = from(content).getBoolean("[0].active");
    boolean returnedCompletedState = from(content).getBoolean("[0].completed");
    boolean returnedTerminatedState = from(content).getBoolean("[0].terminated");

    assertThat(returnedId).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    assertThat(returnedCaseDefinitionId).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_CASE_DEFINITION_ID);
    assertThat(returnedBusinessKeyKey).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);
    assertThat(returnedActiveState).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_IS_ACTIVE);
    assertThat(returnedCompletedState).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_IS_COMPLETED);
    assertThat(returnedTerminatedState).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_IS_TERMINATED);
  }

  @Test
  public void testCaseInstanceRetrievalAsPost() {
    String queryCaseInstanceId = "aCaseInstanceId";

    Map<String, String> queryParameter = new HashMap<String, String>();
    queryParameter.put("caseInstanceId", queryCaseInstanceId);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameter)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .post(CASE_INSTANCE_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).caseInstanceId(queryCaseInstanceId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, String>> caseInstances = from(content).getList("");

    assertThat(caseInstances).hasSize(1);
    assertThat(caseInstances.get(0)).isNotNull();

    String returnedId = from(content).getString("[0].id");
    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedBusinessKeyKey = from(content).getString("[0].businessKey");
    boolean returnedActiveState = from(content).getBoolean("[0].active");
    boolean returnedCompletedState = from(content).getBoolean("[0].completed");
    boolean returnedTerminatedState = from(content).getBoolean("[0].terminated");

    assertThat(returnedId).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    assertThat(returnedCaseDefinitionId).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_CASE_DEFINITION_ID);
    assertThat(returnedBusinessKeyKey).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);
    assertThat(returnedActiveState).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_IS_ACTIVE);
    assertThat(returnedCompletedState).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_IS_COMPLETED);
    assertThat(returnedTerminatedState).isEqualTo(MockProvider.EXAMPLE_CASE_INSTANCE_IS_TERMINATED);
  }

  @Test
  public void testMultipleParameters() {
    Map<String, String> queryParameters = new HashMap<String, String>();

    queryParameters.put("caseInstanceId", "aCaseInstanceId");
    queryParameters.put("caseDefinitionId", "aCaseDefId");
    queryParameters.put("caseDefinitionKey", "aCaseDefKey");
    queryParameters.put("deploymentId", "aDeploymentId");
    queryParameters.put("businessKey", "aBusinessKey");
    queryParameters.put("superProcessInstance", "aSuperProcInstId");
    queryParameters.put("subProcessInstance", "aSubProcInstId");
    queryParameters.put("superCaseInstance", "aSuperCaseInstId");
    queryParameters.put("subCaseInstance", "aSubCaseInstId");
    queryParameters.put("active", "true");
    queryParameters.put("completed", "true");
    queryParameters.put("terminated", "true");

    given()
      .queryParams(queryParameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .get(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).caseInstanceId(queryParameters.get("caseInstanceId"));
    verify(mockedQuery).caseDefinitionId(queryParameters.get("caseDefinitionId"));
    verify(mockedQuery).caseDefinitionKey(queryParameters.get("caseDefinitionKey"));
    verify(mockedQuery).deploymentId(queryParameters.get("deploymentId"));
    verify(mockedQuery).caseInstanceBusinessKey(queryParameters.get("businessKey"));
    verify(mockedQuery).superProcessInstanceId(queryParameters.get("superProcessInstance"));
    verify(mockedQuery).subProcessInstanceId(queryParameters.get("subProcessInstance"));
    verify(mockedQuery).superCaseInstanceId(queryParameters.get("superCaseInstance"));
    verify(mockedQuery).subCaseInstanceId(queryParameters.get("subCaseInstance"));
    verify(mockedQuery).active();
    verify(mockedQuery).completed();
    verify(mockedQuery).terminated();
    verify(mockedQuery).list();
  }

  @Test
  public void testMultipleParametersAsPost() {
    String aCaseInstanceId = "aCaseInstanceId";
    String aCaseDefId = "aCaseDefId";
    String aCaseDefKey = "aCaseDefKey";
    String aDeploymentId = "aDeploymentId";
    String aBusinessKey = "aBusinessKey";
    String aSuperProcInstId = "aSuperProcInstId";
    String aSubProcInstId = "aSubProcInstId";
    String aSuperCaseInstId = "aSuperCaseInstId";
    String aSubCaseInstId = "aSubCaseInstId";

    Map<String, Object> queryParameters = new HashMap<String, Object>();

    queryParameters.put("caseInstanceId", aCaseInstanceId);
    queryParameters.put("caseDefinitionId", aCaseDefId);
    queryParameters.put("caseDefinitionKey", aCaseDefKey);
    queryParameters.put("deploymentId", aDeploymentId);
    queryParameters.put("businessKey", aBusinessKey);
    queryParameters.put("superProcessInstance", aSuperProcInstId);
    queryParameters.put("subProcessInstance", aSubProcInstId);
    queryParameters.put("superCaseInstance", aSuperCaseInstId);
    queryParameters.put("subCaseInstance", aSubCaseInstId);
    queryParameters.put("active", "true");
    queryParameters.put("completed", "true");
    queryParameters.put("terminated", "true");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(queryParameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).caseInstanceId(aCaseInstanceId);
    verify(mockedQuery).caseDefinitionId(aCaseDefId);
    verify(mockedQuery).caseDefinitionKey(aCaseDefKey);
    verify(mockedQuery).deploymentId(aDeploymentId);
    verify(mockedQuery).caseInstanceBusinessKey(aBusinessKey);
    verify(mockedQuery).superProcessInstanceId(aSuperProcInstId);
    verify(mockedQuery).subProcessInstanceId(aSubProcInstId);
    verify(mockedQuery).superCaseInstanceId(aSuperCaseInstId);
    verify(mockedQuery).subCaseInstanceId(aSubCaseInstId);
    verify(mockedQuery).active();
    verify(mockedQuery).completed();
    verify(mockedQuery).terminated();
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
        .get(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);

    // greater then
    queryValue = variableName + "_gt_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueGreaterThan(variableName, variableValue);

    // greater then equals
    queryValue = variableName + "_gteq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueGreaterThanOrEqual(variableName, variableValue);

    // lower then
    queryValue = variableName + "_lt_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueLessThan(variableName, variableValue);

    // lower then equals
    queryValue = variableName + "_lteq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueLessThanOrEqual(variableName, variableValue);

    // like
    queryValue = variableName + "_like_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueLike(variableName, variableValue);

    // not equals
    queryValue = variableName + "_neq_" + variableValue;

    given()
      .queryParam("variables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_INSTANCE_QUERY_URL);

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
        .post(CASE_INSTANCE_QUERY_URL);

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
        .post(CASE_INSTANCE_QUERY_URL);

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
        .post(CASE_INSTANCE_QUERY_URL);

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
        .post(CASE_INSTANCE_QUERY_URL);

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
        .post(CASE_INSTANCE_QUERY_URL);

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
        .post(CASE_INSTANCE_QUERY_URL);

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
        .post(CASE_INSTANCE_QUERY_URL);

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
        .get(CASE_INSTANCE_QUERY_URL);

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
        .post(CASE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).variableValueNotEquals(eq(anotherVariableName), argThat(EqualsPrimitiveValue.numberValue(anotherVariableValue)));
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(CASE_INSTANCE_COUNT_QUERY_URL);

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
      .post(CASE_INSTANCE_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

}
