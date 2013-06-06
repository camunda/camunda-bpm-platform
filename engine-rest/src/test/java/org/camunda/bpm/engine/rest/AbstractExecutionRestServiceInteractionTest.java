package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public abstract class AbstractExecutionRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String EXECUTION_URL = TEST_RESOURCE_ROOT_PATH + "/execution/{id}";
  protected static final String SIGNAL_EXECUTION_URL = EXECUTION_URL + "/signal";
  protected static final String EXECUTION_LOCAL_VARIABLES_URL = EXECUTION_URL + "/localVariables";
  protected static final String SINGLE_EXECUTION_LOCAL_VARIABLE_URL = EXECUTION_LOCAL_VARIABLES_URL + "/{varId}";
  protected static final String MESSAGE_SUBSCRIPTION_URL = EXECUTION_URL + "/messageSubscriptions/{messageName}/trigger";
  
  private RuntimeServiceImpl runtimeServiceMock;
  
  @Before
  public void setUpRuntimeData() {
    runtimeServiceMock = mock(RuntimeServiceImpl.class);
    when(runtimeServiceMock.getVariablesLocal(MockProvider.EXAMPLE_EXECUTION_ID)).thenReturn(EXAMPLE_VARIABLES);

    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
  }
  
  @Test
  public void testGetSingleExecution() {
    Execution mockExecution = MockProvider.createMockExecution();
    ExecutionQuery sampleExecutionQuery = mock(ExecutionQuery.class);
    when(runtimeServiceMock.createExecutionQuery()).thenReturn(sampleExecutionQuery);
    when(sampleExecutionQuery.executionId(MockProvider.EXAMPLE_EXECUTION_ID)).thenReturn(sampleExecutionQuery);
    when(sampleExecutionQuery.singleResult()).thenReturn(mockExecution);
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_EXECUTION_ID))
      .body("ended", equalTo(MockProvider.EXAMPLE_EXECUTION_IS_ENDED))
      .body("processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .when().get(EXECUTION_URL);
  }
  
  @Test
  public void testGetNonExistingExecution() {
    ExecutionQuery sampleExecutionQuery = mock(ExecutionQuery.class);
    when(runtimeServiceMock.createExecutionQuery()).thenReturn(sampleExecutionQuery);
    when(sampleExecutionQuery.executionId(anyString())).thenReturn(sampleExecutionQuery);
    when(sampleExecutionQuery.singleResult()).thenReturn(null);
    
    String nonExistingExecutionId = "aNonExistingInstanceId";
    
    given().pathParam("id", nonExistingExecutionId)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Execution with id " + nonExistingExecutionId + " does not exist"))
      .when().get(EXECUTION_URL);
  }
  
  @Test
  public void testSignalExecution() {
    String variableKey = "aKey";
    int variableValue = 123;
    
    Map<String, Object> variablesJson = new HashMap<String, Object>();
    Map<String, Object> variables = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    variablesJson.put("variables", variables);
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(variablesJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(SIGNAL_EXECUTION_URL);
    
    Map<String, Object> expectedSignalVariables = new HashMap<String, Object>();
    expectedSignalVariables.put(variableKey, variableValue);
    
    verify(runtimeServiceMock).signal(eq(MockProvider.EXAMPLE_EXECUTION_ID), argThat(new EqualsMap(expectedSignalVariables)));
  }
  
  @Test
  public void testSignalNonExistingExecution() {
    doThrow(new ProcessEngineException("expected exception")).when(runtimeServiceMock).signal(anyString(), any(Map.class));
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot signal execution " + MockProvider.EXAMPLE_EXECUTION_ID + ": expected exception"))
      .when().post(SIGNAL_EXECUTION_URL);
  }
  

  @Test
  public void testGetLocalVariables() {
    Response response = given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value", equalTo(EXAMPLE_VARIABLE_VALUE))
      .body(EXAMPLE_VARIABLE_KEY + ".type", equalTo(String.class.getSimpleName()))
      .when().get(EXECUTION_LOCAL_VARIABLES_URL);
    
    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());
  }
  
  @Test
  public void testGetLocalVariablesForNonExistingExecution() {
    when(runtimeServiceMock.getVariablesLocal(anyString())).thenThrow(new ProcessEngineException("expected exception"));
    
    given().pathParam("id", "aNonExistingExecutionId")
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
      .when().get(EXECUTION_LOCAL_VARIABLES_URL);
  }
  
  @Test
  public void testLocalVariableModification() {
    Map<String, Object> messageBodyJson = new HashMap<String, Object>();
    
    String variableKey = "aKey";
    int variableValue = 123;
    
    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);
    
    List<String> deletions = new ArrayList<String>();
    deletions.add("deleteKey");
    messageBodyJson.put("deletions", deletions);
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(EXECUTION_LOCAL_VARIABLES_URL);
    
    Map<String, Object> expectedModifications = new HashMap<String, Object>();
    expectedModifications.put(variableKey, variableValue);
    verify(runtimeServiceMock).updateVariablesLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), argThat(new EqualsMap(expectedModifications)), 
        argThat(new EqualsList(deletions)));
  }
  
  @Test
  public void testLocalVariableModificationForNonExistingExecution() {
    doThrow(new ProcessEngineException("expected exception")).when(runtimeServiceMock).updateVariablesLocal(anyString(), any(Map.class), any(List.class));
    
    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    String variableKey = "aKey";
    int variableValue = 123;
    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for execution " + MockProvider.EXAMPLE_EXECUTION_ID + ": expected exception"))
      .when().post(EXECUTION_LOCAL_VARIABLES_URL);
  }
  
  @Test
  public void testEmptyLocalVariableModification() {
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(EXECUTION_LOCAL_VARIABLES_URL);
  }
  
  @Test
  public void testGetSingleLocalVariable() {
    String variableKey = "aVariableKey";
    int variableValue = 123;
    
    when(runtimeServiceMock.getVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey))).thenReturn(variableValue);
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", is(123))
      .body("type", is("Integer"))
      .when().get(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }
  
  @Test
  public void testNonExistingLocalVariable() {
    String variableKey = "aVariableKey";
    
    when(runtimeServiceMock.getVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey))).thenReturn(null);
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", is("execution variable with name " + variableKey + " does not exist or is null"))
      .when().get(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }
  
  @Test
  public void testGetLocalVariableForNonExistingExecution() {
    String variableKey = "aVariableKey";
    
    when(runtimeServiceMock.getVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey)))
      .thenThrow(new ProcessEngineException("expected exception"));
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot get execution variable " + variableKey + ": expected exception"))
      .when().get(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }
  
  @Test
  public void testPutSingleLocalVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";
    
    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
    
    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), 
        eq(variableValue));
  }
  
  @Test
  public void testPutSingleLocalVariableWithNoValue() {
    String variableKey = "aVariableKey";
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
    
    verify(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), 
        isNull());
  }
  
  @Test
  public void testPutLocalVariableForNonExistingExecution() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";
    
    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);
    
    doThrow(new ProcessEngineException("expected exception"))
      .when(runtimeServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey), eq(variableValue));
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot put execution variable " + variableKey + ": expected exception"))
      .when().put(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }
  
  @Test
  public void testDeleteSingleLocalVariable() {
    String variableKey = "aVariableKey";
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
    
    verify(runtimeServiceMock).removeVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey));
  }
  
  @Test
  public void testDeleteLocalVariableForNonExistingExecution() {
    String variableKey = "aVariableKey";
    
    doThrow(new ProcessEngineException("expected exception"))
      .when(runtimeServiceMock).removeVariableLocal(eq(MockProvider.EXAMPLE_EXECUTION_ID), eq(variableKey));
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot delete execution variable " + variableKey + ": expected exception"))
      .when().delete(SINGLE_EXECUTION_LOCAL_VARIABLE_URL);
  }
  
  @Test
  public void testMessageEventTriggering() {
    String messageName = "aMessageName";
    String variableKey1 = "aVarName";
    String variableValue1 = "aVarValue";
    String variableKey2 = "anotherVarName";
    String variableValue2 = "anotherVarValue";
    
    Map<String, Object> variables = VariablesBuilder.create()
        .variable(variableKey1, variableValue1)
        .variable(variableKey2, variableValue2).getVariables();
    
    Map<String, Object> variablesJson = new HashMap<String, Object>();
    variablesJson.put("variables", variables);
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("messageName", messageName)
      .contentType(ContentType.JSON).body(variablesJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_SUBSCRIPTION_URL);
  
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put(variableKey1, variableValue1);
    expectedVariables.put(variableKey2, variableValue2);
    
    verify(runtimeServiceMock).messageEventReceived(eq(messageName), eq(MockProvider.EXAMPLE_EXECUTION_ID),
        argThat(new EqualsMap(expectedVariables)));
  }
  
  @Test
  public void testMessageEventTriggeringWithoutVariables() {
    String messageName = "aMessageName";
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("messageName", messageName)
      .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_SUBSCRIPTION_URL);
  
    verify(runtimeServiceMock).messageEventReceived(eq(messageName), eq(MockProvider.EXAMPLE_EXECUTION_ID),
        argThat(new EqualsMap(null)));
  }
  
  @Test
  public void testFailingMessageEventTriggering() {
    String messageName = "someMessage";
    doThrow(new ProcessEngineException("expected exception"))
      .when(runtimeServiceMock).messageEventReceived(anyString(), anyString(), any(Map.class));
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("messageName", messageName)
      .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot trigger message " + messageName + " for execution " + MockProvider.EXAMPLE_EXECUTION_ID + ": expected exception"))
      .when().post(MESSAGE_SUBSCRIPTION_URL);
  }
}
