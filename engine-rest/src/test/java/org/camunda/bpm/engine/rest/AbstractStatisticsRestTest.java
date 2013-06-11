package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public abstract class AbstractStatisticsRestTest extends AbstractRestServiceTest {

  protected static final String PROCESS_DEFINITION_STATISTICS_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/statistics";
  protected static final String ACTIVITY_STATISTICS_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/{id}/statistics";
  private ProcessDefinitionStatisticsQuery processDefinitionQueryMock;
  private ActivityStatisticsQuery activityQueryMock;
  
  @Before
  public void setUpRuntimeData() {
    setupProcessDefinitionStatisticsMock();
    setupActivityStatisticsMock();
  }

  private void setupActivityStatisticsMock() {
    List<ActivityStatistics> mocks = MockProvider.createMockActivityStatistics();
    
    activityQueryMock = mock(ActivityStatisticsQuery.class);
    when(activityQueryMock.list()).thenReturn(mocks);
    when(processEngine.getManagementService().createActivityStatisticsQuery(any(String.class))).thenReturn(activityQueryMock);
  }

  private void setupProcessDefinitionStatisticsMock() {
    List<ProcessDefinitionStatistics> mocks = MockProvider.createMockProcessDefinitionStatistics();
    
    processDefinitionQueryMock = mock(ProcessDefinitionStatisticsQuery.class);
    when(processDefinitionQueryMock.list()).thenReturn(mocks);
    when(processEngine.getManagementService().createProcessDefinitionStatisticsQuery()).thenReturn(processDefinitionQueryMock);
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
  public void testActivityStatisticsRetrieval() {
    given().pathParam("id", "aDefinitionId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("id", hasItems(MockProvider.EXAMPLE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID))
    .when().get(ACTIVITY_STATISTICS_URL);
  }


  @Test
  public void testAdditionalFailedJobsOption() {
    given().queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
    
    InOrder inOrder = Mockito.inOrder(processDefinitionQueryMock);
    inOrder.verify(processDefinitionQueryMock).includeFailedJobs();
    inOrder.verify(processDefinitionQueryMock).list();
  }
  
  @Test
  public void testAdditionalIncidentsOption() {
    given().queryParam("incidents", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
    
    InOrder inOrder = Mockito.inOrder(processDefinitionQueryMock);
    inOrder.verify(processDefinitionQueryMock).includeIncidents();
    inOrder.verify(processDefinitionQueryMock).list();
  }
  
  @Test
  public void testAdditionalIncidentsForTypeOption() {
    given().queryParam("incidentsForType", "failedJob")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
    
    InOrder inOrder = Mockito.inOrder(processDefinitionQueryMock);
    inOrder.verify(processDefinitionQueryMock).includeIncidentsForType("failedJob");
    inOrder.verify(processDefinitionQueryMock).list();
  }
  
  @Test
  public void testAdditionalIncidentsAndFailedJobsOption() {
    given().queryParam("incidents", "true").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
    
    InOrder inOrder = Mockito.inOrder(processDefinitionQueryMock);
    inOrder.verify(processDefinitionQueryMock).includeFailedJobs();
    inOrder.verify(processDefinitionQueryMock).includeIncidents();
    inOrder.verify(processDefinitionQueryMock).list();
  }
  
  @Test
  public void testAdditionalIncidentsForTypeAndFailedJobsOption() {
    given().queryParam("incidentsForType", "failedJob").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
    
    InOrder inOrder = Mockito.inOrder(processDefinitionQueryMock);
    inOrder.verify(processDefinitionQueryMock).includeFailedJobs();
    inOrder.verify(processDefinitionQueryMock).includeIncidentsForType("failedJob");
    inOrder.verify(processDefinitionQueryMock).list();
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
  public void testActivtyStatisticsIncidentsAndIncidentsForType() {
    given().pathParam("id", "aDefinitionId")
    .queryParam("incidents", "true").queryParam("incidentsForType", "anIncidentTpye")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_URL);
  }

}
