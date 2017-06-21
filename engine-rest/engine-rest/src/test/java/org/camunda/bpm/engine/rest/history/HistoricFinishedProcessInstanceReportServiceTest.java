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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.history.HistoricFinishedProcessInstanceReport;
import org.camunda.bpm.engine.history.HistoricFinishedProcessInstanceReportResult;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;

public class HistoricFinishedProcessInstanceReportServiceTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history";
  protected static final String HISTORIC_REPORT_URL = HISTORY_URL + "/process-definition/finished-process-instance-report";

  private HistoricFinishedProcessInstanceReport historicFinishedProcessInstanceReport;

  @Before
  public void setUpRuntimeData() {
    setupHistoryReportMock();
  }

  private void setupHistoryReportMock() {
    HistoricFinishedProcessInstanceReportResult reportResult = mock(HistoricFinishedProcessInstanceReportResult.class);

    when(reportResult.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(reportResult.getProcessDefinitionKey()).thenReturn("aKey");
    when(reportResult.getProcessDefinitionName()).thenReturn("aName");
    when(reportResult.getProcessDefinitionVersion()).thenReturn(42);
    when(reportResult.getHistoryTimeToLive()).thenReturn("5");
    when(reportResult.getFinishedProcessInstanceCount()).thenReturn(10l);
    when(reportResult.getCleanableProcessInstanceCount()).thenReturn(5l);

    HistoricFinishedProcessInstanceReportResult anotherReportResult = mock(HistoricFinishedProcessInstanceReportResult.class);

    when(anotherReportResult.getProcessDefinitionId()).thenReturn("pdId");
    when(anotherReportResult.getProcessDefinitionKey()).thenReturn("pdKey");
    when(anotherReportResult.getProcessDefinitionName()).thenReturn("pdName");
    when(anotherReportResult.getProcessDefinitionVersion()).thenReturn(33);
    when(anotherReportResult.getHistoryTimeToLive()).thenReturn(null);
    when(anotherReportResult.getFinishedProcessInstanceCount()).thenReturn(13l);
    when(anotherReportResult.getCleanableProcessInstanceCount()).thenReturn(0l);

    List<HistoricFinishedProcessInstanceReportResult> mocks = new ArrayList<HistoricFinishedProcessInstanceReportResult>();
    mocks.add(reportResult);
    mocks.add(anotherReportResult);
//    List<HistoricFinishedProcessInstanceReportResult> mocks = MockProvider.createMockHistoricFinishedProcessInstanceReport();

    historicFinishedProcessInstanceReport = mock(HistoricFinishedProcessInstanceReport.class);
    when(processEngine.getHistoryService().createHistoricFinishedProcessInstanceReport()).thenReturn(historicFinishedProcessInstanceReport);
    when(historicFinishedProcessInstanceReport.count()).thenReturn(mocks);
  }

  @Test
  public void testGetReport() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    InOrder inOrder = Mockito.inOrder(historicFinishedProcessInstanceReport);
    inOrder.verify(historicFinishedProcessInstanceReport).count();
  }

  @Test
  public void testMissingAuthorization() {
    String message = "not authorized";
    when(historicFinishedProcessInstanceReport.count()).thenThrow(new AuthorizationException(message));


    given()
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when().get(HISTORIC_REPORT_URL);

  }
}
