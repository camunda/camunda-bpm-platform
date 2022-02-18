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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder;
import org.camunda.bpm.engine.history.SetRemovalTimeToHistoricProcessInstancesBuilder;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.JsonPathUtil;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class HistoricProcessInstanceRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();
  
  protected static final String DELETE_REASON = "deleteReason";
  protected static final String TEST_DELETE_REASON = "test";
  protected static final String FAIL_IF_NOT_EXISTS = "failIfNotExists";
  protected static final String HISTORIC_PROCESS_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/process-instance";
  protected static final String HISTORIC_SINGLE_PROCESS_INSTANCE_URL = HISTORIC_PROCESS_INSTANCE_URL + "/{id}";
  protected static final String DELETE_HISTORIC_PROCESS_INSTANCES_ASYNC_URL = HISTORIC_PROCESS_INSTANCE_URL + "/delete";
  protected static final String SET_REMOVAL_TIME_HISTORIC_PROCESS_INSTANCES_ASYNC_URL = HISTORIC_PROCESS_INSTANCE_URL + "/set-removal-time";
  protected static final String HISTORIC_SINGLE_PROCESS_INSTANCE_VARIABLES_URL = HISTORIC_PROCESS_INSTANCE_URL + "/{id}/variable-instances";

  private HistoryService historyServiceMock;

  @Before
  public void setUpRuntimeData() {
    historyServiceMock = mock(HistoryService.class);

    // runtime service
    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);
  }

  @Test
  public void testGetSingleInstance() {
    HistoricProcessInstance mockInstance = MockProvider.createMockHistoricProcessInstance();
    HistoricProcessInstanceQuery sampleInstanceQuery = mock(HistoricProcessInstanceQuery.class);

    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.processInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.singleResult()).thenReturn(mockInstance);

    Response response = given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .when().get(HISTORIC_SINGLE_PROCESS_INSTANCE_URL);

    String content = response.asString();

    String returnedProcessInstanceId = from(content).getString("id");
    String returnedProcessInstanceBusinessKey = from(content).getString("businessKey");
    String returnedProcessDefinitionId = from(content).getString("processDefinitionId");
    String returnedProcessDefinitionKey = from(content).getString("processDefinitionKey");
    String returnedStartTime = from(content).getString("startTime");
    String returnedEndTime = from(content).getString("endTime");
    long returnedDurationInMillis = from(content).getLong("durationInMillis");
    String returnedStartUserId = from(content).getString("startUserId");
    String returnedStartActivityId = from(content).getString("startActivityId");
    String returnedDeleteReason = from(content).getString(DELETE_REASON);
    String returnedSuperProcessInstanceId = from(content).getString("superProcessInstanceId");
    String returnedSuperCaseInstanceId = from(content).getString("superCaseInstanceId");
    String returnedCaseInstanceId = from(content).getString("caseInstanceId");
    String returnedTenantId = from(content).getString("tenantId");
    String returnedState = from(content).getString("state");

    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY, returnedProcessInstanceBusinessKey);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, returnedProcessDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME, returnedStartTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_END_TIME, returnedEndTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS, returnedDurationInMillis);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_USER_ID, returnedStartUserId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_ACTIVITY_ID, returnedStartActivityId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON, returnedDeleteReason);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_PROCESS_INSTANCE_ID, returnedSuperProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_CASE_INSTANCE_ID, returnedSuperCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STATE, returnedState);

  }

  @Test
  public void testGetNonExistingProcessInstance() {
    HistoricProcessInstanceQuery sampleInstanceQuery = mock(HistoricProcessInstanceQuery.class);

    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.processInstanceId(anyString())).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.singleResult()).thenReturn(null);

    given().pathParam("id", "aNonExistingInstanceId")
        .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Historic process instance with id aNonExistingInstanceId does not exist"))
        .when().get(HISTORIC_SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testDeleteProcessInstance() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
        .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
        .when().delete(HISTORIC_SINGLE_PROCESS_INSTANCE_URL);

    verify(historyServiceMock).deleteHistoricProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
  }

  @Test
  public void testDeleteNonExistingProcessInstance() {
    doThrow(new ProcessEngineException("expected exception")).when(historyServiceMock).deleteHistoricProcessInstance(anyString());

    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
        .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Historic process instance with id " + MockProvider.EXAMPLE_PROCESS_INSTANCE_ID + " does not exist"))
        .when().delete(HISTORIC_SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testDeleteNonExistingProcessInstanceIfExists() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).queryParam("failIfNotExists", false)
    .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
    .when().delete(HISTORIC_SINGLE_PROCESS_INSTANCE_URL);
    
    verify(historyServiceMock).deleteHistoricProcessInstanceIfExists(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
  }

  @Test
  public void testDeleteProcessInstanceThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(historyServiceMock).deleteHistoricProcessInstance(anyString());

    given()
        .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
        .then().expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
        .when()
        .delete(HISTORIC_SINGLE_PROCESS_INSTANCE_URL);
  }

  @Test
  public void testDeleteAsync() {
    List<String> ids = Arrays.asList(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID);
    Batch batchEntity = MockProvider.createMockBatch();
    when(historyServiceMock.deleteHistoricProcessInstancesAsync(anyList(), any(), anyString())).thenReturn(batchEntity);

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    messageBodyJson.put("historicProcessInstanceIds", ids);
    messageBodyJson.put(DELETE_REASON, TEST_DELETE_REASON);

    Response response = given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(DELETE_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(historyServiceMock, times(1)).deleteHistoricProcessInstancesAsync(
        eq(ids), eq((HistoricProcessInstanceQuery) null), eq(TEST_DELETE_REASON));
  }

  @Test
  public void testDeleteAsyncWithQuery() {
    Batch batchEntity = MockProvider.createMockBatch();
    when(historyServiceMock.deleteHistoricProcessInstancesAsync(any(), any(), any())
    ).thenReturn(batchEntity);

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    messageBodyJson.put(DELETE_REASON, TEST_DELETE_REASON);
    HistoricProcessInstanceQueryDto query = new HistoricProcessInstanceQueryDto();
    messageBodyJson.put("historicProcessInstanceQuery", query);

    Response response = given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .when().post(DELETE_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);

    verifyBatchJson(response.asString());

    verify(historyServiceMock, times(1)).deleteHistoricProcessInstancesAsync(
        isNull(), isNull(), eq(TEST_DELETE_REASON));
  }


  @Test
  public void testDeleteAsyncWithBadRequestQuery() {
    doThrow(new BadUserRequestException("process instance ids are empty"))
        .when(historyServiceMock).deleteHistoricProcessInstancesAsync(eq((List<String>) null), eq((HistoricProcessInstanceQuery) null), anyString());

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    messageBodyJson.put(DELETE_REASON, TEST_DELETE_REASON);

    given()
        .contentType(ContentType.JSON).body(messageBodyJson)
        .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .when().post(DELETE_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);
  }
  
  @Test
  public void testDeleteAllVariablesByProcessInstanceId() {
    given()
      .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(HISTORIC_SINGLE_PROCESS_INSTANCE_VARIABLES_URL);

    verify(historyServiceMock).deleteHistoricVariableInstancesByProcessInstanceId(EXAMPLE_PROCESS_INSTANCE_ID);
  }
  
  @Test
  public void testDeleteAllVariablesForNonExistingProcessInstance() {
    doThrow(new NotFoundException("No historic process instance found with id: 'NON_EXISTING_ID'"))
    .when(historyServiceMock).deleteHistoricVariableInstancesByProcessInstanceId("NON_EXISTING_ID");
    
    given()
      .pathParam("id", "NON_EXISTING_ID")
    .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("No historic process instance found with id: 'NON_EXISTING_ID'"))
    .when()
      .delete(HISTORIC_SINGLE_PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void shouldSetRemovalTime_ByIds() {
    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricProcessInstances()).thenReturn(builderMock);

    Map<String, Object> payload = new HashMap<>();
    payload.put("historicProcessInstanceIds", Collections.singletonList(EXAMPLE_PROCESS_INSTANCE_ID));
    payload.put("calculatedRemovalTime", true);

    given()
      .contentType(ContentType.JSON)
      .body(payload)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);

    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builder =
      historyServiceMock.setRemovalTimeToHistoricProcessInstances();

    verify(builder).calculatedRemovalTime();
    verify(builder).byIds(EXAMPLE_PROCESS_INSTANCE_ID);
    verify(builder).byQuery(null);
    verify(builder).executeAsync();
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void shouldSetRemovalTime_ByQuery() {
    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricProcessInstances()).thenReturn(builderMock);

    HistoricProcessInstanceQuery query = mock(HistoricProcessInstanceQueryImpl.class, RETURNS_DEEP_STUBS);
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(query);

    Map<String, Object> payload = new HashMap<>();
    payload.put("calculatedRemovalTime", true);
    payload.put("historicProcessInstanceQuery", Collections.singletonMap("processDefinitionId", EXAMPLE_PROCESS_DEFINITION_ID));

    given()
      .contentType(ContentType.JSON)
      .body(payload)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);

    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builder =
      historyServiceMock.setRemovalTimeToHistoricProcessInstances();

    verify(query).processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID);

    verify(builder).calculatedRemovalTime();
    verify(builder).byIds(null);
    verify(builder).byQuery(query);
    verify(builder).executeAsync();
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void shouldSetRemovalTime_Absolute() {
    Date removalTime = new Date();

    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricProcessInstances()).thenReturn(builderMock);

    Map<String, Object> payload = new HashMap<>();
    payload.put("historicProcessInstanceIds", Collections.singletonList(EXAMPLE_PROCESS_INSTANCE_ID));
    payload.put("absoluteRemovalTime", removalTime);

    given()
      .contentType(ContentType.JSON)
      .body(payload)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);

    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builder =
      historyServiceMock.setRemovalTimeToHistoricProcessInstances();

    verify(builder).absoluteRemovalTime(removalTime);
    verify(builder).byIds(EXAMPLE_PROCESS_INSTANCE_ID);
    verify(builder).byQuery(null);
    verify(builder).executeAsync();
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void shouldNotSetRemovalTime_Absolute() {
    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricProcessInstances()).thenReturn(builderMock);

    Map<String, Object> payload = new HashMap<>();
    payload.put("historicProcessInstanceIds", Collections.singletonList(EXAMPLE_PROCESS_INSTANCE_ID));
    payload.put("absoluteRemovalTime", null);

    given()
      .contentType(ContentType.JSON)
      .body(payload)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);

    SetRemovalTimeToHistoricProcessInstancesBuilder builder =
      historyServiceMock.setRemovalTimeToHistoricProcessInstances();

    verify(builder).byIds(EXAMPLE_PROCESS_INSTANCE_ID);
    verify(builder).byQuery(null);
    verify(builder).executeAsync();
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void shouldClearRemovalTime() {
    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricProcessInstances())
      .thenReturn(builderMock);

    Map<String, Object> payload = new HashMap<>();
    payload.put("historicProcessInstanceIds", Collections.singletonList(EXAMPLE_PROCESS_INSTANCE_ID));
    payload.put("clearedRemovalTime", true);

    given()
      .contentType(ContentType.JSON)
      .body(payload)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);

    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builder =
      historyServiceMock.setRemovalTimeToHistoricProcessInstances();

    verify(builder).clearedRemovalTime();
    verify(builder).byIds(EXAMPLE_PROCESS_INSTANCE_ID);
    verify(builder).byQuery(null);
    verify(builder).executeAsync();
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void shouldSetRemovalTime_Response() {
    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricProcessInstances()).thenReturn(builderMock);

    Batch batchEntity = MockProvider.createMockBatch();
    when(builderMock.executeAsync()).thenReturn(batchEntity);

    Response response = given()
      .contentType(ContentType.JSON)
      .body(Collections.emptyMap())
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);

    verifyBatchJson(response.asString());
  }

  @Test
  public void shouldSetRemovalTime_ThrowBadUserException() {
    SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricProcessInstancesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricProcessInstances()).thenReturn(builderMock);

    doThrow(BadUserRequestException.class).when(builderMock).executeAsync();

    given()
      .contentType(ContentType.JSON)
      .body(Collections.emptyMap())
    .then()
      .expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_PROCESS_INSTANCES_ASYNC_URL);
  }

  @Test
  public void testOrQuery() {
    // given
    HistoricProcessInstanceQueryImpl mockedQuery = mock(HistoricProcessInstanceQueryImpl.class);
    when(historyServiceMock.createHistoricProcessInstanceQuery()).thenReturn(mockedQuery);

    String payload = "{ \"orQueries\": [{" +
        "\"processDefinitionKey\": \"aKey\", " +
        "\"processInstanceBusinessKey\": \"aBusinessKey\"}] }";

    // when
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header(ACCEPT_JSON_HEADER)
      .body(payload)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_PROCESS_INSTANCE_URL);

    ArgumentCaptor<HistoricProcessInstanceQueryImpl> argument =
        ArgumentCaptor.forClass(HistoricProcessInstanceQueryImpl.class);

    verify(mockedQuery).addOrQuery(argument.capture());

    // then
    assertThat(argument.getValue().getProcessDefinitionKey()).isEqualTo("aKey");
    assertThat(argument.getValue().getBusinessKey()).isEqualTo("aBusinessKey");
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
