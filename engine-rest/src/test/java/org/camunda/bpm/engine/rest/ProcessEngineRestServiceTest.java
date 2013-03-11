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

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.spi.impl.MockedProcessEngineProvider;
import org.junit.Test;

public class ProcessEngineRestServiceTest extends AbstractRestServiceTest {

  private static final String ENGINES_URL = TEST_RESOURCE_ROOT_PATH + "/engine";
  private static final String SINGLE_ENGINE_URL = ENGINES_URL + "/{name}";
  private static final String PROCESS_DEFINITION_URL = SINGLE_ENGINE_URL + "/process-definition/{id}";
  
  String EXAMPLE_ENGINE_NAME = "anEngineName";
  
  private ProcessEngine namedProcessEngine;
  private RepositoryService mockRepoService;
  
  public void setup() throws IOException {
    setupTestScenario();
    
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();
    
    namedProcessEngine = getProcessEngine(EXAMPLE_ENGINE_NAME);
    mockRepoService = mock(RepositoryService.class);
    when(mockRepoService.getProcessDefinition(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(mockDefinition);
    when(namedProcessEngine.getRepositoryService()).thenReturn(mockRepoService);
  }
  
  @Test
  public void testEngineAccess() throws IOException {
    setup();
    
    given().pathParam("name", EXAMPLE_ENGINE_NAME)
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_URL);
    
    verify(namedProcessEngine).getRepositoryService();
    verify(mockRepoService).getProcessDefinition(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verifyZeroInteractions(processEngine);
  }
  
  @Test
  public void testNonExistingEngineAccess() throws IOException {
    setup();
    
    given().pathParam("name", MockedProcessEngineProvider.NON_EXISTING_PROCESS_ENGINE_NAME)
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(PROCESS_DEFINITION_URL);
  }
  
  @Test
  public void testEngineNamesList() throws IOException {
    setup();
    
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("[0].name", equalTo(MockedProcessEngineProvider.EXAMPLE_PROCESS_ENGINE_NAME))
      .body("[1].name", equalTo(MockedProcessEngineProvider.ANOTHER_EXAMPLE_PROCESS_ENGINE_NAME))
    .when().get(ENGINES_URL);
  }
}
