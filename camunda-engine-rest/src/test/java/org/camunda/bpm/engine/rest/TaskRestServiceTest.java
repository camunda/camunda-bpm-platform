package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.camunda.bpm.engine.rest.helper.EqualsList;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;

import com.jayway.restassured.response.Response;

public class TaskRestServiceTest extends AbstractRestServiceTest {
  
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
    setUpMockedQuery();
    String queryName = "name";
    
    Response response = given().queryParam("name", queryName)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);
    
    InOrder inOrder = inOrder(mockQuery);
    inOrder.verify(mockQuery).taskName(queryName);
    inOrder.verify(mockQuery).list();
    
    String content = response.asString();
    List<String> instances = from(content).getList("");
    Assert.assertEquals("There should be one task returned.", 1, instances.size());
    Assert.assertNotNull("The returned task should not be null.", instances.get(0));
    
    String returnedTaskName = from(content).getString("[0].name");
    
    Assert.assertEquals(EXAMPLE_TASK_NAME, returnedTaskName);
  }

  @Test
  public void testEmptyQuery() {
    setUpMockedQuery();
    String queryKey = "";
    given().queryParam("name", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testNoParametersQuery() {
    setUpMockedQuery();
    expect().statusCode(Status.OK.getStatusCode()).when().get(TASK_QUERY_URL);
    
    verify(mockQuery).list();
    verifyNoMoreInteractions(mockQuery);
  }
  
  @Test
  public void testAdditionalParametersExcludingVariables() {
    setUpMockedQuery();

    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();
    Map<String, Integer> intQueryParameters = getCompleteIntQueryParameters();
    
    given().queryParams(stringQueryParameters).queryParams(intQueryParameters)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);
    
    verify(mockQuery).processInstanceBusinessKey(stringQueryParameters.get("processInstanceBusinessKey"));
    verify(mockQuery).processDefinitionKey(stringQueryParameters.get("processDefinitionKey"));
    verify(mockQuery).processDefinitionId(stringQueryParameters.get("processDefinitionId"));
    verify(mockQuery).executionId(stringQueryParameters.get("executionId"));
    verify(mockQuery).processDefinitionName(stringQueryParameters.get("processDefinitionName"));
    verify(mockQuery).processInstanceId(stringQueryParameters.get("processInstanceId"));
    verify(mockQuery).taskAssignee(stringQueryParameters.get("assignee"));
    verify(mockQuery).taskCandidateGroup(stringQueryParameters.get("candidateGroup"));
    verify(mockQuery).taskCandidateUser(stringQueryParameters.get("candidate"));
    verify(mockQuery).taskDefinitionKey(stringQueryParameters.get("taskDefinitionKey"));
    verify(mockQuery).taskDefinitionKeyLike(stringQueryParameters.get("taskDefinitionKeyLike"));
    verify(mockQuery).taskDescription(stringQueryParameters.get("description"));
    verify(mockQuery).taskDescriptionLike(stringQueryParameters.get("descriptionLike"));
    verify(mockQuery).taskInvolvedUser(stringQueryParameters.get("involved"));
    verify(mockQuery).taskMaxPriority(intQueryParameters.get("maxPriority"));
    verify(mockQuery).taskMinPriority(intQueryParameters.get("minPriority"));
    verify(mockQuery).taskName(stringQueryParameters.get("name"));
    verify(mockQuery).taskNameLike(stringQueryParameters.get("nameLike"));
    verify(mockQuery).taskOwner(stringQueryParameters.get("owner"));
    verify(mockQuery).taskPriority(intQueryParameters.get("priority"));
    
    verify(mockQuery).taskUnassigned();
    
    verify(mockQuery).list();
  }
  
  private Map<String, Integer> getCompleteIntQueryParameters() {
    Map<String, Integer> parameters = new HashMap<String, Integer>();
    
    parameters.put("maxPriority", 10);
    parameters.put("minPriority", 9);
    parameters.put("priority", 8);
    
    
    return parameters;
  }
  
  private Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    
    parameters.put("processInstanceBusinessKey", "aBusinessKey");
    parameters.put("processDefinitionKey", "aProcDefKey");
    parameters.put("processDefinitionId", "aProcDefId");
    parameters.put("executionId", "anExecId");
    parameters.put("processDefinitionName", "aProcDefName");
    parameters.put("processInstanceId", "aProcInstId");
    parameters.put("assignee", "anAssignee");
    parameters.put("candidateGroup", "aCandidateGroup");
    parameters.put("candidate", "aCandidate");
    parameters.put("taskDefinitionKey", "aTaskDefKey");
    parameters.put("taskDefinitionKeyLike", "aTaskDefKeyLike");
    parameters.put("description", "aDesc");
    parameters.put("descriptionLike", "aDescLike");
    parameters.put("involved", "anInvolvedPerson");
    parameters.put("name", "aName");
    parameters.put("nameLike", "aNameLike");
    parameters.put("owner", "anOwner");
    parameters.put("unassigned", "true");

    return parameters;
  }
  
  @Test
  public void testDateParameters() {
    setUpMockedQuery();
    
    Map<String, String> queryParameters = getDateParameters();
    
    given().queryParams(queryParameters)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);

    verify(mockQuery).dueAfter(any(Date.class));
    verify(mockQuery).dueBefore(any(Date.class));
    verify(mockQuery).dueDate(any(Date.class));
    verify(mockQuery).taskCreatedAfter(any(Date.class));
    verify(mockQuery).taskCreatedBefore(any(Date.class));
    verify(mockQuery).taskCreatedOn(any(Date.class));
  }
  
  private Map<String, String> getDateParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("dueAfter", "2013-01-23T14:42:42");
    parameters.put("dueBefore", "2013-01-23T14:42:43");
    parameters.put("due", "2013-01-23T14:42:44");
    parameters.put("createdAfter", "2013-01-23T14:42:45");
    parameters.put("createdBefore", "2013-01-23T14:42:46");
    parameters.put("created", "2013-01-23T14:42:47");
    return parameters;
  }
  
  @Test
  public void testInvalidDateParameter() {
    given().queryParams("due", "anInvalidDate")
      .expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testCandidateGroupInList() {
    setUpMockedQuery();
   
    List<String> candidateGroups = new ArrayList<String>();
    candidateGroups.add("boss");
    candidateGroups.add("worker");
    String queryParam = candidateGroups.get(0) + "," + candidateGroups.get(1);
    
    given().queryParams("candidateGroups", queryParam)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);
    
    verify(mockQuery).taskCandidateGroupIn(argThat(new EqualsList(candidateGroups)));
  }
  
  @Test
  public void testDelegationState() {
    setUpMockedQuery();
    
    given().queryParams("delegationState", "PENDING")
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);
    
    verify(mockQuery).taskDelegationState(DelegationState.PENDING);
    
    given().queryParams("delegationState", "RESOLVED")
    .expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);
  
    verify(mockQuery).taskDelegationState(DelegationState.RESOLVED);
  }
  
  @Test
  public void testLowerCaseDelegationStateParam() {
    setUpMockedQuery();

    given().queryParams("delegationState", "resolved")
    .expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);
  
    verify(mockQuery).taskDelegationState(DelegationState.RESOLVED);
  }
}
