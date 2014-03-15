package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

public abstract class AbstractProcessEngineRestServiceTest extends
    AbstractRestServiceTest {

  protected static final String ENGINES_URL = TEST_RESOURCE_ROOT_PATH + "/engine";
  protected static final String SINGLE_ENGINE_URL = ENGINES_URL + "/{name}";
  protected static final String PROCESS_DEFINITION_URL = SINGLE_ENGINE_URL + "/process-definition/{id}";
  protected static final String PROCESS_INSTANCE_URL = SINGLE_ENGINE_URL + "/process-instance/{id}";
  protected static final String TASK_URL = SINGLE_ENGINE_URL + "/task/{id}";
  protected static final String IDENTITY_GROUPS_URL = SINGLE_ENGINE_URL + "/identity/groups";
  protected static final String MESSAGE_URL = SINGLE_ENGINE_URL + "/message";
  protected static final String EXECUTION_URL = SINGLE_ENGINE_URL + "/execution";
  protected static final String VARIABLE_INSTANCE_URL = SINGLE_ENGINE_URL + "/variable-instance";
  protected static final String USER_URL = SINGLE_ENGINE_URL + "/user";
  protected static final String GROUP_URL = SINGLE_ENGINE_URL + "/group";
  protected static final String INCIDENT_URL = SINGLE_ENGINE_URL + "/incident";
  protected static final String AUTHORIZATION_URL = SINGLE_ENGINE_URL + AuthorizationRestService.PATH;
  protected static final String AUTHORIZATION_CHECK_URL = AUTHORIZATION_URL + "/check";

  protected static final String JOB_DEFINITION_URL = SINGLE_ENGINE_URL + "/job-definition";

  protected static final String HISTORY_URL = SINGLE_ENGINE_URL + "/history";
  protected static final String HISTORY_ACTIVITY_INSTANCE_URL = HISTORY_URL + "/activity-instance";
  protected static final String HISTORY_PROCESS_INSTANCE_URL = HISTORY_URL + "/process-instance";
  protected static final String HISTORY_VARIABLE_INSTANCE_URL = HISTORY_URL + "/variable-instance";
  protected static final String HISTORY_ACTIVITY_STATISTICS_URL = HISTORY_URL + "/process-definition/{id}/statistics";
  protected static final String HISTORY_DETAIL_URL = HISTORY_URL + "/detail";
  protected static final String HISTORY_TASK_INSTANCE_URL = HISTORY_URL + "/task";
  protected static final String HISTORY_INCIDENT_URL = HISTORY_URL + "/incident";

  protected String EXAMPLE_ENGINE_NAME = "anEngineName";

  private ProcessEngine namedProcessEngine;
  private RepositoryService mockRepoService;
  private RuntimeService mockRuntimeService;
  private TaskService mockTaskService;
  private IdentityService mockIdentityService;
  private ManagementService mockManagementService;
  private HistoryService mockHistoryService;

  @Before
  public void setUpRuntimeData() {
    namedProcessEngine = getProcessEngine(EXAMPLE_ENGINE_NAME);
    mockRepoService = mock(RepositoryService.class);
    mockRuntimeService = mock(RuntimeService.class);
    mockTaskService = mock(TaskService.class);
    mockIdentityService = mock(IdentityService.class);
    mockManagementService = mock(ManagementService.class);
    mockHistoryService = mock(HistoryService.class);

    when(namedProcessEngine.getRepositoryService()).thenReturn(mockRepoService);
    when(namedProcessEngine.getRuntimeService()).thenReturn(mockRuntimeService);
    when(namedProcessEngine.getTaskService()).thenReturn(mockTaskService);
    when(namedProcessEngine.getIdentityService()).thenReturn(mockIdentityService);
    when(namedProcessEngine.getManagementService()).thenReturn(mockManagementService);
    when(namedProcessEngine.getHistoryService()).thenReturn(mockHistoryService);

    createProcessDefinitionMock();
    createProcessInstanceMock();
    createTaskMock();
    createIdentityMocks();
    createExecutionMock();
    createVariableInstanceMock();
    createJobDefinitionMock();
    createIncidentMock();

    createHistoricActivityInstanceMock();
    createHistoricProcessInstanceMock();
    createHistoricVariableInstanceMock();
    createHistoricActivityStatisticsMock();
    createHistoricDetailMock();
    createHistoricTaskInstanceMock();
    createHistoricIncidentMock();
  }


  private void createProcessDefinitionMock() {
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();

    when(mockRepoService.getProcessDefinition(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(mockDefinition);
  }

  private void createProcessInstanceMock() {
    ProcessInstance mockInstance = MockProvider.createMockInstance();

    ProcessInstanceQuery mockInstanceQuery = mock(ProcessInstanceQuery.class);
    when(mockInstanceQuery.processInstanceId(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))).thenReturn(mockInstanceQuery);
    when(mockInstanceQuery.singleResult()).thenReturn(mockInstance);
    when(mockRuntimeService.createProcessInstanceQuery()).thenReturn(mockInstanceQuery);
  }

  private void createExecutionMock() {
    Execution mockExecution = MockProvider.createMockExecution();

    ExecutionQuery mockExecutionQuery = mock(ExecutionQuery.class);
    when(mockExecutionQuery.processInstanceId(eq(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))).thenReturn(mockExecutionQuery);
    when(mockExecutionQuery.singleResult()).thenReturn(mockExecution);
    when(mockRuntimeService.createExecutionQuery()).thenReturn(mockExecutionQuery);
  }

  private void createTaskMock() {
    Task mockTask = MockProvider.createMockTask();

    TaskQuery mockTaskQuery = mock(TaskQuery.class);
    when(mockTaskQuery.taskId(eq(MockProvider.EXAMPLE_TASK_ID))).thenReturn(mockTaskQuery);
    when(mockTaskQuery.singleResult()).thenReturn(mockTask);
    when(mockTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
  }

  private void createIdentityMocks() {
    // identity
    UserQuery sampleUserQuery = mock(UserQuery.class);
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    List<User> mockUsers = new ArrayList<User>();
    User mockUser = MockProvider.createMockUser();
    mockUsers.add(mockUser);

    // user query
    when(sampleUserQuery.memberOfGroup(anyString())).thenReturn(sampleUserQuery);
    when(sampleUserQuery.list()).thenReturn(mockUsers);

    // group query
    List<Group> mockGroups = MockProvider.createMockGroups();
    when(sampleGroupQuery.groupMember(anyString())).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.orderByGroupName()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.asc()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.list()).thenReturn(mockGroups);

    when(mockIdentityService.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(mockIdentityService.createUserQuery()).thenReturn(sampleUserQuery);
  }


  private void createVariableInstanceMock() {
    List<VariableInstance> variables = new ArrayList<VariableInstance>();
    VariableInstance mockInstance = MockProvider.createMockVariableInstance();
    variables.add(mockInstance);

    VariableInstanceQuery mockVariableInstanceQuery = mock(VariableInstanceQuery.class);
    when(mockVariableInstanceQuery.list()).thenReturn(variables);
    when(mockRuntimeService.createVariableInstanceQuery()).thenReturn(mockVariableInstanceQuery);
  }

  private void createJobDefinitionMock() {
    List<JobDefinition> jobDefinitions = new ArrayList<JobDefinition>();
    JobDefinition mockJobDefinition = MockProvider.createMockJobDefinition();
    jobDefinitions.add(mockJobDefinition);

    JobDefinitionQuery mockJobDefinitionQuery = mock(JobDefinitionQuery.class);
    when(mockJobDefinitionQuery.list()).thenReturn(jobDefinitions);
    when(mockManagementService.createJobDefinitionQuery()).thenReturn(mockJobDefinitionQuery);
  }

  private void createIncidentMock() {
    IncidentQuery mockIncidentQuery = mock(IncidentQuery.class);
    List<Incident> incidents = MockProvider.createMockIncidents();
    when(mockIncidentQuery.list()).thenReturn(incidents);
    when(mockRuntimeService.createIncidentQuery()).thenReturn(mockIncidentQuery);
  }

  private void createHistoricActivityInstanceMock() {
    List<HistoricActivityInstance> activities = new ArrayList<HistoricActivityInstance>();
    HistoricActivityInstance mockInstance = MockProvider.createMockHistoricActivityInstance();
    activities.add(mockInstance);

    HistoricActivityInstanceQuery mockHistoricActivityInstanceQuery = mock(HistoricActivityInstanceQuery.class);
    when(mockHistoricActivityInstanceQuery.list()).thenReturn(activities);
    when(mockHistoryService.createHistoricActivityInstanceQuery()).thenReturn(mockHistoricActivityInstanceQuery);
  }

  private void createHistoricProcessInstanceMock() {
    List<HistoricProcessInstance> processes = new ArrayList<HistoricProcessInstance>();
    HistoricProcessInstance mockInstance = MockProvider.createMockHistoricProcessInstance();
    processes.add(mockInstance);

    HistoricProcessInstanceQuery mockHistoricProcessInstanceQuery = mock(HistoricProcessInstanceQuery.class);
    when(mockHistoricProcessInstanceQuery.list()).thenReturn(processes);
    when(mockHistoryService.createHistoricProcessInstanceQuery()).thenReturn(mockHistoricProcessInstanceQuery);
  }

  private void createHistoricVariableInstanceMock() {
    List<HistoricVariableInstance> variables = new ArrayList<HistoricVariableInstance>();
    HistoricVariableInstance mockInstance = MockProvider.createMockHistoricVariableInstance();
    variables.add(mockInstance);

    HistoricVariableInstanceQuery mockHistoricVariableInstanceQuery = mock(HistoricVariableInstanceQuery.class);
    when(mockHistoricVariableInstanceQuery.list()).thenReturn(variables);
    when(mockHistoryService.createHistoricVariableInstanceQuery()).thenReturn(mockHistoricVariableInstanceQuery);
  }

  private void createHistoricActivityStatisticsMock() {
    List<HistoricActivityStatistics> statistics = MockProvider.createMockHistoricActivityStatistics();

    HistoricActivityStatisticsQuery query = mock(HistoricActivityStatisticsQuery.class);
    when(mockHistoryService.createHistoricActivityStatisticsQuery(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenReturn(query);
    when(query.list()).thenReturn(statistics);
  }

  private void createHistoricDetailMock() {
    List<HistoricDetail> details = MockProvider.createMockHistoricDetails();

    HistoricDetailQuery query = mock(HistoricDetailQuery.class);
    when(mockHistoryService.createHistoricDetailQuery()).thenReturn(query);
    when(query.list()).thenReturn(details);
  }

  private void createHistoricTaskInstanceMock() {
    List<HistoricTaskInstance> tasks = MockProvider.createMockHistoricTaskInstances();

    HistoricTaskInstanceQuery query = mock(HistoricTaskInstanceQuery.class);
    when(mockHistoryService.createHistoricTaskInstanceQuery()).thenReturn(query);
    when(query.list()).thenReturn(tasks);
  }

  private void createHistoricIncidentMock() {
    HistoricIncidentQuery mockHistoricIncidentQuery = mock(HistoricIncidentQuery.class);
    List<HistoricIncident> historicIncidents = MockProvider.createMockHistoricIncidents();
    when(mockHistoricIncidentQuery.list()).thenReturn(historicIncidents);
    when(mockHistoryService.createHistoricIncidentQuery()).thenReturn(mockHistoricIncidentQuery);
  }

  @Test
  public void testNonExistingEngineAccess() {
    given().pathParam("name", MockProvider.NON_EXISTING_PROCESS_ENGINE_NAME)
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("No process engine available"))
    .when().get(PROCESS_DEFINITION_URL);
  }

  @Test
  public void testEngineNamesList() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("$.size()", is(2))
      .body("name", hasItems(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME, MockProvider.ANOTHER_EXAMPLE_PROCESS_ENGINE_NAME))
    .when().get(ENGINES_URL);
  }

  @Test
  public void testProcessDefinitionServiceEngineAccess() {
    given().pathParam("name", EXAMPLE_ENGINE_NAME)
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_DEFINITION_URL);

    verify(mockRepoService).getProcessDefinition(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID));
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testProcessInstanceServiceEngineAccess() {
    given().pathParam("name", EXAMPLE_ENGINE_NAME)
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_INSTANCE_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(PROCESS_INSTANCE_URL);

    verify(mockRuntimeService).createProcessInstanceQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testTaskServiceEngineAccess() {
    given().pathParam("name", EXAMPLE_ENGINE_NAME)
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(TASK_URL);

    verify(mockTaskService).createTaskQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testIdentityServiceEngineAccess() {
    given().pathParam("name", EXAMPLE_ENGINE_NAME)
      .queryParam("userId", "someId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(IDENTITY_GROUPS_URL);

    verify(mockIdentityService).createUserQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testMessageServiceEngineAccess() {
    String messageName = "aMessage";
    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters).pathParam("name", EXAMPLE_ENGINE_NAME)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(MESSAGE_URL);

    verify(mockRuntimeService).correlateMessage(eq(messageName), eq((String) null),
        argThat(new EqualsMap(null)), argThat(new EqualsMap(null)));
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testExecutionServiceEngineAccess() {
    given().pathParam("name", EXAMPLE_ENGINE_NAME)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(EXECUTION_URL);

    verify(mockRuntimeService).createExecutionQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testVariableInstanceServiceEngineAccess() {
    given().pathParam("name", EXAMPLE_ENGINE_NAME)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(VARIABLE_INSTANCE_URL);

    verify(mockRuntimeService).createVariableInstanceQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testUserServiceEngineAccess() {
    given().pathParam("name", EXAMPLE_ENGINE_NAME)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(USER_URL);

    verify(mockIdentityService).createUserQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testGroupServiceEngineAccess() {
    given().pathParam("name", EXAMPLE_ENGINE_NAME)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(GROUP_URL);

    verify(mockIdentityService).createGroupQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testAuthorizationServiceEngineAccess() {
    given().pathParam("name", EXAMPLE_ENGINE_NAME)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when().get(AUTHORIZATION_CHECK_URL);
  }

  @Test
  public void testHistoryServiceEngineAccess_HistoricActivityInstance() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_ACTIVITY_INSTANCE_URL);

    verify(mockHistoryService).createHistoricActivityInstanceQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testHistoryServiceEngineAccess_HistoricProcessInstance() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_PROCESS_INSTANCE_URL);

    verify(mockHistoryService).createHistoricProcessInstanceQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testHistoryServiceEngineAccess_HistoricVariableInstance() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_VARIABLE_INSTANCE_URL);

    verify(mockHistoryService).createHistoricVariableInstanceQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testHistoryServiceEngineAccess_HistoricActivityStatistics() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_ACTIVITY_STATISTICS_URL);

    verify(mockHistoryService).createHistoricActivityStatisticsQuery(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testJobDefinitionAccess() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(JOB_DEFINITION_URL);

    verify(mockManagementService).createJobDefinitionQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testHistoryServiceEngineAccess_HistoricDetail() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_DETAIL_URL);

    verify(mockHistoryService).createHistoricDetailQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testHistoryServiceEngineAccess_HistoricTaskInstance() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_TASK_INSTANCE_URL);

    verify(mockHistoryService).createHistoricTaskInstanceQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testHistoryServiceEngineAccess_Incident() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(INCIDENT_URL);

    verify(mockRuntimeService).createIncidentQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testHistoryServiceEngineAccess_HistoricIncident() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_INCIDENT_URL);

    verify(mockHistoryService).createHistoricIncidentQuery();
    verifyZeroInteractions(processEngine);
  }
}
