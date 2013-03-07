package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * This test only tests the REST interface, but no interaction with the engine as it assumes a stubbed 
 * implementation.
 * @author Thorben Lindhauer
 *
 */
public class StatisticsRestTest extends AbstractRestServiceTest {

  private static final String PROCESS_DEFINITION_STATISTICS_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/statistics";
  private static final String ACTIVITY_STATISTICS_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition/{id}/statistics";
  
  private static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcessDefinitionId";
  private static final String EXAMPLE_PROCESS_DEFINITION_NAME = "aName";
  private static final String EXAMPLE_PROCESS_DEFINITION_KEY = "aKey";
  private static final int EXAMPLE_FAILED_JOBS = 42;
  private static final int EXAMPLE_INSTANCES = 123;
  
  private static final String ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID = "aProcessDefinitionId:2";
  private static final int ANOTHER_EXAMPLE_FAILED_JOBS = 43;
  private static final int ANOTHER_EXAMPLE_INSTANCES = 124;
  
  private static final String EXAMPLE_ACTIVITY_ID = "anActivity";
  private static final String ANOTHER_EXAMPLE_ACTIVITY_ID = "anotherActivity";
  
  private ProcessDefinitionStatisticsQuery processDefinitionQueryMock;
  private ActivityStatisticsQuery activityQueryMock;
  
  private void setUp() throws IOException {
    setupTestScenario();
    setupProcessDefinitionStatisticsMock();
    setupActivityStatisticsMock();
  }
  
  private void setupActivityStatisticsMock() {
    ActivityStatistics statistics = mock(ActivityStatistics.class);
    when(statistics.getFailedJobs()).thenReturn(EXAMPLE_FAILED_JOBS);
    when(statistics.getInstances()).thenReturn(EXAMPLE_INSTANCES);
    when(statistics.getId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    
    ActivityStatistics anotherStatistics = mock(ActivityStatistics.class);
    when(anotherStatistics.getFailedJobs()).thenReturn(ANOTHER_EXAMPLE_FAILED_JOBS);
    when(anotherStatistics.getInstances()).thenReturn(ANOTHER_EXAMPLE_INSTANCES);
    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);
    
    List<ActivityStatistics> activityResults = new ArrayList<ActivityStatistics>();
    activityResults.add(statistics);
    activityResults.add(anotherStatistics);
    
    activityQueryMock = mock(ActivityStatisticsQuery.class);
    when(activityQueryMock.list()).thenReturn(activityResults);
    when(processEngine.getManagementService().createActivityStatisticsQuery(any(String.class))).thenReturn(activityQueryMock);
  }
  
  private void setupProcessDefinitionStatisticsMock() {
    ProcessDefinitionStatistics statistics = mock(ProcessDefinitionStatistics.class);
    when(statistics.getFailedJobs()).thenReturn(EXAMPLE_FAILED_JOBS);
    when(statistics.getInstances()).thenReturn(EXAMPLE_INSTANCES);
    when(statistics.getId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(statistics.getName()).thenReturn(EXAMPLE_PROCESS_DEFINITION_NAME);
    when(statistics.getKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);
    
    ProcessDefinitionStatistics anotherStatistics = mock(ProcessDefinitionStatistics.class);
    when(anotherStatistics.getFailedJobs()).thenReturn(ANOTHER_EXAMPLE_FAILED_JOBS);
    when(anotherStatistics.getInstances()).thenReturn(ANOTHER_EXAMPLE_INSTANCES);
    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);
    when(anotherStatistics.getName()).thenReturn(EXAMPLE_PROCESS_DEFINITION_NAME);
    when(anotherStatistics.getKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);
    
    List<ProcessDefinitionStatistics> processDefinitionResults = new ArrayList<ProcessDefinitionStatistics>();
    processDefinitionResults.add(statistics);
    processDefinitionResults.add(anotherStatistics);
    
    processDefinitionQueryMock = mock(ProcessDefinitionStatisticsQuery.class);
    when(processDefinitionQueryMock.list()).thenReturn(processDefinitionResults);
    when(processEngine.getManagementService().createProcessDefinitionStatisticsQuery()).thenReturn(processDefinitionQueryMock);
  }
  
  @Test
  public void testStatisticsRetrievalPerProcessDefinitionVersion() throws IOException {
    setUp();
    
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("definition.size()", is(2))
      .body("definition.id", hasItems(EXAMPLE_PROCESS_DEFINITION_ID, ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID))
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
  }
  
  @Test
  public void testAdditionalFailedJobsOption() throws IOException {
    setUp();
    
    given().queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_STATISTICS_URL);
    
    InOrder inOrder = Mockito.inOrder(processDefinitionQueryMock);
    inOrder.verify(processDefinitionQueryMock).includeFailedJobs();
    inOrder.verify(processDefinitionQueryMock).list();
  }
  
  @Test
  public void testActivityStatisticsRetrieval() throws IOException {
    setupTestScenario();
    
    given().pathParam("id", "aDefinitionId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("id", hasItems(EXAMPLE_ACTIVITY_ID, ANOTHER_EXAMPLE_ACTIVITY_ID))
    .when().get(ACTIVITY_STATISTICS_URL);
  }
  
  @Test
  public void testActivityStatisticsWithFailedJobs() throws IOException {
    setUp();
    
    given().pathParam("id", "aDefinitionId").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_URL);
    
    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeFailedJobs();
    inOrder.verify(activityQueryMock).list();
  }
}
