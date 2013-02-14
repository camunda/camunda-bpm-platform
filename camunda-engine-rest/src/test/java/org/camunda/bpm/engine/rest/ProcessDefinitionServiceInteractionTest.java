package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.junit.Test;
import org.mockito.Matchers;

public class ProcessDefinitionServiceInteractionTest extends
    AbstractRestServiceTest {

  private static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcessDefinitionId";
  private static final String EXAMPLE_INSTANCE_ID = "anId";
  
  private static final String PROCESS_DEFINITION_URL = TEST_RESOURCE_ROOT_PATH + "/process-definition";
  private static final String START_PROCESS_INSTANCE_URL = PROCESS_DEFINITION_URL + "/{id}/start";
  
  private ProcessInstance mockInstance;
  private RuntimeService runtimeServiceMock;
  
  public void setupMockInstance() throws IOException {
    setupTestScenario();
    mockInstance = createMockInstance();
    
    // we replace this mock with every test in order to have a clean one (in terms of invocations) for verification
    runtimeServiceMock = mock(RuntimeService.class);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
    when(runtimeServiceMock.startProcessInstanceById(eq(EXAMPLE_PROCESS_DEFINITION_ID), Matchers.<Map<String, Object>>any())).thenReturn(mockInstance);
  }
  
  private ProcessInstance createMockInstance() {
    ProcessInstance mock = mock(ProcessInstance.class);
    
    when(mock.getId()).thenReturn(EXAMPLE_INSTANCE_ID);
    
    return mock;
  }
  
  @Test
  public void testSimpleProcessInstantiation() throws IOException {
    setupMockInstance();
    
    given().pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(EXAMPLE_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testProcessInstantiationWithParameters() throws IOException {
    setupMockInstance();
    
    Map<String, Object> parameters = getInstanceVariablesParameters();
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", parameters);
    
    given().pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(EXAMPLE_INSTANCE_ID))
      .when().post(START_PROCESS_INSTANCE_URL);
    
    verify(runtimeServiceMock).startProcessInstanceById(eq(EXAMPLE_PROCESS_DEFINITION_ID), argThat(new EqualsMap(parameters)));
    
  }
  
  private Map<String, Object> getInstanceVariablesParameters() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aBoolean", Boolean.TRUE);
    variables.put("aString", "aStringVariableValue");
    variables.put("anInteger", 42);
    
    return variables;
  }
  
  /**
   * {@link RuntimeService#startProcessInstanceById(String, Map)} throws an {@link ActivitiException}, if a definition with the given id does not exist.
   */
  @Test
  public void testUnsuccessfulInstantiation() throws IOException {
    setupMockInstance();
    
    when(runtimeServiceMock.startProcessInstanceById(eq(EXAMPLE_PROCESS_DEFINITION_ID), Matchers.<Map<String, Object>>any()))
      .thenThrow(new ActivitiException("expected exception"));
    
    given().pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .when().post(START_PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testInstanceResourceLinkResult() throws IOException {
    setupMockInstance();
    
    String fullInstanceUrl = "http://localhost:" + PORT + TEST_RESOURCE_ROOT_PATH + "/process-instance/" + EXAMPLE_INSTANCE_ID;
    
    given().pathParam("id", EXAMPLE_PROCESS_DEFINITION_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("links[0].href", equalTo(fullInstanceUrl))
      .when().post(START_PROCESS_INSTANCE_URL);
    
  }
  
}
