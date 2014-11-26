package org.camunda.bpm.engine.rest.history;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import javax.xml.registry.InvalidRequestException;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 *
 * @author Roman Smirnov
 *
 */
public abstract class AbstractHistoricIncidentRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String HISTORY_INCIDENT_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/history/incident";
  protected static final String HISTORY_INCIDENT_COUNT_QUERY_URL = HISTORY_INCIDENT_QUERY_URL + "/count";

  private HistoricIncidentQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = mock(HistoricIncidentQuery.class);

    List<HistoricIncident> incidents = MockProvider.createMockHistoricIncidents();

    when(mockedQuery.list()).thenReturn(incidents);
    when(mockedQuery.count()).thenReturn((long) incidents.size());

    when(processEngine.getHistoryService().createHistoricIncidentQuery()).thenReturn(mockedQuery);
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("processInstanceId", queryKey)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_INCIDENT_QUERY_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }


  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("processInstanceId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
      .when()
        .get(HISTORY_INCIDENT_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
    .queryParam("sortOrder", "asc")
  .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORY_INCIDENT_QUERY_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("incidentId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByIncidentId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("incidentId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByIncidentId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("createTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCreateTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("createTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCreateTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCreateTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByEndTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("incidentType", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByIncidentType();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("incidentType", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByIncidentType();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("executionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByExecutionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("executionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByExecutionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByActivityId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("causeIncidentId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCauseIncidentId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("causeIncidentId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCauseIncidentId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("rootCauseIncidentId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByRootCauseIncidentId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("rootCauseIncidentId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByRootCauseIncidentId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("configuration", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByConfiguration();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("configuration", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByConfiguration();
    inOrder.verify(mockedQuery).desc();
  }

  @Test
  public void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORY_INCIDENT_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testSimpleHistoricTaskInstanceQuery() {
    Response response = given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(HISTORY_INCIDENT_QUERY_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> incidents = from(content).getList("");
    Assert.assertEquals("There should be one incident returned.", 1, incidents.size());
    Assert.assertNotNull("The returned incident should not be null.", incidents.get(0));

    String returnedId = from(content).getString("[0].id");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedExecutionId = from(content).getString("[0].executionId");
    Date returnedCreateTime = DateTimeUtil.parseDate(from(content).getString("[0].createTime"));
    Date returnedEndTime = DateTimeUtil.parseDate(from(content).getString("[0].endTime"));
    String returnedIncidentType = from(content).getString("[0].incidentType");
    String returnedActivityId = from(content).getString("[0].activityId");
    String returnedCauseIncidentId = from(content).getString("[0].causeIncidentId");
    String returnedRootCauseIncidentId = from(content).getString("[0].rootCauseIncidentId");
    String returnedConfiguration = from(content).getString("[0].configuration");
    String returnedIncidentMessage = from(content).getString("[0].incidentMessage");
    Boolean returnedIncidentOpen = from(content).getBoolean("[0].open");
    Boolean returnedIncidentDeleted = from(content).getBoolean("[0].deleted");
    Boolean returnedIncidentResolved = from(content).getBoolean("[0].resolved");

    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_ID, returnedId);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_PROC_INST_ID, returnedProcessInstanceId);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HIST_INCIDENT_CREATE_TIME), returnedCreateTime);
    Assert.assertEquals(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HIST_INCIDENT_END_TIME), returnedEndTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_EXECUTION_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_PROC_DEF_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_TYPE, returnedIncidentType);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_ACTIVITY_ID, returnedActivityId);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_CAUSE_INCIDENT_ID, returnedCauseIncidentId);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_ROOT_CAUSE_INCIDENT_ID, returnedRootCauseIncidentId);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_CONFIGURATION, returnedConfiguration);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_MESSAGE, returnedIncidentMessage);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_STATE_OPEN, returnedIncidentOpen);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_STATE_DELETED, returnedIncidentDeleted);
    Assert.assertEquals(MockProvider.EXAMPLE_HIST_INCIDENT_STATE_RESOLVED, returnedIncidentResolved);
  }

  @Test
  public void testQueryByIncidentId() {
    String incidentId = MockProvider.EXAMPLE_HIST_INCIDENT_ID;

    given()
      .queryParam("incidentId", incidentId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).incidentId(incidentId);
  }

  @Test
  public void testQueryByIncidentType() {
    String incidentType = MockProvider.EXAMPLE_HIST_INCIDENT_TYPE;

    given()
      .queryParam("incidentType", incidentType)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).incidentType(incidentType);
  }

  @Test
  public void testQueryByIncidentMessage() {
    String incidentMessage = MockProvider.EXAMPLE_HIST_INCIDENT_MESSAGE;

    given()
      .queryParam("incidentMessage", incidentMessage)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).incidentMessage(incidentMessage);
  }

  @Test
  public void testQueryByProcessDefinitionId() {
    String processDefinitionId = MockProvider.EXAMPLE_HIST_INCIDENT_PROC_DEF_ID;

    given()
      .queryParam("processDefinitionId", processDefinitionId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).processDefinitionId(processDefinitionId);
  }

  @Test
  public void testQueryByProcessInstanceId() {
    String processInstanceId = MockProvider.EXAMPLE_HIST_INCIDENT_PROC_INST_ID;

    given()
      .queryParam("processInstanceId", processInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).processInstanceId(processInstanceId);
  }

  @Test
  public void testQueryByExecutionId() {
    String executionId = MockProvider.EXAMPLE_HIST_INCIDENT_EXECUTION_ID;

    given()
      .queryParam("executionId", executionId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).executionId(executionId);
  }

  @Test
  public void testQueryByActivityId() {
    String activityId = MockProvider.EXAMPLE_HIST_INCIDENT_ACTIVITY_ID;

    given()
      .queryParam("activityId", activityId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).activityId(activityId);
  }

  @Test
  public void testQueryByCauseIncidentId() {
    String causeIncidentId = MockProvider.EXAMPLE_HIST_INCIDENT_CAUSE_INCIDENT_ID;

    given()
      .queryParam("causeIncidentId", causeIncidentId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).causeIncidentId(causeIncidentId);
  }

  @Test
  public void testQueryByRootCauseIncidentId() {
    String rootCauseIncidentId = MockProvider.EXAMPLE_HIST_INCIDENT_ROOT_CAUSE_INCIDENT_ID;

    given()
      .queryParam("rootCauseIncidentId", rootCauseIncidentId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).rootCauseIncidentId(rootCauseIncidentId);
  }

  @Test
  public void testQueryByConfiguration() {
    String configuration = MockProvider.EXAMPLE_HIST_INCIDENT_CONFIGURATION;

    given()
      .queryParam("configuration", configuration)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).configuration(configuration);
  }

  @Test
  public void testQueryByOpen() {
    given()
      .queryParam("open", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).open();
  }

  @Test
  public void testQueryByResolved() {
    given()
      .queryParam("resolved", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).resolved();
  }

  @Test
  public void testQueryByDeleted() {
    given()
      .queryParam("deleted", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORY_INCIDENT_QUERY_URL);

    verify(mockedQuery).deleted();
  }

}
