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
package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricActivityStatisticsQuery;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricExternalTaskLogQuery;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;

import com.jayway.restassured.http.ContentType;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;

public class ProcessEngineRestServiceTest extends
    AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

  protected static final String ENGINES_URL = TEST_RESOURCE_ROOT_PATH + "/engine";
  protected static final String SINGLE_ENGINE_URL = ENGINES_URL + "/{name}";
  protected static final String PROCESS_DEFINITION_URL = SINGLE_ENGINE_URL + "/process-definition/{id}";
  protected static final String PROCESS_INSTANCE_URL = SINGLE_ENGINE_URL + "/process-instance/{id}";
  protected static final String TASK_URL = SINGLE_ENGINE_URL + "/task/{id}";
  protected static final String IDENTITY_GROUPS_URL = SINGLE_ENGINE_URL + "/identity/groups";
  protected static final String MESSAGE_URL = SINGLE_ENGINE_URL + MessageRestService.PATH;

  protected static final String EXECUTION_URL = SINGLE_ENGINE_URL + "/execution";
  protected static final String VARIABLE_INSTANCE_URL = SINGLE_ENGINE_URL + "/variable-instance";
  protected static final String USER_URL = SINGLE_ENGINE_URL + "/user";
  protected static final String GROUP_URL = SINGLE_ENGINE_URL + "/group";
  protected static final String INCIDENT_URL = SINGLE_ENGINE_URL + "/incident";
  protected static final String AUTHORIZATION_URL = SINGLE_ENGINE_URL + AuthorizationRestService.PATH;
  protected static final String AUTHORIZATION_CHECK_URL = AUTHORIZATION_URL + "/check";
  protected static final String DEPLOYMENT_REST_SERVICE_URL = SINGLE_ENGINE_URL + DeploymentRestService.PATH;
  protected static final String DEPLOYMENT_URL = DEPLOYMENT_REST_SERVICE_URL + "/{id}";
  protected static final String EXTERNAL_TASKS_URL = SINGLE_ENGINE_URL + "/external-task";

  protected static final String JOB_DEFINITION_URL = SINGLE_ENGINE_URL + "/job-definition";

  protected static final String CASE_DEFINITION_URL = SINGLE_ENGINE_URL + "/case-definition";
  protected static final String CASE_INSTANCE_URL = SINGLE_ENGINE_URL + "/case-instance";
  protected static final String CASE_EXECUTION_URL = SINGLE_ENGINE_URL + "/case-execution";

  protected static final String FILTER_URL = SINGLE_ENGINE_URL + "/filter";

  protected static final String HISTORY_URL = SINGLE_ENGINE_URL + "/history";
  protected static final String HISTORY_ACTIVITY_INSTANCE_URL = HISTORY_URL + "/activity-instance";
  protected static final String HISTORY_PROCESS_INSTANCE_URL = HISTORY_URL + "/process-instance";
  protected static final String HISTORY_VARIABLE_INSTANCE_URL = HISTORY_URL + "/variable-instance";
  protected static final String HISTORY_BINARY_VARIABLE_INSTANCE_URL = HISTORY_VARIABLE_INSTANCE_URL + "/{id}/data";
  protected static final String HISTORY_ACTIVITY_STATISTICS_URL = HISTORY_URL + "/process-definition/{id}/statistics";
  protected static final String HISTORY_DETAIL_URL = HISTORY_URL + "/detail";
  protected static final String HISTORY_BINARY_DETAIL_URL = HISTORY_DETAIL_URL + "/data";
  protected static final String HISTORY_TASK_INSTANCE_URL = HISTORY_URL + "/task";
  protected static final String HISTORY_INCIDENT_URL = HISTORY_URL + "/incident";
  protected static final String HISTORY_JOB_LOG_URL = HISTORY_URL + "/job-log";
  protected static final String HISTORY_EXTERNAL_TASK_LOG_URL = HISTORY_URL + "/external-task-log";

  protected static final String EXAMPLE_ENGINE_NAME = "anEngineName";

  private ProcessEngine namedProcessEngine;
  private RepositoryService mockRepoService;
  private RuntimeService mockRuntimeService;
  private TaskService mockTaskService;
  private IdentityService mockIdentityService;
  private ManagementService mockManagementService;
  private HistoryService mockHistoryService;
  private ExternalTaskService mockExternalTaskService;
  private CaseService mockCaseService;
  private FilterService mockFilterService;
  private MessageCorrelationBuilder mockMessageCorrelationBuilder;
  private MessageCorrelationResult mockMessageCorrelationResult;

  @Before
  public void setUpRuntimeData() {
    namedProcessEngine = getProcessEngine(EXAMPLE_ENGINE_NAME);
    mockRepoService = mock(RepositoryService.class);
    mockRuntimeService = mock(RuntimeService.class);
    mockTaskService = mock(TaskService.class);
    mockIdentityService = mock(IdentityService.class);
    mockManagementService = mock(ManagementService.class);
    mockHistoryService = mock(HistoryService.class);
    mockCaseService = mock(CaseService.class);
    mockFilterService = mock(FilterService.class);
    mockExternalTaskService = mock(ExternalTaskService.class);

    when(namedProcessEngine.getRepositoryService()).thenReturn(mockRepoService);
    when(namedProcessEngine.getRuntimeService()).thenReturn(mockRuntimeService);
    when(namedProcessEngine.getTaskService()).thenReturn(mockTaskService);
    when(namedProcessEngine.getIdentityService()).thenReturn(mockIdentityService);
    when(namedProcessEngine.getManagementService()).thenReturn(mockManagementService);
    when(namedProcessEngine.getHistoryService()).thenReturn(mockHistoryService);
    when(namedProcessEngine.getCaseService()).thenReturn(mockCaseService);
    when(namedProcessEngine.getFilterService()).thenReturn(mockFilterService);
    when(namedProcessEngine.getExternalTaskService()).thenReturn(mockExternalTaskService);

    createProcessDefinitionMock();
    createProcessInstanceMock();
    createTaskMock();
    createIdentityMocks();
    createExecutionMock();
    createVariableInstanceMock();
    createJobDefinitionMock();
    createIncidentMock();
    createDeploymentMock();
    createMessageCorrelationBuilderMock();
    createCaseDefinitionMock();
    createCaseInstanceMock();
    createCaseExecutionMock();
    createFilterMock();
    createExternalTaskMock();

    createHistoricActivityInstanceMock();
    createHistoricProcessInstanceMock();
    createHistoricVariableInstanceMock();
    createHistoricActivityStatisticsMock();
    createHistoricDetailMock();
    createHistoricTaskInstanceMock();
    createHistoricIncidentMock();
    createHistoricJobLogMock();
    createHistoricExternalTaskLogMock();
  }

  private void createProcessDefinitionMock() {
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();

    when(mockRepoService.getProcessDefinition(eq(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))).thenReturn(mockDefinition);
  }


  private void createCaseDefinitionMock() {
    List<CaseDefinition> caseDefinitions = new ArrayList<CaseDefinition>();
    CaseDefinition mockCaseDefinition = MockProvider.createMockCaseDefinition();
    caseDefinitions.add(mockCaseDefinition);

    CaseDefinitionQuery mockCaseDefinitionQuery = mock(CaseDefinitionQuery.class);
    when(mockCaseDefinitionQuery.list()).thenReturn(caseDefinitions);
    when(mockRepoService.createCaseDefinitionQuery()).thenReturn(mockCaseDefinitionQuery);
  }

  private void createCaseInstanceMock() {
    List<CaseInstance> caseInstances = new ArrayList<CaseInstance>();
    CaseInstance mockCaseInstance = MockProvider.createMockCaseInstance();
    caseInstances.add(mockCaseInstance);

    CaseInstanceQuery mockCaseInstanceQuery = mock(CaseInstanceQuery.class);
    when(mockCaseInstanceQuery.list()).thenReturn(caseInstances);
    when(mockCaseService.createCaseInstanceQuery()).thenReturn(mockCaseInstanceQuery);
  }

  private void createCaseExecutionMock() {
    List<CaseExecution> caseExecutions = new ArrayList<CaseExecution>();
    CaseExecution mockCaseExecution = MockProvider.createMockCaseExecution();
    caseExecutions.add(mockCaseExecution);

    CaseExecutionQuery mockCaseExecutionQuery = mock(CaseExecutionQuery.class);
    when(mockCaseExecutionQuery.list()).thenReturn(caseExecutions);
    when(mockCaseService.createCaseExecutionQuery()).thenReturn(mockCaseExecutionQuery);
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
    when(mockTaskQuery.initializeFormKeys()).thenReturn(mockTaskQuery);
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

  private void createMessageCorrelationBuilderMock() {
    mockMessageCorrelationBuilder = mock(MessageCorrelationBuilder.class);
    mockMessageCorrelationResult = mock(MessageCorrelationResult.class);

    when(mockRuntimeService.createMessageCorrelation(anyString())).thenReturn(mockMessageCorrelationBuilder);
    when(mockMessageCorrelationBuilder.correlateWithResult()).thenReturn(mockMessageCorrelationResult);
    when(mockMessageCorrelationBuilder.processInstanceId(anyString())).thenReturn(mockMessageCorrelationBuilder);
    when(mockMessageCorrelationBuilder.processInstanceBusinessKey(anyString())).thenReturn(mockMessageCorrelationBuilder);
    when(mockMessageCorrelationBuilder.processInstanceVariableEquals(anyString(), any())).thenReturn(mockMessageCorrelationBuilder);
    when(mockMessageCorrelationBuilder.setVariables(Matchers.<Map<String,Object>>any())).thenReturn(mockMessageCorrelationBuilder);
    when(mockMessageCorrelationBuilder.setVariable(anyString(), any())).thenReturn(mockMessageCorrelationBuilder);

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

  private void createDeploymentMock() {
    Deployment mockDeployment = MockProvider.createMockDeployment();

    DeploymentQuery deploymentQueryMock = mock(DeploymentQuery.class);
    when(deploymentQueryMock.deploymentId(anyString())).thenReturn(deploymentQueryMock);
    when(deploymentQueryMock.singleResult()).thenReturn(mockDeployment);

    when(mockRepoService.createDeploymentQuery()).thenReturn(deploymentQueryMock);
  }

  private void createFilterMock() {
    List<Filter> filters = new ArrayList<Filter>();
    Filter mockFilter = MockProvider.createMockFilter();
    filters.add(mockFilter);

    FilterQuery mockFilterQuery = mock(FilterQuery.class);
    when(mockFilterQuery.list()).thenReturn(filters);
    when(mockFilterService.createFilterQuery()).thenReturn(mockFilterQuery);
  }

  private void createHistoricJobLogMock() {
    HistoricJobLogQuery mockHistoricJobLogQuery = mock(HistoricJobLogQuery.class);
    List<HistoricJobLog> historicJobLogs = MockProvider.createMockHistoricJobLogs();
    when(mockHistoricJobLogQuery.list()).thenReturn(historicJobLogs);
    when(mockHistoryService.createHistoricJobLogQuery()).thenReturn(mockHistoricJobLogQuery);
  }

  private void createExternalTaskMock() {
    ExternalTaskQuery query = mock(ExternalTaskQuery.class);
    List<ExternalTask> tasks = MockProvider.createMockExternalTasks();
    when(query.list()).thenReturn(tasks);
    when(mockExternalTaskService.createExternalTaskQuery()).thenReturn(query);
  }

  private void createHistoricExternalTaskLogMock() {
    HistoricExternalTaskLogQuery mockHistoricExternalTaskLogQuery = mock(HistoricExternalTaskLogQuery.class);
    List<HistoricExternalTaskLog> historicExternalTaskLogs = MockProvider.createMockHistoricExternalTaskLogs();
    when(mockHistoricExternalTaskLogQuery.list()).thenReturn(historicExternalTaskLogs);
    when(mockHistoryService.createHistoricExternalTaskLogQuery()).thenReturn(mockHistoricExternalTaskLogQuery);
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
      .header("accept", MediaType.APPLICATION_JSON)
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

    verify(mockRuntimeService).createMessageCorrelation(eq(messageName));
    verify(mockMessageCorrelationBuilder).correlateWithResult();
    verifyNoMoreInteractions(mockMessageCorrelationBuilder);
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testMessageWithResultServiceEngineAccess() {
    String messageName = "aMessage";
    Map<String, Object> messageParameters = new HashMap<String, Object>();
    messageParameters.put("messageName", messageName);
    messageParameters.put("resultEnabled", true);

    given().contentType(POST_JSON_CONTENT_TYPE).body(messageParameters).pathParam("name", EXAMPLE_ENGINE_NAME)
      .then().expect().contentType(ContentType.JSON).statusCode(Status.OK.getStatusCode())
      .when().post(MESSAGE_URL);

    verify(mockRuntimeService).createMessageCorrelation(eq(messageName));
    verify(mockMessageCorrelationBuilder).correlateWithResult();
    verifyNoMoreInteractions(mockMessageCorrelationBuilder);
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

  @Ignore
  @Test
  public void testHistoryServiceEngineAccess_HistoricVariableInstanceBinaryFile() {

    HistoricVariableInstanceQuery query = mock(HistoricVariableInstanceQuery.class);
    HistoricVariableInstance instance = mock(HistoricVariableInstance.class);
    String filename = "test.txt";
    byte[] byteContent = "test".getBytes();
    String encoding = "UTF-8";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(ContentType.TEXT.toString()).encoding(encoding).create();
    when(instance.getTypedValue()).thenReturn(variableValue);
    when(query.singleResult()).thenReturn(instance);
    when(mockHistoryService.createHistoricVariableInstanceQuery()).thenReturn(query);

    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
      .pathParam("id", MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      . body(is(equalTo(new String(byteContent))))
      .and()
        .header("Content-Disposition", "attachment; filename="+filename)
        .contentType(CoreMatchers.<String>either(equalTo(ContentType.TEXT.toString() + ";charset=UTF-8")).or(equalTo(ContentType.TEXT.toString() + " ;charset=UTF-8")))
      .when()
        .get(HISTORY_BINARY_VARIABLE_INSTANCE_URL);

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

  @Ignore
  @Test
  public void testHistoryServiceEngineAccess_HistoricDetailBinaryFile() {
    HistoricDetailQuery query = mock(HistoricDetailQuery.class);
    HistoricVariableUpdate instance = mock(HistoricVariableUpdate.class);
    String filename = "test.txt";
    byte[] byteContent = "test".getBytes();
    String encoding = "UTF-8";
    FileValue variableValue = Variables.fileValue(filename).file(byteContent).mimeType(ContentType.TEXT.toString()).encoding(encoding).create();
    when(instance.getTypedValue()).thenReturn(variableValue);
    when(query.singleResult()).thenReturn(instance);
    when(mockHistoryService.createHistoricDetailQuery()).thenReturn(query);

    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      . body(is(equalTo(new String(byteContent))))
      .and()
        .header("Content-Disposition", "attachment; filename="+filename)
        .contentType(CoreMatchers.<String>either(equalTo(ContentType.TEXT.toString() + ";charset=UTF-8")).or(equalTo(ContentType.TEXT.toString() + " ;charset=UTF-8")))
      .when()
        .get(HISTORY_BINARY_DETAIL_URL);

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

  @Test
  public void testDeploymentRestServiceEngineAccess() {
    given().pathParam("name", EXAMPLE_ENGINE_NAME)
      .pathParam("id", MockProvider.EXAMPLE_DEPLOYMENT_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(DEPLOYMENT_URL);

    verify(mockRepoService).createDeploymentQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testCaseDefinitionAccess() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_DEFINITION_URL);

    verify(mockRepoService).createCaseDefinitionQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testCaseInstanceAccess() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_INSTANCE_URL);

    verify(mockCaseService).createCaseInstanceQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testCaseExecutionAccess() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(CASE_EXECUTION_URL);

    verify(mockCaseService).createCaseExecutionQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testFilterAccess() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(FILTER_URL);

    verify(mockFilterService).createFilterQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testHistoryServiceEngineAccess_HistoricJobLog() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORY_JOB_LOG_URL);

    verify(mockHistoryService).createHistoricJobLogQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testExternalTaskAccess() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(EXTERNAL_TASKS_URL);

    verify(mockExternalTaskService).createExternalTaskQuery();
    verifyZeroInteractions(processEngine);
  }

  @Test
  public void testHistoryServiceEngineAccess_HistoricExternalTaskLog() {
    given()
      .pathParam("name", EXAMPLE_ENGINE_NAME)
      .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(HISTORY_EXTERNAL_TASK_LOG_URL);

    verify(mockHistoryService).createHistoricExternalTaskLogQuery();
    verifyZeroInteractions(processEngine);
  }
}
