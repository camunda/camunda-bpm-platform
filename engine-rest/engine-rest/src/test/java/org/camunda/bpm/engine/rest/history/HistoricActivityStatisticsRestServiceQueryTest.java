/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.rest.history;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.DATE_FORMAT_WITH_TIMEZONE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.impl.HistoricActivityStatisticsQueryImpl;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 *
 * @author Roman Smirnov
 *
 */
public class HistoricActivityStatisticsRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history";
  protected static final String HISTORIC_ACTIVITY_STATISTICS_URL = HISTORY_URL + "/process-definition/{id}/statistics";

  private HistoricActivityStatisticsQuery historicActivityStatisticsQuery;

  @Before
  public void setUpRuntimeData() {
    setupHistoricActivityStatisticsMock();
  }

  private void setupHistoricActivityStatisticsMock() {
    List<HistoricActivityStatistics> mocks = MockProvider.createMockHistoricActivityStatistics();

    historicActivityStatisticsQuery = mock(HistoricActivityStatisticsQueryImpl.class);
    when(processEngine.getHistoryService().createHistoricActivityStatisticsQuery(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(historicActivityStatisticsQuery);
    when(historicActivityStatisticsQuery.unlimitedList()).thenReturn(mocks);
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
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("canceled", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).includeCanceled();
    inOrder.verify(historicActivityStatisticsQuery).unlimitedList();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testAdditionalFinishedOption() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("finished", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).includeFinished();
    inOrder.verify(historicActivityStatisticsQuery).unlimitedList();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testAdditionalCompleteScopeOption() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    . queryParam("completeScope", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).includeCompleteScope();
    inOrder.verify(historicActivityStatisticsQuery).unlimitedList();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testAdditionalStartedAfterOption() {
    final Date testDate = new Date(0);
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("startedAfter", DATE_FORMAT_WITH_TIMEZONE.format(testDate))
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).startedAfter(testDate);
    inOrder.verify(historicActivityStatisticsQuery).unlimitedList();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testAdditionalStartedBeforeOption() {
    final Date testDate = new Date(0);
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("startedBefore", DATE_FORMAT_WITH_TIMEZONE.format(testDate))
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).startedBefore(testDate);
    inOrder.verify(historicActivityStatisticsQuery).unlimitedList();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testAdditionalFinishedAfterOption() {
    final Date testDate = new Date(0);
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("finishedAfter", DATE_FORMAT_WITH_TIMEZONE.format(testDate))
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).finishedAfter(testDate);
    inOrder.verify(historicActivityStatisticsQuery).unlimitedList();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testAdditionalFinishedBeforeOption() {
    final Date testDate = new Date(0);
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("finishedBefore", DATE_FORMAT_WITH_TIMEZONE.format(testDate))
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).finishedBefore(testDate);
    inOrder.verify(historicActivityStatisticsQuery).unlimitedList();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testAdditionalCompleteScopeAndCanceledOption() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("completeScope", "true")
      .queryParam("canceled", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    verify(historicActivityStatisticsQuery).includeCompleteScope();
    verify(historicActivityStatisticsQuery).includeCanceled();
    verify(historicActivityStatisticsQuery).unlimitedList();
    verifyNoMoreInteractions(historicActivityStatisticsQuery);
  }

  @Test
  public void testAdditionalCompleteScopeAndFinishedOption() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("completeScope", "true")
      .queryParam("finished", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    verify(historicActivityStatisticsQuery).includeCompleteScope();
    verify(historicActivityStatisticsQuery).includeFinished();
    verify(historicActivityStatisticsQuery).unlimitedList();
    verifyNoMoreInteractions(historicActivityStatisticsQuery);
  }

  @Test
  public void testAdditionalCanceledAndFinishedOption() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("canceled", "true")
      .queryParam("finished", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    verify(historicActivityStatisticsQuery).includeCanceled();
    verify(historicActivityStatisticsQuery).includeFinished();
    verify(historicActivityStatisticsQuery).unlimitedList();
    verifyNoMoreInteractions(historicActivityStatisticsQuery);
  }

  @Test
  public void testAdditionalCompleteScopeAndFinishedAndCanceledOption() {
    given()
    .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("completeScope", "true")
      .queryParam("finished", "true")
      .queryParam("canceled", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    verify(historicActivityStatisticsQuery).includeCompleteScope();
    verify(historicActivityStatisticsQuery).includeFinished();
    verify(historicActivityStatisticsQuery).includeCanceled();
    verify(historicActivityStatisticsQuery).unlimitedList();
    verifyNoMoreInteractions(historicActivityStatisticsQuery);
  }

  @Test
  public void testAdditionalCompleteScopeAndFinishedAndCanceledOptionFalse() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("completeScope", "false")
      .queryParam("finished", "false")
      .queryParam("canceled", "false")
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    verify(historicActivityStatisticsQuery).unlimitedList();
    verifyNoMoreInteractions(historicActivityStatisticsQuery);
  }


  @Test
  public void testProcessInstanceIdInFilter() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("processInstanceIdIn", "foo,bar")
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).processInstanceIdIn(new String[] {"foo", "bar"});
    inOrder.verify(historicActivityStatisticsQuery).unlimitedList();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testIncidentsFilter() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("incidents", "true")
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).includeIncidents();
    inOrder.verify(historicActivityStatisticsQuery).unlimitedList();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testSimpleTaskQuery() {
    Response response = given()
          .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
         .then().expect()
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
    long openIncidents = from(content).getLong("[0].openIncidents");
    long resolvedIncidents = from(content).getLong("[0].resolvedIncidents");
    long deletedIncidents = from(content).getLong("[0].deletedIncidents");

    Assert.assertEquals(MockProvider.EXAMPLE_ACTIVITY_ID, id);
    Assert.assertEquals(MockProvider.EXAMPLE_INSTANCES_LONG, instances);
    Assert.assertEquals(MockProvider.EXAMPLE_CANCELED_LONG, canceled);
    Assert.assertEquals(MockProvider.EXAMPLE_FINISHED_LONG, finished);
    Assert.assertEquals(MockProvider.EXAMPLE_COMPLETE_SCOPE_LONG, completeScope);
    Assert.assertEquals(MockProvider.EXAMPLE_OPEN_INCIDENTS_LONG, openIncidents);
    Assert.assertEquals(MockProvider.EXAMPLE_RESOLVED_INCIDENTS_LONG, resolvedIncidents);
    Assert.assertEquals(MockProvider.EXAMPLE_DELETED_INCIDENTS_LONG, deletedIncidents);

    id = from(content).getString("[1].id");
    instances = from(content).getLong("[1].instances");
    canceled = from(content).getLong("[1].canceled");
    finished = from(content).getLong("[1].finished");
    completeScope = from(content).getLong("[1].completeScope");
    openIncidents = from(content).getLong("[1].openIncidents");
    resolvedIncidents = from(content).getLong("[1].resolvedIncidents");
    deletedIncidents = from(content).getLong("[1].deletedIncidents");

    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID, id);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_INSTANCES_LONG, instances);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_CANCELED_LONG, canceled);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_FINISHED_LONG, finished);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_COMPLETE_SCOPE_LONG, completeScope);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_OPEN_INCIDENTS_LONG, openIncidents);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_RESOLVED_INCIDENTS_LONG, resolvedIncidents);
    Assert.assertEquals(MockProvider.ANOTHER_EXAMPLE_DELETED_INCIDENTS_LONG, deletedIncidents);

  }

  @Test
  public void testSortByParameterOnly() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("sortBy", "activityId")
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("sortOrder", "asc")
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testInvalidSortOrder() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("sortOrder", "invalid")
      .queryParam("sortBy", "activityId")
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot set query parameter 'sortOrder' to value 'invalid'"))
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testInvalidSortByParameterOnly() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("sortOrder", "asc")
      .queryParam("sortBy", "invalid")
    .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot set query parameter 'sortBy' to value 'invalid'"))
      .when().get(HISTORIC_ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testValidSortingParameters() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("sortOrder", "asc")
      .queryParam("sortBy", "activityId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).orderByActivityId();
    inOrder.verify(historicActivityStatisticsQuery).asc();
    inOrder.verify(historicActivityStatisticsQuery).unlimitedList();
    inOrder.verifyNoMoreInteractions();

    given()
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .queryParam("sortOrder", "desc")
      .queryParam("sortBy", "activityId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_ACTIVITY_STATISTICS_URL);

    inOrder = Mockito.inOrder(historicActivityStatisticsQuery);
    inOrder.verify(historicActivityStatisticsQuery).orderByActivityId();
    inOrder.verify(historicActivityStatisticsQuery).desc();
    inOrder.verify(historicActivityStatisticsQuery).unlimitedList();
    inOrder.verifyNoMoreInteractions();
  }

}
