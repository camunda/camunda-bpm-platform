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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.rest.AbstractTaskRestServiceInteractionTest;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.ExampleDataProvider;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.ResteasyServerBootstrap;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

public class TaskRestServiceInteractionTest extends AbstractTaskRestServiceInteractionTest {

  private static ResteasyServerBootstrap resteasyBootstrap;
  
  private TaskService taskServiceMock;
  private TaskQuery mockQuery;
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
  public void testClaimTask() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", ExampleDataProvider.EXAMPLE_USER_ID);
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(CLAIM_TASK_URL);
    
    verify(taskServiceMock).claim(ExampleDataProvider.EXAMPLE_TASK_ID, ExampleDataProvider.EXAMPLE_USER_ID);
  }
  
  @Test
  public void testMissingUserId() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", null);
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(CLAIM_TASK_URL);
    
    verify(taskServiceMock).claim(ExampleDataProvider.EXAMPLE_TASK_ID, null);
  }
  
  @Test
  public void testUnsuccessfulClaimTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).claim(any(String.class), any(String.class));
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .when().post(CLAIM_TASK_URL);
  }
  
  @Test
  public void testUnclaimTask() {
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(UNCLAIM_TASK_URL);
    
    verify(taskServiceMock).setAssignee(ExampleDataProvider.EXAMPLE_TASK_ID, null);
  }
  
  @Test
  public void testUnsuccessfulUnclaimTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).setAssignee(any(String.class), any(String.class));
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .when().post(UNCLAIM_TASK_URL);
  }
  
  @Test
  public void testCompleteTask() {
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(COMPLETE_TASK_URL);
    
    verify(taskServiceMock).complete(ExampleDataProvider.EXAMPLE_TASK_ID, null);
  }
  
  @Test
  public void testCompleteWithParameters() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", "aStringValue");
    variables.put("anotherVariable", 42);
    variables.put("aThirdVariable", Boolean.TRUE);
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(COMPLETE_TASK_URL);
    
    verify(taskServiceMock).complete(eq(ExampleDataProvider.EXAMPLE_TASK_ID), argThat(new EqualsMap(variables)));
  }
  
  @Test
  public void testUnsuccessfulCompleteTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).complete(any(String.class), Matchers.<Map<String, Object>>any());
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .when().post(COMPLETE_TASK_URL);
  }
  
  @Test
  public void testResolveTask() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", "aStringValue");
    variables.put("anotherVariable", 42);
    variables.put("aThirdVariable", Boolean.TRUE);
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(RESOLVE_TASK_URL);
    
    verify(taskServiceMock).resolveTask(ExampleDataProvider.EXAMPLE_TASK_ID);
    
    RuntimeService runtimeServiceMock = processEngine.getRuntimeService();
    verify(runtimeServiceMock).setVariables(eq(ExampleDataProvider.EXAMPLE_TASK_EXECUTION_ID), argThat(new EqualsMap(variables)));
  }
  
  @Test
  public void testUnsuccessfulResolving() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).resolveTask(any(String.class));
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .when().post(RESOLVE_TASK_URL);
  }
  
  @Test
  public void testGetNonExistingTask() {
    when(mockQuery.singleResult()).thenReturn(null);
    
    given().pathParam("id", "aNonExistingTaskId")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(SINGLE_TASK_URL);
  }
  
  @Test
  public void testGetNonExistingForm() {
    when(formServiceMock.getTaskFormData(anyString())).thenThrow(new ProcessEngineException("Expected exception: task does not exist."));
    
    given().pathParam("id", "aNonExistingTaskId")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(TASK_FORM_URL);
  }
  
  @Test
  public void testDelegateTask() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", ExampleDataProvider.EXAMPLE_USER_ID);
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(DELEGATE_TASK_URL);
    
    verify(taskServiceMock).delegateTask(ExampleDataProvider.EXAMPLE_TASK_ID, ExampleDataProvider.EXAMPLE_USER_ID);
  }
  
  @Test
  public void testUnsuccessfulDelegateTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).delegateTask(any(String.class), any(String.class));
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", ExampleDataProvider.EXAMPLE_USER_ID);
    
    given().pathParam("id", ExampleDataProvider.EXAMPLE_TASK_ID)
    .contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect()
      .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .when().post(DELEGATE_TASK_URL);
  }

}
