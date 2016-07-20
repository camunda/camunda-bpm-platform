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

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.history.HistoricTaskInstanceReport;
import org.camunda.bpm.engine.history.TaskReportResult;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response.Status;
import java.util.Date;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_END_TIME;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_REPORT_COUNT;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_REPORT_DEFINITION;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_START_TIME;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockHistoricTaskInstanceReport;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Stefan Hentschel.
 */
public class HistoricTaskReportRestServiceTest extends AbstractRestServiceTest {


  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String TASK_REPORT_URL = TEST_RESOURCE_ROOT_PATH + "/history/task/report";

  protected HistoricTaskInstanceReport mockedReportQuery;

  @Before
  public void setUpRuntimeData() {
    mockedReportQuery = setUpMockHistoricProcessInstanceReportQuery();
  }

  private HistoricTaskInstanceReport setUpMockHistoricProcessInstanceReportQuery() {
    HistoricTaskInstanceReport mockedReportQuery = mock(HistoricTaskInstanceReport.class);

    List<TaskReportResult> taskReportResults = createMockHistoricTaskInstanceReport();
    when(mockedReportQuery.groupByProcessDefinitionKey()).thenReturn(mockedReportQuery);
    when(mockedReportQuery.taskReport()).thenReturn(taskReportResults);

    when(processEngine.getHistoryService().createHistoricTaskInstanceReport()).thenReturn(mockedReportQuery);

    return mockedReportQuery;
  }

  @Test
  public void testEmptyReport() {
    given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.JSON)
      .when()
        .get(TASK_REPORT_URL);

    verify(mockedReportQuery).taskReport();
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testMissingAuthorization() {
    String message = "not authorized";
    when(mockedReportQuery.taskReport()).thenThrow(new AuthorizationException(message));

    given()
      .then()
        .expect()
          .statusCode(Status.FORBIDDEN.getStatusCode())
          .contentType(ContentType.JSON)
          .body("type", equalTo(AuthorizationException.class.getSimpleName()))
          .body("message", equalTo(message))
      .when()
        .get(TASK_REPORT_URL);
  }

  @Test
  public void testHistoricTaskReport() {
    Response response = given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.JSON)
      .when()
        .get(TASK_REPORT_URL);

    String content = response.asString();
    List<String> reports = from(content).getList("");
    Assert.assertEquals("There should be one report returned.", 1, reports.size());
    Assert.assertNotNull("The returned report should not be null.", reports.get(0));

    String returnedDefinition = from(content).getString("[0].definition");
    Long returnedCount = from(content).getLong("[0].count");

    Assert.assertEquals(EXAMPLE_HISTORIC_TASK_REPORT_DEFINITION, returnedDefinition);
    Assert.assertEquals(EXAMPLE_HISTORIC_TASK_REPORT_COUNT, returnedCount);
  }

  @Test
  public void testHistoricTaskReportWithCompleteBefore() {
    given()
      .queryParam("completeBefore", EXAMPLE_HISTORIC_TASK_START_TIME)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.JSON)
      .when()
        .get(TASK_REPORT_URL);

    verify(mockedReportQuery).taskReport();
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testHistoricTaskReportWithCompleteAfter() {
    given()
      .queryParam("completeAfter", EXAMPLE_HISTORIC_TASK_END_TIME)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.JSON)
      .when()
        .get(TASK_REPORT_URL);

    verify(mockedReportQuery).taskReport();
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testHistoricTaskReportWithGroupByProcDef() {
    given()
      .queryParam("groupBy", "processDefinition")
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(ContentType.JSON)
      .when()
        .get(TASK_REPORT_URL);

    verify(mockedReportQuery).groupByProcessDefinitionKey();
    verify(mockedReportQuery).taskReport();
    verifyNoMoreInteractions(mockedReportQuery);
  }
}
