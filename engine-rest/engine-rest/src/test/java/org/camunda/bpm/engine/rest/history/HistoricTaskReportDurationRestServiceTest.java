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
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricTaskInstanceReport;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response.Status;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.query.PeriodUnit.MONTH;
import static org.camunda.bpm.engine.query.PeriodUnit.QUARTER;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_AVG;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MAX;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_MIN;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_DURATION_REPORT_PERIOD;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_END_TIME;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_TASK_INST_START_TIME;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockHistoricTaskInstanceDurationReport;
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
public class HistoricTaskReportDurationRestServiceTest extends AbstractRestServiceTest {


  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String TASK_DURATION_REPORT_URL = TEST_RESOURCE_ROOT_PATH + "/history/task/report/duration";

  protected HistoricTaskInstanceReport mockedReportQuery;

  @Before
  public void setUpRuntimeData() {
    mockedReportQuery = setUpMockHistoricProcessInstanceReportQuery();
  }

  private HistoricTaskInstanceReport setUpMockHistoricProcessInstanceReportQuery() {
    HistoricTaskInstanceReport mockedReportQuery = mock(HistoricTaskInstanceReport.class);
    
    when(mockedReportQuery.completedAfter(any(Date.class))).thenReturn(mockedReportQuery);
    when(mockedReportQuery.completedBefore(any(Date.class))).thenReturn(mockedReportQuery);

    List<DurationReportResult> durationReportByMonth = createMockHistoricTaskInstanceDurationReport(MONTH);
    when(mockedReportQuery.duration(MONTH)).thenReturn(durationReportByMonth);

    List<DurationReportResult> durationReportByQuarter = createMockHistoricTaskInstanceDurationReport(QUARTER);
    when(mockedReportQuery.duration(QUARTER)).thenReturn(durationReportByQuarter);

    when(mockedReportQuery.duration(null)).thenThrow(new NotValidException("periodUnit is null"));

    when(processEngine.getHistoryService().createHistoricTaskInstanceReport()).thenReturn(mockedReportQuery);

    return mockedReportQuery;
  }

  @Test
  public void testWithoutDurationParam() {
    given()
      .queryParam("periodUnit", "month")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_DURATION_REPORT_URL);

    verify(mockedReportQuery).duration(MONTH);
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testEmptyReportByMonth() {
    given()
      .queryParam("reportType", "duration")
      .queryParam("periodUnit", "month")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_DURATION_REPORT_URL);

    verify(mockedReportQuery).duration(MONTH);
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testEmptyReportByQuarter() {
    given()
      .queryParam("reportType", "duration")
      .queryParam("periodUnit", "quarter")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_DURATION_REPORT_URL);

    verify(mockedReportQuery).duration(QUARTER);
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testInvalidReportType() {
    given()
      .queryParam("reportType", "abc")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot set query parameter 'reportType' to value 'abc'"))
    .when()
      .get(TASK_DURATION_REPORT_URL);
  }

  @Test
  public void testInvalidPeriodUnit() {
    given()
      .queryParam("periodUnit", "abc")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot set query parameter 'periodUnit' to value 'abc'"))
    .when()
      .get(TASK_DURATION_REPORT_URL);
  }

  @Test
  public void testMissingPeriodUnit() {
    given()
      .queryParam("reportType", "duration")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("periodUnit is null"))
    .when()
      .get(TASK_DURATION_REPORT_URL);
  }

  @Test
  public void testMissingAuthorization() {
    String message = "not authorized";
    when(mockedReportQuery.duration(MONTH)).thenThrow(new AuthorizationException(message));

    given()
      .queryParam("reportType", "duration")
      .queryParam("periodUnit", "month")
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
    .when()
      .get(TASK_DURATION_REPORT_URL);
  }

  @Test
  public void testDurationReportByMonth() {
    Response response = given()
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
      .get(TASK_DURATION_REPORT_URL);

    String content = response.asString();
    List<String> reports = from(content).getList("");
    Assert.assertEquals("There should be one report returned.", 1, reports.size());
    Assert.assertNotNull("The returned report should not be null.", reports.get(0));
  }

  @Test
  public void testDurationReportByQuarter() {
    Response response = given()
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
      .get(TASK_DURATION_REPORT_URL);

    String content = response.asString();
    List<String> reports = from(content).getList("");
    Assert.assertEquals("There should be one report returned.", 1, reports.size());
    Assert.assertNotNull("The returned report should not be null.", reports.get(0));
  }

  @Test
  public void testHistoricBeforeAndAfterEndTimeQuery() {
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
      .get(TASK_DURATION_REPORT_URL);

    verifyStringStartParameterQueryInvocations();
  }

  @Test
  public void testHistoricBeforeQuery() {
    given()
      .queryParam("periodUnit", "month")
      .queryParam("reportType", "duration")
      .queryParam("completedBefore", EXAMPLE_HISTORIC_TASK_INST_START_TIME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_DURATION_REPORT_URL);

    verify(mockedReportQuery).completedBefore(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_TASK_INST_START_TIME));
  }

  @Test
  public void testHistoricAfterQuery() {
    given()
      .queryParam("periodUnit", "month")
      .queryParam("reportType", "duration")
      .queryParam("completedAfter", EXAMPLE_HISTORIC_TASK_INST_START_TIME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(TASK_DURATION_REPORT_URL);

    verify(mockedReportQuery).completedAfter(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_TASK_INST_END_TIME));
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
