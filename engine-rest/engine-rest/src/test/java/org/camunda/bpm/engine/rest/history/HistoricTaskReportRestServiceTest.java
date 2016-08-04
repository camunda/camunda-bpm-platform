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
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricTaskInstanceReport;
import org.camunda.bpm.engine.history.HistoricTaskInstanceReportResult;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.query.PeriodUnit;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response.Status;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.camunda.bpm.engine.query.PeriodUnit.MONTH;
import static org.camunda.bpm.engine.query.PeriodUnit.QUARTER;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_END_TIME;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_AVG;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MAX;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MIN;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_PERIOD;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_END_TIME;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_START_TIME;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_REPORT_COUNT;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_REPORT_DEFINITION;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_REPORT_PROC_DEFINITION;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_START_TIME;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockHistoricTaskInstanceDurationReport;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockHistoricTaskInstanceReport;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockHistoricTaskInstanceReportWithProcDef;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.any;
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

    List<HistoricTaskInstanceReportResult> taskReportResults = createMockHistoricTaskInstanceReport();
    List<HistoricTaskInstanceReportResult> taskReportResultsWithProcDef = createMockHistoricTaskInstanceReportWithProcDef();

    when(mockedReportQuery.completedAfter(any(Date.class))).thenReturn(mockedReportQuery);
    when(mockedReportQuery.completedBefore(any(Date.class))).thenReturn(mockedReportQuery);
    
    when(mockedReportQuery.countByTaskDefinitionKey()).thenReturn(taskReportResults);
    when(mockedReportQuery.countByProcessDefinitionKey()).thenReturn(taskReportResultsWithProcDef);

    List<DurationReportResult> durationReportByMonth = createMockHistoricTaskInstanceDurationReport(MONTH);
    when(mockedReportQuery.duration(MONTH)).thenReturn(durationReportByMonth);

    List<DurationReportResult> durationReportByQuarter = createMockHistoricTaskInstanceDurationReport(QUARTER);
    when(mockedReportQuery.duration(QUARTER)).thenReturn(durationReportByQuarter);

    when(processEngine.getHistoryService().createHistoricTaskInstanceReport()).thenReturn(mockedReportQuery);

    return mockedReportQuery;
  }

  @Test
  public void testMissingAuthorization() {
    String message = "not authorized";
    when(mockedReportQuery.countByTaskDefinitionKey()).thenThrow(new AuthorizationException(message));

    given()
      .queryParam("reportType", "count")
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
  public void testTaskCountReport() {
    given()
      .queryParam("reportType", "count")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
        .body("[0].definition", equalTo(EXAMPLE_HISTORIC_TASK_REPORT_DEFINITION))
        .body("[0].count", equalTo(EXAMPLE_HISTORIC_TASK_REPORT_COUNT.intValue()))
    .when()
      .get(TASK_REPORT_URL);

    verify(mockedReportQuery).countByTaskDefinitionKey();
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testTaskCountReportWithCompletedBefore() {
    given()
      .queryParam("reportType", "count")
      .queryParam("completedBefore", EXAMPLE_HISTORIC_TASK_END_TIME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_REPORT_URL);

    verify(mockedReportQuery).completedBefore(any(Date.class));
    verify(mockedReportQuery).countByTaskDefinitionKey();
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testTaskCountReportWithCompletedAfter() {
    given()
      .queryParam("reportType", "count")
      .queryParam("completedAfter", EXAMPLE_HISTORIC_TASK_START_TIME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_REPORT_URL);

    verify(mockedReportQuery).completedAfter(any(Date.class));
    verify(mockedReportQuery).countByTaskDefinitionKey();
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testTaskCountReportWithGroupByProcDef() {
    given()
      .queryParam("reportType", "count")
      .queryParam("groupBy", "processDefinition")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_REPORT_URL);

    verify(mockedReportQuery).countByProcessDefinitionKey();
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testTaskCountReportWithGroupByTaskDef() {
    given()
      .queryParam("reportType", "count")
      .queryParam("groupBy", "taskDefinition")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_REPORT_URL);

    verify(mockedReportQuery).countByTaskDefinitionKey();
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testTaskCountReportWithGroupByAnyDef() {
    // should return same definitions as task definition
    given()
      .queryParam("reportType", "count")
      .queryParam("groupBy", "anotherDefinition")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_REPORT_URL);

    verify(mockedReportQuery).countByTaskDefinitionKey();
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testTaskCountWithAllParameters() {
    given()
      .queryParam("reportType", "count")
      .queryParam("groupBy", "processDefinition")
      .queryParam("completedBefore", EXAMPLE_HISTORIC_TASK_END_TIME)
      .queryParam("completedAfter", EXAMPLE_HISTORIC_TASK_START_TIME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
        .body("[0].definition", equalTo(EXAMPLE_HISTORIC_TASK_REPORT_PROC_DEFINITION))
        .body("[0].count", equalTo(EXAMPLE_HISTORIC_TASK_REPORT_COUNT.intValue()))
    .when()
      .get(TASK_REPORT_URL);

    verify(mockedReportQuery).completedAfter(any(Date.class));
    verify(mockedReportQuery).completedBefore(any(Date.class));
    verify(mockedReportQuery).countByProcessDefinitionKey();
    verifyNoMoreInteractions(mockedReportQuery);

  }

  // TASK DURATION REPORT ///////////////////////////////////////////////////////

  @Test
  public void testTaskDurationReportWithoutDurationParam() {
    given()
      .queryParam("periodUnit", "month")
      .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Parameter reportType is not set."))
      .when()
      .get(TASK_REPORT_URL);
  }

  @Test
  public void testTaskDurationReportWithInvalidPeriodUnit() {
    given()
      .queryParam("periodUnit", "abc")
      .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot set query parameter 'periodUnit' to value 'abc'"))
      .when()
      .get(TASK_REPORT_URL);
  }

  @Test
  public void testTaskDurationReportWithMissingPeriodUnit() {
    given()
      .queryParam("reportType", "duration")
      .then()
      .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("periodUnit is null"))
      .when()
      .get(TASK_REPORT_URL);
  }

  @Test
  public void testTaskDurationReportByMonth() {
    given()
      .queryParam("periodUnit", "month")
      .queryParam("reportType", "duration")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
        .body("[0].average", equalTo((int) EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_AVG))
        .body("[0].maximum", equalTo((int) EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MAX))
        .body("[0].minimum", equalTo((int) EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MIN))
        .body("[0].period", equalTo(EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_PERIOD))
        .body("[0].periodUnit", equalTo(MONTH.toString()))
    .when()
      .get(TASK_REPORT_URL);

    verify(mockedReportQuery).duration(PeriodUnit.MONTH);
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testTaskDurationReportByQuarter() {
    given()
      .queryParam("periodUnit", "quarter")
      .queryParam("reportType", "duration")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
        .body("[0].average", equalTo((int) EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_AVG))
        .body("[0].maximum", equalTo((int) EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MAX))
        .body("[0].minimum", equalTo((int) EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MIN))
        .body("[0].period", equalTo(EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_PERIOD))
        .body("[0].periodUnit", equalTo(QUARTER.toString()))
    .when()
      .get(TASK_REPORT_URL);

    verify(mockedReportQuery).duration(PeriodUnit.QUARTER);
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testTaskDurationReportWithCompletedBeforeAndCompletedAfter() {
    given()
      .queryParam("periodUnit", "month")
      .queryParam("reportType", "duration")
      .queryParam("completedBefore", EXAMPLE_HISTORIC_TASK_INST_START_TIME)
      .queryParam("completedAfter", EXAMPLE_HISTORIC_TASK_INST_END_TIME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_REPORT_URL);

    verifyStringStartParameterQueryInvocations();
    verify(mockedReportQuery).duration(PeriodUnit.MONTH);
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testTaskDurationReportWithCompletedBefore() {
    given()
      .queryParam("periodUnit", "month")
      .queryParam("reportType", "duration")
      .queryParam("completedBefore", EXAMPLE_HISTORIC_TASK_END_TIME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_REPORT_URL);

    verify(mockedReportQuery).completedBefore(any(Date.class));
    verify(mockedReportQuery).duration(PeriodUnit.MONTH);
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testHistoricAfterQuery() {
    given()
      .queryParam("periodUnit", "month")
      .queryParam("reportType", "duration")
      .queryParam("completedAfter", EXAMPLE_HISTORIC_TASK_START_TIME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_REPORT_URL);

    verify(mockedReportQuery).completedAfter(any(Date.class));
    verify(mockedReportQuery).duration(PeriodUnit.MONTH);
    verifyNoMoreInteractions(mockedReportQuery);
  }

  private Map<String, String> getCompleteStartDateAsStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("completedBefore", EXAMPLE_HISTORIC_TASK_INST_START_TIME);
    parameters.put("completedAfter", EXAMPLE_HISTORIC_TASK_INST_END_TIME);

    return parameters;
  }

  private void verifyStringStartParameterQueryInvocations() {
    Map<String, String> startDateParameters = getCompleteStartDateAsStringQueryParameters();

    verify(mockedReportQuery).completedBefore(DateTimeUtil.parseDate(startDateParameters.get("completedBefore")));
    verify(mockedReportQuery).completedAfter(DateTimeUtil.parseDate(startDateParameters.get("completedAfter")));
  }
}
