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

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.MockVariableInstanceBuilder;
import org.camunda.bpm.engine.rest.helper.VariableTypeHelper;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.camunda.bpm.engine.rest.util.OrderingBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class VariableInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String VARIABLE_INSTANCE_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/variable-instance";
  protected static final String VARIABLE_INSTANCE_COUNT_QUERY_URL = VARIABLE_INSTANCE_QUERY_URL + "/count";

  protected VariableInstanceQuery mockedQuery;
  protected VariableInstance mockInstance;
  protected MockVariableInstanceBuilder mockInstanceBuilder;

  @Before
  public void setUpRuntimeData() {
    mockInstanceBuilder = MockProvider.mockVariableInstance();
    mockInstance = mockInstanceBuilder.build();

    mockedQuery = setUpMockVariableInstanceQuery(createMockVariableInstanceList(mockInstance));
  }

  private VariableInstanceQuery setUpMockVariableInstanceQuery(List<VariableInstance> mockedInstances) {
    VariableInstanceQuery sampleInstanceQuery = mock(VariableInstanceQuery.class);

    when(sampleInstanceQuery.list()).thenReturn(mockedInstances);
    when(sampleInstanceQuery.count()).thenReturn((long) mockedInstances.size());
    when(processEngine.getRuntimeService().createVariableInstanceQuery()).thenReturn(sampleInstanceQuery);

    return sampleInstanceQuery;
  }

  protected List<VariableInstance> createMockVariableInstanceList(VariableInstance mockInstance) {
    List<VariableInstance> mocks = new ArrayList<VariableInstance>();

    mocks.add(mockInstance);
    return mocks;
  }

  @Test
  public void testNoParametersQuery() {
    expect().statusCode(Status.OK.getStatusCode()).when().get(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testNoParametersQueryDisableObjectDeserialization() {
    given()
      .queryParam("deserializeValues", false)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();
    verify(mockedQuery).disableCustomObjectDeserialization();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testNoParametersQueryAsPost() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testNoParametersQueryAsPostDisableObjectDeserialization() {
    given()
      .queryParam("deserializeValues", false)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();
    verify(mockedQuery).disableCustomObjectDeserialization();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidVariableRequests() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_" + invalidComparator + "_" + variableValue;
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Invalid variable comparator specified: " + invalidComparator))
      .when().get(VARIABLE_INSTANCE_QUERY_URL);

    // invalid format
    queryValue = "invalidFormattedVariableQuery";
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("variable query parameter has to have format KEY_OPERATOR_VALUE"))
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("variableName", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
      .then().expect().statusCode(expectedStatus.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "variableName")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("variableName", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByVariableName();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("variableType", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByVariableType();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityInstanceId();
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
      .orderBy("variableName").desc()
      .orderBy("activityInstanceId").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(VARIABLE_INSTANCE_QUERY_URL);

    inOrder.verify(mockedQuery).orderByVariableName();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByActivityInstanceId();
    inOrder.verify(mockedQuery).asc();
  }

  @Test
  public void testSuccessfulPagination() {

    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  /**
   * If parameter "firstResult" is missing, we expect 0 as default.
   */
  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;
    given().queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).listPage(0, maxResults);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  /**
   * If parameter "maxResults" is missing, we expect Integer.MAX_VALUE as default.
   */
  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;
    given().queryParam("firstResult", firstResult)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableInstanceRetrieval() {
    String queryVariableName = "aVariableInstanceName";
    Response response = given().queryParam("variableName", queryVariableName)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .and()
          .body("size()", is(1))
          .body("[0].id", equalTo(mockInstanceBuilder.getId()))
          .body("[0].name", equalTo(mockInstanceBuilder.getName()))
          .body("[0].type", equalTo(VariableTypeHelper.toExpectedValueTypeName(mockInstanceBuilder.getTypedValue().getType())))
          .body("[0].value", equalTo(mockInstanceBuilder.getValue()))
          .body("[0].processDefinitionId", equalTo(mockInstanceBuilder.getProcessDefinitionId()))
          .body("[0].processInstanceId", equalTo(mockInstanceBuilder.getProcessInstanceId()))
          .body("[0].executionId", equalTo(mockInstanceBuilder.getExecutionId()))
          .body("[0].caseInstanceId", equalTo(mockInstanceBuilder.getCaseInstanceId()))
          .body("[0].caseExecutionId", equalTo(mockInstanceBuilder.getCaseExecutionId()))
          .body("[0].taskId", equalTo(mockInstanceBuilder.getTaskId()))
          .body("[0].batchId", equalTo(mockInstanceBuilder.getBatchId()))
          .body("[0].activityInstanceId", equalTo(mockInstanceBuilder.getActivityInstanceId()))
          .body("[0].tenantId", equalTo(mockInstanceBuilder.getTenantId()))
          .body("[0].errorMessage", equalTo(mockInstanceBuilder.getErrorMessage()))
          .body("[0].serializedValue", nullValue())
        .when().get(VARIABLE_INSTANCE_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).variableName(queryVariableName);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> variables = from(content).getList("");
    Assert.assertEquals("There should be one variable instance returned.", 1, variables.size());
    Assert.assertNotNull("There should be one variable instance returned", variables.get(0));

    verify(mockedQuery).disableBinaryFetching();
    // requirement to not break existing API; should be:
    // verify(mockedQuery).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }


  @Test
  public void testVariableInstanceRetrievalAsPost() {
    String queryVariableName = "aVariableInstanceName";
    Map<String, String> queryParameter = new HashMap<String, String>();
    queryParameter.put("variableName", queryVariableName);

    Response response = given().contentType(POST_JSON_CONTENT_TYPE).body(queryParameter)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .and()
          .body("size()", is(1))
          .body("[0].id", equalTo(mockInstanceBuilder.getId()))
          .body("[0].name", equalTo(mockInstanceBuilder.getName()))
          .body("[0].type", equalTo(VariableTypeHelper.toExpectedValueTypeName(mockInstanceBuilder.getTypedValue().getType())))
          .body("[0].value", equalTo(mockInstanceBuilder.getTypedValue().getValue()))
          .body("[0].processInstanceId", equalTo(mockInstanceBuilder.getProcessInstanceId()))
          .body("[0].executionId", equalTo(mockInstanceBuilder.getExecutionId()))
          .body("[0].caseInstanceId", equalTo(mockInstanceBuilder.getCaseInstanceId()))
          .body("[0].caseExecutionId", equalTo(mockInstanceBuilder.getCaseExecutionId()))
          .body("[0].taskId", equalTo(mockInstanceBuilder.getTaskId()))
          .body("[0].batchId", equalTo(mockInstanceBuilder.getBatchId()))
          .body("[0].activityInstanceId", equalTo(mockInstanceBuilder.getActivityInstanceId()))
          .body("[0].tenantId", equalTo(mockInstanceBuilder.getTenantId()))
          .body("[0].errorMessage", equalTo(mockInstanceBuilder.getErrorMessage()))
          .body("[0].serializedValue", nullValue())
        .when().post(VARIABLE_INSTANCE_QUERY_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    inOrder.verify(mockedQuery).variableName(queryVariableName);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> variables = from(content).getList("");
    Assert.assertEquals("There should be one process definition returned.", 1, variables.size());
    Assert.assertNotNull("There should be one process definition returned", variables.get(0));

    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(mockedQuery).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testAdditionalParametersExcludingVariableValues() {
    Map<String, String> queryParameters = new HashMap<String, String>();

    queryParameters.put("variableName", "aVariableName");
    queryParameters.put("variableNameLike", "aVariableNameLike");
    queryParameters.put("executionIdIn", "anExecutionId");
    queryParameters.put("processInstanceIdIn", "aProcessInstanceId");
    queryParameters.put("caseExecutionIdIn", "aCaseExecutionId");
    queryParameters.put("caseInstanceIdIn", "aCaseInstanceId");
    queryParameters.put("taskIdIn", "aTaskId");
    queryParameters.put("batchIdIn", "aBatchId");
    queryParameters.put("variableScopeIdIn", "aVariableScopeId");
    queryParameters.put("activityInstanceIdIn", "anActivityInstanceId");
    queryParameters.put("tenantIdIn", "anTenantId");

    given().queryParams(queryParameters)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableName(queryParameters.get("variableName"));
    verify(mockedQuery).variableNameLike(queryParameters.get("variableNameLike"));
    verify(mockedQuery).processInstanceIdIn(queryParameters.get("processInstanceIdIn"));
    verify(mockedQuery).executionIdIn(queryParameters.get("executionIdIn"));
    verify(mockedQuery).caseInstanceIdIn(queryParameters.get("caseInstanceIdIn"));
    verify(mockedQuery).caseExecutionIdIn(queryParameters.get("caseExecutionIdIn"));
    verify(mockedQuery).taskIdIn(queryParameters.get("taskIdIn"));
    verify(mockedQuery).batchIdIn(queryParameters.get("batchIdIn"));
    verify(mockedQuery).variableScopeIdIn(queryParameters.get("variableScopeIdIn"));
    verify(mockedQuery).activityInstanceIdIn(queryParameters.get("activityInstanceIdIn"));
    verify(mockedQuery).tenantIdIn(queryParameters.get("tenantIdIn"));
    verify(mockedQuery).list();

    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(mockedQuery).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testAdditionalParametersExcludingVariableValuesAsPost() {
    String aVariableName = "aVariableName";
    String aVariableNameLike = "aVariableNameLike";
    String aProcessInstanceId = "aProcessInstanceId";
    String anExecutionId = "anExecutionId";
    String aTaskId = "aTaskId";
    String aBatchId = "aBatchId";
    String aVariableScopeId = "aVariableScopeId";
    String anActivityInstanceId = "anActivityInstanceId";
    String aCaseInstanceId = "aCaseInstanceId";
    String aCaseExecutionId = "aCaseExecutionId";
    String aTenantId = "aTenantId";

    Map<String, Object> queryParameters = new HashMap<String, Object>();

    queryParameters.put("variableName", aVariableName);
    queryParameters.put("variableNameLike", aVariableNameLike);

    List<String> executionIdIn = new ArrayList<String>();
    executionIdIn.add(anExecutionId);
    queryParameters.put("executionIdIn", executionIdIn);

    List<String> processInstanceIdIn = new ArrayList<String>();
    processInstanceIdIn.add(aProcessInstanceId);
    queryParameters.put("processInstanceIdIn", processInstanceIdIn);

    List<String> caseExecutionIdIn = new ArrayList<String>();
    caseExecutionIdIn.add(aCaseExecutionId);
    queryParameters.put("caseExecutionIdIn", caseExecutionIdIn);

    List<String> caseInstanceIdIn = new ArrayList<String>();
    caseInstanceIdIn.add(aCaseInstanceId);
    queryParameters.put("caseInstanceIdIn", caseInstanceIdIn);

    List<String> taskIdIn = new ArrayList<String>();
    taskIdIn.add(aTaskId);
    queryParameters.put("taskIdIn", taskIdIn);

    List<String> batchIdIn = new ArrayList<>();
    batchIdIn.add(aBatchId);
    queryParameters.put("batchIdIn", batchIdIn);

    List<String> variableScopeIdIn = new ArrayList<String>();
    variableScopeIdIn.add(aVariableScopeId);
    queryParameters.put("variableScopeIdIn", variableScopeIdIn);

    List<String> activityInstanceIdIn = new ArrayList<String>();
    activityInstanceIdIn.add(anActivityInstanceId);
    queryParameters.put("activityInstanceIdIn", activityInstanceIdIn);

    List<String> tenantIdIn = new ArrayList<String>();
    tenantIdIn.add(aTenantId);
    queryParameters.put("tenantIdIn", tenantIdIn);

    given().contentType(POST_JSON_CONTENT_TYPE).body(queryParameters)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableName(aVariableName);
    verify(mockedQuery).variableNameLike(aVariableNameLike);
    verify(mockedQuery).processInstanceIdIn(aProcessInstanceId);
    verify(mockedQuery).executionIdIn(anExecutionId);
    verify(mockedQuery).taskIdIn(aTaskId);
    verify(mockedQuery).batchIdIn(aBatchId);
    verify(mockedQuery).variableScopeIdIn(aVariableScopeId);
    verify(mockedQuery).activityInstanceIdIn(anActivityInstanceId);
    verify(mockedQuery).tenantIdIn(aTenantId);
    verify(mockedQuery).list();
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(mockedQuery).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueGreaterThan() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_gt_" + variableValue;
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueGreaterThan(variableName, variableValue);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueGreaterThanEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_gteq_" + variableValue;
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueGreaterThanOrEqual(variableName, variableValue);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueLessThan() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_lt_" + variableValue;
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueLessThan(variableName, variableValue);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueLessThanEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_lteq_" + variableValue;
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueLessThanOrEqual(variableName, variableValue);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueLike() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_like_" + variableValue;
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueLike(variableName, variableValue);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueNotEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_neq_" + variableValue;
    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValuesEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;
    given().queryParam("variableValues", queryValue).queryParam("variableValuesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValuesNotEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_neq_" + variableValue;
    given().queryParam("variableValues", queryValue).queryParam("variableValuesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValuesLikeIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_like_" + variableValue;
    given().queryParam("variableValues", queryValue).queryParam("variableValuesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueLike(variableName, variableValue);
    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }


  @Test
  public void testVariableNamesEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;
    given().queryParam("variableValues", queryValue).queryParam("variableNamesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableNamesIgnoreCase();
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableNamesNotEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_neq_" + variableValue;
    given().queryParam("variableValues", queryValue).queryParam("variableNamesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(VARIABLE_INSTANCE_QUERY_URL);
    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableNamesIgnoreCase();
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueEqualsAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "eq");

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueEquals("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueGreaterThanAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "gt");

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueGreaterThan("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueGreaterThanEqualsAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "gteq");

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueGreaterThanOrEqual("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueLessThanAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "lt");

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueLessThan("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueLessThanEqualsAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "lteq");

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueLessThanOrEqual("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueLikeAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "like");

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueLike("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValueNotEqualsAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "neq");

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueNotEquals("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValuesEqualsIgnoreCaseAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "eq");
    
    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);
    json.put("variableValuesIgnoreCase", true);
    
    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).variableValueEquals("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValuesNotEqualsIgnoreCaseAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "neq");
    
    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);
    json.put("variableValuesIgnoreCase", true);
    
    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).variableValueNotEquals("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableValuesLikeIgnoreCaseAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "like");
    
    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);
    json.put("variableValuesIgnoreCase", true);
    
    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).variableValueLike("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }


  @Test
  public void testVariableNamesEqualsIgnoreCaseAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "eq");
    
    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);
    json.put("variableNamesIgnoreCase", true);
    
    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).matchVariableNamesIgnoreCase();
    verify(mockedQuery).variableValueEquals("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testVariableNamesNotEqualsIgnoreCaseAsPost() {
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "neq");
    
    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variableValues", variables);
    json.put("variableNamesIgnoreCase", true);
    
    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(VARIABLE_INSTANCE_QUERY_URL);
    
    verify(mockedQuery).matchVariableNamesIgnoreCase();
    verify(mockedQuery).variableValueNotEquals("varName", "varValue");
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
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

    given().queryParam("variableValues", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueEquals(variableName1, variableValue1);
    verify(mockedQuery).variableValueNotEquals(variableName2, variableValue2);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
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
    json.put("variableValues", variables);

    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).variableValueNotEquals(eq(anotherVariableName), argThat(EqualsPrimitiveValue.numberValue(anotherVariableValue)));
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testMultipleParameters() {
    String aProcessInstanceId = "aProcessInstanceId";
    String anotherProcessInstanceId = "anotherProcessInstanceId";

    String anExecutionId = "anExecutionId";
    String anotherExecutionId = "anotherExecutionId";

    String aTaskId = "aTaskId";
    String anotherTaskId = "anotherTaskId";

    String aVariableScopeId = "aVariableScopeId";
    String anotherVariableScopeId = "anotherVariableScopeId";

    String anActivityInstanceId = "anActivityInstanceId";
    String anotherActivityInstanceId = "anotherActivityInstanceId";

    given()
      .queryParam("processInstanceIdIn", aProcessInstanceId + "," + anotherProcessInstanceId)
      .queryParam("executionIdIn", anExecutionId + "," + anotherExecutionId)
      .queryParam("taskIdIn", aTaskId + "," + anotherTaskId)
      .queryParam("variableScopeIdIn", aVariableScopeId + "," + anotherVariableScopeId)
      .queryParam("activityInstanceIdIn", anActivityInstanceId + "," + anotherActivityInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).processInstanceIdIn(aProcessInstanceId, anotherProcessInstanceId);
    verify(mockedQuery).executionIdIn(anExecutionId, anotherExecutionId);
    verify(mockedQuery).taskIdIn(aTaskId, anotherTaskId);
    verify(mockedQuery).variableScopeIdIn(aVariableScopeId, anotherVariableScopeId);
    verify(mockedQuery).activityInstanceIdIn(anActivityInstanceId, anotherActivityInstanceId);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testMultipleParametersAsPost() {
    String aProcessInstanceId = "aProcessInstanceId";
    String anotherProcessInstanceId = "anotherProcessInstanceId";

    List<String> processDefinitionIdIn= new ArrayList<String>();
    processDefinitionIdIn.add(aProcessInstanceId);
    processDefinitionIdIn.add(anotherProcessInstanceId);

    String anExecutionId = "anExecutionId";
    String anotherExecutionId = "anotherExecutionId";

    List<String> executionIdIn= new ArrayList<String>();
    executionIdIn.add(anExecutionId);
    executionIdIn.add(anotherExecutionId);

    String aTaskId = "aTaskId";
    String anotherTaskId = "anotherTaskId";

    List<String> taskIdIn= new ArrayList<String>();
    taskIdIn.add(aTaskId);
    taskIdIn.add(anotherTaskId);

    String aVariableScopeId = "aVariableScopeId";
    String anotherVariableScopeId = "anotherVariableScopeId";

    List<String> variableScopeIdIn= new ArrayList<String>();
    variableScopeIdIn.add(aVariableScopeId);
    variableScopeIdIn.add(anotherVariableScopeId);

    String anActivityInstanceId = "anActivityInstanceId";
    String anotherActivityInstanceId = "anotherActivityInstanceId";

    List<String> activityInstanceIdIn= new ArrayList<String>();
    activityInstanceIdIn.add(anActivityInstanceId);
    activityInstanceIdIn.add(anotherActivityInstanceId);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("processInstanceIdIn", processDefinitionIdIn);
    json.put("executionIdIn", executionIdIn);
    json.put("taskIdIn", taskIdIn);
    json.put("variableScopeIdIn", variableScopeIdIn);
    json.put("activityInstanceIdIn", activityInstanceIdIn);

    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(VARIABLE_INSTANCE_QUERY_URL);

    verify(mockedQuery).processInstanceIdIn(aProcessInstanceId, anotherProcessInstanceId);
    verify(mockedQuery).executionIdIn(anExecutionId, anotherExecutionId);
    verify(mockedQuery).taskIdIn(aTaskId, anotherTaskId);
    verify(mockedQuery).variableScopeIdIn(aVariableScopeId, anotherVariableScopeId);
    verify(mockedQuery).activityInstanceIdIn(anActivityInstanceId, anotherActivityInstanceId);
    verify(mockedQuery).disableBinaryFetching();

    // requirement to not break existing API; should be:
    // verify(variableInstanceQueryMock).disableCustomObjectDeserialization();
    verify(mockedQuery, never()).disableCustomObjectDeserialization();
  }

  @Test
  public void testQueryCount() {
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(VARIABLE_INSTANCE_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testQueryCountForPost() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().post(VARIABLE_INSTANCE_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

}
