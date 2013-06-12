package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.EqualsList;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.ExampleVariableObject;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractProcessInstanceRestServiceInteractionTest extends
    AbstractRestServiceTest {

  protected static final String PROCESS_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance/{id}";
  protected static final String PROCESS_INSTANCE_VARIABLES_URL = PROCESS_INSTANCE_URL + "/variables";
  protected static final String SINGLE_PROCESS_INSTANCE_VARIABLE_URL = PROCESS_INSTANCE_VARIABLES_URL + "/{varId}";
  protected static final String PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL = PROCESS_INSTANCE_URL + "/activity-instances";
  
  protected static final Map<String, Object> EXAMPLE_OBJECT_VARIABLES = new HashMap<String, Object>();
  static {
    ExampleVariableObject variableValue = new ExampleVariableObject();
    variableValue.setProperty1("aPropertyValue");
    variableValue.setProperty2(true);
    
    EXAMPLE_OBJECT_VARIABLES.put(EXAMPLE_VARIABLE_KEY, variableValue);
  }
  
  private RuntimeServiceImpl runtimeServiceMock;
  
  @Before
  public void setUpRuntimeData() {
    runtimeServiceMock = mock(RuntimeServiceImpl.class);
    // variables
    when(runtimeServiceMock.getVariables(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(EXAMPLE_VARIABLES);
    when(runtimeServiceMock.getVariables(MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(EXAMPLE_OBJECT_VARIABLES);
    
    // activity instances
    when(runtimeServiceMock.getActivityInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(EXAMPLE_ACTIVITY_INSTANCE);
    
    // runtime service
    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
  }
  
  @Test
  public void testGetActivityInstanceTree() {
    Response response = given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
        .then().expect().statusCode(Status.OK.getStatusCode())
        .body("id", equalTo(EXAMPLE_ACTIVITY_INSTANCE_ID))
        .body("parentActivityInstanceId", equalTo(EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID))
        .body("activityId", equalTo(EXAMPLE_ACTIVITY_ID))
        .body("activityName", equalTo(EXAMPLE_ACTIVITY_NAME))
        .body("processInstanceId", equalTo(EXAMPLE_PROCESS_INSTANCE_ID))
        .body("processDefinitionId", equalTo(EXAMPLE_PROCESS_DEFINITION_ID))
        .body("businessKey", equalTo(EXAMPLE_BUSINESS_KEY))
        .body("executionIds", not(empty()))
        .body("executionIds[0]", equalTo(EXAMPLE_ACTIVITY_INSTANCE_ID))
        .body("childInstances", not(empty()))
        .body("childInstances[0].id", equalTo(CHILD_EXAMPLE_ACTIVITY_INSTANCE_ID))
        .body("childInstances[0].parentActivityInstanceId", equalTo(CHILD_EXAMPLE_PARENT_ACTIVITY_INSTANCE_ID))
        .body("childInstances[0].activityId", equalTo(CHILD_EXAMPLE_ACTIVITY_ID))
        .body("childInstances[0].activityName", equalTo(CHILD_EXAMPLE_ACTIVITY_NAME))
        .body("childInstances[0].processInstanceId", equalTo(CHILD_EXAMPLE_PROCESS_INSTANCE_ID))
        .body("childInstances[0].processDefinitionId", equalTo(CHILD_EXAMPLE_PROCESS_DEFINITION_ID))
        .body("childInstances[0].businessKey", equalTo(CHILD_EXAMPLE_BUSINESS_KEY))
        .body("childInstances[0].executionIds", not(empty()))
        .body("childInstances[0].childInstances", empty())
        .when().get(PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL);
    
    Assert.assertEquals("Should return exactly nine properties", 9, response.jsonPath().getMap("").size());
  }
  
  @Test
  public void testGetActivityInstanceTreeForNonExistingProcessInstance() {
    when(runtimeServiceMock.getActivityInstance(anyString())).thenReturn(null);
    
    given().pathParam("id", "aNonExistingProcessInstanceId")
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Process instance with id aNonExistingProcessInstanceId does not exist"))
      .when().get(PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL);
  }
  
  @Test
  public void testGetActivityInstanceTreeWithInternalError() {
    when(runtimeServiceMock.getActivityInstance(anyString())).thenThrow(new ProcessEngineException("expected exception"));
    
    given().pathParam("id", "aNonExistingProcessInstanceId")
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
      .when().get(PROCESS_INSTANCE_ACTIVIY_INSTANCES_URL);
  }
  
  @Test
  public void testGetVariables() {
    Response response = given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value", equalTo(EXAMPLE_VARIABLE_VALUE))
      .body(EXAMPLE_VARIABLE_KEY + ".type", equalTo(String.class.getSimpleName()))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);
    
    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());
  }

  @Test
  public void testJavaObjectVariableSerialization() {
    Response response = given().pathParam("id", MockProvider.ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value.property1", equalTo("aPropertyValue"))
      .body(EXAMPLE_VARIABLE_KEY + ".value.property2", equalTo(true))
      .body(EXAMPLE_VARIABLE_KEY + ".type", equalTo(ExampleVariableObject.class.getSimpleName()))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);
    
    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());
  }

  @Test
  public void testGetVariablesForNonExistingProcessInstance() {
    when(runtimeServiceMock.getVariables(anyString())).thenThrow(new ProcessEngineException("expected exception"));
    
    given().pathParam("id", "aNonExistingProcessInstanceId")
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
      .when().get(PROCESS_INSTANCE_VARIABLES_URL);
  }

  @Test
  public void testGetSingleInstance() {
    ProcessInstance mockInstance = MockProvider.createMockInstance();
    ProcessInstanceQuery sampleInstanceQuery = mock(ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.processInstanceId(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.singleResult()).thenReturn(mockInstance);
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("ended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_ENDED))
      .body("definitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("businessKey", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY))
      .body("suspended", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED))
      .when().get(PROCESS_INSTANCE_URL);
  }

  @Test
  public void testGetNonExistingProcessInstance() {
    ProcessInstanceQuery sampleInstanceQuery = mock(ProcessInstanceQuery.class);
    when(runtimeServiceMock.createProcessInstanceQuery()).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.processInstanceId(anyString())).thenReturn(sampleInstanceQuery);
    when(sampleInstanceQuery.singleResult()).thenReturn(null);
    
    given().pathParam("id", "aNonExistingInstanceId")
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Process instance with id aNonExistingInstanceId does not exist"))
      .when().get(PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testDeleteProcessInstance() {
    String deleteReason = "some delete reason";
    Map<String, String> messageBodyJson = new HashMap<String, String>();
    messageBodyJson.put("deleteReason", deleteReason);
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(PROCESS_INSTANCE_URL);
    
    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, deleteReason);
  }
  
  @Test
  public void testDeleteNonExistingProcessInstance() {
    doThrow(new ProcessEngineException("expected exception")).when(runtimeServiceMock).deleteProcessInstance(anyString(), anyString());
    
    String deleteReason = "some delete reason";
    Map<String, String> messageBodyJson = new HashMap<String, String>();
    messageBodyJson.put("deleteReason", deleteReason);
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Process instance with id " + MockProvider.EXAMPLE_PROCESS_INSTANCE_ID + " does not exist"))
      .when().delete(PROCESS_INSTANCE_URL);
  }
  
  @Test
  public void testNoGivenDeleteReason() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(PROCESS_INSTANCE_URL);
    
    verify(runtimeServiceMock).deleteProcessInstance(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, null);
  }
  
  @Test
  public void testVariableModification() {
    String variableKey = "aKey";
    int variableValue = 123;
    
    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    
    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);
    
    List<String> deletions = new ArrayList<String>();
    deletions.add("deleteKey");
    messageBodyJson.put("deletions", deletions);
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
    
    Map<String, Object> expectedModifications = new HashMap<String, Object>();
    expectedModifications.put(variableKey, variableValue);
    verify(runtimeServiceMock).updateVariables(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), argThat(new EqualsMap(expectedModifications)), 
        argThat(new EqualsList(deletions)));
  }
  
  @Test
  public void testVariableModificationForNonExistingProcessInstance() {
    doThrow(new ProcessEngineException("expected exception")).when(runtimeServiceMock).updateVariables(anyString(), any(Map.class), any(List.class));
    
    String variableKey = "aKey";
    int variableValue = 123;
    
    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    
    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    
    messageBodyJson.put("modifications", modifications);
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for process instance " + MockProvider.EXAMPLE_PROCESS_INSTANCE_ID + ": expected exception"))
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }
  
  @Test
  public void testEmptyVariableModification() {
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(PROCESS_INSTANCE_VARIABLES_URL);
  }
  
  @Test
  public void testGetSingleVariable() {
    String variableKey = "aVariableKey";
    int variableValue = 123;
    
    when(runtimeServiceMock.getVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey))).thenReturn(variableValue);
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", is(123))
      .body("type", is("Integer"))
      .when().get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }
  
  @Test
  public void testNonExistingVariable() {
    String variableKey = "aVariableKey";
    
    when(runtimeServiceMock.getVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey))).thenReturn(null);
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", is("process instance variable with name " + variableKey + " does not exist or is null"))
      .when().get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }
  
  @Test
  public void testGetVariableForNonExistingInstance() {
    String variableKey = "aVariableKey";
    
    when(runtimeServiceMock.getVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey)))
      .thenThrow(new ProcessEngineException("expected exception"));
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot get process instance variable " + variableKey + ": expected exception"))
      .when().get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }
  
  @Test
  public void testPutSingleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";
    
    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
    
    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), 
        eq(variableValue));
  }
  
  @Test
  public void testPutSingleVariableWithNoValue() {
    String variableKey = "aVariableKey";
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
    
    verify(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), 
        isNull());
  }
  
  @Test
  public void testPutVariableForNonExistingInstance() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";
    
    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);
    
    doThrow(new ProcessEngineException("expected exception"))
      .when(runtimeServiceMock).setVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey), eq(variableValue));
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot put process instance variable " + variableKey + ": expected exception"))
      .when().put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }
  
  @Test
  public void testDeleteSingleVariable() {
    String variableKey = "aVariableKey";
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
    
    verify(runtimeServiceMock).removeVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey));
  }
  
  @Test
  public void testDeleteVariableForNonExistingInstance() {
    String variableKey = "aVariableKey";
    
    doThrow(new ProcessEngineException("expected exception"))
      .when(runtimeServiceMock).removeVariable(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID), eq(variableKey));
    
    given().pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot delete process instance variable " + variableKey + ": expected exception"))
      .when().delete(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }
}
