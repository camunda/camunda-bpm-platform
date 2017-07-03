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
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReport;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;

public class CleanableHistoricProcessInstanceReportServiceTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history";
  protected static final String HISTORIC_REPORT_URL = HISTORY_URL + "/process-definition/cleanable-process-instance-report";

  private CleanableHistoricProcessInstanceReport historicProcessInstanceReport;

  @Before
  public void setUpRuntimeData() {
    setupHistoryReportMock();
  }

  private void setupHistoryReportMock() {
    CleanableHistoricProcessInstanceReport report = mock(CleanableHistoricProcessInstanceReport.class);

    when(report.processDefinitionIdIn(anyString())).thenReturn(report);
    when(report.processDefinitionKeyIn(anyString())).thenReturn(report);

    CleanableHistoricProcessInstanceReportResult reportResult = mock(CleanableHistoricProcessInstanceReportResult.class);

    when(reportResult.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(reportResult.getProcessDefinitionKey()).thenReturn("aKey");
    when(reportResult.getProcessDefinitionName()).thenReturn("aName");
    when(reportResult.getProcessDefinitionVersion()).thenReturn(42);
    when(reportResult.getHistoryTimeToLive()).thenReturn(5);
    when(reportResult.getFinishedProcessInstanceCount()).thenReturn(10l);
    when(reportResult.getCleanableProcessInstanceCount()).thenReturn(5l);

    CleanableHistoricProcessInstanceReportResult anotherReportResult = mock(CleanableHistoricProcessInstanceReportResult.class);

    when(anotherReportResult.getProcessDefinitionId()).thenReturn("pdId");
    when(anotherReportResult.getProcessDefinitionKey()).thenReturn("pdKey");
    when(anotherReportResult.getProcessDefinitionName()).thenReturn("pdName");
    when(anotherReportResult.getProcessDefinitionVersion()).thenReturn(33);
    when(anotherReportResult.getHistoryTimeToLive()).thenReturn(null);
    when(anotherReportResult.getFinishedProcessInstanceCount()).thenReturn(13l);
    when(anotherReportResult.getCleanableProcessInstanceCount()).thenReturn(0l);

    List<CleanableHistoricProcessInstanceReportResult> mocks = new ArrayList<CleanableHistoricProcessInstanceReportResult>();
    mocks.add(reportResult);
    mocks.add(anotherReportResult);

    when(report.list()).thenReturn(mocks);

    historicProcessInstanceReport = report;
    when(processEngine.getHistoryService().createCleanableHistoricProcessInstanceReport()).thenReturn(historicProcessInstanceReport);
  }

  @Test
  public void testGetReport() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    InOrder inOrder = Mockito.inOrder(historicProcessInstanceReport);
    inOrder.verify(historicProcessInstanceReport).list();
  }

  @Test
  public void testMissingAuthorization() {
    String message = "not authorized";
    when(historicProcessInstanceReport.list()).thenThrow(new AuthorizationException(message));


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
    String aProcDefId = "anProcDefId";
    String anotherProcDefId = "anotherProcDefId";

    String aProcDefKey = "anProcDefKey";
    String anotherProcDefKey = "anotherProcDefKey";

    given()
      .queryParam("processDefinitionIdIn", aProcDefId + "," + anotherProcDefId)
      .queryParam("processDefinitionKeyIn", aProcDefKey + "," + anotherProcDefKey)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicProcessInstanceReport).processDefinitionIdIn(aProcDefId, anotherProcDefId);
    verify(historicProcessInstanceReport).processDefinitionKeyIn(aProcDefKey, anotherProcDefKey);
    verify(historicProcessInstanceReport).list();
  }
}
