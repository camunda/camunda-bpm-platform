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

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.history.CleanableHistoricBatchReport;
import org.camunda.bpm.engine.history.CleanableHistoricBatchReportResult;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class CleanableHistoricBatchReportServiceTest extends AbstractRestServiceTest {

  private static final String EXAMPLE_TYPE = "batchId1";
  private static final int EXAMPLE_TTL = 5;
  private static final long EXAMPLE_FINISHED_COUNT = 10l;
  private static final long EXAMPLE_CLEANABLE_COUNT = 5l;

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history/batch";
  protected static final String HISTORIC_REPORT_URL = HISTORY_URL + "/cleanable-batch-report";
  protected static final String HISTORIC_REPORT_COUNT_URL = HISTORIC_REPORT_URL + "/count";

  private CleanableHistoricBatchReport historicBatchReport;

  @Before
  public void setUpRuntimeData() {
    setupHistoryReportMock();
  }

  private void setupHistoryReportMock() {
    CleanableHistoricBatchReport report = mock(CleanableHistoricBatchReport.class);

    CleanableHistoricBatchReportResult reportResult = mock(CleanableHistoricBatchReportResult.class);

    when(reportResult.getBatchType()).thenReturn(EXAMPLE_TYPE);
    when(reportResult.getHistoryTimeToLive()).thenReturn(EXAMPLE_TTL);
    when(reportResult.getFinishedBatchesCount()).thenReturn(EXAMPLE_FINISHED_COUNT);
    when(reportResult.getCleanableBatchesCount()).thenReturn(EXAMPLE_CLEANABLE_COUNT);

    CleanableHistoricBatchReportResult anotherReportResult = mock(CleanableHistoricBatchReportResult.class);

    when(anotherReportResult.getBatchType()).thenReturn("batchId2");
    when(anotherReportResult.getHistoryTimeToLive()).thenReturn(null);
    when(anotherReportResult.getFinishedBatchesCount()).thenReturn(13l);
    when(anotherReportResult.getCleanableBatchesCount()).thenReturn(0l);

    List<CleanableHistoricBatchReportResult> mocks = new ArrayList<CleanableHistoricBatchReportResult>();
    mocks.add(reportResult);
    mocks.add(anotherReportResult);

    when(report.list()).thenReturn(mocks);
    when(report.count()).thenReturn((long) mocks.size());

    historicBatchReport = report;
    when(processEngine.getHistoryService().createCleanableHistoricBatchReport()).thenReturn(historicBatchReport);
  }

  @Test
  public void testGetReport() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    InOrder inOrder = Mockito.inOrder(historicBatchReport);
    inOrder.verify(historicBatchReport).list();
  }

  @Test
  public void testReportRetrieval() {
    Response response = given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(historicBatchReport);
    inOrder.verify(historicBatchReport).list();

    String content = response.asString();
    List<String> reportResults = from(content).getList("");
    Assert.assertEquals("There should be two report results returned.", 2, reportResults.size());
    Assert.assertNotNull(reportResults.get(0));

    String returnedBatchType = from(content).getString("[0].batchType");
    int returnedTTL = from(content).getInt("[0].historyTimeToLive");
    long returnedFinishedCount= from(content).getLong("[0].finishedBatchesCount");
    long returnedCleanableCount = from(content).getLong("[0].cleanableBatchesCount");

    Assert.assertEquals(EXAMPLE_TYPE, returnedBatchType);
    Assert.assertEquals(EXAMPLE_TTL, returnedTTL);
    Assert.assertEquals(EXAMPLE_FINISHED_COUNT, returnedFinishedCount);
    Assert.assertEquals(EXAMPLE_CLEANABLE_COUNT, returnedCleanableCount);
  }

  @Test
  public void testMissingAuthorization() {
    String message = "not authorized";
    when(historicBatchReport.list()).thenThrow(new AuthorizationException(message));

    given()
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when().get(HISTORIC_REPORT_URL);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(2))
    .when()
      .get(HISTORIC_REPORT_COUNT_URL);

    verify(historicBatchReport).count();
  }

  @Test
  public void testOrderByFinishedBatchOperationAsc() {
    given()
      .queryParam("sortBy", "finished")
      .queryParam("sortOrder", "asc")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_REPORT_URL);

    verify(historicBatchReport).orderByFinishedBatchOperation();
    verify(historicBatchReport).asc();
  }

  @Test
  public void testOrderByFinishedBatchOperationDesc() {
    given()
      .queryParam("sortBy", "finished")
      .queryParam("sortOrder", "desc")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_REPORT_URL);

    verify(historicBatchReport).orderByFinishedBatchOperation();
    verify(historicBatchReport).desc();
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
      .get(HISTORIC_REPORT_URL);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("finished", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
      .when()
        .get(HISTORIC_REPORT_URL);
  }
}
