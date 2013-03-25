package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.helper.ExampleDataProvider;
import org.camunda.bpm.engine.rest.helper.ExampleVariableObject;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractProcessInstanceRestServiceInteractionTest extends
    AbstractRestServiceTest {

  protected static final String PROCESS_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance/{id}";
  protected static final String PROCESS_INSTANCE_VARIABLES_URL = PROCESS_INSTANCE_URL + "/variables";
  
  protected static final String EXAMPLE_VARIABLE_KEY = "aProcessVariableKey";
  protected static final String EXAMPLE_VARIABLE_VALUE = "aProcessVariableValue";
  
  protected static final Map<String, Object> EXAMPLE_VARIABLES = new HashMap<String, Object>();
  static {
    EXAMPLE_VARIABLES.put(EXAMPLE_VARIABLE_KEY, EXAMPLE_VARIABLE_VALUE);
  }
  
  /**
   * Override this in specific test cases to create runtime data that is asserted.
   */
  @Before
  public abstract void setUpRuntimeData();
  
  @Test
  public void testGetVariables() {
    given().pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("variables.size()", is(1))
      .body("variables[0].name", equalTo(EXAMPLE_VARIABLE_KEY))
      .body("variables[0].value", equalTo(EXAMPLE_VARIABLE_VALUE))
      .body("variables[0].type", equalTo(String.class.getSimpleName()))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testJavaObjectVariableSerialization() {
    ExampleVariableObject variableValue = new ExampleVariableObject();
    variableValue.setProperty1("aPropertyValue");
    variableValue.setProperty2(true);
    
    EXAMPLE_VARIABLES.put(EXAMPLE_VARIABLE_KEY, variableValue);
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("variables.size()", is(1))
      .body("variables[0].name", equalTo(EXAMPLE_VARIABLE_KEY))
      .body("variables[0].value.property1", equalTo("aPropertyValue"))
      .body("variables[0].value.property2", equalTo(true))
      .body("variables[0].type", equalTo(ExampleVariableObject.class.getSimpleName()))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);
  }
}
