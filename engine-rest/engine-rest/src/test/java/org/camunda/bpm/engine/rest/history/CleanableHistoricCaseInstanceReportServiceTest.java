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
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;

public class CleanableHistoricCaseInstanceReportServiceTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history";
  protected static final String HISTORIC_REPORT_URL = HISTORY_URL + "/case-definition/cleanable-case-instance-report";

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

    when(reportResult.getCaseDefinitionId()).thenReturn("anId");
    when(reportResult.getCaseDefinitionKey()).thenReturn("aKey");
    when(reportResult.getCaseDefinitionName()).thenReturn("aName");
    when(reportResult.getCaseDefinitionVersion()).thenReturn(42);
    when(reportResult.getHistoryTimeToLive()).thenReturn(5);
    when(reportResult.getFinishedCaseInstanceCount()).thenReturn(10l);
    when(reportResult.getCleanableCaseInstanceCount()).thenReturn(5l);

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
}
