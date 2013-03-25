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
package org.camunda.bpm.engine.rest.embedded;

import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.ActivityStatisticsQuery;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;
import org.camunda.bpm.engine.rest.AbstractStatisticsRestTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.ResteasyServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * This test only tests the REST interface, but no interaction with the engine as it assumes a stubbed 
 * implementation.
 * @author Thorben Lindhauer
 *
 */
public class StatisticsRestTest extends AbstractStatisticsRestTest {
  
  private static ResteasyServerBootstrap resteasyBootstrap;
  
  private ProcessDefinitionStatisticsQuery processDefinitionQueryMock;
  private ActivityStatisticsQuery activityQueryMock;
  
  @BeforeClass
  public static void setUpEmbeddedRuntime() {
    resteasyBootstrap = new ResteasyServerBootstrap();
    resteasyBootstrap.start();
  }
  
  @AfterClass
  public static void tearDownEmbeddedRuntime() {
    resteasyBootstrap.stop();
  }
  
  @Override
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
  public void testActivityStatisticsWithFailedJobs() {
    given().pathParam("id", "aDefinitionId").queryParam("failedJobs", "true")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(ACTIVITY_STATISTICS_URL);
    
    InOrder inOrder = Mockito.inOrder(activityQueryMock);
    inOrder.verify(activityQueryMock).includeFailedJobs();
    inOrder.verify(activityQueryMock).list();
  }
}
