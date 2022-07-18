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

import static io.restassured.RestAssured.given;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_BATCH_ID;
import static org.camunda.bpm.engine.rest.util.JsonPathUtil.from;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import io.restassured.http.ContentType;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;
import org.camunda.bpm.engine.history.SetRemovalTimeSelectModeForHistoricBatchesBuilder;
import org.camunda.bpm.engine.history.SetRemovalTimeToHistoricBatchesBuilder;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.history.batch.HistoricBatchDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.JsonPathUtil;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;

import io.restassured.response.Response;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HistoricBatchRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_BATCH_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/batch";
  protected static final String HISTORIC_SINGLE_BATCH_RESOURCE_URL = HISTORIC_BATCH_RESOURCE_URL + "/{id}";
  protected static final String SET_REMOVAL_TIME_HISTORIC_BATCHES_ASYNC_URL = HISTORIC_BATCH_RESOURCE_URL + "/set-removal-time";

  protected HistoryService historyServiceMock;
  protected HistoricBatchQuery queryMock;

  @Before
  public void setUpHistoricBatchQueryMock() {
    HistoricBatch historicBatchMock = MockProvider.createMockHistoricBatch();

    queryMock = mock(HistoricBatchQuery.class);
    when(queryMock.batchId(eq(MockProvider.EXAMPLE_BATCH_ID))).thenReturn(queryMock);
    when(queryMock.singleResult()).thenReturn(historicBatchMock);

    historyServiceMock = mock(HistoryService.class);
    when(historyServiceMock.createHistoricBatchQuery()).thenReturn(queryMock);

    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);
  }

  @Test
  public void testGetHistoricBatch() {
    Response response = given()
        .pathParam("id", MockProvider.EXAMPLE_BATCH_ID)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_SINGLE_BATCH_RESOURCE_URL);

    InOrder inOrder = inOrder(queryMock);
    inOrder.verify(queryMock).batchId(MockProvider.EXAMPLE_BATCH_ID);
    inOrder.verify(queryMock).singleResult();
    inOrder.verifyNoMoreInteractions();

    verifyHistoricBatchJson(response.asString());
  }

  @Test
  public void testGetNonExistingHistoricBatch() {
    String nonExistingId = MockProvider.NON_EXISTING_ID;
    HistoricBatchQuery historicBatchQuery = mock(HistoricBatchQuery.class);
    when(historicBatchQuery.batchId(nonExistingId)).thenReturn(historicBatchQuery);
    when(historicBatchQuery.singleResult()).thenReturn(null);
    when(historyServiceMock.createHistoricBatchQuery()).thenReturn(historicBatchQuery);

    given()
      .pathParam("id", nonExistingId)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Historic batch with id '" + nonExistingId + "' does not exist"))
    .when()
      .get(HISTORIC_SINGLE_BATCH_RESOURCE_URL);
  }

  @Test
  public void deleteHistoricBatch() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_BATCH_ID)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(HISTORIC_SINGLE_BATCH_RESOURCE_URL);

    verify(historyServiceMock).deleteHistoricBatch(eq(MockProvider.EXAMPLE_BATCH_ID));
    verifyNoMoreInteractions(historyServiceMock);
  }

  @Test
  public void deleteNonExistingHistoricBatch() {
    String nonExistingId = MockProvider.NON_EXISTING_ID;

    doThrow(new BadUserRequestException("Historic batch for id '" + nonExistingId + "' cannot be found"))
      .when(historyServiceMock).deleteHistoricBatch(eq(nonExistingId));

    given()
      .pathParam("id", nonExistingId)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Unable to delete historic batch with id '" + nonExistingId + "'"))
    .when()
      .delete(HISTORIC_SINGLE_BATCH_RESOURCE_URL);
  }

  @Test
  public void shouldSetRemovalTime_ByIds() {
    SetRemovalTimeSelectModeForHistoricBatchesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricBatchesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricBatches()).thenReturn(builderMock);

    Map<String, Object> payload = new HashMap<>();
    payload.put("historicBatchIds", Collections.singletonList(EXAMPLE_BATCH_ID));
    payload.put("calculatedRemovalTime", true);

    given()
      .contentType(ContentType.JSON)
      .body(payload)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_BATCHES_ASYNC_URL);

    SetRemovalTimeSelectModeForHistoricBatchesBuilder builder =
      historyServiceMock.setRemovalTimeToHistoricBatches();

    verify(builder).calculatedRemovalTime();
    verify(builder).byIds(EXAMPLE_BATCH_ID);
    verify(builder).byQuery(null);
    verify(builder).executeAsync();
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void shouldSetRemovalTime_ByQuery() {
    SetRemovalTimeSelectModeForHistoricBatchesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricBatchesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricBatches()).thenReturn(builderMock);

    Map<String, Object> payload = new HashMap<>();
    payload.put("calculatedRemovalTime", true);
    payload.put("historicBatchQuery", Collections.singletonMap("batchId", EXAMPLE_BATCH_ID));

    given()
      .contentType(ContentType.JSON)
      .body(payload)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_BATCHES_ASYNC_URL);

    SetRemovalTimeSelectModeForHistoricBatchesBuilder builder =
      historyServiceMock.setRemovalTimeToHistoricBatches();

    verify(queryMock).batchId(EXAMPLE_BATCH_ID);

    verify(builder).calculatedRemovalTime();
    verify(builder).byIds(null);
    verify(builder).byQuery(queryMock);
    verify(builder).executeAsync();
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void shouldSetRemovalTime_Absolute() {
    Date removalTime = new Date();

    SetRemovalTimeSelectModeForHistoricBatchesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricBatchesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricBatches()).thenReturn(builderMock);

    Map<String, Object> payload = new HashMap<>();
    payload.put("historicBatchIds", Collections.singletonList(EXAMPLE_BATCH_ID));
    payload.put("absoluteRemovalTime", removalTime);

    given()
      .contentType(ContentType.JSON)
      .body(payload)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_BATCHES_ASYNC_URL);

    SetRemovalTimeSelectModeForHistoricBatchesBuilder builder =
      historyServiceMock.setRemovalTimeToHistoricBatches();

    verify(builder).absoluteRemovalTime(removalTime);
    verify(builder).byIds(EXAMPLE_BATCH_ID);
    verify(builder).byQuery(null);
    verify(builder).executeAsync();
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void shouldNotSetRemovalTime_Absolute() {
    SetRemovalTimeSelectModeForHistoricBatchesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricBatchesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricBatches()).thenReturn(builderMock);

    Map<String, Object> payload = new HashMap<>();
    payload.put("historicBatchIds", Collections.singletonList(EXAMPLE_BATCH_ID));
    payload.put("absoluteRemovalTime", null);

    given()
      .contentType(ContentType.JSON)
      .body(payload)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_BATCHES_ASYNC_URL);

    SetRemovalTimeToHistoricBatchesBuilder builder =
      historyServiceMock.setRemovalTimeToHistoricBatches();

    verify(builder).byIds(EXAMPLE_BATCH_ID);
    verify(builder).byQuery(null);
    verify(builder).executeAsync();
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void shouldClearRemovalTime() {
    SetRemovalTimeSelectModeForHistoricBatchesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricBatchesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricBatches())
      .thenReturn(builderMock);

    Map<String, Object> payload = new HashMap<>();
    payload.put("historicBatchIds", Collections.singletonList(EXAMPLE_BATCH_ID));
    payload.put("clearedRemovalTime", true);

    given()
      .contentType(ContentType.JSON)
      .body(payload)
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_BATCHES_ASYNC_URL);

    SetRemovalTimeSelectModeForHistoricBatchesBuilder builder =
      historyServiceMock.setRemovalTimeToHistoricBatches();

    verify(builder).clearedRemovalTime();
    verify(builder).byIds(EXAMPLE_BATCH_ID);
    verify(builder).byQuery(null);
    verify(builder).executeAsync();
    verifyNoMoreInteractions(builder);
  }

  @Test
  public void shouldSetRemovalTime_Response() {
    SetRemovalTimeSelectModeForHistoricBatchesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricBatchesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricBatches()).thenReturn(builderMock);

    Batch batchEntity = MockProvider.createMockBatch();
    when(builderMock.executeAsync()).thenReturn(batchEntity);

    Response response = given()
      .contentType(ContentType.JSON)
      .body(Collections.emptyMap())
    .then()
      .expect().statusCode(Status.OK.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_BATCHES_ASYNC_URL);

    verifyBatchJson(response.asString());
  }

  @Test
  public void shouldSetRemovalTime_ThrowBadUserException() {
    SetRemovalTimeSelectModeForHistoricBatchesBuilder builderMock =
      mock(SetRemovalTimeSelectModeForHistoricBatchesBuilder.class, RETURNS_DEEP_STUBS);

    when(historyServiceMock.setRemovalTimeToHistoricBatches()).thenReturn(builderMock);

    doThrow(BadUserRequestException.class).when(builderMock).executeAsync();

    given()
      .contentType(ContentType.JSON)
      .body(Collections.emptyMap())
    .then()
      .expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(SET_REMOVAL_TIME_HISTORIC_BATCHES_ASYNC_URL);
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

  protected void verifyHistoricBatchJson(String historicBatchJson) {
    HistoricBatchDto historicBatch = from(historicBatchJson).getObject("", HistoricBatchDto.class);
    assertNotNull("The returned historic batch should not be null.", historicBatch);
    assertEquals(MockProvider.EXAMPLE_BATCH_ID, historicBatch.getId());
    assertEquals(MockProvider.EXAMPLE_BATCH_TYPE, historicBatch.getType());
    assertEquals(MockProvider.EXAMPLE_BATCH_TOTAL_JOBS, historicBatch.getTotalJobs());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOBS_PER_SEED, historicBatch.getBatchJobsPerSeed());
    assertEquals(MockProvider.EXAMPLE_INVOCATIONS_PER_BATCH_JOB, historicBatch.getInvocationsPerBatchJob());
    assertEquals(MockProvider.EXAMPLE_SEED_JOB_DEFINITION_ID, historicBatch.getSeedJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_MONITOR_JOB_DEFINITION_ID, historicBatch.getMonitorJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOB_DEFINITION_ID, historicBatch.getBatchJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_TENANT_ID, historicBatch.getTenantId());
    assertEquals(MockProvider.EXAMPLE_USER_ID, historicBatch.getCreateUserId());
    assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_BATCH_START_TIME), historicBatch.getStartTime());
    assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_BATCH_START_TIME), historicBatch.getExecutionStartTime());
    assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_BATCH_END_TIME), historicBatch.getEndTime());
    assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_BATCH_REMOVAL_TIME), historicBatch.getRemovalTime());
  }

}
