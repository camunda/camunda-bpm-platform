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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 *
 * @author Roman Smirnov
 *
 */
public class AbstractHistoricActivityStatisticsRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history";
  protected static final String HISTORIC_ACTIVITY_STATISTICS_URL = HISTORY_URL + "/process-definition/{id}/statistics";

  private HistoricActivityStatisticsQuery historicActivityStatisticsQuery;

  @Before
  public void setUpRuntimeData() {
    setupHistoricActivityStatisticsMock();
  }

  private void setupHistoricActivityStatisticsMock() {
    List<HistoricActivityStatistics> mocks = MockProvider.createMockHistoricActivityStatistics();

    historicActivityStatisticsQuery = mock(HistoricActivityStatisticsQuery.class);
    when(processEngine.getHistoryService().createHistoricActivityStatisticsQuery(anyString())).thenReturn(historicActivityStatisticsQuery);
    when(historicActivityStatisticsQuery.list()).thenReturn(mocks);
  }

  @Test
  public void testHistoricActivityStatisticsRetrieval() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("id", hasItems(MockProvider.EXAMPLE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID))
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testAdditionalCanceledOption() {
    given().queryParam("canceled", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).includeCanceled();
    inOrder.verify(historicActivityStatisticsQuery).list();
  }

  @Test
  public void testAdditionalFinishedOption() {
    given().queryParam("finished", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).includeFinished();
    inOrder.verify(historicActivityStatisticsQuery).list();
  }

  @Test
  public void testAdditionalCompleteScopeOption() {
    given().queryParam("completeScope", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).includeCompleteScope();
    inOrder.verify(historicActivityStatisticsQuery).list();
  }

  @Test
  public void testAdditionalCompleteScopeAndCanceledOption() {
    given()
      .queryParam("completeScope", "true")
      .queryParam("canceled", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    verify(historicActivityStatisticsQuery).includeCompleteScope();
    verify(historicActivityStatisticsQuery).includeCanceled();
    verify(historicActivityStatisticsQuery).list();
  }

  @Test
  public void testAdditionalCompleteScopeAndFinishedOption() {
    given()
      .queryParam("completeScope", "true")
      .queryParam("finished", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    verify(historicActivityStatisticsQuery).includeCompleteScope();
    verify(historicActivityStatisticsQuery).includeFinished();
    verify(historicActivityStatisticsQuery).list();
  }

  @Test
  public void testAdditionalCanceledAndFinishedOption() {
    given()
      .queryParam("canceled", "true")
      .queryParam("finished", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    verify(historicActivityStatisticsQuery).includeCanceled();
    verify(historicActivityStatisticsQuery).includeFinished();
    verify(historicActivityStatisticsQuery).list();
  }

  @Test
  public void testAdditionalCompleteScopeAndFinishedAndCanceledOption() {
    given()
      .queryParam("completeScope", "true")
      .queryParam("finished", "true")
      .queryParam("canceled", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    verify(historicActivityStatisticsQuery).includeCompleteScope();
    verify(historicActivityStatisticsQuery).includeFinished();
    verify(historicActivityStatisticsQuery).includeCanceled();
    verify(historicActivityStatisticsQuery).list();
  }

  @Test
  public void testSimpleTaskQuery() {
    Response response = given().then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    String content = response.asString();
    List<String> result = from(content).getList("");
    Assert.assertEquals(2, result.size());

    Assert.assertNotNull(result.get(0));
    Assert.assertNotNull(result.get(1));

    String id = from(content).getString("[0].id");
    long instances = from(content).getLong("[0].instances");
    long canceled = from(content).getLong("[0].canceled");
    long finished = from(content).getLong("[0].finished");
    long completeScope = from(content).getLong("[0].completeScope");

    Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_ID, id);
    Assert.assertEquals(MockProvider.EXAMPLE_INSTANCES_LONG, instances);
    Assert.assertEquals(MockProvider.EXAMPLE_CANCELED_LONG, canceled);
    Assert.assertEquals(MockProvider.EXAMPLE_FINISHED_LONG, finished);
    Assert.assertEquals(MockProvider.EXAMPLE_COMPLETE_SCOPE_LONG, completeScope);

    id = from(content).getString("[1].id");
    instances = from(content).getLong("[1].instances");
    canceled = from(content).getLong("[1].canceled");
    finished = from(content).getLong("[1].finished");
    completeScope = from(content).getLong("[1].completeScope");

    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID, id);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_INSTANCES_LONG, instances);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_CANCELED_LONG, canceled);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_FINISHED_LONG, finished);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_COMPLETE_SCOPE_LONG, completeScope);

  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "dueDate")
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testInvalidSortOrder() {
    given()
      .queryParam("sortOrder", "invalid")
      .queryParam("sortBy", "activityId")
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("sortOrder parameter has invalid value: invalid"))
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testInvalidSortByParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
      .queryParam("sortBy", "invalid")
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("sortBy parameter has invalid value: invalid"))
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testValidSortingParameters() {
    given()
      .queryParam("sortOrder", "asc")
      .queryParam("sortBy", "activityId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).orderByActivityId();
    inOrder.verify(historicActivityStatisticsQuery).asc();

    given()
      .queryParam("sortOrder", "desc")
      .queryParam("sortBy", "activityId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_ACTIVITY_STATISTICS_URL);

    inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).orderByActivityId();
    inOrder.verify(historicActivityStatisticsQuery).desc();
  }

}
