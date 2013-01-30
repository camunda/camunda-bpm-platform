package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
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

import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.camunda.bpm.engine.rest.helper.EqualsList;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;

public class TaskRestServiceTest extends AbstractRestServiceTest {
  
  private static final String EXAMPLE_ID = "anId";
  private static final String EXAMPLE_NAME = "aName";
  private static final String EXAMPLE_ASSIGNEE_NAME = "anAssignee";
  private static final String EXAMPLE_CREATE_TIME = "2013-01-23T13:42:42";
  private static final String EXAMPLE_DUE_DATE = "2013-01-23T13:42:43";
  private static final DelegationState EXAMPLE_DELEGATION_STATE = DelegationState.RESOLVED;
  private static final String EXAMPLE_DESCRIPTION = "aDescription";
  private static final String EXAMPLE_EXECUTION_ID = "anExecution";
  private static final String EXAMPLE_OWNER = "kermit";
  private static final String EXAMPLE_PARENT_TASK_ID = "aParentId";
  private static final int EXAMPLE_PRIORITY = 42;
  private static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcDefId";
  private static final String EXAMPLE_PROCESS_INSTANCE_ID = "aProcInstId";
  private static final String EXAMPLE_TASK_DEFINITION_KEY = "aTaskDefinitionKey";

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
    when(mockTask.getId()).thenReturn(EXAMPLE_ID);
    when(mockTask.getName()).thenReturn(EXAMPLE_NAME);
    when(mockTask.getAssignee()).thenReturn(EXAMPLE_ASSIGNEE_NAME);
    when(mockTask.getCreateTime()).thenReturn(DateTime.parse(EXAMPLE_CREATE_TIME).toDate());  
    when(mockTask.getDueDate()).thenReturn(DateTime.parse(EXAMPLE_DUE_DATE).toDate()); 
    when(mockTask.getDelegationState()).thenReturn(EXAMPLE_DELEGATION_STATE); 
    when(mockTask.getDescription()).thenReturn(EXAMPLE_DESCRIPTION); 
    when(mockTask.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID); 
    when(mockTask.getOwner()).thenReturn(EXAMPLE_OWNER); 
    when(mockTask.getParentTaskId()).thenReturn(EXAMPLE_PARENT_TASK_ID); 
    when(mockTask.getPriority()).thenReturn(EXAMPLE_PRIORITY); 
    when(mockTask.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID); 
    when(mockTask.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mockTask.getTaskDefinitionKey()).thenReturn(EXAMPLE_TASK_DEFINITION_KEY);
    
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
    String returnedId = from(content).getString("[0].id");
    String returendAssignee = from(content).getString("[0].assignee");
    String returnedCreateTime = from(content).getString("[0].created");
    String returnedDueDate = from(content).getString("[0].due");
    String returnedDelegationState = from(content).getString("[0].delegationState");
    String returnedDescription = from(content).getString("[0].description");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedOwner = from(content).getString("[0].owner");
    String returnedParentTaskId = from(content).getString("[0].parentTaskId");
    int returnedPriority = from(content).getInt("[0].priority");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedTaskDefinitionKey = from(content).getString("[0].taskDefinitionKey");
    
    Assert.assertEquals(EXAMPLE_NAME, returnedTaskName);
    Assert.assertEquals(EXAMPLE_ID, returnedId);
    Assert.assertEquals(EXAMPLE_ASSIGNEE_NAME, returendAssignee);
    Assert.assertEquals(EXAMPLE_CREATE_TIME, returnedCreateTime);
    Assert.assertEquals(EXAMPLE_DUE_DATE, returnedDueDate);
    Assert.assertEquals(EXAMPLE_DELEGATION_STATE.toString(), returnedDelegationState);
    Assert.assertEquals(EXAMPLE_DESCRIPTION, returnedDescription);
    Assert.assertEquals(EXAMPLE_EXECUTION_ID, returnedExecutionId);
    Assert.assertEquals(EXAMPLE_OWNER, returnedOwner);
    Assert.assertEquals(EXAMPLE_PARENT_TASK_ID, returnedParentTaskId);
    Assert.assertEquals(EXAMPLE_PRIORITY, returnedPriority);
    Assert.assertEquals(EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(EXAMPLE_TASK_DEFINITION_KEY, returnedTaskDefinitionKey);
    
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
    
    verifyIntegerParameterQueryInvocations();
    verifyStringParameterQueryInvocations();
    
    verify(mockQuery).taskUnassigned();
    
    verify(mockQuery).list();
  }
  
  private void verifyIntegerParameterQueryInvocations() {
    Map<String, Integer> intQueryParameters = getCompleteIntQueryParameters();
    
    verify(mockQuery).taskMaxPriority(intQueryParameters.get("maxPriority"));
    verify(mockQuery).taskMinPriority(intQueryParameters.get("minPriority"));
    verify(mockQuery).taskPriority(intQueryParameters.get("priority"));
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
  
  private void verifyStringParameterQueryInvocations() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();
    
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
    verify(mockQuery).taskName(stringQueryParameters.get("name"));
    verify(mockQuery).taskNameLike(stringQueryParameters.get("nameLike"));
    verify(mockQuery).taskOwner(stringQueryParameters.get("owner"));
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
  
  @Test
  public void testSortingParameters() {
    setUpMockedQuery();
    
    InOrder inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("dueDate", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByDueDate();
    inOrder.verify(mockQuery).desc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("executionId", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByExecutionId();
    inOrder.verify(mockQuery).asc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("instanceId", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByProcessInstanceId();
    inOrder.verify(mockQuery).desc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("assignee", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskAssignee();
    inOrder.verify(mockQuery).asc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("created", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskCreateTime();
    inOrder.verify(mockQuery).desc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("description", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskDescription();
    inOrder.verify(mockQuery).asc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("id", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskId();
    inOrder.verify(mockQuery).desc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("name", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskName();
    inOrder.verify(mockQuery).asc();
    setUpMockedQuery();
    
    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("priority", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskPriority();
    inOrder.verify(mockQuery).desc();
  }
  
  private void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
      .then().expect().statusCode(expectedStatus.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testSortByParameterOnly() {
    setUpMockedQuery();
    given().queryParam("sortBy", "dueDate")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testSortOrderParameterOnly() {
    setUpMockedQuery();
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testSuccessfulPagination() {
    setUpMockedQuery();
    
    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);
    
    verify(mockQuery).listPage(firstResult, maxResults);
  }
  
  @Test
  public void testTaskVariableParameters() {
    setUpMockedQuery();
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;    
    given().queryParam("taskVariables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);    
    verify(mockQuery).taskVariableValueEquals(variableName, variableValue);
    
    setUpMockedQuery();
    queryValue = variableName + "_neq_" + variableValue;    
    given().queryParam("taskVariables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);    
    verify(mockQuery).taskVariableValueNotEquals(variableName, variableValue);
  }
  
  @Test
  public void testProcessVariableParameters() {
    setUpMockedQuery();
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;    
    given().queryParam("processVariables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);    
    verify(mockQuery).processVariableValueEquals(variableName, variableValue);
    
    setUpMockedQuery();
    queryValue = variableName + "_neq_" + variableValue;    
    given().queryParam("processVariables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);    
    verify(mockQuery).processVariableValueNotEquals(variableName, variableValue);
  }
  
  @Test
  public void testMultipleVariableParametersAsPost() {
    setUpMockedQuery();
    
    String variableName = "varName";
    String variableValue = "varValue";
    JSONObject queryVariable = new JSONObject();
    queryVariable.put("name", variableName);
    queryVariable.put("operator", "eq");
    queryVariable.put("value", variableValue);
    
    String anotherVariableName = "anotherVarName";
    Integer anotherVariableValue = 30;
    JSONObject anotherQueryVariable = new JSONObject();
    anotherQueryVariable.put("name", anotherVariableName);
    anotherQueryVariable.put("operator", "neq");
    anotherQueryVariable.put("value", anotherVariableValue);
    
    JSONObject json = new JSONObject();
    JSONArray queryVariables = new JSONArray();
    queryVariables.put(queryVariable);
    queryVariables.put(anotherQueryVariable);
    
    json.put("taskVariables", queryVariables);
    
    String body = json.toString();
    
    given().contentType(POST_JSON_CONTENT_TYPE).body(body)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(TASK_QUERY_URL);
    
    verify(mockQuery).taskVariableValueEquals(variableName, variableValue);
    verify(mockQuery).taskVariableValueNotEquals(anotherVariableName, anotherVariableValue);
    
  }
  
  @Test
  public void testCompletePostParameters() {
    setUpMockedQuery();
    
    Map<String, Object> queryParameters = new HashMap<String, Object>();
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();
    Map<String, Integer> intQueryParameters = getCompleteIntQueryParameters();
    
    queryParameters.putAll(stringQueryParameters);
    queryParameters.putAll(intQueryParameters);
    List<String> candidateGroups = new ArrayList<String>();
    candidateGroups.add("boss");
    candidateGroups.add("worker");
    
    queryParameters.put("candidateGroups", candidateGroups);
    
    given().contentType(POST_JSON_CONTENT_TYPE).body(queryParameters)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().post(TASK_QUERY_URL);
    
    verifyStringParameterQueryInvocations();
    verifyIntegerParameterQueryInvocations();
    verify(mockQuery).taskCandidateGroupIn(argThat(new EqualsList(candidateGroups)));
  }
  
}
