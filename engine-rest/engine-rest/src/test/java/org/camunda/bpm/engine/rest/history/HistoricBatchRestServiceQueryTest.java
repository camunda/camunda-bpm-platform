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
import static org.camunda.bpm.engine.rest.util.JsonPathUtil.from;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.batch.history.HistoricBatchQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.dto.history.batch.HistoricBatchDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class HistoricBatchRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_BATCH_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/batch";
  protected static final String HISTORIC_BATCH_QUERY_COUNT_URL = HISTORIC_BATCH_RESOURCE_URL + "/count";

  protected HistoricBatchQuery queryMock;

  @Before
  public void setUpHistoricBatchQueryMock() {
    List<HistoricBatch> mockHistoricBatches = MockProvider.createMockHistoricBatches();
    queryMock = mock(HistoricBatchQuery.class);

    when(queryMock.list()).thenReturn(mockHistoricBatches);
    when(queryMock.count()).thenReturn((long) mockHistoricBatches.size());

    when(processEngine.getHistoryService().createHistoricBatchQuery()).thenReturn(queryMock);
  }

  @Test
  public void testNoParametersQuery() {
    Response response = given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_BATCH_RESOURCE_URL);

    verify(queryMock).list();
    verifyNoMoreInteractions(queryMock);

    verifyHistoricBatchListJson(response.asString());
  }

  @Test
  public void testUnknownQueryParameter() {
    Response response = given()
      .queryParam("unknown", "unknown")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_BATCH_RESOURCE_URL);

    verify(queryMock, never()).batchId(anyString());
    verify(queryMock).list();

    verifyHistoricBatchListJson(response.asString());
  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "batchId")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type",
        equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message",
        equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_BATCH_RESOURCE_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type",
        equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message",
        equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_BATCH_RESOURCE_URL);
  }

  @Test
  public void testHistoricBatchQueryByBatchId() {
    Response response = given()
      .queryParam("batchId", MockProvider.EXAMPLE_BATCH_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_BATCH_RESOURCE_URL);

    InOrder inOrder = inOrder(queryMock);
    inOrder.verify(queryMock).batchId(MockProvider.EXAMPLE_BATCH_ID);
    inOrder.verify(queryMock).list();
    inOrder.verifyNoMoreInteractions();

    verifyHistoricBatchListJson(response.asString());
  }

  @Test
  public void testHistoricBatchQueryByCompleted() {
    Response response = given()
      .queryParam("completed", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_BATCH_RESOURCE_URL);

    InOrder inOrder = inOrder(queryMock);
    inOrder.verify(queryMock).completed(true);
    inOrder.verify(queryMock).list();
    inOrder.verifyNoMoreInteractions();

    verifyHistoricBatchListJson(response.asString());
  }

  @Test
  public void testHistoricBatchQueryByNotCompleted() {
    Response response = given()
      .queryParam("completed", false)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_BATCH_RESOURCE_URL);

    InOrder inOrder = inOrder(queryMock);
    inOrder.verify(queryMock).completed(false);
    inOrder.verify(queryMock).list();
    inOrder.verifyNoMoreInteractions();

    verifyHistoricBatchListJson(response.asString());
  }

  @Test
  public void testFullHistoricBatchQuery() {
    Response response = given()
        .queryParams(getCompleteQueryParameters())
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_BATCH_RESOURCE_URL);

    verifyQueryParameterInvocations();
    verify(queryMock).list();
    verifyNoMoreInteractions(queryMock);

    verifyHistoricBatchListJson(response.asString());
  }

  @Test
  public void testQueryCount() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_BATCH_QUERY_COUNT_URL);

    verify(queryMock).count();
    verifyNoMoreInteractions(queryMock);
  }

  @Test
  public void testFullQueryCount() {
    given()
      .params(getCompleteQueryParameters())
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_BATCH_QUERY_COUNT_URL);

    verifyQueryParameterInvocations();
    verify(queryMock).count();
    verifyNoMoreInteractions(queryMock);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("batchId", "desc", Status.OK);
    inOrder.verify(queryMock).orderById();
    inOrder.verify(queryMock).desc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("batchId", "asc", Status.OK);
    inOrder.verify(queryMock).orderById();
    inOrder.verify(queryMock).asc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("startTime", "desc", Status.OK);
    inOrder.verify(queryMock).orderByStartTime();
    inOrder.verify(queryMock).desc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("startTime", "asc", Status.OK);
    inOrder.verify(queryMock).orderByStartTime();
    inOrder.verify(queryMock).asc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("endTime", "desc", Status.OK);
    inOrder.verify(queryMock).orderByEndTime();
    inOrder.verify(queryMock).desc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("endTime", "asc", Status.OK);
    inOrder.verify(queryMock).orderByEndTime();
    inOrder.verify(queryMock).asc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("tenantId", "desc", Status.OK);
    inOrder.verify(queryMock).orderByTenantId();
    inOrder.verify(queryMock).desc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(queryMock).orderByTenantId();
    inOrder.verify(queryMock).asc();
  }

  private void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then().expect()
      .statusCode(expectedStatus.getStatusCode())
    .when()
      .get(HISTORIC_BATCH_RESOURCE_URL);
  }

  protected Map<String, Object> getCompleteQueryParameters() {
    Map<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("batchId", MockProvider.EXAMPLE_BATCH_ID);
    parameters.put("type", MockProvider.EXAMPLE_BATCH_TYPE);
    parameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID + "," + MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    parameters.put("withoutTenantId", true);

    return parameters;
  }

  protected void verifyQueryParameterInvocations() {
    verify(queryMock).batchId(MockProvider.EXAMPLE_BATCH_ID);
    verify(queryMock).type(MockProvider.EXAMPLE_BATCH_TYPE);
    verify(queryMock).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(queryMock).withoutTenantId();
  }

  protected void verifyHistoricBatchListJson(String historicBatchListJson) {
    List<Object> batches = from(historicBatchListJson).get();
    assertEquals("There should be one historic batch returned.", 1, batches.size());

    HistoricBatchDto historicBatch = from(historicBatchListJson).getObject("[0]", HistoricBatchDto.class);
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
    assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_BATCH_END_TIME), historicBatch.getEndTime());
    assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_BATCH_REMOVAL_TIME), historicBatch.getRemovalTime());
  }

}
