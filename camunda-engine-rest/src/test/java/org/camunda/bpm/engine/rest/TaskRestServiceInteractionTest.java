package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.activiti.engine.TaskService;
import org.junit.Test;

public class TaskRestServiceInteractionTest extends AbstractRestServiceTest {

  private static final String TASK_SERVICE_URL = TEST_RESOURCE_ROOT_PATH + "/task";
  private static final String CLAIM_TASK_URL = TASK_SERVICE_URL + "/{id}/claim";
  
  private static final String EXAMPLE_TASK_ID = "aTaskId";
  private static final String EXAMPLE_USER_ID = "aUser";
  
  private TaskService taskServiceMock;
  
  public void setupMockTask() {
    loadProcessEngineService();
    
    taskServiceMock = mock(TaskService.class);
    when(processEngine.getTaskService()).thenReturn(taskServiceMock);
  }
  
  @Test
  public void testClaimTask() {
    setupMockTask();
    
    given().pathParam("id", EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when().post(CLAIM_TASK_URL);
    
    verify(taskServiceMock).claim(EXAMPLE_TASK_ID, EXAMPLE_USER_ID);
  }
  
  public void testCompleteTask() {
    // TODO implement
  }
  
  public void testTryClaimCompletedTask() {
    // TODO implement
  }
}
