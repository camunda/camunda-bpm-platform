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

package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.camunda.bpm.engine.rest.util.JsonPathUtil.from;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.batch.BatchStatistics;
import org.camunda.bpm.engine.batch.BatchStatisticsQuery;
import org.camunda.bpm.engine.rest.dto.batch.BatchStatisticsDto;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.jayway.restassured.response.Response;

public class BatchRestServiceStatisticsTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String BATCH_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/batch";
  protected static final String BATCH_STATISTICS_URL = BATCH_RESOURCE_URL + "/statistics";
  protected static final String BATCH_STATISTICS_COUNT_URL = BATCH_STATISTICS_URL + "/count";

  protected BatchStatisticsQuery queryMock;
  protected List<BatchStatistics> mockBatchStatisticsList;

  @Before
  public void setUpBatchStatisticsMock() {
    mockBatchStatisticsList = MockProvider.createMockBatchStatisticsList();

    queryMock = mock(BatchStatisticsQuery.class);

    when(queryMock.list()).thenReturn(mockBatchStatisticsList);
    when(queryMock.count()).thenReturn((long) mockBatchStatisticsList.size());

    when(processEngine.getManagementService().createBatchStatisticsQuery()).thenReturn(queryMock);
  }

  @Test
  public void testQuery() {
    Response response = given()
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(BATCH_STATISTICS_URL);

    verify(queryMock).list();
    verifyNoMoreInteractions(queryMock);

    verifyBatchStatisticsListJson(response.asString());
  }

  @Test
  public void testQueryCount() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(BATCH_STATISTICS_COUNT_URL);

    verify(queryMock).count();
    verifyNoMoreInteractions(queryMock);
  }

  @Test
  public void testQueryPagination() {
    when(queryMock.listPage(1, 2))
      .thenReturn(mockBatchStatisticsList);

    Response response = given()
        .queryParam("firstResult", 1)
        .queryParam("maxResults", 2)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(BATCH_STATISTICS_URL);


    verify(queryMock).listPage(1, 2);
    verifyNoMoreInteractions(queryMock);

    verifyBatchStatisticsListJson(response.asString());
  }

  protected void verifyBatchStatisticsListJson(String batchStatisticsListJson) {
    List<Object> batches = from(batchStatisticsListJson).get();
    assertEquals("There should be one batch statistics returned.", 1, batches.size());

    BatchStatisticsDto batchStatistics = from(batchStatisticsListJson).getObject("[0]", BatchStatisticsDto.class);
    assertNotNull("The returned batch statistics should not be null.", batchStatistics);
    assertEquals(MockProvider.EXAMPLE_BATCH_ID, batchStatistics.getId());
    assertEquals(MockProvider.EXAMPLE_BATCH_TYPE, batchStatistics.getType());
    assertEquals(MockProvider.EXAMPLE_BATCH_SIZE, batchStatistics.getSize());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOBS_CREATED, batchStatistics.getJobsCreated());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOBS_PER_SEED, batchStatistics.getBatchJobsPerSeed());
    assertEquals(MockProvider.EXAMPLE_INVOCATIONS_PER_BATCH_JOB, batchStatistics.getInvocationsPerBatchJob());
    assertEquals(MockProvider.EXAMPLE_SEED_JOB_DEFINITION_ID, batchStatistics.getSeedJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_MONITOR_JOB_DEFINITION_ID, batchStatistics.getMonitorJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOB_DEFINITION_ID, batchStatistics.getBatchJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_TENANT_ID, batchStatistics.getTenantId());
    assertEquals(MockProvider.EXAMPLE_BATCH_REMAINING_JOBS, batchStatistics.getRemainingJobs());
    assertEquals(MockProvider.EXAMPLE_BATCH_COMPLETED_JOBS, batchStatistics.getCompletedJobs());
    assertEquals(MockProvider.EXAMPLE_BATCH_FAILED_JOBS, batchStatistics.getFailedJobs());
  }

}
