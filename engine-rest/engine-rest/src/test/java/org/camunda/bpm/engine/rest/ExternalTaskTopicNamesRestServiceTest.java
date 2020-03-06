package org.camunda.bpm.engine.rest;

import io.restassured.response.Response;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ExternalTaskTopicNamesRestServiceTest extends AbstractRestServiceTest {
  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String EXTERNAL_TASK_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/external-task";
  protected static final String GET_EXTERNAL_TASK_TOPIC_NAMES_URL = EXTERNAL_TASK_QUERY_URL + "/topic-names";

  protected static final String WITH_LOCKED_TASKS = "withLockedTasks";
  protected static final String WITH_UNLOCKED_TASKS = "withUnlockedTasks";
  protected static final String WITH_RETRIES_LEFT = "withRetriesLeft";

  @Before
  public void setupMocks(){
    when(processEngine.getExternalTaskService().getTopicNames(false,false,false)).thenReturn(Arrays.asList("allTopics"));
    when(processEngine.getExternalTaskService().getTopicNames(true,false,false)).thenReturn(Arrays.asList("lockedTasks"));
    when(processEngine.getExternalTaskService().getTopicNames(false,true,false)).thenReturn(Arrays.asList("unlockedTasks"));
    when(processEngine.getExternalTaskService().getTopicNames(false,false,true)).thenReturn(Arrays.asList("withRetriesLeft"));
  }

  @Test
  public void testGetTopicNames(){
    Response response = given()
        .then()
        .expect()
        .statusCode(javax.ws.rs.core.Response.Status.OK.getStatusCode())
        .when()
        .get(GET_EXTERNAL_TASK_TOPIC_NAMES_URL);

    String content = response.asString();
    List<String> topicNames = from(content).getList("");

    assertEquals("allTopics", topicNames.get(0));
  }

  @Test
  public void testGetTopicNamesOfLockedTasks(){
    Response response = given()
        .header("accept", MediaType.APPLICATION_JSON)
        .param(WITH_LOCKED_TASKS, true)
        .param(WITH_UNLOCKED_TASKS, false)
        .param(WITH_RETRIES_LEFT, false)
        .then()
        .expect()
        .statusCode(javax.ws.rs.core.Response.Status.OK.getStatusCode())
        .when()
        .get(GET_EXTERNAL_TASK_TOPIC_NAMES_URL);

    String content = response.asString();
    List<String> topicNames = from(content).getList("");

    assertEquals("lockedTasks", topicNames.get(0));
  }

  @Test
  public void testGetTopicNamesOfUnlockedTasks(){
    Response response = given()
        .header("accept", MediaType.APPLICATION_JSON)
        .param(WITH_LOCKED_TASKS, false)
        .param(WITH_UNLOCKED_TASKS, true)
        .param(WITH_RETRIES_LEFT, false)
        .then()
        .expect()
        .statusCode(javax.ws.rs.core.Response.Status.OK.getStatusCode())
        .when()
        .get(GET_EXTERNAL_TASK_TOPIC_NAMES_URL);

    String content = response.asString();
    List<String> topicNames = from(content).getList("");

    assertEquals("unlockedTasks", topicNames.get(0));
  }

  @Test
  public void testGetTopicNamesOfTasksWithRetriesLeft(){
    Response response = given()
        .header("accept", MediaType.APPLICATION_JSON)
        .param(WITH_LOCKED_TASKS, false)
        .param(WITH_UNLOCKED_TASKS, false)
        .param(WITH_RETRIES_LEFT, true)
        .then()
        .expect()
        .statusCode(javax.ws.rs.core.Response.Status.OK.getStatusCode())
        .when()
        .get(GET_EXTERNAL_TASK_TOPIC_NAMES_URL);

    String content = response.asString();
    List<String> topicNames = from(content).getList("");

    assertEquals("withRetriesLeft", topicNames.get(0));
  }
}
