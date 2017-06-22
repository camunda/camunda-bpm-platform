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

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.history.HistoricFinishedDecisionInstanceReport;
import org.camunda.bpm.engine.history.HistoricFinishedDecisionInstanceReportResult;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

public class HistoricFinishedDecisionInstanceReportServiceTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history";
  protected static final String HISTORIC_REPORT_URL = HISTORY_URL + "/decision-definition/finished-decision-instance-report";

  private HistoricFinishedDecisionInstanceReport historicFinishedDecisionInstanceReport;

  @Before
  public void setUpRuntimeData() {
    setupHistoryReportMock();
  }

  private void setupHistoryReportMock() {
    HistoricFinishedDecisionInstanceReportResult reportResult = mock(HistoricFinishedDecisionInstanceReportResult.class);

    when(reportResult.getDecisionDefinitionId()).thenReturn("anId");
    when(reportResult.getDecisionDefinitionKey()).thenReturn("aKey");
    when(reportResult.getDecisionDefinitionName()).thenReturn("aName");
    when(reportResult.getDecisionDefinitionVersion()).thenReturn(42);
    when(reportResult.getHistoryTimeToLive()).thenReturn(5);
    when(reportResult.getFinishedDecisionInstanceCount()).thenReturn(1000l);
    when(reportResult.getCleanableDecisionInstanceCount()).thenReturn(567l);

    HistoricFinishedDecisionInstanceReportResult anotherReportResult = mock(HistoricFinishedDecisionInstanceReportResult.class);

    when(anotherReportResult.getDecisionDefinitionId()).thenReturn("dpId");
    when(anotherReportResult.getDecisionDefinitionKey()).thenReturn("dpKey");
    when(anotherReportResult.getDecisionDefinitionName()).thenReturn("dpName");
    when(anotherReportResult.getDecisionDefinitionVersion()).thenReturn(33);
    when(anotherReportResult.getHistoryTimeToLive()).thenReturn(5);
    when(anotherReportResult.getFinishedDecisionInstanceCount()).thenReturn(10l);
    when(reportResult.getCleanableDecisionInstanceCount()).thenReturn(0l);

    List<HistoricFinishedDecisionInstanceReportResult> mocks = new ArrayList<HistoricFinishedDecisionInstanceReportResult>();
    mocks.add(reportResult);
    mocks.add(anotherReportResult);

    historicFinishedDecisionInstanceReport = mock(HistoricFinishedDecisionInstanceReport.class);
    when(processEngine.getHistoryService().createHistoricFinishedDecisionInstanceReport()).thenReturn(historicFinishedDecisionInstanceReport);
    when(historicFinishedDecisionInstanceReport.list()).thenReturn(mocks);
  }

  @Test
  public void testGetReport() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    InOrder inOrder = Mockito.inOrder(historicFinishedDecisionInstanceReport);
    inOrder.verify(historicFinishedDecisionInstanceReport).list();
  }

  @Test
  public void testMissingAuthorization() {
    String message = "not authorized";
    when(historicFinishedDecisionInstanceReport.list()).thenThrow(new AuthorizationException(message));

    given()
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when().get(HISTORIC_REPORT_URL);
  }
}
