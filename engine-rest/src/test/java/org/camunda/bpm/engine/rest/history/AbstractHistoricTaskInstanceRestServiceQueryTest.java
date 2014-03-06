package org.camunda.bpm.engine.rest.history;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.rest.AbstractRestServiceTest;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import javax.ws.rs.core.Response.Status;
import javax.xml.registry.InvalidRequestException;
import java.util.*;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public abstract class AbstractHistoricTaskInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String HISTORIC_TASK_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/task";
  protected static final String HISTORIC_TASK_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_TASK_INSTANCE_RESOURCE_URL + "/count";

  protected HistoricTaskInstanceQuery mockedQuery;

  @Before
  public void setUpRuntimeData() {
    mockedQuery = mock(HistoricTaskInstanceQuery.class);

    List<HistoricTaskInstance> taskInstances = MockProvider.createMockHistoricTaskInstances();

    when(mockedQuery.list()).thenReturn(taskInstances);
    when(mockedQuery.count()).thenReturn((long) taskInstances.size());

    when(processEngine.getHistoryService().createHistoricTaskInstanceQuery()).thenReturn(mockedQuery);
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("processInstanceId", queryKey)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testNoParametersQueryAsPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  public void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("processInstanceId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given()
    .queryParam("sortOrder", "asc")
  .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("activityInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processDefinitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processInstanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("processInstanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByProcessInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("executionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByExecutionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("executionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByExecutionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricTaskInstanceDuration();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricTaskInstanceDuration();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricTaskInstanceEndTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("endTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricTaskInstanceEndTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("startTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceStartTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("startTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByHistoricActivityInstanceStartTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskName", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskName();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskName", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskName();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskDescription", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDescription();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskDescription", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDescription();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("assignee", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskAssignee();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("assignee", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskAssignee();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("owner", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskOwner();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("owner", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskOwner();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("dueDate", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDueDate();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("dueDate", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDueDate();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("followUpDate", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskFollowUpDate();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("followUpDate", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskFollowUpDate();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("deleteReason", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeleteReason();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("deleteReason", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeleteReason();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskDefinitionKey", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDefinitionKey();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("taskDefinitionKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskDefinitionKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("priority", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskPriority();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("priority", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTaskPriority();
    inOrder.verify(mockedQuery).desc();
  }

  @Test
  public void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  @Test
  public void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  public void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_TASK_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testQueryCountForPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then()
      .expect()
        .body("count", equalTo(1))
      .when()
        .post(HISTORIC_TASK_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  public void testSimpleHistoricTaskInstanceQuery() {
    Response response = given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one historic task instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned historic task instance should not be null.", instances.get(0));

    String returnedId = from(content).getString("[0].id");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedActivityInstanceId = from(content).getString("[0].activityInstanceId");
    String returnedName = from(content).getString("[0].name");
    String returnedDescription = from(content).getString("[0].description");
    String returnedDeleteReason = from(content).getString("[0].deleteReason");
    String returnedOwner = from(content).getString("[0].owner");
    String returnedAssignee = from(content).getString("[0].assignee");
    Date returnedStartTime = DateTimeUtil.parseDateTime(from(content).getString("[0].startTime")).toDate();
    Date returnedEndTime = DateTimeUtil.parseDateTime(from(content).getString("[0].endTime")).toDate();
    Long returnedDurationInMillis = from(content).getLong("[0].duration");
    String returnedTaskDefinitionKey = from(content).getString("[0].taskDefinitionKey");
    int returnedPriority = from(content).getInt("[0].priority");
    String returnedParentTaskId = from(content).getString("[0].parentTaskId");
    Date returnedDue = DateTimeUtil.parseDateTime(from(content).getString("[0].due")).toDate();
    Date returnedFollow = DateTimeUtil.parseDateTime(from(content).getString("[0].followUp")).toDate();

    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_ID, returnedId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID, returnedActivityInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_EXEC_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_NAME, returnedName);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION, returnedDescription);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON, returnedDeleteReason);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_OWNER, returnedOwner);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE, returnedAssignee);
    Assert.assertEquals(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_HISTORIC_TASK_INST_START_TIME).toDate(), returnedStartTime);
    Assert.assertEquals(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_HISTORIC_TASK_INST_END_TIME).toDate(), returnedEndTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DURATION, returnedDurationInMillis);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DEF_KEY, returnedTaskDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PRIORITY, returnedPriority);
    Assert.assertEquals(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE).toDate(), returnedDue);
    Assert.assertEquals(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE).toDate(), returnedFollow);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID, returnedParentTaskId);
  }

  @Test
  public void testSimpleHistoricTaskInstanceQueryAsPost() {
    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(EMPTY_JSON_OBJECT)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
        .when()
          .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one historic task instance returned.", 1, instances.size());
    Assert.assertNotNull("The returned historic task instance should not be null.", instances.get(0));

    String returnedId = from(content).getString("[0].id");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedActivityInstanceId = from(content).getString("[0].activityInstanceId");
    String returnedName = from(content).getString("[0].name");
    String returnedDescription = from(content).getString("[0].description");
    String returnedDeleteReason = from(content).getString("[0].deleteReason");
    String returnedOwner = from(content).getString("[0].owner");
    String returnedAssignee = from(content).getString("[0].assignee");
    Date returnedStartTime = DateTimeUtil.parseDateTime(from(content).getString("[0].startTime")).toDate();
    Date returnedEndTime = DateTimeUtil.parseDateTime(from(content).getString("[0].endTime")).toDate();
    Long returnedDurationInMillis = from(content).getLong("[0].duration");
    String returnedTaskDefinitionKey = from(content).getString("[0].taskDefinitionKey");
    int returnedPriority = from(content).getInt("[0].priority");
    String returnedParentTaskId = from(content).getString("[0].parentTaskId");
    Date returnedDue = DateTimeUtil.parseDateTime(from(content).getString("[0].due")).toDate();
    Date returnedFollow = DateTimeUtil.parseDateTime(from(content).getString("[0].followUp")).toDate();

    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_ID, returnedId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID, returnedActivityInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_EXEC_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_NAME, returnedName);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION, returnedDescription);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON, returnedDeleteReason);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_OWNER, returnedOwner);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE, returnedAssignee);
    Assert.assertEquals(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_HISTORIC_TASK_INST_START_TIME).toDate(), returnedStartTime);
    Assert.assertEquals(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_HISTORIC_TASK_INST_END_TIME).toDate(), returnedEndTime);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DURATION, returnedDurationInMillis);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DEF_KEY, returnedTaskDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PRIORITY, returnedPriority);
    Assert.assertEquals(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE).toDate(), returnedDue);
    Assert.assertEquals(DateTimeUtil.parseDateTime(MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE).toDate(), returnedFollow);
    Assert.assertEquals(MockProvider.EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID, returnedParentTaskId);
  }

  @Test
  public void testQueryByTaskId() {
    String taskId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ID;

    given()
      .queryParam("taskId", taskId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskId(taskId);
  }

  @Test
  public void testQueryByTaskIdAsPost() {
    String taskId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ID;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskId", taskId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskId(taskId);
  }

  @Test
  public void testQueryByProcessInstanceId() {
    String processInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID;

    given()
      .queryParam("processInstanceId", processInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processInstanceId(processInstanceId);
  }

  @Test
  public void testQueryByProcessInstanceIdAsPost() {
    String processInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("processInstanceId", processInstanceId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processInstanceId(processInstanceId);
  }

  @Test
  public void testQueryByExecutionId() {
    String executionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_EXEC_ID;

    given()
      .queryParam("executionId", executionId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).executionId(executionId);
  }

  @Test
  public void testQueryByExecutionIdAsPost() {
    String executionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_EXEC_ID;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("executionId", executionId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).executionId(executionId);
  }

  @Test
  public void testQueryByActivityInstanceId() {
    String activityInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID;

    given()
      .queryParam("activityInstanceIdIn", activityInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).activityInstanceIdIn(activityInstanceId);
  }

  @Test
  public void testQueryByActivityInstanceIdAsPost() {
    String activityInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID;

    List<String> activityInstanceIds = new ArrayList<String>();
    activityInstanceIds.add(activityInstanceId);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("activityInstanceIdIn", activityInstanceIds);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).activityInstanceIdIn(activityInstanceId);
  }

  @Test
  public void testQueryByActivityInstanceIds() {
    String activityInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID;
    String anotherActivityInstanceId = "anotherActivityInstanceId";

    given()
      .queryParam("activityInstanceIdIn", activityInstanceId + "," + anotherActivityInstanceId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).activityInstanceIdIn(activityInstanceId, anotherActivityInstanceId);
  }

  @Test
  public void testQueryByActivityInstanceIdsAsPost() {
    String activityInstanceId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID;
    String anotherActivityInstanceId = "anotherActivityInstanceId";

    List<String> activityInstanceIds = new ArrayList<String>();
    activityInstanceIds.add(activityInstanceId);
    activityInstanceIds.add(anotherActivityInstanceId);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("activityInstanceIdIn", activityInstanceIds);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).activityInstanceIdIn(activityInstanceId, anotherActivityInstanceId);
  }

  @Test
  public void testQueryByProcessDefinitionId() {
    String processDefinitionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID;

    given()
      .queryParam("processDefinitionId", processDefinitionId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionId(processDefinitionId);
  }

  @Test
  public void testQueryByProcessDefinitionIdAsPost() {
    String processDefinitionId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("processDefinitionId", processDefinitionId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionId(processDefinitionId);
  }

  @Test
  public void testQueryByProcessDefinitionKey() {
    String processDefinitionKey = "aProcDefKey";

    given()
      .queryParam("processDefinitionKey", processDefinitionKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionKey(processDefinitionKey);
  }

  @Test
  public void testQueryByProcessDefinitionKeyAsPost() {
    String processDefinitionKey = "aProcDefKey";

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("processDefinitionKey", processDefinitionKey);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionKey(processDefinitionKey);
  }

  @Test
  public void testQueryByProcessDefinitionName() {
    String processDefinitionName = "aProcDefName";

    given()
      .queryParam("processDefinitionName", processDefinitionName)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionName(processDefinitionName);
  }

  @Test
  public void testQueryByProcessDefinitionNameAsPost() {
    String processDefinitionName = "aProcDefName";

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("processDefinitionName", processDefinitionName);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processDefinitionName(processDefinitionName);
  }

  @Test
  public void testQueryByTaskName() {
    String taskName = "aTaskName";

    given()
      .queryParam("taskName", taskName)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskName(taskName);
  }

  @Test
  public void testQueryByTaskNameAsPost() {
    String taskName = "aTaskName";

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskName", taskName);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskName(taskName);
  }

  @Test
  public void testQueryByTaskNameLike() {
    String taskNameLike = "aTaskNameLike";

    given()
      .queryParam("taskNameLike", taskNameLike)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskNameLike(taskNameLike);
  }

  @Test
  public void testQueryByTaskNameLikeAsPost() {
    String taskNameLike = "aTaskNameLike";

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskNameLike", taskNameLike);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskNameLike(taskNameLike);
  }

  @Test
  public void testQueryByTaskDescription() {
    String taskDescription = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION;

    given()
      .queryParam("taskDescription", taskDescription)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDescription(taskDescription);
  }

  @Test
  public void testQueryByTaskDescriptionAsPost() {
    String taskDescription = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskDescription", taskDescription);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDescription(taskDescription);
  }

  @Test
  public void testQueryByTaskDescriptionLike() {
    String taskDescriptionLike = "aTaskDescriptionLike";

    given()
      .queryParam("taskDescriptionLike", taskDescriptionLike)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDescriptionLike(taskDescriptionLike);
  }

  @Test
  public void testQueryByTaskDescriptionLikeAsPost() {
    String taskDescriptionLike = "aTaskDescriptionLike";

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskDescriptionLike", taskDescriptionLike);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDescriptionLike(taskDescriptionLike);
  }

  @Test
  public void testQueryByTaskDefinitionKey() {
    String taskDefinitionKey = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DEF_KEY;

    given()
      .queryParam("taskDefinitionKey", taskDefinitionKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDefinitionKey(taskDefinitionKey);
  }

  @Test
  public void testQueryByTaskDefinitionKeyAsPost() {
    String taskDefinitionKey = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DEF_KEY;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskDefinitionKey", taskDefinitionKey);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDefinitionKey(taskDefinitionKey);
  }

  @Test
  public void testQueryByTaskDeleteReason() {
    String taskDeleteReason = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON;

    given()
      .queryParam("taskDeleteReason", taskDeleteReason)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDeleteReason(taskDeleteReason);
  }

  @Test
  public void testQueryByTaskDeleteReasonAsPost() {
    String taskDeleteReason = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskDeleteReason", taskDeleteReason);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDeleteReason(taskDeleteReason);
  }

  @Test
  public void testQueryByTaskDeleteReasonLike() {
    String taskDeleteReasonLike = "aTaskDeleteReasonLike";

    given()
      .queryParam("taskDeleteReasonLike", taskDeleteReasonLike)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDeleteReasonLike(taskDeleteReasonLike);
  }

  @Test
  public void testQueryByTaskDeleteReasonLikeAsPost() {
    String taskDeleteReasonLike = "aTaskDeleteReasonLike";

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskDeleteReasonLike", taskDeleteReasonLike);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDeleteReasonLike(taskDeleteReasonLike);
  }

  @Test
  public void testQueryByTaskAssignee() {
    String taskAssignee = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE;

    given()
      .queryParam("taskAssignee", taskAssignee)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskAssignee(taskAssignee);
  }

  @Test
  public void testQueryByTaskAssigneeAsPost() {
    String taskAssignee = MockProvider.EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskAssignee", taskAssignee);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskAssignee(taskAssignee);
  }

  @Test
  public void testQueryByTaskAssigneeLike() {
    String taskAssigneeLike = "aTaskAssigneeLike";

    given()
      .queryParam("taskAssigneeLike", taskAssigneeLike)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskAssigneeLike(taskAssigneeLike);
  }

  @Test
  public void testQueryByTaskAssigneeLikeAsPost() {
    String taskAssigneeLike = "aTaskAssigneeLike";

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskAssigneeLike", taskAssigneeLike);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskAssigneeLike(taskAssigneeLike);
  }

  @Test
  public void testQueryByTaskOwner() {
    String taskOwner = MockProvider.EXAMPLE_HISTORIC_TASK_INST_OWNER;

    given()
      .queryParam("taskOwner", taskOwner)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskOwner(taskOwner);
  }

  @Test
  public void testQueryByTaskOwnerAsPost() {
    String taskOwner = MockProvider.EXAMPLE_HISTORIC_TASK_INST_OWNER;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskOwner", taskOwner);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskOwner(taskOwner);
  }

  @Test
  public void testQueryByTaskOwnerLike() {
    String taskOwnerLike = "aTaskOwnerLike";

    given()
      .queryParam("taskOwnerLike", taskOwnerLike)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskOwnerLike(taskOwnerLike);
  }

  @Test
  public void testQueryByTaskOwnerLikeAsPost() {
    String taskOwnerLike = "aTaskOwnerLike";

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskOwnerLike", taskOwnerLike);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskOwnerLike(taskOwnerLike);
  }

  @Test
  public void testQueryByTaskPriority() {
    int taskPriority = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PRIORITY;

    given()
      .queryParam("taskPriority", taskPriority)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskPriority(taskPriority);
  }

  @Test
  public void testQueryByTaskPriorityAsPost() {
    int taskPriority = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PRIORITY;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskPriority", taskPriority);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskPriority(taskPriority);
  }

  @Test
  public void testQueryByFinished() {
    given()
      .queryParam("finished", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).finished();
  }

  @Test
  public void testQueryByFinishedAsPost() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("finished", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).finished();
  }

  @Test
  public void testQueryByUnfinished() {
    given()
      .queryParam("unfinished", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).unfinished();
  }

  @Test
  public void testQueryByUnfinishedAsPost() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("unfinished", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).unfinished();
  }

  @Test
  public void testQueryByProcessFinished() {
    given()
      .queryParam("processFinished", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processFinished();
  }

  @Test
  public void testQueryByProcessFinishedAsPost() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("processFinished", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processFinished();
  }

  @Test
  public void testQueryByProcessUnfinished() {
    given()
      .queryParam("processUnfinished", true)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processUnfinished();
  }

  @Test
  public void testQueryByProcessUnfinishedAsPost() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("processUnfinished", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processUnfinished();
  }

  @Test
  public void testQueryByTaskParentTaskId() {
    String taskParentTaskId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID;

    given()
      .queryParam("taskParentTaskId", taskParentTaskId)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskParentTaskId(taskParentTaskId);
  }

  @Test
  public void testQueryByTaskParentTaskIdAsPost() {
    String taskParentTaskId = MockProvider.EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskParentTaskId", taskParentTaskId);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskParentTaskId(taskParentTaskId);
  }

  @Test
  public void testQueryByTaskDueDate() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    given()
      .queryParam("taskDueDate", due)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueDate(DateTimeUtil.parseDateTime(due).toDate());
  }

  @Test
  public void testQueryByTaskDueDateAsPost() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskDueDate", DateTimeUtil.parseDateTime(due).toDate());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueDate(DateTimeUtil.parseDateTime(due).toDate());
  }

  @Test
  public void testQueryByTaskDueDateBefore() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    given()
      .queryParam("taskDueDateBefore", due)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueBefore(DateTimeUtil.parseDateTime(due).toDate());
  }

  @Test
  public void testQueryByTaskDueDateBeforeAsPost() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskDueDateBefore", DateTimeUtil.parseDateTime(due).toDate());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueBefore(DateTimeUtil.parseDateTime(due).toDate());
  }

  @Test
  public void testQueryByTaskDueDateAfter() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    given()
      .queryParam("taskDueDateAfter", due)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueAfter(DateTimeUtil.parseDateTime(due).toDate());
  }

  @Test
  public void testQueryByTaskDueDateAfterAsPost() {
    String due = MockProvider.EXAMPLE_HISTORIC_TASK_INST_DUE_DATE;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskDueDateAfter", DateTimeUtil.parseDateTime(due).toDate());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskDueAfter(DateTimeUtil.parseDateTime(due).toDate());
  }

  @Test
  public void testQueryByTaskFollowUpDate() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    given()
    .queryParam("taskFollowUpDate", followUp)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpDate(DateTimeUtil.parseDateTime(followUp).toDate());
  }

  @Test
  public void testQueryByTaskFollowUpDateAsPost() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskFollowUpDate", DateTimeUtil.parseDateTime(followUp).toDate());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpDate(DateTimeUtil.parseDateTime(followUp).toDate());
  }

  @Test
  public void testQueryByTaskFollowUpDateBefore() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    given()
      .queryParam("taskFollowUpDateBefore", followUp)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpBefore(DateTimeUtil.parseDateTime(followUp).toDate());
  }

  @Test
  public void testQueryByTaskFollowUpDateBeforeAsPost() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskFollowUpDateBefore", DateTimeUtil.parseDateTime(followUp).toDate());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpBefore(DateTimeUtil.parseDateTime(followUp).toDate());
  }

  @Test
  public void testQueryByTaskFollowUpDateAfter() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    given()
      .queryParam("taskFollowUpDateAfter", followUp)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpAfter(DateTimeUtil.parseDateTime(followUp).toDate());
  }

  @Test
  public void testQueryByTaskFollowUpDateAfterAsPost() {
    String followUp = MockProvider.EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskFollowUpDateAfter", DateTimeUtil.parseDateTime(followUp).toDate());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskFollowUpAfter(DateTimeUtil.parseDateTime(followUp).toDate());
  }

  @Test
  public void testQueryByTaskVariable() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_eq_" + variableValue;

    String queryValue = variableParameter;

    given()
      .queryParam("taskVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskVariableValueEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByTaskVariableAsPost() {
    String variableName = "varName";
    String variableValue = "varValue";

    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", variableName);
    variableJson.put("operator", "eq");
    variableJson.put("value", variableValue);

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskVariables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).taskVariableValueEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByInvalidTaskVariable() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_" + invalidComparator + "_" + variableValue;

    given()
      .queryParam("taskVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Invalid variable comparator specified: " + invalidComparator))
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    // invalid format
    queryValue = "invalidFormattedVariableQuery";

    given()
      .queryParam("taskVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("variable query parameter has to have format KEY_OPERATOR_VALUE"))
      .when()
      .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testQueryByInvalidTaskVariableAsPost() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";

    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", variableName);
    variableJson.put("operator", invalidComparator);
    variableJson.put("value", variableValue);

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("taskVariables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Invalid variable comparator specified: " + invalidComparator))
      .when()
        .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testQueryByProcessVariable() {
    String variableName = "varName";
    String variableValue = "varValue";
    String variableParameter = variableName + "_eq_" + variableValue;

    String queryValue = variableParameter;

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processVariableValueEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByProcessVariableAsPost() {
    String variableName = "varName";
    String variableValue = "varValue";

    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", variableName);
    variableJson.put("operator", "eq");
    variableJson.put("value", variableValue);

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("processVariables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).processVariableValueEquals(variableName, variableValue);
  }

  @Test
  public void testQueryByInvalidProcessVariable() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_" + invalidComparator + "_" + variableValue;

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Invalid variable comparator specified: " + invalidComparator))
      .when()
        .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);

    // invalid format
    queryValue = "invalidFormattedVariableQuery";

    given()
      .queryParam("processVariables", queryValue)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("variable query parameter has to have format KEY_OPERATOR_VALUE"))
      .when()
      .get(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

  @Test
  public void testQueryByInvalidProcessVariableAsPost() {
    // invalid comparator
    String invalidComparator = "anInvalidComparator";
    String variableName = "varName";
    String variableValue = "varValue";

    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", variableName);
    variableJson.put("operator", invalidComparator);
    variableJson.put("value", variableValue);

    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("processVariables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(params)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Invalid variable comparator specified: " + invalidComparator))
      .when()
        .post(HISTORIC_TASK_INSTANCE_RESOURCE_URL);
  }

}
