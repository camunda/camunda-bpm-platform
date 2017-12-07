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
package org.camunda.bpm.engine.rest.history;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDecisionInstanceQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.JsonPathUtil;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

import javax.ws.rs.core.Response.Status;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class HistoricDecisionInstanceRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_DECISION_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/decision-instance";
  protected static final String HISTORIC_SINGLE_DECISION_INSTANCE_URL = HISTORIC_DECISION_INSTANCE_URL + "/{id}";
  protected static final String HISTORIC_DECISION_INSTANCES_DELETE_ASYNC_URL = HISTORIC_DECISION_INSTANCE_URL + "/delete";

  protected HistoryService historyServiceMock;
  protected HistoricDecisionInstance historicInstanceMock;
  protected HistoricDecisionInstanceQuery historicQueryMock;

  @Before
  public void setUpRuntimeData() {
    historyServiceMock = mock(HistoryService.class);

    // runtime service
    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);

    historicInstanceMock = MockProvider.createMockHistoricDecisionInstance();
    historicQueryMock = mock(HistoricDecisionInstanceQuery.class);

    when(historyServiceMock.createHistoricDecisionInstanceQuery()).thenReturn(historicQueryMock);
    when(historicQueryMock.decisionInstanceId(anyString())).thenReturn(historicQueryMock);
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);
  }

  @Test
  public void testGetSingleHistoricDecisionInstance() {
    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock).singleResult();

    String content = response.asString();

    String returnedHistoricDecisionInstanceId = from(content).getString("id");
    String returnedDecisionDefinitionId = from(content).getString("decisionDefinitionId");
    String returnedDecisionDefinitionKey = from(content).getString("decisionDefinitionKey");
    String returnedDecisionDefinitionName = from(content).getString("decisionDefinitionName");
    String returnedEvaluationTime = from(content).getString("evaluationTime");
    String returnedProcessDefinitionId = from(content).getString("processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("processDefinitionKey");
    String returnedProcessInstanceId = from(content).getString("processInstanceId");
    String returnedCaseDefinitionId = from(content).getString("caseDefinitionId");
    String returnedCaseDefinitionKey = from(content).getString("caseDefinitionKey");
    String returnedCaseInstanceId = from(content).getString("caseInstanceId");
    String returnedActivityId = from(content).getString("activityId");
    String returnedActivityInstanceId = from(content).getString("activityInstanceId");
    String returnedUserId = from(content).getString("userId");
    List<Map<String, Object>> returnedInputs = from(content).getList("inputs");
    List<Map<String, Object>> returnedOutputs = from(content).getList("outputs");
    Double returnedCollectResultValue = from(content).getDouble("collectResultValue");
    String returnedTenantId = from(content).getString("tenantId");
    String returnedRootDecisionInstanceId = from(content).getString("rootDecisionInstanceId");
    String returnedDecisionRequirementsDefinitionId = from(content).getString("decisionRequirementsDefinitionId");
    String returnedDecisionRequirementsDefinitionKey = from(content).getString("decisionRequirementsDefinitionKey");

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
    assertThat(returnedUserId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_USER_ID));
    assertThat(returnedInputs, is(nullValue()));
    assertThat(returnedOutputs, is(nullValue()));
    assertThat(returnedCollectResultValue, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_COLLECT_RESULT_VALUE));
    assertThat(returnedTenantId, is(MockProvider.EXAMPLE_TENANT_ID));
    assertThat(returnedRootDecisionInstanceId, is(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID));
    assertThat(returnedDecisionRequirementsDefinitionId, is(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID));
    assertThat(returnedDecisionRequirementsDefinitionKey, is(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY));
  }

  @Test
  public void testGetSingleHistoricDecisionInstanceWithInputs() {
    historicInstanceMock = MockProvider.createMockHistoricDecisionInstanceWithInputs();
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);

    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
        .queryParam("includeInputs", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock).includeInputs();
    inOrder.verify(historicQueryMock, never()).includeOutputs();
    inOrder.verify(historicQueryMock).singleResult();

    String content = response.asString();

    List<Map<String, Object>> returnedInputs = from(content).getList("inputs");
    List<Map<String, Object>> returnedOutputs = from(content).getList("outputs");
    assertThat(returnedInputs, is(notNullValue()));
    assertThat(returnedInputs, hasSize(3));
    assertThat(returnedOutputs, is(nullValue()));
  }

  @Test
  public void testGetSingleHistoricDecisionInstanceWithOutputs() {
    historicInstanceMock = MockProvider.createMockHistoricDecisionInstanceWithOutputs();
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);

    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
        .queryParam("includeOutputs", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock, never()).includeInputs();
    inOrder.verify(historicQueryMock).includeOutputs();
    inOrder.verify(historicQueryMock).singleResult();

    String content = response.asString();

    List<Map<String, Object>> returnedInputs = from(content).getList("inputs");
    List<Map<String, Object>> returnedOutputs = from(content).getList("outputs");
    assertThat(returnedInputs, is(nullValue()));
    assertThat(returnedOutputs, is(notNullValue()));
    assertThat(returnedOutputs, hasSize(3));
  }

  @Test
  public void testGetSingleHistoricDecisionInstanceWithInputsAndOutputs() {
    historicInstanceMock = MockProvider.createMockHistoricDecisionInstanceWithInputsAndOutputs();
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);

    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
        .queryParam("includeInputs", true)
        .queryParam("includeOutputs", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock).includeInputs();
    inOrder.verify(historicQueryMock).includeOutputs();
    inOrder.verify(historicQueryMock).singleResult();

    String content = response.asString();

    List<Map<String, Object>> returnedInputs = from(content).getList("inputs");
    List<Map<String, Object>> returnedOutputs = from(content).getList("outputs");
    assertThat(returnedInputs, is(notNullValue()));
    assertThat(returnedInputs, hasSize(3));
    assertThat(returnedOutputs, is(notNullValue()));
    assertThat(returnedOutputs, hasSize(3));
  }

  @Test
  public void testGetSingleHistoricDecisionInstanceWithDisabledBinaryFetching() {
    historicInstanceMock = MockProvider.createMockHistoricDecisionInstanceWithInputsAndOutputs();
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);

    given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
        .queryParam("disableBinaryFetching", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock).disableBinaryFetching();
    inOrder.verify(historicQueryMock).singleResult();
  }

  @Test
  public void testGetSingleHistoricDecisionInstanceWithDisabledCustomObjectDeserialization() {
    historicInstanceMock = MockProvider.createMockHistoricDecisionInstanceWithInputsAndOutputs();
    when(historicQueryMock.singleResult()).thenReturn(historicInstanceMock);

    given()
        .pathParam("id", MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID)
        .queryParam("disableCustomObjectDeserialization", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);

    InOrder inOrder = inOrder(historicQueryMock);
    inOrder.verify(historicQueryMock).decisionInstanceId(MockProvider.EXAMPLE_HISTORIC_DECISION_INSTANCE_ID);
    inOrder.verify(historicQueryMock).disableCustomObjectDeserialization();
    inOrder.verify(historicQueryMock).singleResult();
  }

  @Test
  public void testGetNonExistingHistoricCaseInstance() {
    when(historicQueryMock.singleResult()).thenReturn(null);

    given()
      .pathParam("id", MockProvider.NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Historic decision instance with id '" + MockProvider.NON_EXISTING_ID + "' does not exist"))
    .when()
      .get(HISTORIC_SINGLE_DECISION_INSTANCE_URL);
  }

  @Test
  public void testDeleteAsync() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_DECISION_INSTANCE_ID);

    Batch batchEntity = MockProvider.createMockBatch();

    when(historyServiceMock.deleteHistoricDecisionInstancesAsync(
        anyListOf(String.class),
        any(HistoricDecisionInstanceQuery.class),
        anyString()
    )).thenReturn(batchEntity);

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    messageBodyJson.put("historicDecisionInstanceIds", ids);
    messageBodyJson.put("deleteReason", "a-delete-reason");

    Response response = given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(HISTORIC_DECISION_INSTANCES_DELETE_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(historyServiceMock, times(1)).deleteHistoricDecisionInstancesAsync(eq(ids), eq((HistoricDecisionInstanceQuery) null), eq("a-delete-reason"));
  }

  @Test
  public void testDeleteAsyncWithQuery() {
    Batch batchEntity = MockProvider.createMockBatch();

    when(historyServiceMock.deleteHistoricDecisionInstancesAsync(
        anyListOf(String.class),
        any(HistoricDecisionInstanceQuery.class),
        anyString()
    )).thenReturn(batchEntity);

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    HistoricDecisionInstanceQueryDto query = new HistoricDecisionInstanceQueryDto();
    query.setDecisionDefinitionKey("decision");
    messageBodyJson.put("historicDecisionInstanceQuery", query);
    messageBodyJson.put("deleteReason", "a-delete-reason");

    Response response = given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(HISTORIC_DECISION_INSTANCES_DELETE_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(historyServiceMock, times(1)).deleteHistoricDecisionInstancesAsync(eq((List<String>) null), any(HistoricDecisionInstanceQuery.class), eq("a-delete-reason"));
  }

  @Test
  public void testDeleteAsyncWithIdsAndQuery() {
    Batch batchEntity = MockProvider.createMockBatch();

    when(historyServiceMock.deleteHistoricDecisionInstancesAsync(
        anyListOf(String.class),
        any(HistoricDecisionInstanceQuery.class),
        anyString()
    )).thenReturn(batchEntity);

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    HistoricDecisionInstanceQueryDto query = new HistoricDecisionInstanceQueryDto();
    query.setDecisionDefinitionKey("decision");
    messageBodyJson.put("historicDecisionInstanceQuery", query);

    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_DECISION_INSTANCE_ID);
    messageBodyJson.put("historicDecisionInstanceIds", ids);
    messageBodyJson.put("deleteReason", "a-delete-reason");

    Response response = given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(HISTORIC_DECISION_INSTANCES_DELETE_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(historyServiceMock, times(1)).deleteHistoricDecisionInstancesAsync(eq(ids), any(HistoricDecisionInstanceQuery.class), eq("a-delete-reason"));
  }

  @Test
  public void testDeleteAsyncWithBadRequestQuery() {
    doThrow(new BadUserRequestException("process instance ids are empty"))
        .when(historyServiceMock).deleteHistoricDecisionInstancesAsync(eq((List<String>) null), eq((HistoricDecisionInstanceQuery) null), anyString());

    given()
        .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
        .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .when().post(HISTORIC_DECISION_INSTANCES_DELETE_ASYNC_URL);
  }

  protected void verifyBatchJson(String batchJson) {
    BatchDto batch = JsonPathUtil.from(batchJson).getObject("", BatchDto.class);
    assertNotNull("The returned batch should not be null.", batch);
    assertEquals(MockProvider.EXAMPLE_BATCH_ID, batch.getId());
    assertEquals(MockProvider.EXAMPLE_BATCH_TYPE, batch.getType());
    assertEquals(MockProvider.EXAMPLE_BATCH_TOTAL_JOBS, batch.getTotalJobs());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOBS_PER_SEED, batch.getBatchJobsPerSeed());
    assertEquals(MockProvider.EXAMPLE_INVOCATIONS_PER_BATCH_JOB, batch.getInvocationsPerBatchJob());
    assertEquals(MockProvider.EXAMPLE_SEED_JOB_DEFINITION_ID, batch.getSeedJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_MONITOR_JOB_DEFINITION_ID, batch.getMonitorJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOB_DEFINITION_ID, batch.getBatchJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_TENANT_ID, batch.getTenantId());
  }

}
