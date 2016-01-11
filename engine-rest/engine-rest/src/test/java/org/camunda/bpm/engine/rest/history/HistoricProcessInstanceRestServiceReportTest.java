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
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.query.PeriodUnit.MONTH;
import static org.camunda.bpm.engine.query.PeriodUnit.QUARTER;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_AVG;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MAX;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MIN;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_PERIOD;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockHistoricProcessInstanceDurationReportByMonth;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockHistoricProcessInstanceDurationReportByQuarter;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricProcessInstanceReport;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.dto.converter.ReportResultToCsvConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response.Status;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricProcessInstanceRestServiceReportTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORIC_PROCESS_INSTANCE_REPORT_URL = TEST_RESOURCE_ROOT_PATH + "/history/process-instance/report";

  protected HistoricProcessInstanceReport mockedReportQuery;

  @Before
  public void setUpRuntimeData() {
    mockedReportQuery = setUpMockHistoricProcessInstanceReportQuery();
  }

  private HistoricProcessInstanceReport setUpMockHistoricProcessInstanceReportQuery() {
    HistoricProcessInstanceReport mockedReportQuery = mock(HistoricProcessInstanceReport.class);

    when(mockedReportQuery.processDefinitionIdIn(anyString())).thenReturn(mockedReportQuery);
    when(mockedReportQuery.processDefinitionKeyIn(anyString())).thenReturn(mockedReportQuery);
    when(mockedReportQuery.startedAfter(any(Date.class))).thenReturn(mockedReportQuery);
    when(mockedReportQuery.startedBefore(any(Date.class))).thenReturn(mockedReportQuery);

    List<DurationReportResult> durationReportByMonth = createMockHistoricProcessInstanceDurationReportByMonth();
    when(mockedReportQuery.duration(MONTH)).thenReturn(durationReportByMonth);

    List<DurationReportResult> durationReportByQuarter = createMockHistoricProcessInstanceDurationReportByQuarter();
    when(mockedReportQuery.duration(QUARTER)).thenReturn(durationReportByQuarter);

    when(mockedReportQuery.duration(null)).thenThrow(new NotValidException("periodUnit is null"));

    when(processEngine.getHistoryService().createHistoricProcessInstanceReport()).thenReturn(mockedReportQuery);

    return mockedReportQuery;
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
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

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
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

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
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot set query parameter 'reportType' to value 'abc'"))
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);
  }

  @Test
  public void testInvalidPeriodUnit() {
    given()
    .queryParam("periodUnit", "abc")
  .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot set query parameter 'periodUnit' to value 'abc'"))
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);
  }

  @Test
  public void testMissingReportType() {
    given()
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Unknown report type null"))
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);
  }

  @Test
  public void testMissingPeriodUnit() {
    given()
      .queryParam("reportType", "duration")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("periodUnit is null"))
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);
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
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);
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
        .when()
          .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    String content = response.asString();
    List<String> reports = from(content).getList("");
    Assert.assertEquals("There should be one report returned.", 1, reports.size());
    Assert.assertNotNull("The returned report should not be null.", reports.get(0));

    long returnedAvg = from(content).getLong("[0].average");
    long returnedMax = from(content).getLong("[0].maximum");
    long returnedMin = from(content).getLong("[0].minimum");
    int returnedPeriod = from(content).getInt("[0].period");
    String returnedPeriodUnit = from(content).getString("[0].periodUnit");

    Assert.assertEquals(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_AVG, returnedAvg);
    Assert.assertEquals(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MAX, returnedMax);
    Assert.assertEquals(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MIN, returnedMin);
    Assert.assertEquals(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_PERIOD, returnedPeriod);
    Assert.assertEquals(MONTH.toString(), returnedPeriodUnit);
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
        .when()
          .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    String content = response.asString();
    List<String> reports = from(content).getList("");
    Assert.assertEquals("There should be one report returned.", 1, reports.size());
    Assert.assertNotNull("The returned report should not be null.", reports.get(0));

    long returnedAvg = from(content).getLong("[0].average");
    long returnedMax = from(content).getLong("[0].maximum");
    long returnedMin = from(content).getLong("[0].minimum");
    int returnedPeriod = from(content).getInt("[0].period");
    String returnedPeriodUnit = from(content).getString("[0].periodUnit");

    Assert.assertEquals(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_AVG, returnedAvg);
    Assert.assertEquals(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MAX, returnedMax);
    Assert.assertEquals(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MIN, returnedMin);
    Assert.assertEquals(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_PERIOD, returnedPeriod);
    Assert.assertEquals(QUARTER.toString(), returnedPeriodUnit);
  }

  @Test
  public void testListParameters() {
    String aProcDefId = "anProcDefId";
    String anotherProcDefId = "anotherProcDefId";

    String aProcDefKey = "anProcDefKey";
    String anotherProcDefKey = "anotherProcDefKey";

    given()
      .queryParam("periodUnit", "month")
      .queryParam("reportType", "duration")
      .queryParam("processDefinitionIdIn", aProcDefId + "," + anotherProcDefId)
      .queryParam("processDefinitionKeyIn", aProcDefKey + "," + anotherProcDefKey)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    verify(mockedReportQuery).processDefinitionIdIn(aProcDefId, anotherProcDefId);
    verify(mockedReportQuery).processDefinitionKeyIn(aProcDefKey, anotherProcDefKey);
    verify(mockedReportQuery).duration(MONTH);
  }

  @Test
  public void testHistoricBeforeAndAfterStartTimeQuery() {
    given()
      .queryParam("periodUnit", "month")
      .queryParam("reportType", "duration")
      .queryParam("startedBefore", EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE)
      .queryParam("startedAfter", EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    verifyStringStartParameterQueryInvocations();
  }

  @Test
  public void testEmptyCsvReportByMonth() {
    given()
      .queryParam("reportType", "duration")
      .queryParam("periodUnit", "month")
      .accept("text/csv")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType("text/csv")
        .header("Content-Disposition", "attachment; filename=process-instance-report.csv")
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    verify(mockedReportQuery).duration(MONTH);
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testEmptyCsvReportByQuarter() {
    given()
      .queryParam("reportType", "duration")
      .queryParam("periodUnit", "quarter")
      .accept("text/csv")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType("text/csv")
        .header("Content-Disposition", "attachment; filename=process-instance-report.csv")
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    verify(mockedReportQuery).duration(QUARTER);
    verifyNoMoreInteractions(mockedReportQuery);
  }

  @Test
  public void testCsvInvalidReportType() {
    given()
    .queryParam("reportType", "abc")
  .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot set query parameter 'reportType' to value 'abc'"))
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);
  }

  @Test
  public void testCsvInvalidPeriodUnit() {
    given()
    .queryParam("periodUnit", "abc")
  .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Cannot set query parameter 'periodUnit' to value 'abc'"))
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);
  }

  @Test
  public void testCsvMissingReportType() {
    given()
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Unknown report type null"))
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);
  }

  @Test
  public void testCsvMissingPeriodUnit() {
    given()
      .queryParam("reportType", "duration")
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("periodUnit is null"))
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);
  }

  @Test
  public void testCsvMissingAuthorization() {
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
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);
  }

  @Test
  public void testCsvDurationReportByMonth() {
    Response response = given()
        .queryParam("reportType", "duration")
        .queryParam("periodUnit", "month")
        .accept("text/csv")
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("text/csv")
          .header("Content-Disposition", "attachment; filename=process-instance-report.csv")
      .when().get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    String responseContent = response.asString();
    assertTrue(responseContent.contains(ReportResultToCsvConverter.DURATION_HEADER));
    assertTrue(responseContent.contains(MONTH.toString()));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_AVG)));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MIN)));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MAX)));
  }

  @Test
  public void testCsvDurationReportByQuarter() {
    Response response = given()
        .queryParam("reportType", "duration")
        .queryParam("periodUnit", "quarter")
        .accept("text/csv")
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("text/csv")
          .header("Content-Disposition", "attachment; filename=process-instance-report.csv")
      .when().get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    String responseContent = response.asString();
    assertTrue(responseContent.contains(ReportResultToCsvConverter.DURATION_HEADER));
    assertTrue(responseContent.contains(QUARTER.toString()));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_AVG)));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MIN)));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MAX)));
  }

  @Test
  public void testApplicationCsvDurationReportByMonth() {
    Response response = given()
        .queryParam("reportType", "duration")
        .queryParam("periodUnit", "month")
        .accept("application/csv")
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("application/csv")
          .header("Content-Disposition", "attachment; filename=process-instance-report.csv")
      .when().get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    String responseContent = response.asString();
    assertTrue(responseContent.contains(ReportResultToCsvConverter.DURATION_HEADER));
    assertTrue(responseContent.contains(MONTH.toString()));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_AVG)));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MIN)));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MAX)));
  }

  @Test
  public void testApplicationCsvDurationReportByQuarter() {
    Response response = given()
        .queryParam("reportType", "duration")
        .queryParam("periodUnit", "quarter")
        .accept("application/csv")
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType("application/csv")
          .header("Content-Disposition", "attachment; filename=process-instance-report.csv")
      .when().get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    String responseContent = response.asString();
    assertTrue(responseContent.contains(ReportResultToCsvConverter.DURATION_HEADER));
    assertTrue(responseContent.contains(QUARTER.toString()));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_AVG)));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MIN)));
    assertTrue(responseContent.contains(String.valueOf(EXAMPLE_HISTORIC_PROC_INST_DURATION_REPORT_MAX)));
  }

  @Test
  public void testCsvListParameters() {
    String aProcDefId = "anProcDefId";
    String anotherProcDefId = "anotherProcDefId";

    String aProcDefKey = "anProcDefKey";
    String anotherProcDefKey = "anotherProcDefKey";

    given()
      .queryParam("periodUnit", "month")
      .queryParam("reportType", "duration")
      .queryParam("processDefinitionIdIn", aProcDefId + "," + anotherProcDefId)
      .queryParam("processDefinitionKeyIn", aProcDefKey + "," + anotherProcDefKey)
      .accept("text/csv")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType("text/csv")
        .header("Content-Disposition", "attachment; filename=process-instance-report.csv")
      .when()
        .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    verify(mockedReportQuery).processDefinitionIdIn(aProcDefId, anotherProcDefId);
    verify(mockedReportQuery).processDefinitionKeyIn(aProcDefKey, anotherProcDefKey);
    verify(mockedReportQuery).duration(MONTH);
  }

  @Test
  public void testCsvHistoricBeforeAndAfterStartTimeQuery() {
    given()
      .queryParam("periodUnit", "month")
      .queryParam("reportType", "duration")
      .queryParam("startedBefore", EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE)
      .queryParam("startedAfter", EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER)
      .accept("text/csv")
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType("text/csv")
        .header("Content-Disposition", "attachment; filename=process-instance-report.csv")
    .when()
      .get(HISTORIC_PROCESS_INSTANCE_REPORT_URL);

    verifyStringStartParameterQueryInvocations();
  }

  private Map<String, String> getCompleteStartDateAsStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("startedAfter", EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER);
    parameters.put("startedBefore", EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE);

    return parameters;
  }

  private void verifyStringStartParameterQueryInvocations() {
    Map<String, String> startDateParameters = getCompleteStartDateAsStringQueryParameters();

    verify(mockedReportQuery).startedBefore(DateTimeUtil.parseDate(startDateParameters.get("startedBefore")));
    verify(mockedReportQuery).startedAfter(DateTimeUtil.parseDate(startDateParameters.get("startedAfter")));
  }

}
