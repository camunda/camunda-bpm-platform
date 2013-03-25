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
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.AbstractProcessDefinitionRestServiceInteractionTest;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.ExampleDataProvider;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.ResteasyServerBootstrap;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

public class ProcessDefinitionRestServiceInteractionTest extends
  AbstractProcessDefinitionRestServiceInteractionTest {

  private static ResteasyServerBootstrap resteasyBootstrap;  
  
  private RuntimeService runtimeServiceMock;
  private RepositoryService repositoryServiceMock;
  private FormService formServiceMock;
  
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
    ProcessInstance mockInstance = MockProvider.createMockInstance();
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();
    
    // we replace this mock with every test in order to have a clean one (in terms of invocations) for verification
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
    when(runtimeServiceMock.startProcessInstanceById(eq(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID), Matchers.<Map<String, Object>>any())).thenReturn(mockInstance);
    
    repositoryServiceMock = mock(RepositoryService.class);
    when(processEngine.getRepositoryService()).thenReturn(repositoryServiceMock);
    when(repositoryServiceMock.getProcessDefinition(eq(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(mockDefinition);
    when(repositoryServiceMock.getProcessModel(eq(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(createMockProcessDefinionBpmn20Xml());
    
    StartFormData formDataMock = MockProvider.createMockStartFormData(mockDefinition);
    formServiceMock = mock(FormService.class);
    when(processEngine.getFormService()).thenReturn(formServiceMock);
    when(formServiceMock.getStartFormData(eq(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(formDataMock);
  }
  
  private InputStream createMockProcessDefinionBpmn20Xml() {
    // do not close the input stream, will be done in implementation
    InputStream bpmn20XmlIn = null;
    bpmn20XmlIn = ReflectUtil.getResourceAsStream("processes/fox-invoice_en_long_id.bpmn");
    Assert.assertNotNull(bpmn20XmlIn);
    return bpmn20XmlIn;
  }
  
  @Test
  public void testProcessInstantiationWithParameters() throws IOException {
    Map<String, Object> parameters = getInstanceVariablesParameters();
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", parameters);
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);
    
    verify(runtimeServiceMock).startProcessInstanceById(eq(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID), argThat(new EqualsMap(parameters)));
    
  }
  
  private Map<String, Object> getInstanceVariablesParameters() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aBoolean", Boolean.TRUE);
    variables.put("aString", "aStringVariableValue");
    variables.put("anInteger", 42);
    
    return variables;
  }
  
  /**
   * {@link RuntimeService#startProcessInstanceById(String, Map)} throws an {@link ProcessEngineException}, if a definition with the given id does not exist.
   */
  @Test
  public void testUnsuccessfulInstantiation() {
    when(runtimeServiceMock.startProcessInstanceById(eq(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID), Matchers.<Map<String, Object>>any()))
      .thenThrow(new ProcessEngineException("expected exception"));
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .when().post(START_PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testDefinitionRetrieval() {
    given().pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("key", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_KEY))
      .body("category", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_CATEGORY))
      .body("name", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_NAME))
      .body("description", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_DESCRIPTION))
      .body("deploymentId", equalTo(ExampleDataProvider.EXAMPLE_DEPLOYMENT_ID))
      .body("version", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_VERSION))
      .body("resource", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME))
      .body("diagram", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME))
      .body("suspended", equalTo(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED))
    .when().get(SINGLE_PROCESS_DEFINITION_URL);
    
    verify(repositoryServiceMock).getProcessDefinition(ExampleDataProvider.EXAMPLE_PROCESS_DEFINITION_ID);
  }
  
  @Test
  public void testNonExistingProcessDefinitionRetrieval() {
    String nonExistingId = "aNonExistingDefinitionId";
    when(repositoryServiceMock.getProcessDefinition(eq(nonExistingId))).thenThrow(new ProcessEngineException("no matching definition"));
    
    given().pathParam("id", "aNonExistingDefinitionId")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(SINGLE_PROCESS_DEFINITION_URL);
  }
  
  
  @Test
  public void testNonExistingProcessDefinitionBpmn20XmlRetrieval() {
    String nonExistingId = "aNonExistingDefinitionId";
    when(repositoryServiceMock.getProcessModel(eq(nonExistingId))).thenThrow(new ProcessEngineException("no matching process definition found."));
    
    given().pathParam("id", nonExistingId)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(XML_DEFINITION_URL);
  }
  
  @Test
  public void testGetStartFormDataForNonExistingProcessDefinition() {
    when(formServiceMock.getStartFormData(anyString())).thenThrow(new ProcessEngineException("expected exception"));
    
    given().pathParam("id", "aNonExistingProcessDefinitionId")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(START_FORM_URL);
  }
}
