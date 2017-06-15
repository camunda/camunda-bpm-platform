/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.rest;

import static org.camunda.bpm.engine.rest.util.DateTimeUtils.DATE_FORMAT_WITH_TIMEZONE;
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.withTimezone;
import static org.hamcrest.Matchers.*;
import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import javax.ws.rs.core.Response.Status;


import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Daniel Meyer
 */
public class MetricsRestServiceInteractionTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  public static final String METRICS_URL = TEST_RESOURCE_ROOT_PATH + MetricsRestService.PATH;
  public static final String SINGLE_METER_URL = METRICS_URL + "/{name}";
  public static final String SUM_URL = SINGLE_METER_URL + "/sum";

  protected ManagementService managementServiceMock;
  private MetricsQuery meterQueryMock;

  @Before
  public void setUpRuntimeData() {
    managementServiceMock = mock(ManagementService.class);

    when(processEngine.getManagementService()).thenReturn(managementServiceMock);

    meterQueryMock = MockProvider.createMockMeterQuery();
    when(managementServiceMock.createMetricsQuery()).thenReturn(meterQueryMock);

  }

  @Test
  public void testGetInterval() {
    when(meterQueryMock.interval()).thenReturn(MockProvider.createMockMetricIntervalResult());

    given()
      .then()
        .expect()
          .body("[0].name", equalTo("metricName"))
          .body("[0].timestamp", equalTo(withTimezone("1970-01-01T01:45:00")))
          .body("[0].reporter", equalTo("REPORTER"))
          .body("[0].value", equalTo(23))

          .body("[1].name", equalTo("metricName"))
          .body("[1].timestamp", equalTo(withTimezone("1970-01-01T01:30:00")))
          .body("[1].reporter", equalTo("REPORTER"))
          .body("[1].value", equalTo(22))

          .body("[2].name", equalTo("metricName"))
          .body("[2].timestamp", equalTo(withTimezone("1970-01-01T01:15:00")))
          .body("[2].reporter", equalTo("REPORTER"))
          .body("[2].value", equalTo(21))
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  public void testGetIntervalByName() {
    given()
      .queryParam("name", MockProvider.EXAMPLE_METRICS_NAME)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(MockProvider.EXAMPLE_METRICS_NAME);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }


  @Test
  public void testGetIntervalByReporter() {
    given()
      .queryParam("reporter", MockProvider.EXAMPLE_METRICS_REPORTER)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(MockProvider.EXAMPLE_METRICS_REPORTER);
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }


  @Test
  public void testGetIntervalWithOffset() {
    given()
      .queryParam("firstResult", 10)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock).offset(10);
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  public void testGetIntervalWithLimit() {
    given()
      .queryParam("maxResults", 10)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock).limit(10);
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  public void testGetIntervalWithStartDate() {

    given()
      .queryParam("startDate", DATE_FORMAT_WITH_TIMEZONE.format(new Date(0)))
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock).startDate(new Date(0));
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  public void testGetIntervalWithEndDate() {

    given()
      .queryParam("endDate", DATE_FORMAT_WITH_TIMEZONE.format(new Date(15 * 60 * 1000)))
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock).endDate(new Date(15 * 60 * 1000));
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  public void testGetIntervalWithCustomInterval() {
    given()
      .queryParam("interval", 300)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock, times(1)).interval(300);
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  public void testGetIntervalWithAll() {

    given()
      .queryParam("name", MockProvider.EXAMPLE_METRICS_NAME)
      .queryParam("reporter", MockProvider.EXAMPLE_METRICS_REPORTER)
      .queryParam("maxResults", 10)
      .queryParam("firstResult", 10)
      .queryParam("startDate", DATE_FORMAT_WITH_TIMEZONE.format(new Date(0)))
      .queryParam("endDate", DATE_FORMAT_WITH_TIMEZONE.format(new Date(15 * 60 * 1000)))
      .queryParam("interval", 300)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(MockProvider.EXAMPLE_METRICS_NAME);
    verify(meterQueryMock).reporter(MockProvider.EXAMPLE_METRICS_REPORTER);
    verify(meterQueryMock).offset(10);
    verify(meterQueryMock).limit(10);
    verify(meterQueryMock).startDate(new Date(0));
    verify(meterQueryMock).endDate(new Date(15 * 60 * 1000));
    verify(meterQueryMock, times(1)).interval(300);
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  public void testGetSum() {

    when(meterQueryMock.sum()).thenReturn(10l);

    given()
      .pathParam("name", Metrics.ACTIVTY_INSTANCE_START)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("result", equalTo(10))
     .when()
      .get(SUM_URL);

    verify(meterQueryMock).name(Metrics.ACTIVTY_INSTANCE_START);
    verify(meterQueryMock, times(1)).sum();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  public void testGetSumWithTimestamps() {

    when(meterQueryMock.sum()).thenReturn(10l);

    given()
      .pathParam("name", Metrics.ACTIVTY_INSTANCE_START)
      .queryParam("startDate", MockProvider.EXAMPLE_METRICS_START_DATE)
      .queryParam("endDate", MockProvider.EXAMPLE_METRICS_END_DATE)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("result", equalTo(10))
     .when()
      .get(SUM_URL);

    verify(meterQueryMock).name(Metrics.ACTIVTY_INSTANCE_START);
    verify(meterQueryMock).startDate(any(Date.class));
    verify(meterQueryMock).endDate(any(Date.class));
    verify(meterQueryMock, times(1)).sum();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  public void testGetSumWithInvalidTimestamp() {

    when(meterQueryMock.sum()).thenReturn(10l);

    given()
      .pathParam("name", Metrics.ACTIVTY_INSTANCE_START)
      .queryParam("startDate", "INVALID-TIME-STAMP")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
     .when()
      .get(SUM_URL);

  }


}
