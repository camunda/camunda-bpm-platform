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
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Meyer
 */
public abstract class AbstractMetricsRestServiceInteractionTest extends AbstractRestServiceTest {

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
