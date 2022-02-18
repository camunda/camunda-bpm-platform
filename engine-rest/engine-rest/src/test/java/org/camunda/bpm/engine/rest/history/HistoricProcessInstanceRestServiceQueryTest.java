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
package org.camunda.bpm.engine.rest.history;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
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

import io.restassured.http.ContentType;
import io.restassured.response.Response;


public class HistoricProcessInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String QUERY_PARAM_EXECUTED_JOB_BEFORE = "executedJobBefore";
  protected static final String QUERY_PARAM_EXECUTED_JOB_AFTER = "executedJobAfter";
  protected static final String QUERY_PARAM_EXECUTED_ACTIVITY_BEFORE = "executedActivityBefore";
  protected static final String QUERY_PARAM_EXECUTED_ACTIVITY_AFTER = "executedActivityAfter";
  protected static final String QUERY_PARAM_EXECUTED_ACTIVITY_IDS = "executedActivityIdIn";
  protected static final String QUERY_PARAM_ACTIVE_ACTIVITY_IDS = "activeActivityIdIn";

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

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
    executeAndVerifySorting("definitionKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionName", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionName();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionVersion", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionVersion();
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
      .orderBy("instanceId").desc()
      .orderBy("startTime").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByProcessInstanceStartTime();
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
    String returnedProcessDefinitionKey = from(content).getString("[0].processDefinitionKey");
    String returnedProcessDefinitionName = from(content).getString("[0].processDefinitionName");
    int returnedProcessDefinitionVersion= from(content).getInt("[0].processDefinitionVersion");
    String returnedStartTime = from(content).getString("[0].startTime");
    String returnedEndTime = from(content).getString("[0].endTime");
    String returnedRemovalTime = from(content).getString("[0].removalTime");
    long returnedDurationInMillis = from(content).getLong("[0].durationInMillis");
    String returnedStartUserId = from(content).getString("[0].startUserId");
    String returnedStartActivityId = from(content).getString("[0].startActivityId");
    String returnedDeleteReason = from(content).getString("[0].deleteReason");
    String returnedRootProcessInstanceId = from(content).getString("[0].rootProcessInstanceId");
    String returnedSuperProcessInstanceId = from(content).getString("[0].superProcessInstanceId");
    String returnedSuperCaseInstanceId = from(content).getString("[0].superCaseInstanceId");
    String returnedCaseInstanceId = from(content).getString("[0].caseInstanceId");
    String returnedTenantId = from(content).getString("[0].tenantId");
    String returnedState = from(content).getString("[0].state");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY, returnedProcessInstanceBusinessKey);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, returnedProcessDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_NAME, returnedProcessDefinitionName);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_VERSION, returnedProcessDefinitionVersion);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME, returnedStartTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_END_TIME, returnedEndTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_REMOVAL_TIME, returnedRemovalTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS, returnedDurationInMillis);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_USER_ID, returnedStartUserId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_ACTIVITY_ID, returnedStartActivityId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON, returnedDeleteReason);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_ROOT_PROCESS_INSTANCE_ID, returnedRootProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_PROCESS_INSTANCE_ID, returnedSuperProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_CASE_INSTANCE_ID, returnedSuperCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STATE, returnedState);
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
    parameters.put("superCaseInstanceId", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_CASE_INSTANCE_ID);
    parameters.put("subCaseInstanceId", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUB_CASE_INSTANCE_ID);
    parameters.put("caseInstanceId", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_CASE_INSTANCE_ID);
    parameters.put("state", MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STATE);
    parameters.put("incidentType", MockProvider.EXAMPLE_INCIDENT_TYPE);

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
    verify(mockedQuery).superCaseInstanceId(stringQueryParameters.get("superCaseInstanceId"));
    verify(mockedQuery).subCaseInstanceId(stringQueryParameters.get("subCaseInstanceId"));
    verify(mockedQuery).caseInstanceId(stringQueryParameters.get("caseInstanceId"));
    verify(mockedQuery).incidentType(stringQueryParameters.get("incidentType"));

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
  public void testQueryWithIncidents() {
    given()
      .queryParam("withIncidents", true)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).withIncidents();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testQueryWithIncidentsAsPost() {
    Map<String, Boolean> body = new HashMap<String, Boolean>();
    body.put("withIncidents", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).withIncidents();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testQueryWithIncidentStatusOpen() {
    given()
      .queryParam("incidentStatus", "open")
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).incidentStatus("open");
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testQueryWithIncidentStatusOpenAsPost() {
    Map<String, String> body = new HashMap<String, String>();
    body.put("incidentStatus", "open");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).incidentStatus("open");
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testQueryCountIncidentStatusOpenForPost() {
    Map<String,String> body = new HashMap<String, String>();
    body.put("incidentStatus", "open");
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
    .then()
      .expect()
        .body("count", equalTo(1))
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
    verify(mockedQuery).incidentStatus("open");
  }

  @Test
  public void testQueryWithIncidentStatusResolved() {
    given()
      .queryParam("incidentStatus", "resolved")
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).incidentStatus("resolved");
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testQueryWithIncidentStatusResolvedAsPost() {
    Map<String, String> body = new HashMap<String, String>();
    body.put("incidentStatus", "resolved");

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).incidentStatus("resolved");
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testQueryCountIncidentStatusResolvedForPost() {
    Map<String,String> body = new HashMap<String, String>();
    body.put("incidentStatus", "resolved");
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
    .then()
      .expect()
        .body("count", equalTo(1))
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
    verify(mockedQuery).incidentStatus("resolved");
  }

  @Test
  public void testQueryIncidentType() {
    given()
      .queryParam("incidentType", MockProvider.EXAMPLE_INCIDENT_TYPE)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).incidentType(MockProvider.EXAMPLE_INCIDENT_TYPE);
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testQueryIncidentTypeAsPost() {
    Map<String, String> body = new HashMap<String, String>();
    body.put("incidentType", MockProvider.EXAMPLE_INCIDENT_TYPE);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).incidentType(MockProvider.EXAMPLE_INCIDENT_TYPE);
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testQueryIncidentMessage() {
    given()
      .queryParam("incidentMessage", MockProvider.EXAMPLE_INCIDENT_MESSAGE)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).incidentMessage(MockProvider.EXAMPLE_INCIDENT_MESSAGE);
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testQueryIncidentMessageAsPost() {
    Map<String, String> body = new HashMap<String, String>();
    body.put("incidentMessage", MockProvider.EXAMPLE_INCIDENT_MESSAGE);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).incidentMessage(MockProvider.EXAMPLE_INCIDENT_MESSAGE);
    inOrder.verify(mockedQuery).list();
  }
  @Test
  public void testQueryIncidentMessageLike() {
    given()
      .queryParam("incidentMessageLike", MockProvider.EXAMPLE_INCIDENT_MESSAGE_LIKE)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).incidentMessageLike(MockProvider.EXAMPLE_INCIDENT_MESSAGE_LIKE);
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testQueryIncidentMessageLikeAsPost() {
    Map<String, String> body = new HashMap<String, String>();
    body.put("incidentMessageLike", MockProvider.EXAMPLE_INCIDENT_MESSAGE_LIKE);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).incidentMessageLike(MockProvider.EXAMPLE_INCIDENT_MESSAGE_LIKE);
    inOrder.verify(mockedQuery).list();
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

    verifyProcessInstanceIdSetInvocation();
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

    verifyProcessInstanceIdSetInvocation();
  }

  private Map<String, Set<String>> getCompleteProcessInstanceIdSetQueryParameters() {
    Map<String, Set<String>> parameters = new HashMap<String, Set<String>>();

    Set<String> processInstanceIds = new HashSet<String>();
    processInstanceIds.add("firstProcessInstanceId");
    processInstanceIds.add("secondProcessInstanceId");

    parameters.put("processInstanceIds", processInstanceIds);

    return parameters;
  }

  private void verifyProcessInstanceIdSetInvocation() {
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

    verifyProcessDefinitionKeyNotInListInvocation();
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

    verifyProcessDefinitionKeyNotInListInvocation();
  }

  private Map<String, List<String>> getCompleteProcessDefinitionKeyNotInListQueryParameters() {
    Map<String, List<String>> parameters = new HashMap<String, List<String>>();

    List<String> processInstanceIds = new ArrayList<String>();
    processInstanceIds.add("firstProcessInstanceKey");
    processInstanceIds.add("secondProcessInstanceKey");

    parameters.put("processDefinitionKeyNotIn", processInstanceIds);

    return parameters;
  }

  private void verifyProcessDefinitionKeyNotInListInvocation() {
    Map<String, List<String>> parameters = getCompleteProcessDefinitionKeyNotInListQueryParameters();

    verify(mockedQuery).processDefinitionKeyNotIn(parameters.get("processDefinitionKeyNotIn"));
    verify(mockedQuery).list();
  }

  @Test
  public void shouldQueryByBusinessKeyIn() {
    given()
        .queryParam("processInstanceBusinessKeyIn", "business-key-one,business-key-two")
        .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
        .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyBusinessKeyInListInvocation();
  }

  @Test
  public void shouldQueryByBusinessKeyInAsPost() {
    Map<String, List<String>> parameters = getCompleteBusinessKeyInListQueryParameters();

    given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(parameters)
        .then()
        .expect()
        .statusCode(Status.OK.getStatusCode())
        .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyBusinessKeyInListInvocation();
  }

  protected Map<String, List<String>> getCompleteBusinessKeyInListQueryParameters() {
    Map<String, List<String>> parameters = new HashMap<>();

    List<String> processInstanceBusinessKeys = new ArrayList<>();
    processInstanceBusinessKeys.add("business-key-one");
    processInstanceBusinessKeys.add("business-key-two");

    parameters.put("processInstanceBusinessKeyIn", processInstanceBusinessKeys);

    return parameters;
  }

  protected void verifyBusinessKeyInListInvocation() {
    Map<String, List<String>> parameters = getCompleteBusinessKeyInListQueryParameters();
    List<String> value = parameters.get("processInstanceBusinessKeyIn");

    verify(mockedQuery).processInstanceBusinessKeyIn(value.toArray(new String[0]));
    verify(mockedQuery).list();
  }

  @Test
  public void testQueryByProcessDefinitionKeyIn() {
    given()
      .queryParam("processDefinitionKeyIn", "firstProcessDefinitionKey,secondProcessDefinitionKey")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyProcessDefinitionKeyInListInvocation();
  }

  @Test
  public void testQueryByProcessDefinitionKeyInAsPost() {
    Map<String, List<String>> parameters = getCompleteProcessDefinitionKeyInListQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyProcessDefinitionKeyInListInvocation();
  }

  private Map<String, List<String>> getCompleteProcessDefinitionKeyInListQueryParameters() {
    Map<String, List<String>> parameters = new HashMap<String, List<String>>();

    List<String> processInstanceIds = new ArrayList<String>();
    processInstanceIds.add("firstProcessDefinitionKey");
    processInstanceIds.add("secondProcessDefinitionKey");

    parameters.put("processDefinitionKeyIn", processInstanceIds);

    return parameters;
  }

  private void verifyProcessDefinitionKeyInListInvocation() {
    Map<String, List<String>> parameters = getCompleteProcessDefinitionKeyInListQueryParameters();
    List<String> value = parameters.get("processDefinitionKeyIn");

    verify(mockedQuery).processDefinitionKeyIn(value.toArray(new String[value.size()]));
    verify(mockedQuery).list();
  }

  @Test
  public void testVariableValueEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
  }

  @Test
  public void testVariableValueGreaterThan() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_gt_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueGreaterThan(variableName, variableValue);
  }

  @Test
  public void testVariableValueGreaterThanEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_gteq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueGreaterThanOrEqual(variableName, variableValue);
  }

  @Test
  public void testVariableValueLessThan() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_lt_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueLessThan(variableName, variableValue);
  }

  @Test
  public void testVariableValueLessThanEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_lteq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueLessThanOrEqual(variableName, variableValue);
  }

  @Test
  public void testVariableValueLike() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_like_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueLike(variableName, variableValue);
  }

  @Test
  public void testVariableValueNotEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_neq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
  }

  @Test
  public void testVariableValuesEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;
    given().queryParam("variables", queryValue).queryParam("variableValuesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableValuesIgnoreCase();
  }

  @Test
  public void testVariableValuesNotEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_neq_" + variableValue;
    given().queryParam("variables", queryValue).queryParam("variableValuesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableValuesIgnoreCase();
  }

  @Test
  public void testVariableValuesLikeIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_like_" + variableValue;
    given().queryParam("variables", queryValue).queryParam("variableValuesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueLike(variableName, variableValue);
    verify(mockedQuery).matchVariableValuesIgnoreCase();
  }


  @Test
  public void testVariableNamesEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;
    given().queryParam("variables", queryValue).queryParam("variableNamesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableNamesIgnoreCase();
  }

  @Test
  public void testVariableNamesNotEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_neq_" + variableValue;
    given().queryParam("variables", queryValue).queryParam("variableNamesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableNamesIgnoreCase();
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
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueEquals("varName", "varValue");
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
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueGreaterThan("varName", "varValue");
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
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueGreaterThanOrEqual("varName", "varValue");
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
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLessThan("varName", "varValue");
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
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLessThanOrEqual("varName", "varValue");
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
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLike("varName", "varValue");
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
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueNotEquals("varName", "varValue");
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
    json.put("variables", variables);
    json.put("variableValuesIgnoreCase", true);
    
    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    
    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).variableValueEquals("varName", "varValue");
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
    json.put("variables", variables);
    json.put("variableValuesIgnoreCase", true);
    
    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    
    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).variableValueNotEquals("varName", "varValue");
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
    json.put("variables", variables);
    json.put("variableValuesIgnoreCase", true);
    
    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    
    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).variableValueLike("varName", "varValue");
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
    json.put("variables", variables);
    json.put("variableNamesIgnoreCase", true);
    
    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    
    verify(mockedQuery).matchVariableNamesIgnoreCase();
    verify(mockedQuery).variableValueEquals("varName", "varValue");
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
    json.put("variables", variables);
    json.put("variableNamesIgnoreCase", true);
    
    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);
    
    verify(mockedQuery).matchVariableNamesIgnoreCase();
    verify(mockedQuery).variableValueNotEquals("varName", "varValue");
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
    verify(mockedQuery).variableValueNotEquals(eq(anotherVariableName), argThat(EqualsPrimitiveValue.numberValue(anotherVariableValue)));
  }

  @Test
  public void testTenantIdListParameter() {
    mockedQuery = setUpMockHistoricProcessInstanceQuery(createMockHistoricProcessInstancesTwoTenants());

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

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
    mockedQuery = setUpMockHistoricProcessInstanceQuery(createMockHistoricProcessInstancesTwoTenants());

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST.split(","));

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

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
  public void testWithoutTenantIdParameter() {
    mockedQuery = setUpMockHistoricProcessInstanceQuery(Collections.singletonList(MockProvider.createMockHistoricProcessInstance(null)));

    Response response = given()
      .queryParam("withoutTenantId", true)
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

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
    mockedQuery = setUpMockHistoricProcessInstanceQuery(Collections.singletonList(MockProvider.createMockHistoricProcessInstance(null)));

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    queryParameters.put("withoutTenantId", true);

    Response response = given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(queryParameters)
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<String> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId1).isEqualTo(null);
  }

  private List<HistoricProcessInstance> createMockHistoricProcessInstancesTwoTenants() {
    return Arrays.asList(
        MockProvider.createMockHistoricProcessInstance(MockProvider.EXAMPLE_TENANT_ID),
        MockProvider.createMockHistoricProcessInstance(MockProvider.ANOTHER_EXAMPLE_TENANT_ID));
  }

  @Test
  public void testExecutedActivityBeforeAndAfterTimeQuery() {
    given()
      .queryParam(QUERY_PARAM_EXECUTED_ACTIVITY_BEFORE, MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE)
      .queryParam(QUERY_PARAM_EXECUTED_ACTIVITY_AFTER, MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyExecutedActivityParameterQueryInvocations();
  }

  @Test
  public void testExecutedActivityBeforeAndAfterTimeQueryAsPost() {
    Map<String, Date> parameters = getCompleteExecutedActivityDateQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyExecutedActivityParameterQueryInvocations();
  }

  @Test
  public void testExecutedActivityBeforeAndAfterTimeAsStringQueryAsPost() {
    Map<String, String> parameters = getCompleteExecutedActivityDateAsStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyStringExecutedActivityParameterQueryInvocations();
  }


  private void verifyExecutedActivityParameterQueryInvocations() {
    Map<String, Date> startDateParameters = getCompleteExecutedActivityDateQueryParameters();

    verify(mockedQuery).executedActivityBefore(startDateParameters.get(QUERY_PARAM_EXECUTED_ACTIVITY_BEFORE));
    verify(mockedQuery).executedActivityAfter(startDateParameters.get(QUERY_PARAM_EXECUTED_ACTIVITY_AFTER));

    verify(mockedQuery).list();
  }

  private void verifyStringExecutedActivityParameterQueryInvocations() {
    Map<String, String> startDateParameters = getCompleteExecutedActivityDateAsStringQueryParameters();

    verify(mockedQuery).executedActivityBefore(DateTimeUtil.parseDate(startDateParameters.get(QUERY_PARAM_EXECUTED_ACTIVITY_BEFORE)));
    verify(mockedQuery).executedActivityAfter(DateTimeUtil.parseDate(startDateParameters.get(QUERY_PARAM_EXECUTED_ACTIVITY_AFTER)));

    verify(mockedQuery).list();
  }

  private Map<String, Date> getCompleteExecutedActivityDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<String, Date>();

    parameters.put(QUERY_PARAM_EXECUTED_ACTIVITY_BEFORE, DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE));
    parameters.put(QUERY_PARAM_EXECUTED_ACTIVITY_AFTER, DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER));

    return parameters;
  }

  private Map<String, String> getCompleteExecutedActivityDateAsStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put(QUERY_PARAM_EXECUTED_ACTIVITY_AFTER, MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER);
    parameters.put(QUERY_PARAM_EXECUTED_ACTIVITY_BEFORE, MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE);

    return parameters;
  }

  // ===================================================================================================================

  @Test
  public void testExecutedJobBeforeAndAfterTimeQuery() {
    given()
      .queryParam(QUERY_PARAM_EXECUTED_JOB_BEFORE, MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE)
      .queryParam(QUERY_PARAM_EXECUTED_JOB_AFTER, MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyExecutedJobParameterQueryInvocations();
  }

  @Test
  public void testExecutedJobBeforeAndAfterTimeQueryAsPost() {
    Map<String, Date> parameters = getCompleteExecutedJobDateQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyExecutedJobParameterQueryInvocations();
  }

  @Test
  public void testExecutedJobBeforeAndAfterTimeAsStringQueryAsPost() {
    Map<String, String> parameters = getCompleteExecutedJobDateAsStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verifyStringExecutedJobParameterQueryInvocations();
  }

  @Test
  public void testExecutedActivityIdIn() {

    given()
      .queryParam(QUERY_PARAM_EXECUTED_ACTIVITY_IDS, "1,2")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).executedActivityIdIn("1", "2");
  }

  @Test
  public void testExecutedActivityIdInAsPost() {
    Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    parameters.put(QUERY_PARAM_EXECUTED_ACTIVITY_IDS, Arrays.asList("1", "2"));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).executedActivityIdIn("1", "2");
  }

  @Test
  public void testActiveActivityIdIn() {

    given()
      .queryParam(QUERY_PARAM_ACTIVE_ACTIVITY_IDS, "1,2")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).activeActivityIdIn("1", "2");
  }

  @Test
  public void testActiveActivityIdInAsPost() {
    Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    parameters.put(QUERY_PARAM_ACTIVE_ACTIVITY_IDS, Arrays.asList("1", "2"));

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).activeActivityIdIn("1", "2");
  }

  @Test
  public void testQueryWithRootIncidents() {
    given()
      .queryParam("withRootIncidents", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).withRootIncidents();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  public void testQueryWithRootIncidentsAsPost() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("withRootIncidents", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).withRootIncidents();
  }

  private void verifyExecutedJobParameterQueryInvocations() {
    Map<String, Date> startDateParameters = getCompleteExecutedJobDateQueryParameters();

    verify(mockedQuery).executedJobBefore(startDateParameters.get(QUERY_PARAM_EXECUTED_JOB_BEFORE));
    verify(mockedQuery).executedJobAfter(startDateParameters.get(QUERY_PARAM_EXECUTED_JOB_AFTER));

    verify(mockedQuery).list();
  }

  private void verifyStringExecutedJobParameterQueryInvocations() {
    Map<String, String> startDateParameters = getCompleteExecutedJobDateAsStringQueryParameters();

    verify(mockedQuery).executedJobBefore(DateTimeUtil.parseDate(startDateParameters.get(QUERY_PARAM_EXECUTED_JOB_BEFORE)));
    verify(mockedQuery).executedJobAfter(DateTimeUtil.parseDate(startDateParameters.get(QUERY_PARAM_EXECUTED_JOB_AFTER)));

    verify(mockedQuery).list();
  }

  private Map<String, Date> getCompleteExecutedJobDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<String, Date>();

    parameters.put(QUERY_PARAM_EXECUTED_JOB_BEFORE, DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE));
    parameters.put(QUERY_PARAM_EXECUTED_JOB_AFTER, DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER));

    return parameters;
  }

  private Map<String, String> getCompleteExecutedJobDateAsStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put(QUERY_PARAM_EXECUTED_JOB_AFTER, MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER);
    parameters.put(QUERY_PARAM_EXECUTED_JOB_BEFORE, MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE);

    return parameters;
  }

  @Test
  public void testQueryByActive() {
    given()
      .queryParam("active", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).active();
  }


  @Test
  public void testQueryByCompleted() {
    given()
      .queryParam("completed", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).completed();
  }

  @Test
  public void testQueryBySuspended() {
    given()
      .queryParam("suspended", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).suspended();
  }

  @Test
  public void testQueryByExternallyTerminated() {
    given()
      .queryParam("externallyTerminated", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).externallyTerminated();
  }

  @Test
  public void testQueryByInternallyTerminated() {
    given()
      .queryParam("internallyTerminated", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).internallyTerminated();
  }

  @Test
  public void testQueryByTwoStates() {
    String message = "expected exception";
    doThrow(new BadUserRequestException(message)).when(mockedQuery).completed();

    given()
      .queryParam("active", true)
      .queryParam("completed", true)
    .then()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(BadUserRequestException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).active();
  }

  @Test
  public void testQueryByActiveAsPost() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("active", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).active();
  }

  @Test
  public void testQueryByCompletedAsPost() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("completed", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).completed();
  }

  @Test
  public void testQueryBySuspendedAsPost() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("suspended", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).suspended();
  }

  @Test
  public void testQueryByExternallyTerminatedAsPost() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("externallyTerminated", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).externallyTerminated();
  }


  @Test
  public void testQueryByInternallyTerminatedAsPost() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("internallyTerminated", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).internallyTerminated();
  }

  @Test
  public void testQueryByTwoStatesAsPost() {
    String message = "expected exception";
    doThrow(new BadUserRequestException(message)).when(mockedQuery).completed();

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("active", true);
    parameters.put("completed", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(BadUserRequestException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).active();
  }

  @Test
  public void testQueryByRootProcessInstances() {
    given()
      .queryParam("rootProcessInstances", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).rootProcessInstances();
  }

  @Test
  public void testQueryByRootProcessInstancesAsPost() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("rootProcessInstances", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_PROCESS_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).rootProcessInstances();
  }

}
