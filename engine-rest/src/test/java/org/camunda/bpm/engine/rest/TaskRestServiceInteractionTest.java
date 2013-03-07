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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Test;
import org.mockito.Matchers;

public class TaskRestServiceInteractionTest extends AbstractRestServiceTest {

  private static final String TASK_SERVICE_URL = TEST_RESOURCE_ROOT_PATH + "/task";
  private static final String SINGLE_TASK_URL = TASK_SERVICE_URL + "/{id}";
  private static final String CLAIM_TASK_URL = SINGLE_TASK_URL + "/claim";
  private static final String UNCLAIM_TASK_URL = SINGLE_TASK_URL + "/unclaim";
  private static final String COMPLETE_TASK_URL = SINGLE_TASK_URL + "/complete";
  private static final String RESOLVE_TASK_URL = SINGLE_TASK_URL + "/resolve";
  private static final String DELEGATE_TASK_URL = SINGLE_TASK_URL + "/delegate";
  private static final String TASK_FORM_URL = SINGLE_TASK_URL + "/form";
  
  private TaskService taskServiceMock;
  private TaskQuery mockQuery;
  private FormService formServiceMock;
  
  public void setupMockTaskService() throws IOException {
    setupTestScenario();
    
    taskServiceMock = mock(TaskService.class);
    when(processEngine.getTaskService()).thenReturn(taskServiceMock);

    Task mockTask = MockProvider.createMockTask();
    mockQuery = mock(TaskQuery.class);
    when(mockQuery.taskId(anyString())).thenReturn(mockQuery);
    when(mockQuery.singleResult()).thenReturn(mockTask);
    when(taskServiceMock.createTaskQuery()).thenReturn(mockQuery);
    
    formServiceMock = mock(FormService.class);
    when(processEngine.getFormService()).thenReturn(formServiceMock);
    TaskFormData mockFormData = MockProvider.createMockTaskFormData();
    when(formServiceMock.getTaskFormData(anyString())).thenReturn(mockFormData);
  }
  
  @Test
  public void testClaimTask() throws IOException {
    setupMockTaskService();
    
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
  public void testMissingUserId() throws IOException {
    setupMockTaskService();
      
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
  public void testUnsuccessfulClaimTask() throws IOException {
    setupMockTaskService();
    
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).claim(any(String.class), any(String.class));
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .when().post(CLAIM_TASK_URL);
  }
  
  @Test
  public void testUnclaimTask() throws IOException {
    setupMockTaskService();
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", MockProvider.EXAMPLE_USER_ID);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(UNCLAIM_TASK_URL);
    
    verify(taskServiceMock).setAssignee(MockProvider.EXAMPLE_TASK_ID, null);
  }
  
  @Test
  public void testUnsuccessfulUnclaimTask() throws IOException {
    setupMockTaskService();
    
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).setAssignee(any(String.class), any(String.class));
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", MockProvider.EXAMPLE_USER_ID);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .when().post(UNCLAIM_TASK_URL);
    
    verify(taskServiceMock).setAssignee(MockProvider.EXAMPLE_TASK_ID, null);
  }
  
  @Test
  public void testCompleteTask() throws IOException {
    setupMockTaskService();
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(COMPLETE_TASK_URL);
    
    verify(taskServiceMock).complete(MockProvider.EXAMPLE_TASK_ID, null);
  }
  
  @Test
  public void testCompleteWithParameters() throws IOException {
    setupMockTaskService();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", "aStringValue");
    variables.put("anotherVariable", 42);
    variables.put("aThirdVariable", Boolean.TRUE);
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(COMPLETE_TASK_URL);
    
    verify(taskServiceMock).complete(eq(MockProvider.EXAMPLE_TASK_ID), argThat(new EqualsMap(variables)));
  }
  
  @Test
  public void testUnsuccessfulCompleteTask() throws IOException {
    setupMockTaskService();
    
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).complete(any(String.class), Matchers.<Map<String, Object>>any());
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .when().post(COMPLETE_TASK_URL);
  }
  
  @Test
  public void testResolveTask() throws IOException {
    setupMockTaskService();
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", "aStringValue");
    variables.put("anotherVariable", 42);
    variables.put("aThirdVariable", Boolean.TRUE);
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(RESOLVE_TASK_URL);
    
    verify(taskServiceMock).resolveTask(MockProvider.EXAMPLE_TASK_ID);
    
    RuntimeService runtimeServiceMock = processEngine.getRuntimeService();
    verify(runtimeServiceMock).setVariables(eq(MockProvider.EXAMPLE_TASK_EXECUTION_ID), argThat(new EqualsMap(variables)));
  }
  
  @Test
  public void testUnsuccessfulResolving() throws IOException {
    setupMockTaskService();
    
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).resolveTask(any(String.class));
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .when().post(RESOLVE_TASK_URL);
  }
  
  @Test
  public void testGetSingleTask() throws IOException {
    setupMockTaskService();
    
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
  public void testGetNonExistingTask() throws IOException {
    setupMockTaskService();
    
    when(mockQuery.singleResult()).thenReturn(null);
    
    given().pathParam("id", "aNonExistingTaskId")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(SINGLE_TASK_URL);
  }
  
  @Test
  public void testGetForm() throws IOException {
    setupMockTaskService();
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("key", equalTo(MockProvider.EXAMPLE_FORM_KEY))
      .when().get(TASK_FORM_URL);
  }
  
  @Test
  public void testGetNonExistingForm() throws IOException {
    setupMockTaskService();
    
    when(formServiceMock.getTaskFormData(anyString())).thenThrow(new ProcessEngineException("Expected exception: task does not exist."));
    
    given().pathParam("id", "aNonExistingTaskId")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(TASK_FORM_URL);
  }
  
  @Test
  public void testDelegateTask() throws IOException {
    setupMockTaskService();
    
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
  public void testUnsuccessfulDelegateTask() throws IOException {
    setupMockTaskService();
    
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).delegateTask(any(String.class), any(String.class));
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", MockProvider.EXAMPLE_USER_ID);
    
    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .when().post(DELEGATE_TASK_URL);
  }
}
