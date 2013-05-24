package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
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
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

public abstract class AbstractExecutionRestServiceInteractionTest extends AbstractRestServiceTest {

  protected static final String EXECUTION_URL = TEST_RESOURCE_ROOT_PATH + "/execution/{id}";
  protected static final String SIGNAL_EXECUTION_URL = EXECUTION_URL + "/signal";
  
  private RuntimeServiceImpl runtimeServiceMock;
  
  @Before
  public void setUpRuntimeData() {
    runtimeServiceMock = mock(RuntimeServiceImpl.class);
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
    Map<String, Object> variablesJson = new HashMap<String, Object>();
    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    Map<String, Object> variable = new HashMap<String, Object>();
    variable.put("name", "aKey");
    variable.put("value", 123);
    variable.put("type", "Integer");
    variables.add(variable);
    
    variablesJson.put("variables", variables);
    
    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).contentType(ContentType.JSON).body(variablesJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(SIGNAL_EXECUTION_URL);
    
    Map<String, Object> expectedSignalVariables = new HashMap<String, Object>();
    expectedSignalVariables.put("aKey", 123);
    
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
  
}
