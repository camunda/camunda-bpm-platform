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
package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.rest.helper.MockProvider;
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
  
  private ProcessDefinitionStatisticsQuery processDefinitionQueryMock;
  private ActivityStatisticsQuery activityQueryMock;
  
  private void setUp() throws IOException {
    setupTestScenario();
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
  public void testStatisticsRetrievalPerProcessDefinitionVersion() throws IOException {
    setUp();
    
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("definition.size()", is(2))
      .body("definition.id", hasItems(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, MockProvider.ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID))
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
    setUp();
    
    given().pathParam("id", "aDefinitionId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("id", hasItems(MockProvider.EXAMPLE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_ACTIVITY_ID))
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
