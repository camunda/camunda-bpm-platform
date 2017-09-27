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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReportResult;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class CleanableHistoricCaseInstanceReportServiceTest extends AbstractRestServiceTest {

  private static final String EXAMPLE_CD_ID = "anId";
  private static final String EXAMPLE_CD_KEY = "aKey";
  private static final String EXAMPLE_CD_NAME = "aName";
  private static final int EXAMPLE_CD_VERSION = 42;
  private static final int EXAMPLE_TTL = 5;
  private static final long EXAMPLE_FINISHED_CI_COUNT = 10l;
  private static final long EXAMPLE_CLEANABLE_CI_COUNT = 5l;

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history/case-definition";
  protected static final String HISTORIC_REPORT_URL = HISTORY_URL + "/cleanable-case-instance-report";
  protected static final String HISTORIC_REPORT_COUNT_URL = HISTORIC_REPORT_URL + "/count";

  private CleanableHistoricCaseInstanceReport historicCaseInstanceReport;

  @Before
  public void setUpRuntimeData() {
    setupHistoryReportMock();
  }

  private void setupHistoryReportMock() {
    CleanableHistoricCaseInstanceReport report = mock(CleanableHistoricCaseInstanceReport.class);

    when(report.caseDefinitionIdIn(anyString())).thenReturn(report);
    when(report.caseDefinitionKeyIn(anyString())).thenReturn(report);

    CleanableHistoricCaseInstanceReportResult reportResult = mock(CleanableHistoricCaseInstanceReportResult.class);

    when(reportResult.getCaseDefinitionId()).thenReturn(EXAMPLE_CD_ID);
    when(reportResult.getCaseDefinitionKey()).thenReturn(EXAMPLE_CD_KEY);
    when(reportResult.getCaseDefinitionName()).thenReturn(EXAMPLE_CD_NAME);
    when(reportResult.getCaseDefinitionVersion()).thenReturn(EXAMPLE_CD_VERSION);
    when(reportResult.getHistoryTimeToLive()).thenReturn(EXAMPLE_TTL);
    when(reportResult.getFinishedCaseInstanceCount()).thenReturn(EXAMPLE_FINISHED_CI_COUNT);
    when(reportResult.getCleanableCaseInstanceCount()).thenReturn(EXAMPLE_CLEANABLE_CI_COUNT);

    CleanableHistoricCaseInstanceReportResult anotherReportResult = mock(CleanableHistoricCaseInstanceReportResult.class);

    when(anotherReportResult.getCaseDefinitionId()).thenReturn("pdId");
    when(anotherReportResult.getCaseDefinitionKey()).thenReturn("pdKey");
    when(anotherReportResult.getCaseDefinitionName()).thenReturn("pdName");
    when(anotherReportResult.getCaseDefinitionVersion()).thenReturn(33);
    when(anotherReportResult.getHistoryTimeToLive()).thenReturn(null);
    when(anotherReportResult.getFinishedCaseInstanceCount()).thenReturn(13l);
    when(anotherReportResult.getCleanableCaseInstanceCount()).thenReturn(0l);

    List<CleanableHistoricCaseInstanceReportResult> mocks = new ArrayList<CleanableHistoricCaseInstanceReportResult>();
    mocks.add(reportResult);
    mocks.add(anotherReportResult);

    when(report.list()).thenReturn(mocks);
    when(report.count()).thenReturn((long) mocks.size());

    historicCaseInstanceReport = report;
    when(processEngine.getHistoryService().createCleanableHistoricCaseInstanceReport()).thenReturn(historicCaseInstanceReport);
  }

  @Test
  public void testGetReport() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    InOrder inOrder = Mockito.inOrder(historicCaseInstanceReport);
    inOrder.verify(historicCaseInstanceReport).list();
  }

  @Test
  public void testReportRetrieval() {
    Response response = given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(historicCaseInstanceReport);
    inOrder.verify(historicCaseInstanceReport).list();

    String content = response.asString();
    List<String> reportResults = from(content).getList("");
    Assert.assertEquals("There should be two report results returned.", 2, reportResults.size());
    Assert.assertNotNull(reportResults.get(0));

    String returnedDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedDefinitionKey = from(content).getString("[0].caseDefinitionKey");
    String returnedDefinitionName = from(content).getString("[0].caseDefinitionName");
    int returnedDefinitionVersion = from(content).getInt("[0].caseDefinitionVersion");
    int returnedTTL = from(content).getInt("[0].historyTimeToLive");
    long returnedFinishedCount= from(content).getLong("[0].finishedCaseInstanceCount");
    long returnedCleanableCount = from(content).getLong("[0].cleanableCaseInstanceCount");

    Assert.assertEquals(EXAMPLE_CD_ID, returnedDefinitionId);
    Assert.assertEquals(EXAMPLE_CD_KEY, returnedDefinitionKey);
    Assert.assertEquals(EXAMPLE_CD_NAME, returnedDefinitionName);
    Assert.assertEquals(EXAMPLE_CD_VERSION, returnedDefinitionVersion);
    Assert.assertEquals(EXAMPLE_TTL, returnedTTL);
    Assert.assertEquals(EXAMPLE_FINISHED_CI_COUNT, returnedFinishedCount);
    Assert.assertEquals(EXAMPLE_CLEANABLE_CI_COUNT, returnedCleanableCount);
  }

  @Test
  public void testMissingAuthorization() {
    String message = "not authorized";
    when(historicCaseInstanceReport.list()).thenThrow(new AuthorizationException(message));


    given()
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when().get(HISTORIC_REPORT_URL);
  }

  @Test
  public void testListParameters() {
    String aCaseDefId = "anCaseDefId";
    String anotherCaseDefId = "anotherCaseDefId";

    String aCaseDefKey = "anCaseDefKey";
    String anotherCaseDefKey = "anotherCaseDefKey";

    given()
      .queryParam("caseDefinitionIdIn", aCaseDefId + "," + anotherCaseDefId)
      .queryParam("caseDefinitionKeyIn", aCaseDefKey + "," + anotherCaseDefKey)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicCaseInstanceReport).caseDefinitionIdIn(aCaseDefId, anotherCaseDefId);
    verify(historicCaseInstanceReport).caseDefinitionKeyIn(aCaseDefKey, anotherCaseDefKey);
    verify(historicCaseInstanceReport).list();
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(2))
    .when()
      .get(HISTORIC_REPORT_COUNT_URL);

    verify(historicCaseInstanceReport).count();
  }
}
