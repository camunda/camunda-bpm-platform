package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;

public class StatisticsRestTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String PROCESS_DEFINITION_STATISTICS_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/statistics";
  protected static final String ACTIVITY_STATISTICS_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/{id}/statistics";
  protected static final String ACTIVITY_STATISTICS_BY_KEY_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/key/{key}/statistics";
  private ProcessDefinitionStatisticsQuery processDefinitionStatisticsQueryMock;
  private ActivityStatisticsQuery activityQueryMock;
  private ProcessDefinitionQuery processDefinitionQueryMock;

  @Before
  public void setUpRuntimeData() {
    setupProcessDefinitionStatisticsMock();
    setupActivityStatisticsMock();
    setupProcessDefinitionMock();
  }

  private void setupActivityStatisticsMock() {
    List<ActivityStatistics> mocks = MockProvider.createMockActivityStatistics();

    activityQueryMock = mock(ActivityStatisticsQuery.class);
    when(activityQueryMock.list()).thenReturn(mocks);
    when(processEngine.getManagementService().createActivityStatisticsQuery(any(String.class))).thenReturn(activityQueryMock);
  }

  private void setupProcessDefinitionStatisticsMock() {
    List<ProcessDefinitionStatistics> mocks = MockProvider.createMockProcessDefinitionStatistics();

    processDefinitionStatisticsQueryMock = mock(ProcessDefinitionStatisticsQuery.class);
    when(processDefinitionStatisticsQueryMock.list()).thenReturn(mocks);
    when(processEngine.getManagementService().createProcessDefinitionStatisticsQuery()).thenReturn(processDefinitionStatisticsQueryMock);
  }

  private void setupProcessDefinitionMock() {
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();
    processDefinitionQueryMock = mock(ProcessDefinitionQuery.class);
    when(processDefinitionQueryMock.processDefinitionKey(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.tenantIdIn(anyString())).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.withoutTenantId()).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.latestVersion()).thenReturn(processDefinitionQueryMock);
    when(processDefinitionQueryMock.singleResult()).thenReturn(mockDefinition);
    when(processDefinitionQueryMock.list()).thenReturn(Collections.singletonList(mockDefinition));
    when(processDefinitionQueryMock.count()).thenReturn(1L);
    when(processEngine.getRepositoryService().createProcessDefinitionQuery()).thenReturn(processDefinitionQueryMock);
  }

  @Test
  public void testStatisticsRetrievalPerProcessDefinitionVersion() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("definition.size()", is(2))
      .body("definition.id", hasItems(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, MockProvider.ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID))
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }

  @Test
  public void testProcessDefinitionStatisticsRetrieval() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("[0].definition.id", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("[0].definition.key", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY))
      .body("[0].definition.category", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_CATEGORY))
      .body("[0].definition.name", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_NAME))
      .body("[0].definition.description", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_DESCRIPTION))
      .body("[0].definition.deploymentId", equalTo(MockProvider.EXAMPLE_DEPLOYMENT_ID))
      .body("[0].definition.version", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_VERSION))
      .body("[0].definition.resource", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME))
      .body("[0].definition.diagram", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME))
      .body("[0].definition.tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
      .body("[0].definition.versionTag", equalTo(MockProvider.EXAMPLE_VERSION_TAG))
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }

  @Test
  public void testActivityStatisticsRetrieval() {
    given().pathParam("id", "aDefinitionId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("id", hasItems(MockProvider.EXAMPLE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID))
    .when().get(ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testActivityStatisticsRetrievalByKey() {
    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("id", hasItems(MockProvider.EXAMPLE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID))
    .when().get(ACTIVITY_STATISTICS_BY_KEY_URL);
  }

  @Test
  public void testAdditionalFailedJobsOption() {
    given().queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(processDefinitionStatisticsQueryMock);
    inOrder.verify(processDefinitionStatisticsQueryMock).includeFailedJobs();
    inOrder.verify(processDefinitionStatisticsQueryMock).list();
  }

  @Test
  public void testAdditionalIncidentsOption() {
    given().queryParam("incidents", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(processDefinitionStatisticsQueryMock);
    inOrder.verify(processDefinitionStatisticsQueryMock).includeIncidents();
    inOrder.verify(processDefinitionStatisticsQueryMock).list();
  }

  @Test
  public void testAdditionalIncidentsForTypeOption() {
    given().queryParam("incidentsForType", "failedJob")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(processDefinitionStatisticsQueryMock);
    inOrder.verify(processDefinitionStatisticsQueryMock).includeIncidentsForType("failedJob");
    inOrder.verify(processDefinitionStatisticsQueryMock).list();
  }

  @Test
  public void testAdditionalIncidentsAndFailedJobsOption() {
    given().queryParam("incidents", "true").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(processDefinitionStatisticsQueryMock);
    inOrder.verify(processDefinitionStatisticsQueryMock).includeFailedJobs();
    inOrder.verify(processDefinitionStatisticsQueryMock).includeIncidents();
    inOrder.verify(processDefinitionStatisticsQueryMock).list();
  }

  @Test
  public void testAdditionalIncidentsForTypeAndFailedJobsOption() {
    given().queryParam("incidentsForType", "failedJob").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(processDefinitionStatisticsQueryMock);
    inOrder.verify(processDefinitionStatisticsQueryMock).includeFailedJobs();
    inOrder.verify(processDefinitionStatisticsQueryMock).includeIncidentsForType("failedJob");
    inOrder.verify(processDefinitionStatisticsQueryMock).list();
  }

  @Test
  public void testAdditionalIncidentsAndIncidentsForType() {
    given().queryParam("incidents", "true").queryParam("incidentsForType", "anIncidentTpye")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }

  @Test
  public void testActivityStatisticsWithFailedJobs() {
    given().pathParam("id", "aDefinitionId").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeFailedJobs();
    inOrder.verify(activityQueryMock).list();
  }

  @Test
  public void testActivityStatisticsWithFailedJobsByKey() {
    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY).queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_BY_KEY_URL);

    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeFailedJobs();
    inOrder.verify(activityQueryMock).list();
  }

  @Test
  public void testActivityStatisticsWithIncidents() {
    given().pathParam("id", "aDefinitionId").queryParam("incidents", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeIncidents();
    inOrder.verify(activityQueryMock).list();
  }

  @Test
  public void testActivityStatisticsWithIncidentsByKey() {
    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY).queryParam("incidents", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_BY_KEY_URL);

    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeIncidents();
    inOrder.verify(activityQueryMock).list();
  }

  @Test
  public void testActivityStatisticsIncidentsForTypeTypeOption() {
    given().pathParam("id", "aDefinitionId").queryParam("incidentsForType", "failedJob")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeIncidentsForType("failedJob");
    inOrder.verify(activityQueryMock).list();
  }

  @Test
  public void testActivityStatisticsIncidentsForTypeTypeOptionByKey() {
    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY).queryParam("incidentsForType", "failedJob")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_BY_KEY_URL);

    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeIncidentsForType("failedJob");
    inOrder.verify(activityQueryMock).list();
  }

  @Test
  public void testActivtyStatisticsIncidentsAndFailedJobsOption() {
    given().pathParam("id", "aDefinitionId")
    .queryParam("incidents", "true").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeFailedJobs();
    inOrder.verify(activityQueryMock).includeIncidents();
    inOrder.verify(activityQueryMock).list();
  }

  @Test
  public void testActivtyStatisticsIncidentsAndFailedJobsOptionByKey() {
    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .queryParam("incidents", "true").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_BY_KEY_URL);

    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeFailedJobs();
    inOrder.verify(activityQueryMock).includeIncidents();
    inOrder.verify(activityQueryMock).list();
  }

  @Test
  public void testActivtyStatisticsIncidentsForTypeAndFailedJobsOption() {
    given().pathParam("id", "aDefinitionId")
    .queryParam("incidentsForType", "failedJob").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeFailedJobs();
    inOrder.verify(activityQueryMock).includeIncidentsForType("failedJob");
    inOrder.verify(activityQueryMock).list();
  }

  @Test
  public void testActivtyStatisticsByIdThrowsAuthorizationException() {
    String message = "expected exception";
    when(activityQueryMock.list()).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", "aDefinitionId")
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testActivtyStatisticsIncidentsForTypeAndFailedJobsOptionByKey() {
    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .queryParam("incidentsForType", "failedJob").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_BY_KEY_URL);

    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeFailedJobs();
    inOrder.verify(activityQueryMock).includeIncidentsForType("failedJob");
    inOrder.verify(activityQueryMock).list();
  }

  @Test
  public void testActivtyStatisticsIncidentsAndIncidentsForType() {
    given().pathParam("id", "aDefinitionId")
    .queryParam("incidents", "true").queryParam("incidentsForType", "anIncidentTpye")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_URL);
  }

  @Test
  public void testActivtyStatisticsIncidentsAndIncidentsForTypeByKey() {
    given().pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .queryParam("incidents", "true").queryParam("incidentsForType", "anIncidentTpye")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_BY_KEY_URL);
  }

  @Test
  public void testActivtyStatisticsByIdThrowsAuthorizationExceptionByKey() {
    String message = "expected exception";
    when(activityQueryMock.list()).thenThrow(new AuthorizationException(message));

    given()
      .pathParam("key", MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(ACTIVITY_STATISTICS_BY_KEY_URL);
  }

  @Test
  public void testProcessDefinitionStatisticsWithRootIncidents() {
    given().queryParam("rootIncidents", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);

    InOrder inOrder = Mockito.inOrder(processDefinitionStatisticsQueryMock);;
    inOrder.verify(processDefinitionStatisticsQueryMock).includeRootIncidents();
    inOrder.verify(processDefinitionStatisticsQueryMock).list();
  }
}
