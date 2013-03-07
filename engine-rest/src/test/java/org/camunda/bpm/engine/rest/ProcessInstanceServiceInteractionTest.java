package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.helper.ExampleVariableObject;
import org.junit.Test;

import com.jayway.restassured.response.Response;

public class ProcessInstanceServiceInteractionTest extends AbstractRestServiceTest {
  
  private static final String PROCESS_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance/{id}";
  private static final String PROCESS_INSTANCE_VARIABLES_URL = PROCESS_INSTANCE_URL + "/variables";
  
  private static final String EXAMPLE_PROCESS_INSTANCE_ID = "aProcessInstanceId";
  private static final String EXAMPLE_VARIABLE_KEY = "aProcessVariableKey";
  private static final String EXAMPLE_VARIABLE_VALUE = "aProcessVariableValue";
  
  private static final Map<String, Object> EXAMPLE_VARIABLES = new HashMap<String, Object>();
  static {
    EXAMPLE_VARIABLES.put(EXAMPLE_VARIABLE_KEY, EXAMPLE_VARIABLE_VALUE);
  }

  private RuntimeService runtimeServiceMock;
  
  private void setupMocks() throws IOException {
    setupTestScenario();
    
    runtimeServiceMock = mock(RuntimeService.class);
    when(runtimeServiceMock.getVariables(anyString())).thenReturn(EXAMPLE_VARIABLES);
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
  }
  
  @Test
  public void testGetVariables() throws IOException {
    setupMocks();
    
    given().pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("variables.size()", is(1))
      .body("variables[0].name", equalTo(EXAMPLE_VARIABLE_KEY))
      .body("variables[0].value", equalTo(EXAMPLE_VARIABLE_VALUE))
      .body("variables[0].type", equalTo(String.class.getSimpleName()))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);
  }
  
  @Test
  public void testGetVariablesForNonExistingProcessInstance() throws IOException {
    setupMocks();
    
    when(runtimeServiceMock.getVariables(anyString())).thenThrow(new ProcessEngineException("expected exception"));
    
    given().pathParam("id", "aNonExistingProcessInstanceId")
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);
  }
  
  @Test
  public void testJavaObjectVariableSerialization() throws IOException {
    ExampleVariableObject variableValue = new ExampleVariableObject();
    variableValue.setProperty1("aPropertyValue");
    variableValue.setProperty2(true);
    
    EXAMPLE_VARIABLES.put(EXAMPLE_VARIABLE_KEY, variableValue);
    
    setupMocks();
    
    given().pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("variables.size()", is(1))
      .body("variables[0].name", equalTo(EXAMPLE_VARIABLE_KEY))
      .body("variables[0].value.property1", equalTo("aPropertyValue"))
      .body("variables[0].value.property2", equalTo(true))
      .body("variables[0].type", equalTo(ExampleVariableObject.class.getSimpleName()))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);
  }

}
