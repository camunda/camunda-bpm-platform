/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static com.jayway.restassured.RestAssured.given;
import static org.camunda.bpm.engine.rest.util.JsonPathUtil.from;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
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

import com.jayway.restassured.response.Response;

public class HistoricBatchRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_BATCH_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/batch";
  protected static final String HISTORIC_SINGLE_BATCH_RESOURCE_URL = HISTORIC_BATCH_RESOURCE_URL + "/{id}";

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
    assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_BATCH_START_TIME), historicBatch.getStartTime());
    assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_BATCH_END_TIME), historicBatch.getEndTime());
  }

}
