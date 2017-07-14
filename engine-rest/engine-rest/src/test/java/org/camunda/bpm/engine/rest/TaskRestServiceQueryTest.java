package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static junit.framework.TestCase.assertEquals;
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.withTimezone;
import static org.camunda.bpm.engine.rest.util.QueryParamUtils.arrayAsCommaSeperatedList;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.hal.Hal;
import org.camunda.bpm.engine.rest.helper.EqualsList;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.ValueGenerator;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.camunda.bpm.engine.rest.util.OrderingBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class TaskRestServiceQueryTest extends AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String TASK_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/task";
  protected static final String TASK_COUNT_QUERY_URL = TASK_QUERY_URL + "/count";
  private TaskQuery mockQuery;

  @Before
  public void setUpRuntimeData() {
    mockQuery = setUpMockTaskQuery(MockProvider.createMockTasks());
  }

  private TaskQuery setUpMockTaskQuery(List<Task> mockedTasks) {
    TaskQuery sampleTaskQuery = mock(TaskQueryImpl.class);
    when(sampleTaskQuery.list()).thenReturn(mockedTasks);
    when(sampleTaskQuery.count()).thenReturn((long) mockedTasks.size());
    when(sampleTaskQuery.taskCandidateGroup(anyString())).thenReturn(sampleTaskQuery);

    when(processEngine.getTaskService().createTaskQuery()).thenReturn(sampleTaskQuery);

    return sampleTaskQuery;
  }

  @Test
  public void testEmptyQuery() {
    String queryKey = "";
    given().queryParam("name", queryKey)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }

  @Test
  public void testInvalidDateParameter() {
    given().queryParams("due", "anInvalidDate")
      .header("accept", MediaType.APPLICATION_JSON)
      .expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot set query parameter 'due' to value 'anInvalidDate': "
          + "Cannot convert value \"anInvalidDate\" to java type java.util.Date"))
      .when().get(TASK_QUERY_URL);
  }

  @Test
  public void testSortByParameterOnly() {
    given().queryParam("sortBy", "dueDate")
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when().get(TASK_QUERY_URL);
  }

  @Test
  public void testSortOrderParameterOnly() {
    given().queryParam("sortOrder", "asc")
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
      .when().get(TASK_QUERY_URL);
  }

  @Test
  public void testSimpleTaskQuery() {
    String queryName = "name";

    Response response = given().queryParam("name", queryName)
      .header("accept", MediaType.APPLICATION_JSON)
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
    String returnedFollowUpDate = from(content).getString("[0].followUp");
    String returnedDelegationState = from(content).getString("[0].delegationState");
    String returnedDescription = from(content).getString("[0].description");
    String returnedExecutionId = from(content).getString("[0].executionId");
    String returnedOwner = from(content).getString("[0].owner");
    String returnedParentTaskId = from(content).getString("[0].parentTaskId");
    int returnedPriority = from(content).getInt("[0].priority");
    String returnedProcessDefinitionId = from(content).getString("[0].processDefinitionId");
    String returnedProcessInstanceId = from(content).getString("[0].processInstanceId");
    String returnedTaskDefinitionKey = from(content).getString("[0].taskDefinitionKey");
    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedCaseInstanceId = from(content).getString("[0].caseInstanceId");
    String returnedCaseExecutionId = from(content).getString("[0].caseExecutionId");
    boolean returnedSuspensionState = from(content).getBoolean("[0].suspended");
    String returnedFormKey = from(content).getString("[0].formKey");
    String returnedTenantId = from(content).getString("[0].tenantId");

    Assert.assertEquals(MockProvider.EXAMPLE_TASK_NAME, returnedTaskName);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_ID, returnedId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME, returendAssignee);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_CREATE_TIME, returnedCreateTime);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DUE_DATE, returnedDueDate);
    Assert.assertEquals(MockProvider.EXAMPLE_FOLLOW_UP_DATE, returnedFollowUpDate);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DELEGATION_STATE.toString(), returnedDelegationState);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DESCRIPTION, returnedDescription);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_EXECUTION_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_OWNER, returnedOwner);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_PARENT_TASK_ID, returnedParentTaskId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_PRIORITY, returnedPriority);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DEFINITION_KEY, returnedTaskDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_ID, returnedCaseDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_EXECUTION_ID, returnedCaseExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_SUSPENSION_STATE, returnedSuspensionState);
    Assert.assertEquals(MockProvider.EXAMPLE_FORM_KEY, returnedFormKey);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);

  }

  @Test
  public void testSimpleHalTaskQuery() {
    String queryName = "name";

    // setup user query mock
    List<User> mockUsers = MockProvider.createMockUsers();
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(sampleUserQuery.listPage(0, 1)).thenReturn(mockUsers);
    when(sampleUserQuery.userIdIn(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userIdIn(MockProvider.EXAMPLE_TASK_OWNER)).thenReturn(sampleUserQuery);
    when(sampleUserQuery.count()).thenReturn(1l);
    when(processEngine.getIdentityService().createUserQuery()).thenReturn(sampleUserQuery);

    // setup process definition query mock
    List<ProcessDefinition> mockDefinitions = MockProvider.createMockDefinitions();
    ProcessDefinitionQuery sampleProcessDefinitionQuery = mock(ProcessDefinitionQuery.class);
    when(sampleProcessDefinitionQuery.listPage(0, 1)).thenReturn(mockDefinitions);
    when(sampleProcessDefinitionQuery.processDefinitionIdIn(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenReturn(sampleProcessDefinitionQuery);
    when(sampleProcessDefinitionQuery.count()).thenReturn(1l);
    when(processEngine.getRepositoryService().createProcessDefinitionQuery()).thenReturn(sampleProcessDefinitionQuery);

    // setup case definition query mock
    List<CaseDefinition> mockCaseDefinitions = MockProvider.createMockCaseDefinitions();
    CaseDefinitionQuery sampleCaseDefinitionQuery = mock(CaseDefinitionQuery.class);
    when(sampleCaseDefinitionQuery.listPage(0, 1)).thenReturn(mockCaseDefinitions);
    when(sampleCaseDefinitionQuery.caseDefinitionIdIn(MockProvider.EXAMPLE_CASE_DEFINITION_ID)).thenReturn(sampleCaseDefinitionQuery);
    when(sampleCaseDefinitionQuery.count()).thenReturn(1l);
    when(processEngine.getRepositoryService().createCaseDefinitionQuery()).thenReturn(sampleCaseDefinitionQuery);

    // setup example process application context path
    when(processEngine.getManagementService().getProcessApplicationForDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID))
      .thenReturn(MockProvider.EXAMPLE_PROCESS_APPLICATION_NAME);

    // replace the runtime container delegate & process application service with a mock
    ProcessApplicationService processApplicationService = mock(ProcessApplicationService.class);
    ProcessApplicationInfo appMock = MockProvider.createMockProcessApplicationInfo();
    when(processApplicationService.getProcessApplicationInfo(MockProvider.EXAMPLE_PROCESS_APPLICATION_NAME)).thenReturn(appMock);

    RuntimeContainerDelegate delegate = mock(RuntimeContainerDelegate.class);
    when(delegate.getProcessApplicationService()).thenReturn(processApplicationService);
    RuntimeContainerDelegate.INSTANCE.set(delegate);

    Response response = given().queryParam("name", queryName)
      .header("accept", Hal.APPLICATION_HAL_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .contentType(Hal.APPLICATION_HAL_JSON)
      .when().get(TASK_QUERY_URL);

    InOrder inOrder = inOrder(mockQuery);
    inOrder.verify(mockQuery).taskName(queryName);
    inOrder.verify(mockQuery).list();

    // validate embedded tasks
    String content = response.asString();
    List<Map<String,Object>> instances = from(content).getList("_embedded.task");
    Assert.assertEquals("There should be one task returned.", 1, instances.size());
    Assert.assertNotNull("The returned task should not be null.", instances.get(0));

    Map<String, Object> taskObject = instances.get(0);

    String returnedTaskName = (String) taskObject.get("name");
    String returnedId = (String) taskObject.get("id");
    String returnedAssignee = (String) taskObject.get("assignee");
    String returnedCreateTime = (String) taskObject.get("created");
    String returnedDueDate = (String) taskObject.get("due");
    String returnedFollowUpDate = (String) taskObject.get("followUp");
    String returnedDelegationState = (String) taskObject.get("delegationState");
    String returnedDescription = (String) taskObject.get("description");
    String returnedExecutionId = (String) taskObject.get("executionId");
    String returnedOwner = (String) taskObject.get("owner");
    String returnedParentTaskId = (String) taskObject.get("parentTaskId");
    int returnedPriority = (Integer) taskObject.get("priority");
    String returnedProcessDefinitionId = (String) taskObject.get("processDefinitionId");
    String returnedProcessInstanceId = (String) taskObject.get("processInstanceId");
    String returnedTaskDefinitionKey = (String) taskObject.get("taskDefinitionKey");
    String returnedCaseDefinitionId = (String) taskObject.get("caseDefinitionId");
    String returnedCaseInstanceId = (String) taskObject.get("caseInstanceId");
    String returnedCaseExecutionId = (String) taskObject.get("caseExecutionId");
    boolean returnedSuspensionState = (Boolean) taskObject.get("suspended");
    String returnedFormKey = (String) taskObject.get("formKey");
    String returnedTenantId = (String) taskObject.get("tenantId");

    Assert.assertEquals(MockProvider.EXAMPLE_TASK_NAME, returnedTaskName);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_ID, returnedId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME, returnedAssignee);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_CREATE_TIME, returnedCreateTime);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DUE_DATE, returnedDueDate);
    Assert.assertEquals(MockProvider.EXAMPLE_FOLLOW_UP_DATE, returnedFollowUpDate);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DELEGATION_STATE.toString(), returnedDelegationState);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DESCRIPTION, returnedDescription);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_EXECUTION_ID, returnedExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_OWNER, returnedOwner);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_PARENT_TASK_ID, returnedParentTaskId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_PRIORITY, returnedPriority);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, returnedProcessDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID, returnedProcessInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_DEFINITION_KEY, returnedTaskDefinitionKey);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_ID, returnedCaseDefinitionId);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_EXECUTION_ID, returnedCaseExecutionId);
    Assert.assertEquals(MockProvider.EXAMPLE_TASK_SUSPENSION_STATE, returnedSuspensionState);
    Assert.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);

    // validate the task count
    Assert.assertEquals(1l, from(content).getLong("count"));

    // validate links
    Map<String,Object> selfReference = from(content).getMap("_links.self");
    Assert.assertNotNull(selfReference);
    Assert.assertEquals("/task", selfReference.get("href"));

    // validate embedded assignees:
    List<Map<String,Object>> embeddedAssignees = from(content).getList("_embedded.assignee");
    Assert.assertEquals("There should be one assignee returned.", 1, embeddedAssignees.size());
    Map<String, Object> embeddedAssignee = embeddedAssignees.get(0);
    Assert.assertNotNull("The returned assignee should not be null.", embeddedAssignee);
    Assert.assertEquals(MockProvider.EXAMPLE_USER_ID, embeddedAssignee.get("id"));
    Assert.assertEquals(MockProvider.EXAMPLE_USER_FIRST_NAME, embeddedAssignee.get("firstName"));
    Assert.assertEquals(MockProvider.EXAMPLE_USER_LAST_NAME, embeddedAssignee.get("lastName"));
    Assert.assertEquals(MockProvider.EXAMPLE_USER_EMAIL, embeddedAssignee.get("email"));

    // validate embedded owners:
    List<Map<String,Object>> embeddedOwners = from(content).getList("_embedded.owner");
    Assert.assertEquals("There should be one owner returned.", 1, embeddedOwners.size());
    Map<String, Object> embeddedOwner = embeddedOwners.get(0);
    Assert.assertNotNull("The returned owner should not be null.", embeddedOwner);
    Assert.assertEquals(MockProvider.EXAMPLE_USER_ID, embeddedOwner.get("id"));
    Assert.assertEquals(MockProvider.EXAMPLE_USER_FIRST_NAME, embeddedOwner.get("firstName"));
    Assert.assertEquals(MockProvider.EXAMPLE_USER_LAST_NAME, embeddedOwner.get("lastName"));
    Assert.assertEquals(MockProvider.EXAMPLE_USER_EMAIL, embeddedOwner.get("email"));

    // validate embedded processDefinitions:
    List<Map<String,Object>> embeddedDefinitions = from(content).getList("_embedded.processDefinition");
    Assert.assertEquals("There should be one processDefinition returned.", 1, embeddedDefinitions.size());
    Map<String, Object> embeddedProcessDefinition = embeddedDefinitions.get(0);
    Assert.assertNotNull("The returned processDefinition should not be null.", embeddedProcessDefinition);
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID, embeddedProcessDefinition.get("id"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY, embeddedProcessDefinition.get("key"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_CATEGORY, embeddedProcessDefinition.get("category"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_NAME, embeddedProcessDefinition.get("name"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_DESCRIPTION, embeddedProcessDefinition.get("description"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_VERSION, embeddedProcessDefinition.get("version"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME, embeddedProcessDefinition.get("resource"));
    Assert.assertEquals(MockProvider.EXAMPLE_DEPLOYMENT_ID, embeddedProcessDefinition.get("deploymentId"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME, embeddedProcessDefinition.get("diagram"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED, embeddedProcessDefinition.get("suspended"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH, embeddedProcessDefinition.get("contextPath"));

    // validate embedded caseDefinitions:
    List<Map<String,Object>> embeddedCaseDefinitions = from(content).getList("_embedded.caseDefinition");
    Assert.assertEquals("There should be one caseDefinition returned.", 1, embeddedCaseDefinitions.size());
    Map<String, Object> embeddedCaseDefinition = embeddedCaseDefinitions.get(0);
    Assert.assertNotNull("The returned caseDefinition should not be null.", embeddedCaseDefinition);
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_ID, embeddedCaseDefinition.get("id"));
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_KEY, embeddedCaseDefinition.get("key"));
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_CATEGORY, embeddedCaseDefinition.get("category"));
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_NAME, embeddedCaseDefinition.get("name"));
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_VERSION, embeddedCaseDefinition.get("version"));
    Assert.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_RESOURCE_NAME, embeddedCaseDefinition.get("resource"));
    Assert.assertEquals(MockProvider.EXAMPLE_DEPLOYMENT_ID, embeddedCaseDefinition.get("deploymentId"));
    Assert.assertEquals(MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH, embeddedCaseDefinition.get("contextPath"));
  }

  @Test
  public void testNoParametersQuery() {
    given()
      .header("accept", MediaType.APPLICATION_JSON)
    .expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);

    verify(mockQuery).initializeFormKeys();
    verify(mockQuery).list();
    verifyNoMoreInteractions(mockQuery);
  }

  @Test
  public void testAdditionalParametersExcludingVariables() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();
    Map<String, Integer> intQueryParameters = getCompleteIntQueryParameters();
    Map<String, Boolean> booleanQueryParameters = getCompleteBooleanQueryParameters();

    Map<String, String[]> arrayQueryParameters = getCompleteStringArrayQueryParameters();

    given()
      .queryParams(stringQueryParameters)
      .queryParams(intQueryParameters)
      .queryParams(booleanQueryParameters)
      .queryParam("activityInstanceIdIn", arrayAsCommaSeperatedList(arrayQueryParameters.get("activityInstanceIdIn")))
      .queryParam("taskDefinitionKeyIn", arrayAsCommaSeperatedList(arrayQueryParameters.get("taskDefinitionKeyIn")))
      .queryParam("processDefinitionKeyIn", arrayAsCommaSeperatedList(arrayQueryParameters.get("processDefinitionKeyIn")))
      .queryParam("processInstanceBusinessKeyIn", arrayAsCommaSeperatedList(arrayQueryParameters.get("processInstanceBusinessKeyIn")))
      .queryParam("tenantIdIn", arrayAsCommaSeperatedList(arrayQueryParameters.get("tenantIdIn")))
      .header("accept", MediaType.APPLICATION_JSON)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);

    verifyIntegerParameterQueryInvocations();
    verifyStringParameterQueryInvocations();
    verifyBooleanParameterQueryInvocation();
    verifyStringArrayParametersInvocations();

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

  private Map<String, String[]> getCompleteStringArrayQueryParameters() {
    Map<String, String[]> parameters = new HashMap<String, String[]>();

    String[] activityInstanceIds = { "anActivityInstanceId", "anotherActivityInstanceId" };
    String[] taskDefinitionKeys = { "aTaskDefinitionKey", "anotherTaskDefinitionKey" };
    String[] processDefinitionKeys = { "aProcessDefinitionKey", "anotherProcessDefinitionKey" };
    String[] processInstanceBusinessKeys = { "aBusinessKey", "anotherBusinessKey" };
    String[] tenantIds = { MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID };


    parameters.put("activityInstanceIdIn", activityInstanceIds);
    parameters.put("taskDefinitionKeyIn", taskDefinitionKeys);
    parameters.put("processDefinitionKeyIn", processDefinitionKeys);
    parameters.put("processInstanceBusinessKeyIn", processInstanceBusinessKeys);
    parameters.put("tenantIdIn", tenantIds);

    return parameters;
  }

  private Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put("processInstanceBusinessKey", "aBusinessKey");
    parameters.put("processInstanceBusinessKeyLike", "aBusinessKeyLike");
    parameters.put("processDefinitionKey", "aProcDefKey");
    parameters.put("processDefinitionId", "aProcDefId");
    parameters.put("executionId", "anExecId");
    parameters.put("processDefinitionName", "aProcDefName");
    parameters.put("processDefinitionNameLike", "aProcDefNameLike");
    parameters.put("processInstanceId", "aProcInstId");
    parameters.put("assignee", "anAssignee");
    parameters.put("assigneeLike", "anAssigneeLike");
    parameters.put("candidateGroup", "aCandidateGroup");
    parameters.put("candidateUser", "aCandidate");
    parameters.put("includeAssignedTasks", "false");
    parameters.put("taskDefinitionKey", "aTaskDefKey");
    parameters.put("taskDefinitionKeyLike", "aTaskDefKeyLike");
    parameters.put("description", "aDesc");
    parameters.put("descriptionLike", "aDescLike");
    parameters.put("involvedUser", "anInvolvedPerson");
    parameters.put("name", "aName");
    parameters.put("nameNotEqual", "aNameNotEqual");
    parameters.put("nameLike", "aNameLike");
    parameters.put("nameNotLike", "aNameNotLike");
    parameters.put("owner", "anOwner");
    parameters.put("caseDefinitionKey", "aCaseDefKey");
    parameters.put("caseDefinitionId", "aCaseDefId");
    parameters.put("caseDefinitionName", "aCaseDefName");
    parameters.put("caseDefinitionNameLike", "aCaseDefNameLike");
    parameters.put("caseInstanceId", "anCaseInstanceId");
    parameters.put("caseInstanceBusinessKey", "aCaseInstanceBusinessKey");
    parameters.put("caseInstanceBusinessKeyLike", "aCaseInstanceBusinessKeyLike");
    parameters.put("caseExecutionId", "aCaseExecutionId");
    parameters.put("parentTaskId", "aParentTaskId");

    return parameters;
  }

  private Map<String, Boolean> getCompleteBooleanQueryParameters() {
    Map<String, Boolean> parameters = new HashMap<String, Boolean>();

    parameters.put("assigned", true);
    parameters.put("unassigned", true);
    parameters.put("active", true);
    parameters.put("suspended", true);
    parameters.put("withoutTenantId", true);
    parameters.put("withCandidateGroups", true);
    parameters.put("withoutCandidateGroups", true);
    parameters.put("withCandidateUsers", true);
    parameters.put("withoutCandidateUsers", true);

    return parameters;
  }

  private void verifyStringParameterQueryInvocations() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    verify(mockQuery).processInstanceBusinessKey(stringQueryParameters.get("processInstanceBusinessKey"));
    verify(mockQuery).processInstanceBusinessKeyLike(stringQueryParameters.get("processInstanceBusinessKeyLike"));
    verify(mockQuery).processDefinitionKey(stringQueryParameters.get("processDefinitionKey"));
    verify(mockQuery).processDefinitionId(stringQueryParameters.get("processDefinitionId"));
    verify(mockQuery).executionId(stringQueryParameters.get("executionId"));
    verify(mockQuery).processDefinitionName(stringQueryParameters.get("processDefinitionName"));
    verify(mockQuery).processDefinitionNameLike(stringQueryParameters.get("processDefinitionNameLike"));
    verify(mockQuery).processInstanceId(stringQueryParameters.get("processInstanceId"));
    verify(mockQuery).taskAssignee(stringQueryParameters.get("assignee"));
    verify(mockQuery).taskAssigneeLike(stringQueryParameters.get("assigneeLike"));
    verify(mockQuery).taskCandidateGroup(stringQueryParameters.get("candidateGroup"));
    verify(mockQuery).taskCandidateUser(stringQueryParameters.get("candidateUser"));
    verify(mockQuery).taskDefinitionKey(stringQueryParameters.get("taskDefinitionKey"));
    verify(mockQuery).taskDefinitionKeyLike(stringQueryParameters.get("taskDefinitionKeyLike"));
    verify(mockQuery).taskDescription(stringQueryParameters.get("description"));
    verify(mockQuery).taskDescriptionLike(stringQueryParameters.get("descriptionLike"));
    verify(mockQuery).taskInvolvedUser(stringQueryParameters.get("involvedUser"));
    verify(mockQuery).taskName(stringQueryParameters.get("name"));
    verify(mockQuery).taskNameNotEqual(stringQueryParameters.get("nameNotEqual"));
    verify(mockQuery).taskNameLike(stringQueryParameters.get("nameLike"));
    verify(mockQuery).taskNameNotLike(stringQueryParameters.get("nameNotLike"));
    verify(mockQuery).taskOwner(stringQueryParameters.get("owner"));
    verify(mockQuery).caseDefinitionKey(stringQueryParameters.get("caseDefinitionKey"));
    verify(mockQuery).caseDefinitionId(stringQueryParameters.get("caseDefinitionId"));
    verify(mockQuery).caseDefinitionName(stringQueryParameters.get("caseDefinitionName"));
    verify(mockQuery).caseDefinitionNameLike(stringQueryParameters.get("caseDefinitionNameLike"));
    verify(mockQuery).caseInstanceId(stringQueryParameters.get("caseInstanceId"));
    verify(mockQuery).caseInstanceBusinessKey(stringQueryParameters.get("caseInstanceBusinessKey"));
    verify(mockQuery).caseInstanceBusinessKeyLike(stringQueryParameters.get("caseInstanceBusinessKeyLike"));
    verify(mockQuery).caseExecutionId(stringQueryParameters.get("caseExecutionId"));
    verify(mockQuery).taskParentTaskId(stringQueryParameters.get("parentTaskId"));

  }

  private void verifyStringArrayParametersInvocations() {
    Map<String, String[]> stringArrayParameters = getCompleteStringArrayQueryParameters();

    verify(mockQuery).activityInstanceIdIn(stringArrayParameters.get("activityInstanceIdIn"));
    verify(mockQuery).taskDefinitionKeyIn(stringArrayParameters.get("taskDefinitionKeyIn"));
    verify(mockQuery).processDefinitionKeyIn(stringArrayParameters.get("processDefinitionKeyIn"));
    verify(mockQuery).processInstanceBusinessKeyIn(stringArrayParameters.get("processInstanceBusinessKeyIn"));
    verify(mockQuery).tenantIdIn(stringArrayParameters.get("tenantIdIn"));
  }

  private void verifyBooleanParameterQueryInvocation() {
    verify(mockQuery).taskUnassigned();
    verify(mockQuery).active();
    verify(mockQuery).suspended();
    verify(mockQuery).withoutTenantId();
    verify(mockQuery).withCandidateGroups();
    verify(mockQuery).withoutCandidateGroups();
    verify(mockQuery).withCandidateUsers();
    verify(mockQuery).withoutCandidateUsers();
  }

  @Test
  public void testDateParameters() {
    Map<String, String> queryParameters = getDateParameters();

    given().queryParams(queryParameters)
      .header("accept", MediaType.APPLICATION_JSON)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);

    verify(mockQuery).dueAfter(any(Date.class));
    verify(mockQuery).dueBefore(any(Date.class));
    verify(mockQuery).dueDate(any(Date.class));
    verify(mockQuery).followUpAfter(any(Date.class));
    verify(mockQuery).followUpBefore(any(Date.class));
    verify(mockQuery).followUpBeforeOrNotExistent(any(Date.class));
    verify(mockQuery).followUpDate(any(Date.class));
    verify(mockQuery).taskCreatedAfter(any(Date.class));
    verify(mockQuery).taskCreatedBefore(any(Date.class));
    verify(mockQuery).taskCreatedOn(any(Date.class));
  }

  @Test
  public void testDateParametersPost() {
    Map<String, String> json = getDateParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(TASK_QUERY_URL);

    verify(mockQuery).dueAfter(any(Date.class));
    verify(mockQuery).dueBefore(any(Date.class));
    verify(mockQuery).dueDate(any(Date.class));
    verify(mockQuery).followUpAfter(any(Date.class));
    verify(mockQuery).followUpBefore(any(Date.class));
    verify(mockQuery).followUpBeforeOrNotExistent(any(Date.class));
    verify(mockQuery).followUpDate(any(Date.class));
    verify(mockQuery).taskCreatedAfter(any(Date.class));
    verify(mockQuery).taskCreatedBefore(any(Date.class));
    verify(mockQuery).taskCreatedOn(any(Date.class));
  }

  @Test
  public void testDeprecatedDateParameters() {
    Map<String, String> queryParameters = new HashMap<String, String>();
    queryParameters.put("due", withTimezone("2013-01-23T14:42:44"));
    queryParameters.put("created", withTimezone("2013-01-23T14:42:47"));
    queryParameters.put("followUp", withTimezone("2013-01-23T14:42:50"));

    given()
      .queryParams(queryParameters)
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(TASK_QUERY_URL);

    verify(mockQuery).dueDate(any(Date.class));
    verify(mockQuery).taskCreatedOn(any(Date.class));
    verify(mockQuery).followUpDate(any(Date.class));
  }

  private Map<String, String> getDateParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("dueAfter", withTimezone("2013-01-23T14:42:42"));
    parameters.put("dueBefore", withTimezone("2013-01-23T14:42:43"));
    parameters.put("dueDate", withTimezone("2013-01-23T14:42:44"));
    parameters.put("createdAfter", withTimezone("2013-01-23T14:42:45"));
    parameters.put("createdBefore", withTimezone("2013-01-23T14:42:46"));
    parameters.put("createdOn", withTimezone("2013-01-23T14:42:47"));
    parameters.put("followUpAfter", withTimezone("2013-01-23T14:42:48"));
    parameters.put("followUpBefore", withTimezone("2013-01-23T14:42:49"));
    parameters.put("followUpBeforeOrNotExistent", withTimezone("2013-01-23T14:42:49"));
    parameters.put("followUpDate", withTimezone("2013-01-23T14:42:50"));
    return parameters;
  }

  @Test
  public void testCandidateGroupInList() {
    List<String> candidateGroups = new ArrayList<String>();
    candidateGroups.add("boss");
    candidateGroups.add("worker");
    String queryParam = candidateGroups.get(0) + "," + candidateGroups.get(1);

    given().queryParams("candidateGroups", queryParam)
      .header("accept", MediaType.APPLICATION_JSON)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);

    verify(mockQuery).taskCandidateGroupIn(argThat(new EqualsList(candidateGroups)));
  }

  @Test
  public void testDelegationState() {
    given().queryParams("delegationState", "PENDING")
      .header("accept", MediaType.APPLICATION_JSON)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);

    verify(mockQuery).taskDelegationState(DelegationState.PENDING);

    given().queryParams("delegationState", "RESOLVED")
    .header("accept", MediaType.APPLICATION_JSON)
    .expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);

    verify(mockQuery).taskDelegationState(DelegationState.RESOLVED);
  }

  @Test
  public void testLowerCaseDelegationStateParam() {
    given().queryParams("delegationState", "resolved")
    .header("accept", MediaType.APPLICATION_JSON)
    .expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);

    verify(mockQuery).taskDelegationState(DelegationState.RESOLVED);
  }

  @Test
  public void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("dueDate", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByDueDate();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("followUpDate", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByFollowUpDate();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("instanceId", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByProcessInstanceId();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("created", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskCreateTime();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("id", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskId();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("priority", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskPriority();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("executionId", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByExecutionId();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("assignee", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskAssignee();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("description", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskDescription();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("name", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskName();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("nameCaseInsensitive", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskNameCaseInsensitive();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("caseInstanceId", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByCaseInstanceId();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("dueDate", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByDueDate();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("followUpDate", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByFollowUpDate();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("instanceId", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByProcessInstanceId();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("created", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskCreateTime();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("id", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskId();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("priority", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskPriority();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("executionId", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByExecutionId();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("assignee", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskAssignee();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("description", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskDescription();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("name", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskName();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("nameCaseInsensitive", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTaskNameCaseInsensitive();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("caseInstanceId", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByCaseInstanceId();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("tenantId", "desc", Status.OK);
    inOrder.verify(mockQuery).orderByTenantId();
    inOrder.verify(mockQuery).desc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockQuery).orderByTenantId();
    inOrder.verify(mockQuery).asc();

  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given().queryParam("sortBy", sortBy).queryParam("sortOrder", sortOrder)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(expectedStatus.getStatusCode())
      .when().get(TASK_QUERY_URL);
  }

  protected void executeAndVerifySortingAsPost(List<Map<String, Object>> sortingJson, Status expectedStatus) {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("sorting", sortingJson);

    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(TASK_QUERY_URL);
  }

  @Test
  public void testSecondarySortingAsPost() {
    InOrder inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySortingAsPost(
      OrderingBuilder.create()
        .orderBy("dueDate").desc()
        .orderBy("caseExecutionId").asc()
        .getJson(),
      Status.OK);

    inOrder.verify(mockQuery).orderByDueDate();
    inOrder.verify(mockQuery).desc();
    inOrder.verify(mockQuery).orderByCaseExecutionId();
    inOrder.verify(mockQuery).asc();

    inOrder = Mockito.inOrder(mockQuery);
    executeAndVerifySortingAsPost(
      OrderingBuilder.create()
        .orderBy("processVariable").desc()
          .parameter("variable", "var")
          .parameter("type", "String")
        .orderBy("executionVariable").asc()
          .parameter("variable", "var2")
          .parameter("type", "Integer")
        .orderBy("taskVariable").desc()
          .parameter("variable", "var3")
          .parameter("type", "Double")
        .orderBy("caseInstanceVariable").asc()
          .parameter("variable", "var4")
          .parameter("type", "Long")
        .orderBy("caseExecutionVariable").desc()
          .parameter("variable", "var5")
          .parameter("type", "Date")
        .getJson(),
      Status.OK);

    inOrder.verify(mockQuery).orderByProcessVariable("var", ValueType.STRING);
    inOrder.verify(mockQuery).desc();
    inOrder.verify(mockQuery).orderByExecutionVariable("var2", ValueType.INTEGER);
    inOrder.verify(mockQuery).asc();
    inOrder.verify(mockQuery).orderByTaskVariable("var3", ValueType.DOUBLE);
    inOrder.verify(mockQuery).desc();
    inOrder.verify(mockQuery).orderByCaseInstanceVariable("var4", ValueType.LONG);
    inOrder.verify(mockQuery).asc();
    inOrder.verify(mockQuery).orderByCaseExecutionVariable("var5", ValueType.DATE);
    inOrder.verify(mockQuery).desc();
  }

  @Test
  public void testSuccessfulPagination() {

    int firstResult = 0;
    int maxResults = 10;
    given().queryParam("firstResult", firstResult).queryParam("maxResults", maxResults)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);

    verify(mockQuery).listPage(firstResult, maxResults);
  }

  @Test
  public void testTaskVariableParameters() {
    // equals
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;

    given()
      .queryParam("taskVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).taskVariableValueEquals(variableName, variableValue);

    // greater then
    queryValue = variableName + "_gt_" + variableValue;

    given()
      .queryParam("taskVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).taskVariableValueGreaterThan(variableName, variableValue);

    // greater then equals
    queryValue = variableName + "_gteq_" + variableValue;

    given()
      .queryParam("taskVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).taskVariableValueGreaterThanOrEquals(variableName, variableValue);

    // lower then
    queryValue = variableName + "_lt_" + variableValue;

    given()
      .queryParam("taskVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).taskVariableValueLessThan(variableName, variableValue);

    // lower then equals
    queryValue = variableName + "_lteq_" + variableValue;

    given()
      .queryParam("taskVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).taskVariableValueLessThanOrEquals(variableName, variableValue);

    // like
    queryValue = variableName + "_like_" + variableValue;

    given()
      .queryParam("taskVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).taskVariableValueLike(variableName, variableValue);

    // not equals
    queryValue = variableName + "_neq_" + variableValue;

    given()
      .queryParam("taskVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).taskVariableValueNotEquals(variableName, variableValue);
  }

  @Test
  public void testProcessVariableParameters() {
    // equals
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;

    given()
      .queryParam("processVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).processVariableValueEquals(variableName, variableValue);

    // greater then
    queryValue = variableName + "_gt_" + variableValue;

    given()
      .queryParam("processVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).processVariableValueGreaterThan(variableName, variableValue);

    // greater then equals
    queryValue = variableName + "_gteq_" + variableValue;

    given()
      .queryParam("processVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).processVariableValueGreaterThanOrEquals(variableName, variableValue);

    // lower then
    queryValue = variableName + "_lt_" + variableValue;

    given()
      .queryParam("processVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).processVariableValueLessThan(variableName, variableValue);

    // lower then equals
    queryValue = variableName + "_lteq_" + variableValue;

    given()
      .queryParam("processVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).processVariableValueLessThanOrEquals(variableName, variableValue);

    // like
    queryValue = variableName + "_like_" + variableValue;

    given()
      .queryParam("processVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).processVariableValueLike(variableName, variableValue);

    // not equals
    queryValue = variableName + "_neq_" + variableValue;

    given()
      .queryParam("processVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).processVariableValueNotEquals(variableName, variableValue);
  }

  @Test
  public void testCaseVariableParameters() {
    // equals
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).caseInstanceVariableValueEquals(variableName, variableValue);

    // greater then
    queryValue = variableName + "_gt_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).caseInstanceVariableValueGreaterThan(variableName, variableValue);

    // greater then equals
    queryValue = variableName + "_gteq_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).caseInstanceVariableValueGreaterThanOrEquals(variableName, variableValue);

    // lower then
    queryValue = variableName + "_lt_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).caseInstanceVariableValueLessThan(variableName, variableValue);

    // lower then equals
    queryValue = variableName + "_lteq_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).caseInstanceVariableValueLessThanOrEquals(variableName, variableValue);

    // like
    queryValue = variableName + "_like_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).caseInstanceVariableValueLike(variableName, variableValue);

    // not equals
    queryValue = variableName + "_neq_" + variableValue;

    given()
      .queryParam("caseInstanceVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(TASK_QUERY_URL);

    verify(mockQuery).caseInstanceVariableValueNotEquals(variableName, variableValue);
  }

  @Test
  public void testMultipleVariableParameters() {
    String variableName1 = "varName";
    String variableValue1 = "varValue";
    String variableParameter1 = variableName1 + "_eq_" + variableValue1;

    String variableName2 = "anotherVarName";
    String variableValue2 = "anotherVarValue";
    String variableParameter2 = variableName2 + "_neq_" + variableValue2;

    String queryValue = variableParameter1 + "," + variableParameter2;

    given().queryParam("taskVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);

    verify(mockQuery).taskVariableValueEquals(variableName1, variableValue1);
    verify(mockQuery).taskVariableValueNotEquals(variableName2, variableValue2);
  }

  @Test
  public void testMultipleVariableParametersAsPost() {
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
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(TASK_QUERY_URL);

    verify(mockQuery).taskVariableValueEquals(variableName, variableValue);
    verify(mockQuery).taskVariableValueNotEquals(eq(anotherVariableName), argThat(EqualsPrimitiveValue.numberValue(anotherVariableValue)));

  }

  @Test
  public void testMultipleProcessVariableParameters() {
    String variableName1 = "varName";
    String variableValue1 = "varValue";
    String variableParameter1 = variableName1 + "_eq_" + variableValue1;

    String variableName2 = "anotherVarName";
    String variableValue2 = "anotherVarValue";
    String variableParameter2 = variableName2 + "_neq_" + variableValue2;

    String queryValue = variableParameter1 + "," + variableParameter2;

    given().queryParam("processVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);

    verify(mockQuery).processVariableValueEquals(variableName1, variableValue1);
    verify(mockQuery).processVariableValueNotEquals(variableName2, variableValue2);
  }

  @Test
  public void testMultipleProcessVariableParametersAsPost() {
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
    json.put("processVariables", variables);

    given()
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(TASK_QUERY_URL);

    verify(mockQuery).processVariableValueEquals(variableName, variableValue);
    verify(mockQuery).processVariableValueNotEquals(eq(anotherVariableName), argThat(EqualsPrimitiveValue.numberValue(anotherVariableValue)));
  }

  @Test
  public void testMultipleCaseVariableParameters() {
    String variableName1 = "varName";
    String variableValue1 = "varValue";
    String variableParameter1 = variableName1 + "_eq_" + variableValue1;

    String variableName2 = "anotherVarName";
    String variableValue2 = "anotherVarValue";
    String variableParameter2 = variableName2 + "_neq_" + variableValue2;

    String queryValue = variableParameter1 + "," + variableParameter2;

    given().queryParam("caseInstanceVariables", queryValue)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(TASK_QUERY_URL);

    verify(mockQuery).caseInstanceVariableValueEquals(variableName1, variableValue1);
    verify(mockQuery).caseInstanceVariableValueNotEquals(variableName2, variableValue2);
  }

  @Test
  public void testMultipleCaseVariableParametersAsPost() {
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
    json.put("caseInstanceVariables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header("accept", MediaType.APPLICATION_JSON)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(TASK_QUERY_URL);

    verify(mockQuery).caseInstanceVariableValueEquals(variableName, variableValue);
    verify(mockQuery).caseInstanceVariableValueNotEquals(eq(anotherVariableName), argThat(EqualsPrimitiveValue.numberValue(anotherVariableValue)));
  }

  @Test
  public void testCompletePostParameters() {

    Map<String, Object> queryParameters = new HashMap<String, Object>();
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();
    Map<String, Integer> intQueryParameters = getCompleteIntQueryParameters();
    Map<String, Boolean> booleanQueryParameters = getCompleteBooleanQueryParameters();
    Map<String, String[]> stringArrayQueryParameters = getCompleteStringArrayQueryParameters();

    queryParameters.putAll(stringQueryParameters);
    queryParameters.putAll(intQueryParameters);
    queryParameters.putAll(booleanQueryParameters);
    queryParameters.putAll(stringArrayQueryParameters);

    List<String> candidateGroups = new ArrayList<String>();
    candidateGroups.add("boss");
    candidateGroups.add("worker");

    queryParameters.put("candidateGroups", candidateGroups);

    queryParameters.put("includeAssignedTasks", true);

    given().contentType(POST_JSON_CONTENT_TYPE).body(queryParameters)
      .header("accept", MediaType.APPLICATION_JSON)
      .expect().statusCode(Status.OK.getStatusCode())
      .when().post(TASK_QUERY_URL);

    verifyStringParameterQueryInvocations();
    verifyIntegerParameterQueryInvocations();
    verifyStringArrayParametersInvocations();
    verifyBooleanParameterQueryInvocation();

    verify(mockQuery).includeAssignedTasks();
    verify(mockQuery).taskCandidateGroupIn(argThat(new EqualsList(candidateGroups)));
  }

  @Test
  public void testQueryCount() {
    given()
        .header("accept", MediaType.APPLICATION_JSON)
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when()
        .get(TASK_COUNT_QUERY_URL);

    verify(mockQuery).count();
  }

  @Test
  public void testQueryCountForPost() {
    given().contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .header("accept", MediaType.APPLICATION_JSON)
    .expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().post(TASK_COUNT_QUERY_URL);

    verify(mockQuery).count();
  }

  @Test
  public void testQueryWithExpressions() {
    String testExpression = "${'test-%s'}";

    ValueGenerator generator = new ValueGenerator(testExpression);

    Map<String, String> params = new HashMap<String, String>();
    params.put("assigneeExpression", generator.getValue("assigneeExpression"));
    params.put("assigneeLikeExpression", generator.getValue("assigneeLikeExpression"));
    params.put("ownerExpression", generator.getValue("ownerExpression"));
    params.put("involvedUserExpression", generator.getValue("involvedUserExpression"));
    params.put("candidateUserExpression", generator.getValue("candidateUserExpression"));
    params.put("candidateGroupExpression", generator.getValue("candidateGroupExpression"));
    params.put("candidateGroupsExpression", generator.getValue("candidateGroupsExpression"));
    params.put("createdBeforeExpression", generator.getValue("createdBeforeExpression"));
    params.put("createdOnExpression", generator.getValue("createdOnExpression"));
    params.put("createdAfterExpression", generator.getValue("createdAfterExpression"));
    params.put("dueBeforeExpression", generator.getValue("dueBeforeExpression"));
    params.put("dueDateExpression", generator.getValue("dueDateExpression"));
    params.put("dueAfterExpression", generator.getValue("dueAfterExpression"));
    params.put("followUpBeforeExpression", generator.getValue("followUpBeforeExpression"));
    params.put("followUpDateExpression", generator.getValue("followUpDateExpression"));
    params.put("followUpAfterExpression", generator.getValue("followUpAfterExpression"));
    params.put("processInstanceBusinessKeyExpression", generator.getValue("processInstanceBusinessKeyExpression"));
    params.put("processInstanceBusinessKeyLikeExpression", generator.getValue("processInstanceBusinessKeyLikeExpression"));

    // get
    given()
      .header(ACCEPT_JSON_HEADER)
      .queryParams(params)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(TASK_QUERY_URL);

    verifyExpressionMocks(generator);

    // reset mock
    reset(mockQuery);

    // post
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header(ACCEPT_JSON_HEADER)
      .body(params)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(TASK_QUERY_URL);

    verifyExpressionMocks(generator);

  }

  protected void verifyExpressionMocks(ValueGenerator generator) {
    verify(mockQuery).taskAssigneeExpression(generator.getValue("assigneeExpression"));
    verify(mockQuery).taskAssigneeLikeExpression(generator.getValue("assigneeLikeExpression"));
    verify(mockQuery).taskOwnerExpression(generator.getValue("ownerExpression"));
    verify(mockQuery).taskInvolvedUserExpression(generator.getValue("involvedUserExpression"));
    verify(mockQuery).taskCandidateUserExpression(generator.getValue("candidateUserExpression"));
    verify(mockQuery).taskCandidateGroupExpression(generator.getValue("candidateGroupExpression"));
    verify(mockQuery).taskCandidateGroupInExpression(generator.getValue("candidateGroupsExpression"));
    verify(mockQuery).taskCreatedBeforeExpression(generator.getValue("createdBeforeExpression"));
    verify(mockQuery).taskCreatedOnExpression(generator.getValue("createdOnExpression"));
    verify(mockQuery).taskCreatedAfterExpression(generator.getValue("createdAfterExpression"));
    verify(mockQuery).dueBeforeExpression(generator.getValue("dueBeforeExpression"));
    verify(mockQuery).dueDateExpression(generator.getValue("dueDateExpression"));
    verify(mockQuery).dueAfterExpression(generator.getValue("dueAfterExpression"));
    verify(mockQuery).followUpBeforeExpression(generator.getValue("followUpBeforeExpression"));
    verify(mockQuery).followUpDateExpression(generator.getValue("followUpDateExpression"));
    verify(mockQuery).followUpAfterExpression(generator.getValue("followUpAfterExpression"));
    verify(mockQuery).processInstanceBusinessKeyExpression(generator.getValue("processInstanceBusinessKeyExpression"));
    verify(mockQuery).processInstanceBusinessKeyLikeExpression(generator.getValue("processInstanceBusinessKeyLikeExpression"));

  }

  @Test
  public void testQueryWithCandidateUsers() {
    given().queryParam("withCandidateUsers", true)
    .accept(MediaType.APPLICATION_JSON)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);

    verify(mockQuery).withCandidateUsers();
  }

  @Test
  public void testQueryWithoutCandidateUsers() {
    given().queryParam("withoutCandidateUsers", true)
    .accept(MediaType.APPLICATION_JSON)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);

    verify(mockQuery).withoutCandidateUsers();
  }

  @Test
  public void testNeverQueryWithCandidateUsers() {
    given().queryParam("withCandidateUsers", false)
    .accept(MediaType.APPLICATION_JSON)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);

    verify(mockQuery, never()).withCandidateUsers();
  }

  @Test
  public void testNeverQueryWithoutCandidateUsers() {
    given().queryParam("withoutCandidateUsers", false)
    .accept(MediaType.APPLICATION_JSON)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);

    verify(mockQuery, never()).withoutCandidateUsers();
  }

  @Test
  public void testNeverQueryWithCandidateGroups() {
    given().queryParam("withCandidateGroups", false)
    .accept(MediaType.APPLICATION_JSON)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);

    verify(mockQuery, never()).withCandidateGroups();
  }

  @Test
  public void testNeverQueryWithoutCandidateGroups() {
    given().queryParam("withoutCandidateGroups", false)
    .accept(MediaType.APPLICATION_JSON)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(TASK_QUERY_URL);

    verify(mockQuery, never()).withoutCandidateGroups();
  }

  @Test
  public void testOrQuery() {
    TaskQueryDto queryDto = TaskQueryDto.fromQuery(new TaskQueryImpl()
      .or()
        .taskName(MockProvider.EXAMPLE_TASK_NAME)
        .taskDescription(MockProvider.EXAMPLE_TASK_DESCRIPTION)
      .endOr());

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .header(ACCEPT_JSON_HEADER)
      .body(queryDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(TASK_QUERY_URL);

    ArgumentCaptor<TaskQueryImpl> argument = ArgumentCaptor.forClass(TaskQueryImpl.class);
    verify(((TaskQueryImpl) mockQuery)).addOrQuery(argument.capture());
    assertEquals(MockProvider.EXAMPLE_TASK_NAME, argument.getValue().getName());
    assertEquals(MockProvider.EXAMPLE_TASK_DESCRIPTION, argument.getValue().getDescription());
  }

}
