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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReportResult;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;

public class CleanableHistoricDecisionInstanceReportServiceTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history/decision-definition";
  protected static final String HISTORIC_REPORT_URL = HISTORY_URL + "/cleanable-decision-instance-report";
  protected static final String HISTORIC_REPORT_COUNT_URL = HISTORIC_REPORT_URL + "/count";

  private CleanableHistoricDecisionInstanceReport historicDecisionInstanceReport;

  @Before
  public void setUpRuntimeData() {
    setupHistoryReportMock();
  }

  private void setupHistoryReportMock() {
    CleanableHistoricDecisionInstanceReport report = mock(CleanableHistoricDecisionInstanceReport.class);

    when(report.decisionDefinitionIdIn(anyString())).thenReturn(report);
    when(report.decisionDefinitionKeyIn(anyString())).thenReturn(report);

    CleanableHistoricDecisionInstanceReportResult reportResult = mock(CleanableHistoricDecisionInstanceReportResult.class);

    when(reportResult.getDecisionDefinitionId()).thenReturn("anId");
    when(reportResult.getDecisionDefinitionKey()).thenReturn("aKey");
    when(reportResult.getDecisionDefinitionName()).thenReturn("aName");
    when(reportResult.getDecisionDefinitionVersion()).thenReturn(42);
    when(reportResult.getHistoryTimeToLive()).thenReturn(5);
    when(reportResult.getFinishedDecisionInstanceCount()).thenReturn(1000l);
    when(reportResult.getCleanableDecisionInstanceCount()).thenReturn(567l);

    CleanableHistoricDecisionInstanceReportResult anotherReportResult = mock(CleanableHistoricDecisionInstanceReportResult.class);

    when(anotherReportResult.getDecisionDefinitionId()).thenReturn("dpId");
    when(anotherReportResult.getDecisionDefinitionKey()).thenReturn("dpKey");
    when(anotherReportResult.getDecisionDefinitionName()).thenReturn("dpName");
    when(anotherReportResult.getDecisionDefinitionVersion()).thenReturn(33);
    when(anotherReportResult.getHistoryTimeToLive()).thenReturn(5);
    when(anotherReportResult.getFinishedDecisionInstanceCount()).thenReturn(10l);
    when(reportResult.getCleanableDecisionInstanceCount()).thenReturn(0l);

    List<CleanableHistoricDecisionInstanceReportResult> mocks = new ArrayList<CleanableHistoricDecisionInstanceReportResult>();
    mocks.add(reportResult);
    mocks.add(anotherReportResult);

    when(report.list()).thenReturn(mocks);
    when(report.count()).thenReturn((long) mocks.size());

    historicDecisionInstanceReport = report;
    when(processEngine.getHistoryService().createCleanableHistoricDecisionInstanceReport()).thenReturn(historicDecisionInstanceReport);
  }

  @Test
  public void testGetReport() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    InOrder inOrder = Mockito.inOrder(historicDecisionInstanceReport);
    inOrder.verify(historicDecisionInstanceReport).list();
  }

  @Test
  public void testMissingAuthorization() {
    String message = "not authorized";
    when(historicDecisionInstanceReport.list()).thenThrow(new AuthorizationException(message));

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
    String aDecDefId = "anDecDefId";
    String anotherDecDefId = "anotherDecDefId";

    String aDecDefKey = "anDecDefKey";
    String anotherDecDefKey = "anotherDecDefKey";

   given()
     .queryParam("decisionDefinitionIdIn", aDecDefId + "," + anotherDecDefId)
     .queryParam("decisionDefinitionKeyIn", aDecDefKey + "," + anotherDecDefKey)
   .then().expect()
     .statusCode(Status.OK.getStatusCode())
     .contentType(ContentType.JSON)
   .when().get(HISTORIC_REPORT_URL);

   verify(historicDecisionInstanceReport).decisionDefinitionIdIn(aDecDefId, anotherDecDefId);
   verify(historicDecisionInstanceReport).decisionDefinitionKeyIn(aDecDefKey, anotherDecDefKey);
   verify(historicDecisionInstanceReport).list();
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(2))
    .when()
      .get(HISTORIC_REPORT_COUNT_URL);

    verify(historicDecisionInstanceReport).count();
  }
}
