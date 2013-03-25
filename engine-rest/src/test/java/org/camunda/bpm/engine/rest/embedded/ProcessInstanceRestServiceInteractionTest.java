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
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.AbstractProcessInstanceRestServiceInteractionTest;
import org.camunda.bpm.engine.rest.helper.ExampleDataProvider;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.ResteasyServerBootstrap;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProcessInstanceRestServiceInteractionTest extends AbstractProcessInstanceRestServiceInteractionTest {
  
  private static ResteasyServerBootstrap resteasyBootstrap;
  
  private RuntimeService runtimeServiceMock;

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
    runtimeServiceMock = mock(RuntimeService.class);
    when(runtimeServiceMock.getVariables(ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(EXAMPLE_VARIABLES);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
  }
  
  @Test
  public void testGetSingleInstance() {
    ProcessInstance mockInstance = MockProvider.createMockInstance();
    ProcessInstanceQuery sampleInstanceQuery = mock(ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.processInstanceId(ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.singleResult()).thenReturn(mockInstance);
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("ended", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
      .body("definitionId", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("businessKey", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
      .body("suspended", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
      .when().get(PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testGetNonExistingProcessInstance() {
    ProcessInstanceQuery sampleInstanceQuery = mock(ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.processInstanceId(anyString())).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.singleResult()).thenReturn(null);
    
    given().pathParam("id", "aNonExistingInstanceId")
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .when().get(PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testGetVariablesForNonExistingProcessInstance() {
    when(runtimeServiceMock.getVariables(anyString())).thenThrow(new ProcessEngineException("expected exception"));
    
    given().pathParam("id", "aNonExistingProcessInstanceId")
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);
  }

}
