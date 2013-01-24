package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;

import com.jayway.restassured.response.Response;

public class TaskServiceTest extends AbstractRestServiceTest {
  
  private static final String EXAMPLE_TASK_ID = "anId";
  private static final String EXAMPLE_TASK_NAME = "aName";

  private static final String TASK_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/task";
  
  private TaskQuery mockQuery;
  
  private TaskQuery setUpMockTaskQuery(List<Task> mockedTasks) {
    TaskQuery sampleTaskQuery = mock(TaskQuery.class);
    when(sampleTaskQuery.list()).thenReturn(mockedTasks);
    when(processEngine.getTaskService().createTaskQuery()).thenReturn(sampleTaskQuery);
    return sampleTaskQuery;
  }
  
  private List<Task> createMockTasks() {
    List<Task> mocks = new ArrayList<Task>();
    
    Task mockTask = mock(Task.class);
    when(mockTask.getId()).thenReturn(EXAMPLE_TASK_ID);
    when(mockTask.getName()).thenReturn(EXAMPLE_TASK_NAME);
    
    mocks.add(mockTask);
    return mocks;
  }
  
  public void setUpMockedQuery() {
    loadProcessEngineService();
    mockQuery = setUpMockTaskQuery(createMockTasks());
  }
  
  @Test
  public void testSimpleTaskQuery() {
    String queryName = "name";
    
    Response response = given().queryParam("name", queryName)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);
    
    InOrder inOrder = inOrder(mockQuery);
    inOrder.verify(mockQuery).taskName(EXAMPLE_TASK_NAME);
    inOrder.verify(mockQuery).list();
    
    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one task returned.", 1, instances.size());
    Assert.assertNotNull("The returned task should not be null.", instances.get(0));
    
    String returnedTaskName = from(content).getString("[0].name");
    
    Assert.assertEquals(EXAMPLE_TASK_NAME, returnedTaskName);
  }
}
