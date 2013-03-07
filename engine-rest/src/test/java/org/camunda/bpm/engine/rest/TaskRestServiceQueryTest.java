package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.rest.helper.EqualsList;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.response.Response;

public class TaskRestServiceQueryTest extends AbstractRestServiceTest {

  private static final String TASK_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/task";
  private static final String TASK_GROUPS_URL = TASK_QUERY_URL + "/groups";
  private static final String TASK_COUNT_QUERY_URL = TASK_QUERY_URL + "/count";
  
  private TaskQuery mockQuery;
  
  private TaskQuery setUpMockTaskQuery(List<Task> mockedTasks) {
    UserQuery sampleUserQuery = mock(UserQuery.class);

    List<User> mockUsers = new ArrayList<User>();
    
    User mockUser = MockProvider.createMockUser();
    mockUsers.add(mockUser);

    when(sampleUserQuery.list()).thenReturn(mockUsers);
    when(sampleUserQuery.memberOfGroup(anyString())).thenReturn(sampleUserQuery);
    when(sampleUserQuery.count()).thenReturn((long) mockedTasks.size());

    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    
    List<Group> mockGroups = MockProvider.createMockGroups();
    when(sampleGroupQuery.list()).thenReturn(mockGroups);
    when(sampleGroupQuery.groupMember(anyString())).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.orderByGroupName()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.orderByGroupId()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.orderByGroupType()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.asc()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.desc()).thenReturn(sampleGroupQuery);

    when(processEngine.getIdentityService().createGroupQuery()).thenReturn(sampleGroupQuery);

    TaskQuery sampleTaskQuery = mock(TaskQuery.class);
    when(sampleTaskQuery.list()).thenReturn(mockedTasks);
    when(sampleTaskQuery.count()).thenReturn((long) mockedTasks.size());
    when(sampleTaskQuery.taskCandidateGroup(anyString())).thenReturn(sampleTaskQuery);

    when(processEngine.getTaskService().createTaskQuery()).thenReturn(sampleTaskQuery);
    when(processEngine.getIdentityService().createUserQuery()).thenReturn(sampleUserQuery);

    return sampleTaskQuery;
  }
  
  public void setUpMockedQuery() throws IOException {
    setupTestScenario();
    mockQuery = setUpMockTaskQuery(MockProvider.createMockTasks());
  }
  
  @Test
  public void testSimpleTaskQuery() throws IOException {
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
    
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_NAME, returnedTaskName);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_ID, returnedId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME, returendAssignee);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_CREATE_TIME, returnedCreateTime);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DUE_DATE, returnedDueDate);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DELEGATION_STATE.toString(), returnedDelegationState);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DESCRIPTION, returnedDescription);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_EXECUTION_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_OWNER, returnedOwner);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_PARENT_TASK_ID, returnedParentTaskId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_PRIORITY, returnedPriority);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DEFINITION_KEY, returnedTaskDefinitionKey);
    
  }

  @Test
  public void testEmptyQuery() throws IOException {
    setUpMockedQuery();
    String queryKey = "";
    given().queryParam("name", queryKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testNoParametersQuery() throws IOException {
    setUpMockedQuery();
    expect().statusCode(Status.OK.getStatusCode()).when().get(TASK_QUERY_URL);
    
    verify(mockQuery).list();
    verifyNoMoreInteractions(mockQuery);
  }
  
  @Test
  public void testAdditionalParametersExcludingVariables() throws IOException {
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
    parameters.put("candidateUser", "aCandidate");
    parameters.put("taskDefinitionKey", "aTaskDefKey");
    parameters.put("taskDefinitionKeyLike", "aTaskDefKeyLike");
    parameters.put("description", "aDesc");
    parameters.put("descriptionLike", "aDescLike");
    parameters.put("involvedUser", "anInvolvedPerson");
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
    verify(mockQuery).taskCandidateUser(stringQueryParameters.get("candidateUser"));
    verify(mockQuery).taskDefinitionKey(stringQueryParameters.get("taskDefinitionKey"));
    verify(mockQuery).taskDefinitionKeyLike(stringQueryParameters.get("taskDefinitionKeyLike"));
    verify(mockQuery).taskDescription(stringQueryParameters.get("description"));
    verify(mockQuery).taskDescriptionLike(stringQueryParameters.get("descriptionLike"));
    verify(mockQuery).taskInvolvedUser(stringQueryParameters.get("involvedUser"));
    verify(mockQuery).taskName(stringQueryParameters.get("name"));
    verify(mockQuery).taskNameLike(stringQueryParameters.get("nameLike"));
    verify(mockQuery).taskOwner(stringQueryParameters.get("owner"));
  }
  
  @Test
  public void testDateParameters() throws IOException {
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
  public void testCandidateGroupInList() throws IOException {
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
  public void testDelegationState() throws IOException {
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
  public void testLowerCaseDelegationStateParam() throws IOException {
    setUpMockedQuery();

    given().queryParams("delegationState", "resolved")
    .expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);
  
    verify(mockQuery).taskDelegationState(DelegationState.RESOLVED);
  }
  
  @Test
  public void testSortingParameters() throws IOException {
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
  public void testSortByParameterOnly() throws IOException {
    setUpMockedQuery();
    given().queryParam("sortBy", "dueDate")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testSortOrderParameterOnly() throws IOException {
    setUpMockedQuery();
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }
  
  @Test
  public void testSuccessfulPagination() throws IOException {
    setUpMockedQuery();
    
    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);
    
    verify(mockQuery).listPage(firstResult, maxResults);
  }
  
  @Test
  public void testTaskVariableParameters() throws IOException {
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
  public void testProcessVariableParameters() throws IOException {
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
  public void testMultipleVariableParametersAsPost() throws IOException {
    setUpMockedQuery();
    
    String variableName = "varName";
    String variableValue = "varValue";
    String anotherVariableName = "anotherVarName";
    Integer anotherVariableValue = 30;
    
    Map<String, Object> variableJson = new HashMap<String, Object>();
    variableJson.put("name", variableName);
    variableJson.put("operator", "eq");
    variableJson.put("value", variableValue);
    
    Map<String, Object> anotherVariableJson = new HashMap<String, Object>();
    anotherVariableJson.put("name", anotherVariableName);
    anotherVariableJson.put("operator", "neq");
    anotherVariableJson.put("value", anotherVariableValue);
    
    List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
    variables.add(variableJson);
    variables.add(anotherVariableJson);
    
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("taskVariables", variables);
    
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(TASK_QUERY_URL);
    
    verify(mockQuery).taskVariableValueEquals(variableName, variableValue);
    verify(mockQuery).taskVariableValueNotEquals(anotherVariableName, anotherVariableValue);
    
  }
  
  @Test
  public void testCompletePostParameters() throws IOException {
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
  
  @Test
  public void testQueryCount() throws IOException {
    setUpMockedQuery();
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(TASK_COUNT_QUERY_URL);
    
    verify(mockQuery).count();
  }
  
  @Test
  public void testQueryCountForPost() throws IOException {
    setUpMockedQuery();
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().post(TASK_COUNT_QUERY_URL);
    
    verify(mockQuery).count();
  }

  @Test
  public void testGroupInfoQuery() throws IOException {
    setUpMockedQuery();

    given().queryParam("userId", "name")
        .then().expect().statusCode(Status.OK.getStatusCode())
        .body("groups.size()", is(1))
        .body("groups[0].id", equalTo(MockProvider.EXAMPLE_GROUP_ID))
        .body("groups[0].name", equalTo(MockProvider.EXAMPLE_GROUP_NAME))
        .when().get(TASK_GROUPS_URL);
  }
  
  @Test
  public void testGroupInfoQueryWithMissingUserParameter() throws IOException {
    setUpMockedQuery();
    expect().statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(TASK_GROUPS_URL);
  }
  
}
