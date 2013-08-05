package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.jayway.restassured.http.ContentType;

public abstract class AbstractTaskRestServiceInteractionTest extends
    AbstractRestServiceTest {

  protected static final String TASK_SERVICE_URL = TEST_RESOURCE_ROOT_PATH + "/task";
  protected static final String SINGLE_TASK_URL = TASK_SERVICE_URL + "/{id}";
  protected static final String CLAIM_TASK_URL = SINGLE_TASK_URL + "/claim";
  protected static final String UNCLAIM_TASK_URL = SINGLE_TASK_URL + "/unclaim";
  protected static final String COMPLETE_TASK_URL = SINGLE_TASK_URL + "/complete";
  protected static final String RESOLVE_TASK_URL = SINGLE_TASK_URL + "/resolve";
  protected static final String DELEGATE_TASK_URL = SINGLE_TASK_URL + "/delegate";
  protected static final String TASK_FORM_URL = SINGLE_TASK_URL + "/form";
  
  private Task mockTask;
  private TaskService taskServiceMock;
  private TaskQuery mockQuery;
  private FormService formServiceMock;
  private ManagementService managementServiceMock;
  private RepositoryService repositoryServiceMock;
  
  @Before
  public void setUpRuntimeData() {
    taskServiceMock = mock(TaskService.class);
    when(processEngine.getTaskService()).thenReturn(taskServiceMock);
  
    mockTask = MockProvider.createMockTask();
    mockQuery = mock(TaskQuery.class);
    when(mockQuery.taskId(anyString())).thenReturn(mockQuery);
    when(mockQuery.singleResult()).thenReturn(mockTask);
    when(taskServiceMock.createTaskQuery()).thenReturn(mockQuery);
    
    formServiceMock = mock(FormService.class);
    when(processEngine.getFormService()).thenReturn(formServiceMock);
    TaskFormData mockFormData = MockProvider.createMockTaskFormData();
    when(formServiceMock.getTaskFormData(anyString())).thenReturn(mockFormData);
    
    repositoryServiceMock = mock(RepositoryService.class);
    when(processEngine.getRepositoryService()).thenReturn(repositoryServiceMock);
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();
    when(repositoryServiceMock.getProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenReturn(mockDefinition);
    
    managementServiceMock = mock(ManagementService.class);
    when(processEngine.getManagementService()).thenReturn(managementServiceMock);
    when(managementServiceMock.getProcessApplicationForDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID)).thenReturn(MockProvider.EXAMPLE_PROCESS_APPLICATION_NAME);
    
    // replace the runtime container delegate & process application service with a mock
    
    ProcessApplicationService processApplicationService = mock(ProcessApplicationService.class);
    ProcessApplicationInfo appMock = MockProvider.createMockProcessApplicationInfo();
    when(processApplicationService.getProcessApplicationInfo(MockProvider.EXAMPLE_PROCESS_APPLICATION_NAME)).thenReturn(appMock);
    
    RuntimeContainerDelegate delegate = mock(RuntimeContainerDelegate.class);
    when(delegate.getProcessApplicationService()).thenReturn(processApplicationService);
    RuntimeContainerDelegate.INSTANCE.set(delegate);
  }
  
  @Test
  public void testGetSingleTask() {
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_TASK_ID))
      .body("name", equalTo(MockProvider.EXAMPLE_TASK_NAME))
      .body("assignee", equalTo(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME))
      .body("created", equalTo(MockProvider.EXAMPLE_TASK_CREATE_TIME))
      .body("due", equalTo(MockProvider.EXAMPLE_TASK_DUE_DATE))
      .body("delegationState", equalTo(MockProvider.EXAMPLE_TASK_DELEGATION_STATE.toString()))
      .body("description", equalTo(MockProvider.EXAMPLE_TASK_DESCRIPTION))
      .body("executionId", equalTo(MockProvider.EXAMPLE_TASK_EXECUTION_ID))
      .body("owner", equalTo(MockProvider.EXAMPLE_TASK_OWNER))
      .body("parentTaskId", equalTo(MockProvider.EXAMPLE_TASK_PARENT_TASK_ID))
      .body("priority", equalTo(MockProvider.EXAMPLE_TASK_PRIORITY))
      .body("processDefinitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("taskDefinitionKey", equalTo(MockProvider.EXAMPLE_TASK_DEFINITION_KEY))
      .when().get(SINGLE_TASK_URL);
  }
  
  @Test
  public void testGetForm() {
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("key", equalTo(MockProvider.EXAMPLE_FORM_KEY))
      .body("contextPath", equalTo(MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH))
      .when().get(TASK_FORM_URL);
  }
  
  /**
   * Assuming the task belongs to a deployment that does not belong to any process application
   */
  @Test
  public void testGetFormForNonRegisteredDeployment() {
    when(managementServiceMock.getProcessApplicationForDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID)).thenReturn(null);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("key", equalTo(MockProvider.EXAMPLE_FORM_KEY))
      .body("contextPath", nullValue())
      .when().get(TASK_FORM_URL);
  }
  
  /**
   * Assuming that the task belongs to no process definition
   */
  @Test
  public void getFormForIndependentTask() {
    when(mockTask.getProcessDefinitionId()).thenReturn(null);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("key", equalTo(MockProvider.EXAMPLE_FORM_KEY))
      .body("contextPath", nullValue())
      .when().get(TASK_FORM_URL);
    
    verify(repositoryServiceMock, never()).getProcessDefinition(null);
  }

  @Test
  public void testClaimTask() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", MockProvider.EXAMPLE_USER_ID);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(CLAIM_TASK_URL);
    
    verify(taskServiceMock).claim(MockProvider.EXAMPLE_TASK_ID, MockProvider.EXAMPLE_USER_ID);
  }

  @Test
  public void testMissingUserId() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", null);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(CLAIM_TASK_URL);
    
    verify(taskServiceMock).claim(MockProvider.EXAMPLE_TASK_ID, null);
  }

  @Test
  public void testUnsuccessfulClaimTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).claim(any(String.class), any(String.class));
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("expected exception"))
      .when().post(CLAIM_TASK_URL);
  }

  @Test
  public void testUnclaimTask() {
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(UNCLAIM_TASK_URL);
    
    verify(taskServiceMock).setAssignee(MockProvider.EXAMPLE_TASK_ID, null);
  }

  @Test
  public void testUnsuccessfulUnclaimTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).setAssignee(any(String.class), any(String.class));
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("expected exception"))
      .when().post(UNCLAIM_TASK_URL);
  }

  @Test
  public void testCompleteTask() {
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(COMPLETE_TASK_URL);
    
    verify(taskServiceMock).complete(MockProvider.EXAMPLE_TASK_ID, null);
  }

  @Test
  public void testCompleteWithParameters() {
    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", "aStringValue")
        .variable("anotherVariable", 42)
        .variable("aThirdValue", Boolean.TRUE).getVariables();
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(COMPLETE_TASK_URL);
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("aVariable", "aStringValue");
    expectedVariables.put("anotherVariable", 42);
    expectedVariables.put("aThirdValue", Boolean.TRUE);
    
    verify(taskServiceMock).complete(eq(MockProvider.EXAMPLE_TASK_ID), argThat(new EqualsMap(expectedVariables)));
  }
  
  @Test
  public void testCompleteWithUnparseableIntegerVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot complete task anId due to number format exception: For input string: \"1abc\""))
    .when().post(COMPLETE_TASK_URL);
  }

  @Test
  public void testCompleteWithUnparseableShortVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot complete task anId due to number format exception: For input string: \"1abc\""))
    .when().post(COMPLETE_TASK_URL);
  }
  
  @Test
  public void testCompleteWithUnparseableLongVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot complete task anId due to number format exception: For input string: \"1abc\""))
    .when().post(COMPLETE_TASK_URL);
  }
  
  @Test
  public void testCompleteWithUnparseableDoubleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot complete task anId due to number format exception: For input string: \"1abc\""))
    .when().post(COMPLETE_TASK_URL);
  }
  
  @Test
  public void testCompleteWithUnparseableDateVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot complete task anId due to parse exception: Unparseable date: \"1abc\""))
    .when().post(COMPLETE_TASK_URL);
  }

  @Test
  public void testCompleteWithNotSupportedVariableType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot complete task anId: The variable type 'X' is not supported."))
    .when().post(COMPLETE_TASK_URL);
  }
  
  @Test
  public void testUnsuccessfulCompleteTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).complete(any(String.class), Matchers.<Map<String, Object>>any());
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("expected exception"))
      .when().post(COMPLETE_TASK_URL);
  }

  @Test
  public void testResolveTask() {
    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", "aStringValue")
        .variable("anotherVariable", 42)
        .variable("aThirdValue", Boolean.TRUE).getVariables();
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(RESOLVE_TASK_URL);
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("aVariable", "aStringValue");
    expectedVariables.put("anotherVariable", 42);
    expectedVariables.put("aThirdValue", Boolean.TRUE);
    
    verify(taskServiceMock).resolveTask(eq(MockProvider.EXAMPLE_TASK_ID), argThat(new EqualsMap(expectedVariables)));
  }
  
  @Test
  public void testResolveTaskWithUnparseableIntegerVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot resolve task anId due to number format exception: For input string: \"1abc\""))
    .when().post(RESOLVE_TASK_URL);
  }

  @Test
  public void testResolveTaskWithUnparseableShortVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot resolve task anId due to number format exception: For input string: \"1abc\""))
    .when().post(RESOLVE_TASK_URL);
  }
  
  @Test
  public void testResolveTaskWithUnparseableLongVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot resolve task anId due to number format exception: For input string: \"1abc\""))
    .when().post(RESOLVE_TASK_URL);
  }
  
  @Test
  public void testResolveTaskWithUnparseableDoubleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot resolve task anId due to number format exception: For input string: \"1abc\""))
    .when().post(RESOLVE_TASK_URL);
  }
  
  @Test
  public void testResolveTaskWithUnparseableDateVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot resolve task anId due to parse exception: Unparseable date: \"1abc\""))
    .when().post(RESOLVE_TASK_URL);
  }

  @Test
  public void testResolveTaskWithNotSupportedVariableType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";
    
    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);
      
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(variables)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", containsString("Cannot resolve task anId: The variable type 'X' is not supported."))
    .when().post(RESOLVE_TASK_URL);
  }

  @Test
  public void testUnsuccessfulResolving() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).resolveTask(any(String.class), any(Map.class));
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
    .when().post(RESOLVE_TASK_URL);
  }

  @Test
  public void testGetNonExistingTask() {
    when(mockQuery.singleResult()).thenReturn(null);
    
    given().pathParam("id", "aNonExistingTaskId")
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("No matching task with id aNonExistingTaskId"))
      .when().get(SINGLE_TASK_URL);
  }

  @Test
  public void testGetNonExistingForm() {
    when(formServiceMock.getTaskFormData(anyString())).thenThrow(new ProcessEngineException("Expected exception: task does not exist."));
    
    given().pathParam("id", "aNonExistingTaskId")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot get form for task aNonExistingTaskId"))
      .when().get(TASK_FORM_URL);
  }

  @Test
  public void testDelegateTask() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", MockProvider.EXAMPLE_USER_ID);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(DELEGATE_TASK_URL);
    
    verify(taskServiceMock).delegateTask(MockProvider.EXAMPLE_TASK_ID, MockProvider.EXAMPLE_USER_ID);
  }

  @Test
  public void testUnsuccessfulDelegateTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).delegateTask(any(String.class), any(String.class));
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", MockProvider.EXAMPLE_USER_ID);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("expected exception"))
    .when().post(DELEGATE_TASK_URL);
  }

}
