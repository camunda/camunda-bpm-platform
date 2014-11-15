package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_CASE_DEFINITION_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_CASE_EXECUTION_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_CASE_INSTANCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_ATTACHMENT_DESCRIPTION;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_ATTACHMENT_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_ATTACHMENT_NAME;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_ATTACHMENT_TYPE;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_ATTACHMENT_URL;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_COMMENT_FULL_MESSAGE;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_COMMENT_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_COMMENT_TIME;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_EXECUTION_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_OWNER;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_TASK_PARENT_TASK_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_USER_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.NON_EXISTING_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.createMockHistoricTaskInstance;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.core.variable.type.ObjectTypeImpl;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.hal.Hal;
import org.camunda.bpm.engine.rest.helper.EqualsList;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.ErrorMessageHelper;
import org.camunda.bpm.engine.rest.helper.MockObjectValue;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.VariableTypeHelper;
import org.camunda.bpm.engine.rest.helper.variable.EqualsNullValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsObjectValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.camunda.bpm.engine.rest.helper.variable.EqualsUntypedValue;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public abstract class AbstractTaskRestServiceInteractionTest extends
    AbstractRestServiceTest {

  protected static final String TASK_SERVICE_URL = TEST_RESOURCE_ROOT_PATH + "/task";
  protected static final String SINGLE_TASK_URL = TASK_SERVICE_URL + "/{id}";
  protected static final String CLAIM_TASK_URL = SINGLE_TASK_URL + "/claim";
  protected static final String UNCLAIM_TASK_URL = SINGLE_TASK_URL + "/unclaim";
  protected static final String COMPLETE_TASK_URL = SINGLE_TASK_URL + "/complete";
  protected static final String RESOLVE_TASK_URL = SINGLE_TASK_URL + "/resolve";
  protected static final String DELEGATE_TASK_URL = SINGLE_TASK_URL + "/delegate";
  protected static final String ASSIGNEE_TASK_URL = SINGLE_TASK_URL + "/assignee";
  protected static final String TASK_IDENTITY_LINKS_URL = SINGLE_TASK_URL + "/identity-links";

  protected static final String TASK_FORM_URL = SINGLE_TASK_URL + "/form";
  protected static final String RENDERED_FORM_URL = SINGLE_TASK_URL + "/rendered-form";
  protected static final String SUBMIT_FORM_URL = SINGLE_TASK_URL + "/submit-form";

  protected static final String FORM_VARIABLES_URL = SINGLE_TASK_URL + "/form-variables";

  protected static final String SINGLE_TASK_ADD_COMMENT_URL = SINGLE_TASK_URL + "/comment/create";
  protected static final String SINGLE_TASK_COMMENTS_URL = SINGLE_TASK_URL + "/comment";
  protected static final String SINGLE_TASK_SINGLE_COMMENT_URL = SINGLE_TASK_COMMENTS_URL + "/{commentId}";

  protected static final String SINGLE_TASK_ADD_ATTACHMENT_URL = SINGLE_TASK_URL + "/attachment/create";
  protected static final String SINGLE_TASK_ATTACHMENTS_URL = SINGLE_TASK_URL + "/attachment";
  protected static final String SINGLE_TASK_SINGLE_ATTACHMENT_URL = SINGLE_TASK_ATTACHMENTS_URL + "/{attachmentId}";
  protected static final String SINGLE_TASK_DELETE_SINGLE_ATTACHMENT_URL = SINGLE_TASK_SINGLE_ATTACHMENT_URL;
  protected static final String SINGLE_TASK_SINGLE_ATTACHMENT_DATA_URL = SINGLE_TASK_ATTACHMENTS_URL + "/{attachmentId}/data";

  protected static final String SINGLE_TASK_VARIABLES_URL = SINGLE_TASK_URL + "/localVariables";
  protected static final String SINGLE_TASK_SINGLE_VARIABLE_URL = SINGLE_TASK_VARIABLES_URL + "/{varId}";
  protected static final String SINGLE_TASK_PUT_SINGLE_VARIABLE_URL = SINGLE_TASK_SINGLE_VARIABLE_URL;
  protected static final String SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL = SINGLE_TASK_PUT_SINGLE_VARIABLE_URL + "/data";
  protected static final String SINGLE_TASK_DELETE_SINGLE_VARIABLE_URL = SINGLE_TASK_SINGLE_VARIABLE_URL;
  protected static final String SINGLE_TASK_MODIFY_VARIABLES_URL = SINGLE_TASK_VARIABLES_URL;

  protected static final String TASK_CREATE_URL = TASK_SERVICE_URL + "/create";

  private Task mockTask;
  private TaskService taskServiceMock;
  private TaskQuery mockQuery;
  private FormService formServiceMock;
  private ManagementService managementServiceMock;
  private RepositoryService repositoryServiceMock;

  private IdentityLink mockUserAssigneeIdentityLink;
  private IdentityLink mockCandidateGroupIdentityLink;
  private IdentityLink mockCandidateGroup2IdentityLink;

  private HistoricTaskInstanceQuery historicTaskInstanceQueryMock;

  private Comment mockTaskComment;
  private List<Comment> mockTaskComments;

  private Attachment mockTaskAttachment;
  private List<Attachment> mockTaskAttachments;

  @Before
  public void setUpRuntimeData() {
    taskServiceMock = mock(TaskService.class);
    when(processEngine.getTaskService()).thenReturn(taskServiceMock);

    mockTask = MockProvider.createMockTask();
    mockQuery = mock(TaskQuery.class);
    when(mockQuery.initializeFormKeys()).thenReturn(mockQuery);
    when(mockQuery.taskId(anyString())).thenReturn(mockQuery);
    when(mockQuery.singleResult()).thenReturn(mockTask);
    when(taskServiceMock.createTaskQuery()).thenReturn(mockQuery);

    List<IdentityLink> identityLinks = new ArrayList<IdentityLink>();
    mockUserAssigneeIdentityLink = MockProvider.createMockUserAssigneeIdentityLink();
    identityLinks.add(mockUserAssigneeIdentityLink);
    mockCandidateGroupIdentityLink = MockProvider.createMockCandidateGroupIdentityLink();
    identityLinks.add(mockCandidateGroupIdentityLink);
    mockCandidateGroup2IdentityLink = MockProvider.createAnotherMockCandidateGroupIdentityLink();
    identityLinks.add(mockCandidateGroup2IdentityLink);
    when(taskServiceMock.getIdentityLinksForTask(EXAMPLE_TASK_ID)).thenReturn(identityLinks);

    mockTaskComment = MockProvider.createMockTaskComment();
    when(taskServiceMock.getTaskComment(EXAMPLE_TASK_ID, EXAMPLE_TASK_COMMENT_ID)).thenReturn(mockTaskComment);
    mockTaskComments = MockProvider.createMockTaskComments();
    when(taskServiceMock.getTaskComments(EXAMPLE_TASK_ID)).thenReturn(mockTaskComments);
    when(taskServiceMock.createComment(EXAMPLE_TASK_ID, null, EXAMPLE_TASK_COMMENT_FULL_MESSAGE)).thenReturn(mockTaskComment);

    mockTaskAttachment = MockProvider.createMockTaskAttachment();
    when(taskServiceMock.getTaskAttachment(EXAMPLE_TASK_ID, EXAMPLE_TASK_ATTACHMENT_ID)).thenReturn(mockTaskAttachment);
    mockTaskAttachments = MockProvider.createMockTaskAttachments();
    when(taskServiceMock.getTaskAttachments(EXAMPLE_TASK_ID)).thenReturn(mockTaskAttachments);
    when(taskServiceMock.createAttachment(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(mockTaskAttachment);
    when(taskServiceMock.createAttachment(anyString(), anyString(), anyString(), anyString(), anyString(), any(InputStream.class))).thenReturn(mockTaskAttachment);
    when(taskServiceMock.getTaskAttachmentContent(EXAMPLE_TASK_ID, EXAMPLE_TASK_ATTACHMENT_ID)).thenReturn(new ByteArrayInputStream(createMockByteData()));

    when(taskServiceMock.getVariablesLocalTyped(EXAMPLE_TASK_ID, true)).thenReturn(EXAMPLE_VARIABLES);

    formServiceMock = mock(FormService.class);
    when(processEngine.getFormService()).thenReturn(formServiceMock);
    TaskFormData mockFormData = MockProvider.createMockTaskFormData();
    when(formServiceMock.getTaskFormData(anyString())).thenReturn(mockFormData);

    VariableMap variablesMock = MockProvider.createMockFormVariables();
    when(formServiceMock.getTaskFormVariables(eq(EXAMPLE_TASK_ID), Matchers.<Collection<String>>any(), eq(true))).thenReturn(variablesMock);

    repositoryServiceMock = mock(RepositoryService.class);
    when(processEngine.getRepositoryService()).thenReturn(repositoryServiceMock);
    ProcessDefinition mockDefinition = MockProvider.createMockDefinition();
    when(repositoryServiceMock.getProcessDefinition(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID)).thenReturn(mockDefinition);

    managementServiceMock = mock(ManagementService.class);
    when(processEngine.getManagementService()).thenReturn(managementServiceMock);
    when(managementServiceMock.getProcessApplicationForDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID)).thenReturn(MockProvider.EXAMPLE_PROCESS_APPLICATION_NAME);
    when(managementServiceMock.getHistoryLevel()).thenReturn(ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL);

    HistoryService historyServiceMock = mock(HistoryService.class);
    when(processEngine.getHistoryService()).thenReturn(historyServiceMock);
    historicTaskInstanceQueryMock = mock(HistoricTaskInstanceQuery.class);
    when(historyServiceMock.createHistoricTaskInstanceQuery()).thenReturn(historicTaskInstanceQueryMock);
    when(historicTaskInstanceQueryMock.taskId(eq(EXAMPLE_TASK_ID))).thenReturn(historicTaskInstanceQueryMock);
    HistoricTaskInstance historicTaskInstanceMock = createMockHistoricTaskInstance();
    when(historicTaskInstanceQueryMock.singleResult()).thenReturn(historicTaskInstanceMock);

    // replace the runtime container delegate & process application service with a mock

    ProcessApplicationService processApplicationService = mock(ProcessApplicationService.class);
    ProcessApplicationInfo appMock = MockProvider.createMockProcessApplicationInfo();
    when(processApplicationService.getProcessApplicationInfo(MockProvider.EXAMPLE_PROCESS_APPLICATION_NAME)).thenReturn(appMock);

    RuntimeContainerDelegate delegate = mock(RuntimeContainerDelegate.class);
    when(delegate.getProcessApplicationService()).thenReturn(processApplicationService);
    RuntimeContainerDelegate.INSTANCE.set(delegate);
  }

  private TaskServiceImpl mockTaskServiceImpl() {
    TaskServiceImpl taskServiceMock = mock(TaskServiceImpl.class);
    when(processEngine.getTaskService()).thenReturn(taskServiceMock);
    return taskServiceMock;
  }

  public void mockHistoryDisabled() {
    when(managementServiceMock.getHistoryLevel()).thenReturn(ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE);
  }

  private byte[] createMockByteData() {
    return "someContent".getBytes();
  }

  @Test
  public void testGetSingleTask() {
    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(EXAMPLE_TASK_ID))
      .body("name", equalTo(MockProvider.EXAMPLE_TASK_NAME))
      .body("assignee", equalTo(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME))
      .body("created", equalTo(MockProvider.EXAMPLE_TASK_CREATE_TIME))
      .body("due", equalTo(MockProvider.EXAMPLE_TASK_DUE_DATE))
      .body("delegationState", equalTo(MockProvider.EXAMPLE_TASK_DELEGATION_STATE.toString()))
      .body("description", equalTo(MockProvider.EXAMPLE_TASK_DESCRIPTION))
      .body("executionId", equalTo(MockProvider.EXAMPLE_TASK_EXECUTION_ID))
      .body("owner", equalTo(MockProvider.EXAMPLE_TASK_OWNER))
      .body("parentTaskId", equalTo(MockProvider.EXAMPLE_TASK_PARENT_TASK_ID))
      .body("priority", equalTo(MockProvider.EXAMPLE_TASK_PRIORITY))
      .body("processDefinitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("taskDefinitionKey", equalTo(MockProvider.EXAMPLE_TASK_DEFINITION_KEY))
      .body("suspended", equalTo(MockProvider.EXAMPLE_TASK_SUSPENSION_STATE))
      .body("caseExecutionId", equalTo(MockProvider.EXAMPLE_CASE_EXECUTION_ID))
      .body("caseInstanceId", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
      .body("caseDefinitionId", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_ID))
      .when().get(SINGLE_TASK_URL);
  }

  @Test
  public void testGetSingleTaskHal() {

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

    // setup case defintion query mock
    List<CaseDefinition> mockCaseDefinitions = MockProvider.createMockCaseDefinitions();
    CaseDefinitionQuery sampleCaseDefinitionQuery = mock(CaseDefinitionQuery.class);
    when(sampleCaseDefinitionQuery.listPage(0, 1)).thenReturn(mockCaseDefinitions);
    when(sampleCaseDefinitionQuery.caseDefinitionIdIn(MockProvider.EXAMPLE_CASE_DEFINITION_ID)).thenReturn(sampleCaseDefinitionQuery);
    when(sampleCaseDefinitionQuery.count()).thenReturn(1l);
    when(processEngine.getRepositoryService().createCaseDefinitionQuery()).thenReturn(sampleCaseDefinitionQuery);

    Response response = given()
      .header("accept", Hal.APPLICATION_HAL_JSON)
      .pathParam("id", EXAMPLE_TASK_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(EXAMPLE_TASK_ID))
      .body("name", equalTo(MockProvider.EXAMPLE_TASK_NAME))
      .body("assignee", equalTo(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME))
      .body("created", equalTo(MockProvider.EXAMPLE_TASK_CREATE_TIME))
      .body("due", equalTo(MockProvider.EXAMPLE_TASK_DUE_DATE))
      .body("delegationState", equalTo(MockProvider.EXAMPLE_TASK_DELEGATION_STATE.toString()))
      .body("description", equalTo(MockProvider.EXAMPLE_TASK_DESCRIPTION))
      .body("executionId", equalTo(MockProvider.EXAMPLE_TASK_EXECUTION_ID))
      .body("owner", equalTo(MockProvider.EXAMPLE_TASK_OWNER))
      .body("parentTaskId", equalTo(MockProvider.EXAMPLE_TASK_PARENT_TASK_ID))
      .body("priority", equalTo(MockProvider.EXAMPLE_TASK_PRIORITY))
      .body("processDefinitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("taskDefinitionKey", equalTo(MockProvider.EXAMPLE_TASK_DEFINITION_KEY))
      .body("suspended", equalTo(MockProvider.EXAMPLE_TASK_SUSPENSION_STATE))
      .body("caseExecutionId", equalTo(MockProvider.EXAMPLE_CASE_EXECUTION_ID))
      .body("caseInstanceId", equalTo(MockProvider.EXAMPLE_CASE_INSTANCE_ID))
      .body("caseDefinitionId", equalTo(MockProvider.EXAMPLE_CASE_DEFINITION_ID))

      // links
      .body("_links.assignee.href", endsWith(EXAMPLE_TASK_ASSIGNEE_NAME))
      .body("_links.caseDefinition.href", endsWith(EXAMPLE_CASE_DEFINITION_ID))
      .body("_links.caseExecution.href", endsWith(EXAMPLE_CASE_EXECUTION_ID))
      .body("_links.caseInstance.href", endsWith(EXAMPLE_CASE_INSTANCE_ID))
      .body("_links.execution.href", endsWith(EXAMPLE_TASK_EXECUTION_ID))
      .body("_links.owner.href", endsWith(EXAMPLE_TASK_OWNER))
      .body("_links.parentTask.href", endsWith(EXAMPLE_TASK_PARENT_TASK_ID))
      .body("_links.processDefinition.href", endsWith(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("_links.processInstance.href", endsWith(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("_links.self.href", endsWith(EXAMPLE_TASK_ID))

      .when().get(SINGLE_TASK_URL);

    String content = response.asString();

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

    Map<String, Object> links = (Map<String, Object>) embeddedProcessDefinition.get("_links");
    Assert.assertEquals(3, links.size());
    assertHalLink(links, "self", "/process-definition/" +  MockProvider.EXAMPLE_PROCESS_DEFINITION_ID);
    assertHalLink(links, "deployment", "/deployment/" +  MockProvider.EXAMPLE_DEPLOYMENT_ID);
    assertHalLink(links, "resource", "/deployment/" +  MockProvider.EXAMPLE_DEPLOYMENT_ID + "/resources/"
        + MockProvider.EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME);


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

    links = (Map<String, Object>) embeddedCaseDefinition.get("_links");
    Assert.assertEquals(3, links.size());
    assertHalLink(links, "self", "/case-definition/" +  MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    assertHalLink(links, "deployment", "/deployment/" +  MockProvider.EXAMPLE_DEPLOYMENT_ID);
    assertHalLink(links, "resource", "/deployment/" +  MockProvider.EXAMPLE_DEPLOYMENT_ID + "/resources/"
        + MockProvider.EXAMPLE_CASE_DEFINITION_RESOURCE_NAME);
  }

  protected void assertHalLink(Map<String, Object> links, String key, String expectedLink) {
    Map<String, Object> linkObject = (Map<String, Object>) links.get(key);
    Assert.assertNotNull(linkObject);

    String actualLink = (String) linkObject.get("href");
    Assert.assertEquals(expectedLink, actualLink);
  }

  @Test
  public void testGetForm() {
    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("key", equalTo(MockProvider.EXAMPLE_FORM_KEY))
      .body("contextPath", equalTo(MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH))
      .when().get(TASK_FORM_URL);
  }

  /**
   * Assuming the task belongs to a deployment that does not belong to any process application
   */
  @Test
  public void testGetFormForNonRegisteredDeployment() {
    when(managementServiceMock.getProcessApplicationForDeployment(MockProvider.EXAMPLE_DEPLOYMENT_ID)).thenReturn(null);

    given().pathParam("id", EXAMPLE_TASK_ID)
    .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("key", equalTo(MockProvider.EXAMPLE_FORM_KEY))
      .body("contextPath", nullValue())
      .when().get(TASK_FORM_URL);
  }

  /**
   * Assuming that the task belongs to no process definition
   */
  @Test
  public void getFormForIndependentTask() {
    when(mockTask.getProcessDefinitionId()).thenReturn(null);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("key", equalTo(MockProvider.EXAMPLE_FORM_KEY))
      .body("contextPath", nullValue())
      .when().get(TASK_FORM_URL);

    verify(repositoryServiceMock, never()).getProcessDefinition(null);
  }

  @Test
  public void testGetForm_shouldReturnKeyContainingTaskId() {
    TaskFormData mockTaskFormData = MockProvider.createMockTaskFormDataUsingFormFieldsWithoutFormKey();
    when(formServiceMock.getTaskFormData(EXAMPLE_TASK_ID)).thenReturn(mockTaskFormData);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("key", equalTo("embedded:engine://engine/:engine/task/" + EXAMPLE_TASK_ID + "/rendered-form"))
      .body("contextPath", equalTo(MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH))
      .when().get(TASK_FORM_URL);
  }

  @Test
  public void testGetRenderedForm() {
    String expectedResult = "<formField>anyContent</formField>";

    when(formServiceMock.getRenderedTaskForm(EXAMPLE_TASK_ID)).thenReturn(expectedResult);

    Response response = given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
          .contentType(XHTML_XML_CONTENT_TYPE)
      .when()
        .get(RENDERED_FORM_URL);

    String responseContent = response.asString();
    System.out.println(responseContent);
    Assertions.assertThat(responseContent).isEqualTo(expectedResult);
  }

  @Test
  public void testGetRenderedFormReturnsNotFound() {
    when(formServiceMock.getRenderedTaskForm(anyString(), anyString())).thenReturn(null);

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .then()
        .expect()
          .statusCode(Status.NOT_FOUND.getStatusCode())
          .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
          .body("message", equalTo("No matching rendered form for task with the id " + EXAMPLE_TASK_ID + " found."))
      .when()
        .get(RENDERED_FORM_URL);
  }

  @Test
  public void testSubmitForm() {
    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(SUBMIT_FORM_URL);

    verify(formServiceMock).submitTaskForm(EXAMPLE_TASK_ID, null);
  }

  @Test
  public void testSubmitFormWithParameters() {
    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", "aStringValue")
        .variable("anotherVariable", 42)
        .variable("aThirdValue", Boolean.TRUE).getVariables();

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(SUBMIT_FORM_URL);

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("aVariable", "aStringValue");
    expectedVariables.put("anotherVariable", 42);
    expectedVariables.put("aThirdValue", Boolean.TRUE);

    verify(formServiceMock).submitTaskForm(eq(EXAMPLE_TASK_ID), argThat(new EqualsMap(expectedVariables)));
  }

  @Test
  public void testSubmitFormWithUnparseableIntegerVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot submit task form anId: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
      .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitFormWithUnparseableShortVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot submit task form anId: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
      .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitFormWithUnparseableLongVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot submit task form anId: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
      .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitFormWithUnparseableDoubleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot submit task form anId: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
      .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitFormWithUnparseableDateVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot submit task form anId: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
      .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testSubmitFormWithNotSupportedVariableType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot submit task form anId: Unsupported value type 'X'"))
      .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testUnsuccessfulSubmitForm() {
    doThrow(new ProcessEngineException("expected exception")).when(formServiceMock).submitTaskForm(any(String.class), Matchers.<Map<String, Object>>any());

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(RestException.class.getSimpleName()))
        .body("message", equalTo("Cannot submit task form " + EXAMPLE_TASK_ID + ": expected exception"))
      .when().post(SUBMIT_FORM_URL);
  }

  @Test
  public void testGetTaskFormVariables() {

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
        .body(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME+".type",
            equalTo(VariableTypeHelper.toExpectedValueTypeName(MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE.getType())))
        .body(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME+".value",
            equalTo(MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE.getValue()))
      .when().get(FORM_VARIABLES_URL)
      .body();

    verify(formServiceMock, times(1)).getTaskFormVariables(EXAMPLE_TASK_ID, null, true);
  }

  @Test
  public void testGetTaskFormVariablesVarNames() {
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .queryParam("variableNames", "a,b,c")
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
      .when().get(FORM_VARIABLES_URL);

    verify(formServiceMock, times(1)).getTaskFormVariables(EXAMPLE_TASK_ID, Arrays.asList(new String[]{"a","b","c"}), true);
  }

  @Test
  public void testClaimTask() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", EXAMPLE_USER_ID);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(CLAIM_TASK_URL);

    verify(taskServiceMock).claim(EXAMPLE_TASK_ID, EXAMPLE_USER_ID);
  }

  @Test
  public void testMissingUserId() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", null);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(CLAIM_TASK_URL);

    verify(taskServiceMock).claim(EXAMPLE_TASK_ID, null);
  }

  @Test
  public void testUnsuccessfulClaimTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).claim(any(String.class), any(String.class));

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("expected exception"))
      .when().post(CLAIM_TASK_URL);
  }

  @Test
  public void testUnclaimTask() {
    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(UNCLAIM_TASK_URL);

    verify(taskServiceMock).setAssignee(EXAMPLE_TASK_ID, null);
  }

  @Test
  public void testUnsuccessfulUnclaimTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).setAssignee(any(String.class), any(String.class));

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("expected exception"))
      .when().post(UNCLAIM_TASK_URL);
  }

  @Test
  public void testSetAssignee() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", EXAMPLE_USER_ID);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(ASSIGNEE_TASK_URL);

    verify(taskServiceMock).setAssignee(EXAMPLE_TASK_ID, EXAMPLE_USER_ID);
  }

  @Test
  public void testMissingUserIdSetAssignee() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", null);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(ASSIGNEE_TASK_URL);

    verify(taskServiceMock).setAssignee(EXAMPLE_TASK_ID, null);
  }

  @Test
  public void testUnsuccessfulSetAssignee() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).setAssignee(any(String.class), any(String.class));

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("expected exception"))
      .when().post(ASSIGNEE_TASK_URL);
  }

  protected Map<String, Object> toExpectedJsonMap(IdentityLink identityLink) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("userId", identityLink.getUserId());
    result.put("groupId", identityLink.getGroupId());
    result.put("type", identityLink.getType());
    return result;
  }

  @Test
  public void testGetIdentityLinks() {
    Map<String, Object> expectedUserIdentityLink = toExpectedJsonMap(mockUserAssigneeIdentityLink);
    Map<String, Object> expectedGroupIdentityLink = toExpectedJsonMap(mockCandidateGroupIdentityLink);
    Map<String, Object> expectedGroupIdentityLink2 = toExpectedJsonMap(mockCandidateGroup2IdentityLink);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
        .body("$.size()", equalTo(3))
        .body("$", hasItem(expectedUserIdentityLink))
        .body("$", hasItem(expectedGroupIdentityLink))
        .body("$", hasItem(expectedGroupIdentityLink2))
      .when().get(TASK_IDENTITY_LINKS_URL);

    verify(taskServiceMock).getIdentityLinksForTask(EXAMPLE_TASK_ID);
  }

  @Test
  public void testGetIdentityLinksByType() {
    Map<String, Object> expectedGroupIdentityLink = toExpectedJsonMap(mockCandidateGroupIdentityLink);
    Map<String, Object> expectedGroupIdentityLink2 = toExpectedJsonMap(mockCandidateGroup2IdentityLink);

    given().pathParam("id", EXAMPLE_TASK_ID).queryParam("type", IdentityLinkType.CANDIDATE)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
        .body("$.size()", equalTo(2))
        .body("$", hasItem(expectedGroupIdentityLink))
        .body("$", hasItem(expectedGroupIdentityLink2))
      .when().get(TASK_IDENTITY_LINKS_URL);

    verify(taskServiceMock).getIdentityLinksForTask(EXAMPLE_TASK_ID);
  }

  @Test
  public void testAddUserIdentityLink() {
    String userId = "someUserId";
    String taskId = EXAMPLE_TASK_ID;
    String type = "someType";

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", userId);
    json.put("taskId", taskId);
    json.put("type", type);

    given().pathParam("id", taskId)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(TASK_IDENTITY_LINKS_URL);

    verify(taskServiceMock).addUserIdentityLink(taskId, userId, type);
  }

  @Test
  public void testAddGroupIdentityLink() {
    String groupId = "someGroupId";
    String taskId = EXAMPLE_TASK_ID;
    String type = "someType";

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("groupId", groupId);
    json.put("taskId", taskId);
    json.put("type", type);

    given().pathParam("id", taskId)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(TASK_IDENTITY_LINKS_URL);

    verify(taskServiceMock).addGroupIdentityLink(taskId, groupId, type);
  }

  @Test
  public void testInvalidAddIdentityLink() {
    String groupId = "someGroupId";
    String userId = "someUserId";
    String taskId = EXAMPLE_TASK_ID;
    String type = "someType";

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("groupId", groupId);
    json.put("userId", userId);
    json.put("taskId", taskId);
    json.put("type", type);

    given().pathParam("id", taskId)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Identity Link requires userId or groupId, but not both"))
      .when().post(TASK_IDENTITY_LINKS_URL);

    verify(taskServiceMock, never()).addGroupIdentityLink(anyString(), anyString(), anyString());
    verify(taskServiceMock, never()).addGroupIdentityLink(anyString(), anyString(), anyString());
  }

  @Test
  public void testUnderspecifiedAddIdentityLink() {
    String taskId = EXAMPLE_TASK_ID;
    String type = "someType";

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("taskId", taskId);
    json.put("type", type);

    given().pathParam("id", taskId)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
        .contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Identity Link requires userId or groupId"))
      .when().post(TASK_IDENTITY_LINKS_URL);

    verify(taskServiceMock, never()).addGroupIdentityLink(anyString(), anyString(), anyString());
    verify(taskServiceMock, never()).addGroupIdentityLink(anyString(), anyString(), anyString());
  }

  @Test
  public void testDeleteUserIdentityLink() {
    String deleteIdentityLinkUrl = TASK_IDENTITY_LINKS_URL + "/delete";

    String taskId = EXAMPLE_TASK_ID;
    String userId = EXAMPLE_USER_ID;
    String type = "someIdentityLinkType";

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", userId);
    json.put("type", type);

    given().pathParam("id", taskId)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(deleteIdentityLinkUrl);

    verify(taskServiceMock).deleteUserIdentityLink(taskId, userId, type);
    verify(taskServiceMock, never()).deleteGroupIdentityLink(anyString(), anyString(), anyString());
  }

  @Test
  public void testDeleteGroupIdentityLink() {
    String deleteIdentityLinkUrl = TASK_IDENTITY_LINKS_URL + "/delete";

    String taskId = EXAMPLE_TASK_ID;
    String groupId = MockProvider.EXAMPLE_GROUP_ID;
    String type = "someIdentityLinkType";

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("groupId", groupId);
    json.put("type", type);

    given().pathParam("id", taskId)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().post(deleteIdentityLinkUrl);

    verify(taskServiceMock).deleteGroupIdentityLink(taskId, groupId, type);
    verify(taskServiceMock, never()).deleteUserIdentityLink(anyString(), anyString(), anyString());
  }

  @Test
  public void testCompleteTask() {
    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(COMPLETE_TASK_URL);

    verify(taskServiceMock).complete(EXAMPLE_TASK_ID, null);
  }

  @Test
  public void testCompleteWithParameters() {
    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", "aStringValue")
        .variable("anotherVariable", 42)
        .variable("aThirdValue", Boolean.TRUE).getVariables();

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(COMPLETE_TASK_URL);

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("aVariable", "aStringValue");
    expectedVariables.put("anotherVariable", 42);
    expectedVariables.put("aThirdValue", Boolean.TRUE);

    verify(taskServiceMock).complete(eq(EXAMPLE_TASK_ID), argThat(new EqualsMap(expectedVariables)));
  }

  @Test
  public void testCompleteWithUnparseableIntegerVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot complete task anId: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
      .when().post(COMPLETE_TASK_URL);
  }

  @Test
  public void testCompleteWithUnparseableShortVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot complete task anId: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
      .when().post(COMPLETE_TASK_URL);
  }

  @Test
  public void testCompleteWithUnparseableLongVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot complete task anId: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
      .when().post(COMPLETE_TASK_URL);
  }

  @Test
  public void testCompleteWithUnparseableDoubleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot complete task anId: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
      .when().post(COMPLETE_TASK_URL);
  }

  @Test
  public void testCompleteWithUnparseableDateVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot complete task anId: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
      .when().post(COMPLETE_TASK_URL);
  }

  @Test
  public void testCompleteWithNotSupportedVariableType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot complete task anId: Unsupported value type 'X'"))
      .when().post(COMPLETE_TASK_URL);
  }

  @Test
  public void testUnsuccessfulCompleteTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).complete(any(String.class), Matchers.<Map<String, Object>>any());

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(RestException.class.getSimpleName()))
        .body("message", equalTo("Cannot complete task " + EXAMPLE_TASK_ID + ": expected exception"))
      .when().post(COMPLETE_TASK_URL);
  }

  @Test
  public void testResolveTask() {
    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", "aStringValue")
        .variable("anotherVariable", 42)
        .variable("aThirdValue", Boolean.TRUE).getVariables();

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(RESOLVE_TASK_URL);

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("aVariable", "aStringValue");
    expectedVariables.put("anotherVariable", 42);
    expectedVariables.put("aThirdValue", Boolean.TRUE);

    verify(taskServiceMock).resolveTask(eq(EXAMPLE_TASK_ID), argThat(new EqualsMap(expectedVariables)));
  }

  @Test
  public void testResolveTaskWithUnparseableIntegerVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot resolve task anId: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Integer.class)))
      .when().post(RESOLVE_TASK_URL);
  }

  @Test
  public void testResolveTaskWithUnparseableShortVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Short";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot resolve task anId: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Short.class)))
      .when().post(RESOLVE_TASK_URL);
  }

  @Test
  public void testResolveTaskWithUnparseableLongVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Long";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot resolve task anId: "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Long.class)))
      .when().post(RESOLVE_TASK_URL);
  }

  @Test
  public void testResolveTaskWithUnparseableDoubleVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Double";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot resolve task anId: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Double.class)))
      .when().post(RESOLVE_TASK_URL);
  }

  @Test
  public void testResolveTaskWithUnparseableDateVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "Date";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot resolve task anId: "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, variableType, Date.class)))
      .when().post(RESOLVE_TASK_URL);
  }

  @Test
  public void testResolveTaskWithNotSupportedVariableType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String variableType = "X";

    Map<String, Object> variableJson = VariablesBuilder.create().variable(variableKey, variableValue, variableType).getVariables();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variables", variableJson);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(variables)
      .then().expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("Cannot resolve task anId: Unsupported value type 'X'"))
      .when().post(RESOLVE_TASK_URL);
  }

  @Test
  public void testUnsuccessfulResolving() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).resolveTask(any(String.class), any(Map.class));

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("expected exception"))
      .when().post(RESOLVE_TASK_URL);
  }

  @Test
  public void testGetNonExistingTask() {
    when(mockQuery.singleResult()).thenReturn(null);

    given().pathParam("id", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("No matching task with id " + NON_EXISTING_ID))
      .when().get(SINGLE_TASK_URL);
  }

  @Test
  public void testGetNonExistingForm() {
    when(formServiceMock.getTaskFormData(anyString())).thenThrow(new ProcessEngineException("Expected exception: task does not exist."));

    given().pathParam("id", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot get form for task " + NON_EXISTING_ID))
      .when().get(TASK_FORM_URL);
  }

  @Test
  public void testDelegateTask() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", EXAMPLE_USER_ID);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(DELEGATE_TASK_URL);

    verify(taskServiceMock).delegateTask(EXAMPLE_TASK_ID, EXAMPLE_USER_ID);
  }

  @Test
  public void testUnsuccessfulDelegateTask() {
    doThrow(new ProcessEngineException("expected exception")).when(taskServiceMock).delegateTask(any(String.class), any(String.class));

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", EXAMPLE_USER_ID);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("expected exception"))
      .when().post(DELEGATE_TASK_URL);
  }

  @Test
  public void testGetSingleTaskComment() {
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("commentId", EXAMPLE_TASK_COMMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
      .body("id", equalTo(EXAMPLE_TASK_COMMENT_ID))
      .body("taskId", equalTo(EXAMPLE_TASK_ID))
      .body("userId", equalTo(EXAMPLE_USER_ID))
      .body("time", equalTo(EXAMPLE_TASK_COMMENT_TIME))
      .body("message", equalTo(EXAMPLE_TASK_COMMENT_FULL_MESSAGE))
    .when()
      .get(SINGLE_TASK_SINGLE_COMMENT_URL);
  }

  @Test
  public void testGetSingleTaskCommentWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("commentId", EXAMPLE_TASK_COMMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("History is not enabled"))
    .when()
      .get(SINGLE_TASK_SINGLE_COMMENT_URL);
  }

  @Test
  public void testGetSingleTaskCommentForNonExistingComment() {
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("commentId", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("Task comment with id " + NON_EXISTING_ID + " does not exist for task id '" + EXAMPLE_TASK_ID + "'."))
    .when().get(SINGLE_TASK_SINGLE_COMMENT_URL);
  }

  @Test
  public void testGetSingleTaskCommentForNonExistingCommentWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("commentId", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("History is not enabled"))
    .when().get(SINGLE_TASK_SINGLE_COMMENT_URL);
  }

  @Test
  public void testGetSingleTaskCommentForNonExistingTask() {
    given()
      .pathParam("id", NON_EXISTING_ID)
      .pathParam("commentId", EXAMPLE_TASK_COMMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("Task comment with id " + EXAMPLE_TASK_COMMENT_ID + " does not exist for task id '" + NON_EXISTING_ID + "'"))
    .when()
      .get(SINGLE_TASK_SINGLE_COMMENT_URL);
  }

  @Test
  public void testGetSingleTaskCommentForNonExistingTaskWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", NON_EXISTING_ID)
      .pathParam("commentId", EXAMPLE_TASK_COMMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("History is not enabled"))
    .when()
      .get(SINGLE_TASK_SINGLE_COMMENT_URL);
  }

  @Test
  public void testGetTaskComments() {
    Response response = given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
      .body("$.size()", equalTo(1))
    .when()
      .get(SINGLE_TASK_COMMENTS_URL);

    verifyTaskComments(mockTaskComments, response);
    verify(taskServiceMock).getTaskComments(EXAMPLE_TASK_ID);
  }

  @Test
  public void testGetTaskCommentsWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
      .body("$.size()", equalTo(0))
    .when()
      .get(SINGLE_TASK_COMMENTS_URL);
  }

  @Test
  public void testGetTaskNonExistingComments() {
    when(taskServiceMock.getTaskComments(EXAMPLE_TASK_ID)).thenReturn(Collections.<Comment>emptyList());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
      .body("$.size()", equalTo(0))
    .when()
      .get(SINGLE_TASK_COMMENTS_URL);
  }

  @Test
  public void testGetTaskNonExistingCommentsWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
      .body("$.size()", equalTo(0))
    .when()
      .get(SINGLE_TASK_COMMENTS_URL);
  }

  @Test
  public void testGetTaskCommentsForNonExistingTask() {
    when(historicTaskInstanceQueryMock.taskId(NON_EXISTING_ID)).thenReturn(historicTaskInstanceQueryMock);
    when(historicTaskInstanceQueryMock.singleResult()).thenReturn(null);

    given()
      .pathParam("id", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .contentType(ContentType.JSON)
      .body(containsString("No task found for task id " + NON_EXISTING_ID))
    .when()
      .get(SINGLE_TASK_COMMENTS_URL);
  }

  @Test
  public void testGetTaskCommentsForNonExistingTaskWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
      .body("$.size()", equalTo(0))
    .when()
      .get(SINGLE_TASK_COMMENTS_URL);
  }

  @Test
  public void testAddCompleteTaskComment() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("message", EXAMPLE_TASK_COMMENT_FULL_MESSAGE);

    Response response = given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(ContentType.JSON)
      .body(json)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when()
      .post(SINGLE_TASK_ADD_COMMENT_URL);

    verify(taskServiceMock).createComment(EXAMPLE_TASK_ID, null, EXAMPLE_TASK_COMMENT_FULL_MESSAGE);

    verifyCreatedTaskComment(mockTaskComment, response);
  }

  @Test
  public void testAddCompleteTaskCommentWithHistoryDisabled() {

    mockHistoryDisabled();

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("message", EXAMPLE_TASK_COMMENT_FULL_MESSAGE);

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .contentType(ContentType.JSON)
      .body(json)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body(containsString("History is not enabled"))
    .when()
      .post(SINGLE_TASK_ADD_COMMENT_URL);
  }

  @Test
  public void testAddCommentToNonExistingTask() {
    when(historicTaskInstanceQueryMock.taskId(eq(NON_EXISTING_ID))).thenReturn(historicTaskInstanceQueryMock);
    when(historicTaskInstanceQueryMock.singleResult()).thenReturn(null);

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("message", EXAMPLE_TASK_COMMENT_FULL_MESSAGE);

    given()
      .pathParam("id", NON_EXISTING_ID)
      .contentType(ContentType.JSON)
      .body(json)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("No task found for task id " + NON_EXISTING_ID))
    .when()
      .post(SINGLE_TASK_ADD_COMMENT_URL);
  }

  @Test
  public void testAddCommentToNonExistingTaskWithHistoryDisabled() {
    mockHistoryDisabled();

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("message", EXAMPLE_TASK_COMMENT_FULL_MESSAGE);

    given()
      .pathParam("id", NON_EXISTING_ID)
      .contentType(ContentType.JSON)
      .body(json)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body(containsString("History is not enabled"))
    .when()
      .post(SINGLE_TASK_ADD_COMMENT_URL);
  }

  @Test
  public void testAddTaskCommentWithoutBody() {
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode())
    .when()
      .post(SINGLE_TASK_ADD_COMMENT_URL);
  }

  @Test
  public void testAddTaskCommentWithoutMessage() {

    doThrow(new ProcessEngineException("Message is null")).when(taskServiceMock).createComment(EXAMPLE_TASK_ID, null, null);

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .contentType(ContentType.JSON)
      .body(EMPTY_JSON_OBJECT)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("Not enough parameters submitted"))
    .when()
      .post(SINGLE_TASK_ADD_COMMENT_URL);
  }

  @Test
  public void testGetSingleTaskAttachment() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .pathParam("attachmentId", MockProvider.EXAMPLE_TASK_ATTACHMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect().statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
      .body("id", equalTo(MockProvider.EXAMPLE_TASK_ATTACHMENT_ID))
      .body("taskId", equalTo(MockProvider.EXAMPLE_TASK_ID))
      .body("description", equalTo(MockProvider.EXAMPLE_TASK_ATTACHMENT_DESCRIPTION))
      .body("type", equalTo(MockProvider.EXAMPLE_TASK_ATTACHMENT_TYPE))
      .body("name", equalTo(MockProvider.EXAMPLE_TASK_ATTACHMENT_NAME))
      .body("url", equalTo(MockProvider.EXAMPLE_TASK_ATTACHMENT_URL))
    .when().get(SINGLE_TASK_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testGetSingleTaskAttachmentWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("attachmentId", EXAMPLE_TASK_ATTACHMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
        .body(containsString("History is not enabled"))
      .when()
        .get(SINGLE_TASK_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testGetSingleTaskAttachmentForNonExistingAttachmentId() {
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("attachmentId", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body(containsString("Task attachment with id " + NON_EXISTING_ID + " does not exist for task id '" + EXAMPLE_TASK_ID +  "'."))
    .when().get(SINGLE_TASK_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testGetSingleTaskAttachmentForNonExistingAttachmentIdWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("attachmentId", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("History is not enabled"))
    .when().get(SINGLE_TASK_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testGetSingleTaskAttachmentForNonExistingTask() {
    given()
      .pathParam("id", NON_EXISTING_ID)
      .pathParam("attachmentId", EXAMPLE_TASK_ATTACHMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("Task attachment with id " + EXAMPLE_TASK_ATTACHMENT_ID + " does not exist for task id '" + NON_EXISTING_ID + "'"))
    .when()
      .get(SINGLE_TASK_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testGetSingleTaskAttachmentForNonExistingTaskWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", NON_EXISTING_ID)
      .pathParam("attachmentId", EXAMPLE_TASK_ATTACHMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("History is not enabled"))
    .when()
      .get(SINGLE_TASK_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testGetTaskAttachments() {
    Response response = given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
        .body("$.size()", equalTo(1))
      .when().get(SINGLE_TASK_ATTACHMENTS_URL);

    verifyTaskAttachments(mockTaskAttachments, response);
    verify(taskServiceMock).getTaskAttachments(MockProvider.EXAMPLE_TASK_ID);
  }

  @Test
  public void testGetTaskAttachmentsWithHistoryDisabled() {
    mockHistoryDisabled();

    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
      .body("$.size()", equalTo(0))
    .when().get(SINGLE_TASK_ATTACHMENTS_URL);
  }

  @Test
  public void testGetTaskAttachmentsForNonExistingTaskId() {
    when(historicTaskInstanceQueryMock.taskId(NON_EXISTING_ID)).thenReturn(historicTaskInstanceQueryMock);
    when(historicTaskInstanceQueryMock.singleResult()).thenReturn(null);

    given().pathParam("id", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body(containsString("No task found for task id " + NON_EXISTING_ID))
    .when().get(SINGLE_TASK_ATTACHMENTS_URL);
  }

  @Test
  public void testGetTaskAttachmentsForNonExistingTaskWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
      .body("$.size()", equalTo(0))
    .when()
      .get(SINGLE_TASK_ATTACHMENTS_URL);
  }

  @Test
  public void testGetTaskAttachmentsForNonExistingAttachments() {
    when(taskServiceMock.getTaskAttachments(EXAMPLE_TASK_ID)).thenReturn(Collections.<Attachment>emptyList());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
      .body("$.size()", equalTo(0))
    .when()
      .get(SINGLE_TASK_ATTACHMENTS_URL);
  }

  @Test
  public void testGetTaskAttachmentsForNonExistingAttachmentsWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
      .body("$.size()", equalTo(0))
    .when()
      .get(SINGLE_TASK_ATTACHMENTS_URL);
  }

  @Test
  public void testCreateCompleteTaskAttachmentWithContent() {
    Response response = given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .multiPart("attachment-name", EXAMPLE_TASK_ATTACHMENT_NAME)
      .multiPart("attachment-description", EXAMPLE_TASK_ATTACHMENT_DESCRIPTION)
      .multiPart("attachment-type", EXAMPLE_TASK_ATTACHMENT_TYPE)
      .multiPart("content", createMockByteData())
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(SINGLE_TASK_ADD_ATTACHMENT_URL);

    verifyCreatedTaskAttachment(mockTaskAttachment, response, false);
  }

  @Test
  public void testCreateTaskAttachmentWithContentToNonExistingTask() {
    when(historicTaskInstanceQueryMock.taskId(eq(NON_EXISTING_ID))).thenReturn(historicTaskInstanceQueryMock);
    when(historicTaskInstanceQueryMock.singleResult()).thenReturn(null);

    given()
    .pathParam("id", NON_EXISTING_ID)
      .multiPart("attachment-name", EXAMPLE_TASK_ATTACHMENT_NAME)
      .multiPart("attachment-description", EXAMPLE_TASK_ATTACHMENT_DESCRIPTION)
      .multiPart("attachment-type", EXAMPLE_TASK_ATTACHMENT_TYPE)
      .multiPart("content", createMockByteData())
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("No task found for task id " + NON_EXISTING_ID))
    .when()
      .post(SINGLE_TASK_ADD_ATTACHMENT_URL);
  }

  @Test
  public void testCreateCompleteTaskAttachmentWithUrl() {
    Response response = given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .multiPart("attachment-name", EXAMPLE_TASK_ATTACHMENT_NAME)
      .multiPart("attachment-description", EXAMPLE_TASK_ATTACHMENT_DESCRIPTION)
      .multiPart("attachment-type", EXAMPLE_TASK_ATTACHMENT_TYPE)
      .multiPart("url", EXAMPLE_TASK_ATTACHMENT_URL)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(SINGLE_TASK_ADD_ATTACHMENT_URL);

    verifyCreatedTaskAttachment(mockTaskAttachment, response, true);
  }

  @Test
  public void testCreateCompleteTaskAttachmentWithUrlWithHistoryDisabled() {

    mockHistoryDisabled();

    given()
        .pathParam("id", EXAMPLE_TASK_ID)
        .multiPart("attachment-name", EXAMPLE_TASK_ATTACHMENT_NAME)
        .multiPart("attachment-description", EXAMPLE_TASK_ATTACHMENT_DESCRIPTION)
        .multiPart("attachment-type", EXAMPLE_TASK_ATTACHMENT_TYPE)
        .multiPart("url", EXAMPLE_TASK_ATTACHMENT_URL)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body(containsString("History is not enabled"))
      .when()
        .post(SINGLE_TASK_ADD_ATTACHMENT_URL);
  }

  @Test
  public void testCreateTaskAttachmentWithUrlToNonExistingTask() {
    when(historicTaskInstanceQueryMock.taskId(eq(NON_EXISTING_ID))).thenReturn(historicTaskInstanceQueryMock);
    when(historicTaskInstanceQueryMock.singleResult()).thenReturn(null);

    given()
    .pathParam("id", NON_EXISTING_ID)
      .multiPart("attachment-name", EXAMPLE_TASK_ATTACHMENT_NAME)
      .multiPart("attachment-description", EXAMPLE_TASK_ATTACHMENT_DESCRIPTION)
      .multiPart("attachment-type", EXAMPLE_TASK_ATTACHMENT_TYPE)
      .multiPart("url", EXAMPLE_TASK_ATTACHMENT_URL)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("No task found for task id " + NON_EXISTING_ID))
    .when()
      .post(SINGLE_TASK_ADD_ATTACHMENT_URL);
  }

  @Test
  public void testCreateTaskAttachmentWithUrlToNonExistingTaskWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
    .pathParam("id", NON_EXISTING_ID)
      .multiPart("attachment-name", EXAMPLE_TASK_ATTACHMENT_NAME)
      .multiPart("attachment-description", EXAMPLE_TASK_ATTACHMENT_DESCRIPTION)
      .multiPart("attachment-type", EXAMPLE_TASK_ATTACHMENT_TYPE)
      .multiPart("url", EXAMPLE_TASK_ATTACHMENT_URL)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body(containsString("History is not enabled"))
    .when()
      .post(SINGLE_TASK_ADD_ATTACHMENT_URL);
  }

  @Test
  public void testCreateTaskAttachmentWithoutMultiparts() {
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode())
    .when()
      .post(SINGLE_TASK_ADD_ATTACHMENT_URL);
  }

  @Test
  public void testGetSingleTaskAttachmentContent() {
    Response response = given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .pathParam("attachmentId", MockProvider.EXAMPLE_TASK_ATTACHMENT_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when().get(SINGLE_TASK_SINGLE_ATTACHMENT_DATA_URL);

    byte[] responseContent = IoUtil.readInputStream(response.asInputStream(), "attachmentContent");
    assertEquals("someContent", new String(responseContent));
  }

  @Test
  public void testGetSingleTaskAttachmentContentWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
    .pathParam("id", EXAMPLE_TASK_ID)
    .pathParam("attachmentId", EXAMPLE_TASK_ATTACHMENT_ID)
    .then().expect()
    .statusCode(Status.NOT_FOUND.getStatusCode())
    .body(containsString("History is not enabled"))
    .when()
    .get(SINGLE_TASK_SINGLE_ATTACHMENT_DATA_URL);
  }

  @Test
  public void testGetSingleTaskAttachmentContentForNonExistingAttachmentId() {
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("attachmentId", NON_EXISTING_ID)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("Attachment content for attachment with id '" + NON_EXISTING_ID + "' does not exist for task id '" + EXAMPLE_TASK_ID + "'."))
    .when().get(SINGLE_TASK_SINGLE_ATTACHMENT_DATA_URL);
  }

  @Test
  public void testGetSingleTaskAttachmentContentForNonExistingAttachmentIdWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("attachmentId", NON_EXISTING_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("History is not enabled"))
    .when().get(SINGLE_TASK_SINGLE_ATTACHMENT_DATA_URL);
  }

  @Test
  public void testGetSingleTaskAttachmentContentForNonExistingTask() {
    given()
      .pathParam("id", NON_EXISTING_ID)
      .pathParam("attachmentId", EXAMPLE_TASK_ATTACHMENT_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("Attachment content for attachment with id '" + EXAMPLE_TASK_ATTACHMENT_ID + "' does not exist for task id '" + NON_EXISTING_ID + "'."))
    .when()
      .get(SINGLE_TASK_SINGLE_ATTACHMENT_DATA_URL);
  }

  @Test
  public void testGetSingleTaskAttachmentContentForNonExistingTaskWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", NON_EXISTING_ID)
      .pathParam("attachmentId", EXAMPLE_TASK_ATTACHMENT_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("History is not enabled"))
    .when()
      .get(SINGLE_TASK_SINGLE_ATTACHMENT_DATA_URL);
  }

  @Test
  public void testDeleteSingleTaskAttachment() {
    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .pathParam("attachmentId", MockProvider.EXAMPLE_TASK_ATTACHMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when().delete(SINGLE_TASK_DELETE_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testDeleteSingleTaskAttachmentWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("attachmentId", EXAMPLE_TASK_ATTACHMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body(containsString("History is not enabled"))
    .when()
    .delete(SINGLE_TASK_DELETE_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testDeleteSingleTaskAttachmentForNonExistingAttachmentId() {
    doThrow(new ProcessEngineException()).when(taskServiceMock).deleteTaskAttachment(EXAMPLE_TASK_ID, NON_EXISTING_ID);

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("attachmentId", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body(containsString("Deletion is not possible. No attachment exists for task id '" + EXAMPLE_TASK_ID + "' and attachment id '" + NON_EXISTING_ID + "'."))
    .when().delete(SINGLE_TASK_DELETE_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testDeleteSingleTaskAttachmentForNonExistingAttachmentIdWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("attachmentId", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body(containsString("History is not enabled"))
    .when().delete(SINGLE_TASK_DELETE_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testDeleteSingleTaskAttachmentForNonExistingTask() {
    doThrow(new ProcessEngineException()).when(taskServiceMock).deleteTaskAttachment(NON_EXISTING_ID, EXAMPLE_TASK_ATTACHMENT_ID);

    given()
      .pathParam("id", NON_EXISTING_ID)
      .pathParam("attachmentId", EXAMPLE_TASK_ATTACHMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body(containsString("Deletion is not possible. No attachment exists for task id '" + NON_EXISTING_ID + "' and attachment id '" + EXAMPLE_TASK_ATTACHMENT_ID + "'."))
    .when()
      .delete(SINGLE_TASK_DELETE_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testDeleteSingleTaskAttachmentForNonExistingTaskWithHistoryDisabled() {
    mockHistoryDisabled();

    given()
      .pathParam("id", NON_EXISTING_ID)
      .pathParam("attachmentId", EXAMPLE_TASK_ATTACHMENT_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body(containsString("History is not enabled"))
    .when()
      .delete(SINGLE_TASK_DELETE_SINGLE_ATTACHMENT_URL);
  }

  @Test
  public void testGetLocalVariables() {
    Response response = given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(EXAMPLE_VARIABLE_KEY, notNullValue())
      .body(EXAMPLE_VARIABLE_KEY + ".value", equalTo(EXAMPLE_VARIABLE_VALUE.getValue()))
      .body(EXAMPLE_VARIABLE_KEY + ".type", equalTo(VariableTypeHelper.toExpectedValueTypeName(EXAMPLE_VARIABLE_VALUE.getType())))
      .when().get(SINGLE_TASK_VARIABLES_URL);

    Assert.assertEquals("Should return exactly one variable", 1, response.jsonPath().getMap("").size());
  }

  @Test
  public void testGetLocalObjectVariables() {
    // given
    String variableKey = "aVariableId";

    List<String> payload = Arrays.asList("a", "b");
    ObjectValue variableValue =
        MockObjectValue
            .fromObjectValue(Variables
                .objectValue(payload)
                .serializationDataFormat("application/json")
                .create())
            .objectTypeName(ArrayList.class.getName())
            .serializedValue("a serialized value"); // this should differ from the serialized json

    when(taskServiceMock.getVariablesLocalTyped(eq(EXAMPLE_TASK_ID), anyBoolean()))
      .thenReturn(Variables.createVariables().putValueTyped(variableKey, variableValue));

    // when
    given().pathParam("id", EXAMPLE_TASK_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body(variableKey + ".value", equalTo(payload))
      .body(variableKey + ".type", equalTo("Object"))
      .body(variableKey + ".valueInfo." + ObjectTypeImpl.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body(variableKey + ".valueInfo." + ObjectTypeImpl.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_TASK_VARIABLES_URL);

    // then
    verify(taskServiceMock).getVariablesLocalTyped(EXAMPLE_TASK_ID, true);
  }

  @Test
  public void testGetLocalObjectVariablesSerialized() {
    // given
    String variableKey = "aVariableId";

    ObjectValue variableValue =
        Variables
          .serializedObjectValue("a serialized value")
          .serializationDataFormat("application/json")
          .objectTypeName(ArrayList.class.getName())
          .create();

    when(taskServiceMock.getVariablesLocalTyped(eq(EXAMPLE_TASK_ID), anyBoolean()))
      .thenReturn(Variables.createVariables().putValueTyped(variableKey, variableValue));

    // when
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .queryParam("deserializeValues", false)
    .then().expect().statusCode(Status.OK.getStatusCode())
      .body(variableKey + ".value", equalTo("a serialized value"))
      .body(variableKey + ".type", equalTo("Object"))
      .body(variableKey + ".valueInfo." + ObjectTypeImpl.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body(variableKey + ".valueInfo." + ObjectTypeImpl.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_TASK_VARIABLES_URL);

    // then
    verify(taskServiceMock).getVariablesLocalTyped(EXAMPLE_TASK_ID, false);
  }

  @Test
  public void testGetLocalVariablesForNonExistingTaskId() {
    when(taskServiceMock.getVariablesLocalTyped(NON_EXISTING_ID, true)).thenThrow(new ProcessEngineException("task " + NON_EXISTING_ID + " doesn't exist"));

    given().pathParam("id", NON_EXISTING_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
      .body("message", equalTo("task " + NON_EXISTING_ID + " doesn't exist"))
      .when().get(SINGLE_TASK_VARIABLES_URL);
  }

  @Test
  public void testLocalVariableModification() {
    TaskServiceImpl taskServiceMock = mockTaskServiceImpl();

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    String variableKey = "aKey";
    int variableValue = 123;

    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    List<String> deletions = new ArrayList<String>();
    deletions.add("deleteKey");
    messageBodyJson.put("deletions", deletions);

    given().pathParam("id", EXAMPLE_TASK_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(SINGLE_TASK_MODIFY_VARIABLES_URL);

    Map<String, Object> expectedModifications = new HashMap<String, Object>();
    expectedModifications.put(variableKey, variableValue);
    verify(taskServiceMock).updateVariablesLocal(eq(EXAMPLE_TASK_ID), argThat(new EqualsMap(expectedModifications)),
        argThat(new EqualsList(deletions)));
  }

  @Test
  public void testLocalVariableModificationForNonExistingTaskId() {
    TaskServiceImpl taskServiceMock = mockTaskServiceImpl();
    doThrow(new ProcessEngineException("Cannot find task with id " + NON_EXISTING_ID)).when(taskServiceMock).updateVariablesLocal(anyString(), any(Map.class), any(List.class));

    Map<String, Object> messageBodyJson = new HashMap<String, Object>();

    String variableKey = "aKey";
    int variableValue = 123;
    Map<String, Object> modifications = VariablesBuilder.create().variable(variableKey, variableValue).getVariables();
    messageBodyJson.put("modifications", modifications);

    given().pathParam("id", NON_EXISTING_ID).contentType(ContentType.JSON).body(messageBodyJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(RestException.class.getSimpleName()))
      .body("message", equalTo("Cannot modify variables for task " + NON_EXISTING_ID + ": Cannot find task with id " + NON_EXISTING_ID))
      .when().post(SINGLE_TASK_MODIFY_VARIABLES_URL);
  }

  @Test
  public void testEmptyLocalVariableModification() {
    mockTaskServiceImpl();

    given().pathParam("id", EXAMPLE_TASK_ID).contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(SINGLE_TASK_MODIFY_VARIABLES_URL);
  }

  @Test
  public void testGetSingleLocalVariable() {
    String variableKey = "aVariableKey";
    int variableValue = 123;

    when(taskServiceMock.getVariableLocalTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean()))
      .thenReturn(Variables.integerValue(variableValue));

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", is(123))
      .body("type", is("Integer"))
      .when().get(SINGLE_TASK_SINGLE_VARIABLE_URL);
  }


  @Test
  public void testGetSingleLocalVariableData() {

    when(taskServiceMock.getVariableLocalTyped(anyString(), eq(EXAMPLE_BYTES_VARIABLE_KEY), eq(false))).thenReturn(EXAMPLE_VARIABLE_VALUE_BYTES);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .pathParam("varId", EXAMPLE_BYTES_VARIABLE_KEY)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .when()
      .get(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).getVariableLocalTyped(MockProvider.EXAMPLE_TASK_ID, EXAMPLE_BYTES_VARIABLE_KEY, false);
  }

  @Test
  public void testGetSingleLocalVariableDataNonExisting() {

    when(taskServiceMock.getVariableLocalTyped(anyString(), eq("nonExisting"), eq(false))).thenReturn(null);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .pathParam("varId", "nonExisting")
    .then()
      .expect()
        .statusCode(Status.NOT_FOUND.getStatusCode())
        .body("type", is(InvalidRequestException.class.getSimpleName()))
        .body("message", is("task variable with name " + "nonExisting" + " does not exist"))
    .when()
      .get(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).getVariableLocalTyped(MockProvider.EXAMPLE_TASK_ID, "nonExisting", false);
  }

  @Test
  public void testGetSingleLocalVariabledataNotBinary() {

    when(taskServiceMock.getVariableLocalTyped(anyString(), eq(EXAMPLE_VARIABLE_KEY), eq(false))).thenReturn(EXAMPLE_VARIABLE_VALUE);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .pathParam("varId", EXAMPLE_VARIABLE_KEY)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .get(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).getVariableLocalTyped(MockProvider.EXAMPLE_TASK_ID, EXAMPLE_VARIABLE_KEY, false);
  }

  @Test
  public void testGetSingleLocalObjectVariable() {
    // given
    String variableKey = "aVariableId";

    List<String> payload = Arrays.asList("a", "b");
    ObjectValue variableValue =
        MockObjectValue
            .fromObjectValue(Variables
                .objectValue(payload)
                .serializationDataFormat("application/json")
                .create())
            .objectTypeName(ArrayList.class.getName())
            .serializedValue("a serialized value"); // this should differ from the serialized json

    when(taskServiceMock.getVariableLocalTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    // when
    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", equalTo(payload))
      .body("type", equalTo("Object"))
      .body("valueInfo." + ObjectTypeImpl.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body("valueInfo." + ObjectTypeImpl.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_TASK_SINGLE_VARIABLE_URL);

    // then
    verify(taskServiceMock).getVariableLocalTyped(EXAMPLE_TASK_ID, variableKey, true);
  }

  @Test
  public void testGetSingleLocalObjectVariableSerialized() {
    // given
    String variableKey = "aVariableId";

    ObjectValue variableValue =
        Variables
          .serializedObjectValue("a serialized value")
          .serializationDataFormat("application/json")
          .objectTypeName(ArrayList.class.getName())
          .create();

    when(taskServiceMock.getVariableLocalTyped(eq(EXAMPLE_TASK_ID), eq(variableKey), anyBoolean())).thenReturn(variableValue);

    // when
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .pathParam("varId", variableKey)
      .queryParam("deserializeValue", false)
    .then().expect().statusCode(Status.OK.getStatusCode())
      .body("value", equalTo("a serialized value"))
      .body("type", equalTo("Object"))
      .body("valueInfo." + ObjectTypeImpl.VALUE_INFO_SERIALIZATION_DATA_FORMAT, equalTo("application/json"))
      .body("valueInfo." + ObjectTypeImpl.VALUE_INFO_OBJECT_TYPE_NAME, equalTo(ArrayList.class.getName()))
      .when().get(SINGLE_TASK_SINGLE_VARIABLE_URL);

    // then
    verify(taskServiceMock).getVariableLocalTyped(EXAMPLE_TASK_ID, variableKey, false);
  }

  @Test
  public void testNonExistingLocalVariable() {
    String variableKey = "aVariableKey";

    when(taskServiceMock.getVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey))).thenReturn(null);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode())
      .body("type", is(InvalidRequestException.class.getSimpleName()))
      .body("message", is("task variable with name " + variableKey + " does not exist"))
      .when().get(SINGLE_TASK_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testGetLocalVariableForNonExistingTaskId() {
    String variableKey = "aVariableKey";

    when(taskServiceMock.getVariableLocalTyped(eq(NON_EXISTING_ID), eq(variableKey), anyBoolean()))
      .thenThrow(new ProcessEngineException("task " + NON_EXISTING_ID + " doesn't exist"));

    given().pathParam("id", NON_EXISTING_ID).pathParam("varId", variableKey)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot get task variable " + variableKey + ": task " + NON_EXISTING_ID + " doesn't exist"))
      .when().get(SINGLE_TASK_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleLocalVariable() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsUntypedValue.matcher().value(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithTypeInteger() {
    String variableKey = "aVariableKey";
    Integer variableValue = 123;
    String type = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.integerValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableInteger() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Integer";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " + variableKey + ": "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Integer.class)))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeShort() {
    String variableKey = "aVariableKey";
    Short variableValue = 123;
    String type = "Short";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.shortValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableShort() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Short";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " +  variableKey + ": "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Short.class)))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeLong() {
    String variableKey = "aVariableKey";
    Long variableValue = Long.valueOf(123);
    String type = "Long";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.longValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableLong() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Long";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", MockProvider.EXAMPLE_EXECUTION_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " + variableKey + ": "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Long.class)))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeDouble() {
    String variableKey = "aVariableKey";
    Double variableValue = 123.456;
    String type = "Double";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.doubleValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableDouble() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Double";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " + variableKey + ": "
            + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Double.class)))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithTypeBoolean() {
    String variableKey = "aVariableKey";
    Boolean variableValue = true;
    String type = "Boolean";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.booleanValue(variableValue)));
  }

  @Test
  public void testPutSingleVariableWithTypeDate() throws Exception {
    Date now = new Date();
    SimpleDateFormat pattern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    String variableKey = "aVariableKey";
    String variableValue = pattern.format(now);
    String type = "Date";

    Date expectedValue = pattern.parse(variableValue);

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.dateValue(expectedValue)));
  }

  @Test
  public void testPutSingleVariableWithUnparseableDate() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "Date";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " + variableKey + ": "
          + ErrorMessageHelper.getExpectedFailingConversionMessage(variableValue, type, Date.class)))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleVariableWithNotSupportedType() {
    String variableKey = "aVariableKey";
    String variableValue = "1abc";
    String type = "X";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, type);

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable " + variableKey + ": Unsupported value type 'X'"))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleLocalVariableWithNoValue() {
    String variableKey = "aVariableKey";

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(EMPTY_JSON_OBJECT)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsNullValue.matcher()));
  }

  @Test
  public void testPutLocalVariableForNonExistingTaskId() {
    String variableKey = "aVariableKey";
    String variableValue = "aVariableValue";

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue);

    doThrow(new ProcessEngineException("Cannot find task with id " + NON_EXISTING_ID))
      .when(taskServiceMock).setVariableLocal(eq(NON_EXISTING_ID), eq(variableKey), any());

    given().pathParam("id", NON_EXISTING_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(variableJson)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot put task variable " + variableKey + ": Cannot find task with id " + NON_EXISTING_ID))
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPostSingleLocalBinaryVariable() throws Exception {
    byte[] bytes = "someContent".getBytes();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", "unspecified", bytes)
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPostSingleLocalBinaryVariableWithNoValue() throws Exception {
    byte[] bytes = new byte[0];

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", "unspecified", bytes)
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsPrimitiveValue.bytesValue(bytes)));
  }

  @Test
  public void testPostSingleLocalSerializableVariable() throws Exception {

    ArrayList<String> serializable = new ArrayList<String>();
    serializable.add("foo");

    ObjectMapper mapper = new ObjectMapper();
    String jsonBytes = mapper.writeValueAsString(serializable);
    String typeName = TypeFactory.type(serializable.getClass()).toCanonical();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", jsonBytes, MediaType.APPLICATION_JSON)
      .multiPart("type", typeName, MediaType.TEXT_PLAIN)
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsObjectValue.objectValueMatcher().isDeserialized().value(serializable)));
  }

  @Test
  public void testPostSingleLocalSerializableVariableUnsupportedMediaType() throws Exception {

    ArrayList<String> serializable = new ArrayList<String>();
    serializable.add("foo");

    ObjectMapper mapper = new ObjectMapper();
    String jsonBytes = mapper.writeValueAsString(serializable);
    String typeName = TypeFactory.type(serializable.getClass()).toCanonical();

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .multiPart("data", jsonBytes, "unsupported")
      .multiPart("type", typeName, MediaType.TEXT_PLAIN)
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body(containsString("Unrecognized content type for serialized java type: unsupported"))
    .when()
      .post(SINGLE_TASK_SINGLE_BINARY_VARIABLE_URL);

    verify(taskServiceMock, never()).setVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey),
        eq(serializable));
  }

  @Test
  public void testPutSingleLocalVariableFromSerialized() throws Exception {
    String serializedValue = "{\"prop\" : \"value\"}";
    Map<String, Object> requestJson = VariablesBuilder
        .getObjectValueMap(serializedValue, ValueType.OBJECT.getName(), "aDataFormat", "aRootType");

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON)
      .body(requestJson)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(
        eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsObjectValue.objectValueMatcher()
          .serializedValue(serializedValue)
          .serializationFormat("aDataFormat")
          .objectTypeName("aRootType")));
  }

  @Test
  public void testPutSingleLocalVariableFromInvalidSerialized() throws Exception {
    String serializedValue = "{\"prop\" : \"value\"}";

    Map<String, Object> requestJson = VariablesBuilder
        .getObjectValueMap(serializedValue, "aNonExistingType", null, null);

    String variableKey = "aVariableKey";

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON)
      .body(requestJson)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Cannot put task variable aVariableKey: Unsupported value type 'aNonExistingType'"))
    .when()
      .put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPutSingleLocalVariableFromSerializedWithNoValue() {
    String variableKey = "aVariableKey";

    Map<String, Object> requestJson = VariablesBuilder
        .getObjectValueMap(null, ValueType.OBJECT.getName(), null, null);

    given().pathParam("id", MockProvider.EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .contentType(ContentType.JSON).body(requestJson)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().put(SINGLE_TASK_PUT_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).setVariableLocal(
        eq(MockProvider.EXAMPLE_TASK_ID), eq(variableKey),
        argThat(EqualsObjectValue.objectValueMatcher()));
  }

  @Test
  public void testDeleteSingleLocalVariable() {
    String variableKey = "aVariableKey";

    given().pathParam("id", EXAMPLE_TASK_ID).pathParam("varId", variableKey)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.NO_CONTENT.getStatusCode())
      .when().delete(SINGLE_TASK_DELETE_SINGLE_VARIABLE_URL);

    verify(taskServiceMock).removeVariableLocal(eq(EXAMPLE_TASK_ID), eq(variableKey));
  }

  @Test
  public void testDeleteLocalVariableForNonExistingTaskId() {
    String variableKey = "aVariableKey";

    doThrow(new ProcessEngineException("Cannot find task with id " + NON_EXISTING_ID))
      .when(taskServiceMock).removeVariableLocal(eq(NON_EXISTING_ID), eq(variableKey));

    given().pathParam("id", NON_EXISTING_ID).pathParam("varId", variableKey)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", is(RestException.class.getSimpleName()))
      .body("message", is("Cannot delete task variable " + variableKey + ": Cannot find task with id " + NON_EXISTING_ID))
      .when().delete(SINGLE_TASK_DELETE_SINGLE_VARIABLE_URL);
  }

  @Test
  public void testPostCreateTask() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("id", "anyTaskId");
    json.put("name", "A Task");
    json.put("description", "Some description");
    json.put("priority", 30);
    json.put("assignee", "demo");
    json.put("owner", "mary");
    json.put("delegationState", "PENDING");
    json.put("due", "2014-01-01T00:00:00");
    json.put("followUp", "2014-01-01T00:00:00");
    json.put("parentTaskId", "aParentTaskId");
    json.put("caseInstanceId", "aCaseInstanceId");

    Task newTask = mock(Task.class);
    when(taskServiceMock.newTask(anyString())).thenReturn(newTask);

    given()
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .post(TASK_CREATE_URL);

    verify(taskServiceMock).newTask((String) json.get("id"));
    verify(newTask).setName((String) json.get("name"));
    verify(newTask).setDescription((String) json.get("description"));
    verify(newTask).setPriority((Integer) json.get("priority"));
    verify(newTask).setAssignee((String) json.get("assignee"));
    verify(newTask).setOwner((String) json.get("owner"));
    verify(newTask).setDelegationState(DelegationState.valueOf((String) json.get("delegationState")));
    verify(newTask).setDueDate(any(Date.class));
    verify(newTask).setFollowUpDate(any(Date.class));
    verify(newTask).setParentTaskId((String) json.get("parentTaskId"));
    verify(newTask).setCaseInstanceId((String) json.get("caseInstanceId"));
    verify(taskServiceMock).saveTask(newTask);
  }

  @Test
  public void testPostCreateTaskPartialProperties() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("name", "A Task");
    json.put("description", "Some description");
    json.put("assignee", "demo");
    json.put("owner", "mary");
    json.put("due", "2014-01-01T00:00:00");
    json.put("parentTaskId", "aParentTaskId");

    Task newTask = mock(Task.class);
    when(taskServiceMock.newTask(anyString())).thenReturn(newTask);

    given()
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .post(TASK_CREATE_URL);

    verify(taskServiceMock).newTask(null);
    verify(newTask).setName((String) json.get("name"));
    verify(newTask).setDescription((String) json.get("description"));
    verify(newTask).setPriority(0);
    verify(newTask).setAssignee((String) json.get("assignee"));
    verify(newTask).setOwner((String) json.get("owner"));
    verify(newTask).setDelegationState(null);
    verify(newTask).setDueDate(any(Date.class));
    verify(newTask).setFollowUpDate(null);
    verify(newTask).setParentTaskId((String) json.get("parentTaskId"));
    verify(newTask).setCaseInstanceId(null);
    verify(taskServiceMock).saveTask(newTask);
  }

  @Test
  public void testPostCreateTaskDelegationStateResolved() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("delegationState", "RESOLVED");

    Task newTask = mock(Task.class);
    when(taskServiceMock.newTask(anyString())).thenReturn(newTask);

    given()
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .post(TASK_CREATE_URL);

    verify(taskServiceMock).newTask(null);
    verify(newTask).setDelegationState(DelegationState.valueOf((String) json.get("delegationState")));
    verify(taskServiceMock).saveTask(newTask);
  }

  @Test
  public void testPostCreateTaskDelegationStatePending() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("delegationState", "PENDING");

    Task newTask = mock(Task.class);
    when(taskServiceMock.newTask(anyString())).thenReturn(newTask);

    given()
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .post(TASK_CREATE_URL);

    verify(taskServiceMock).newTask(null);
    verify(newTask).setDelegationState(DelegationState.valueOf((String) json.get("delegationState")));
    verify(taskServiceMock).saveTask(newTask);
  }

  @Test
  public void testPostCreateTaskUnsupportedDelegationState() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("delegationState", "unsupported");

    Task newTask = mock(Task.class);
    when(taskServiceMock.newTask(anyString())).thenReturn(newTask);

    given()
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Valid values for property 'delegationState' are 'PENDING' or 'RESOLVED', but was 'unsupported'"))
    .when()
        .post(TASK_CREATE_URL);
  }

  @Test
  public void testPostCreateTaskLowercaseDelegationState() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("delegationState", "pending");

    Task newTask = mock(Task.class);
    when(taskServiceMock.newTask(anyString())).thenReturn(newTask);

    given()
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .post(TASK_CREATE_URL);

    verify(taskServiceMock).newTask(null);
    verify(newTask).setDelegationState(DelegationState.PENDING);
    verify(taskServiceMock).saveTask(newTask);
  }

  @Test
  public void testPutUpdateTask() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("id", "anyTaskId");
    json.put("name", "A Task");
    json.put("description", "Some description");
    json.put("priority", 30);
    json.put("assignee", "demo");
    json.put("owner", "mary");
    json.put("delegationState", "PENDING");
    json.put("due", "2014-01-01T00:00:00");
    json.put("followUp", "2014-01-01T00:00:00");
    json.put("parentTaskId", "aParentTaskId");
    json.put("caseInstanceId", "aCaseInstanceId");

    given()
        .pathParam("id", EXAMPLE_TASK_ID)
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .put(SINGLE_TASK_URL);

    verify(mockTask).setName((String) json.get("name"));
    verify(mockTask).setDescription((String) json.get("description"));
    verify(mockTask).setPriority((Integer) json.get("priority"));
    verify(mockTask).setAssignee((String) json.get("assignee"));
    verify(mockTask).setOwner((String) json.get("owner"));
    verify(mockTask).setDelegationState(DelegationState.valueOf((String) json.get("delegationState")));
    verify(mockTask).setDueDate(any(Date.class));
    verify(mockTask).setFollowUpDate(any(Date.class));
    verify(mockTask).setParentTaskId((String) json.get("parentTaskId"));
    verify(mockTask).setCaseInstanceId((String) json.get("caseInstanceId"));
    verify(taskServiceMock).saveTask(mockTask);
  }

  @Test
  public void testPutUpdateTaskPartialProperties() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("name", "A Task");
    json.put("description", "Some description");
    json.put("assignee", "demo");
    json.put("owner", "mary");
    json.put("due", "2014-01-01T00:00:00");
    json.put("parentTaskId", "aParentTaskId");

    given()
        .pathParam("id", EXAMPLE_TASK_ID)
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .put(SINGLE_TASK_URL);

    verify(mockTask).setName((String) json.get("name"));
    verify(mockTask).setDescription((String) json.get("description"));
    verify(mockTask).setPriority(0);
    verify(mockTask).setAssignee((String) json.get("assignee"));
    verify(mockTask).setOwner((String) json.get("owner"));
    verify(mockTask).setDelegationState(null);
    verify(mockTask).setDueDate(any(Date.class));
    verify(mockTask).setFollowUpDate(null);
    verify(mockTask).setParentTaskId((String) json.get("parentTaskId"));
    verify(mockTask).setCaseInstanceId(null);
    verify(taskServiceMock).saveTask(mockTask);
  }

  @Test
  public void testPutUpdateTaskNotFound() {
    when(mockQuery.singleResult()).thenReturn(null);

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .body(EMPTY_JSON_OBJECT)
      .contentType(ContentType.JSON)
      .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("No matching task with id "+EXAMPLE_TASK_ID))
    .when()
        .put(SINGLE_TASK_URL);
  }

  @Test
  public void testPutUpdateTaskDelegationStateResolved() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("delegationState", "RESOLVED");

    given()
        .pathParam("id", EXAMPLE_TASK_ID)
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .put(SINGLE_TASK_URL);

    verify(mockTask).setDelegationState(DelegationState.valueOf((String) json.get("delegationState")));
    verify(taskServiceMock).saveTask(mockTask);
  }

  @Test
  public void testPutUpdateTaskDelegationStatePending() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("delegationState", "PENDING");

    Task newTask = mock(Task.class);
    when(taskServiceMock.newTask(anyString())).thenReturn(newTask);

    given()
        .pathParam("id", EXAMPLE_TASK_ID)
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .put(SINGLE_TASK_URL);

    verify(mockTask).setDelegationState(DelegationState.valueOf((String) json.get("delegationState")));
    verify(taskServiceMock).saveTask(mockTask);
  }

  @Test
  public void testPutUpdateTaskUnsupportedDelegationState() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("delegationState", "unsupported");

    Task newTask = mock(Task.class);
    when(taskServiceMock.newTask(anyString())).thenReturn(newTask);

    given()
        .pathParam("id", EXAMPLE_TASK_ID)
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Valid values for property 'delegationState' are 'PENDING' or 'RESOLVED', but was 'unsupported'"))
    .when()
        .put(SINGLE_TASK_URL);
  }

  @Test
  public void testPutUpdateTaskLowercaseDelegationState() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("delegationState", "pending");

    Task newTask = mock(Task.class);
    when(taskServiceMock.newTask(anyString())).thenReturn(newTask);

    given()
        .pathParam("id", EXAMPLE_TASK_ID)
        .body(json)
        .contentType(ContentType.JSON)
        .header("accept", MediaType.APPLICATION_JSON)
    .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
        .put(SINGLE_TASK_URL);

    verify(mockTask).setDelegationState(DelegationState.PENDING);
    verify(taskServiceMock).saveTask(mockTask);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void verifyTaskComments(List<Comment> mockTaskComments, Response response) {
    List list = response.as(List.class);
    assertEquals(1, list.size());

    LinkedHashMap<String, String> resourceHashMap = (LinkedHashMap<String, String>) list.get(0);

    String returnedId = resourceHashMap.get("id");
    String returnedUserId = resourceHashMap.get("userId");
    String returnedTaskId = resourceHashMap.get("taskId");
    Date returnedTime = DateTimeUtil.parseDate(resourceHashMap.get("time"));
    String returnedFullMessage = resourceHashMap.get("message");

    Comment mockComment = mockTaskComments.get(0);

    assertEquals(mockComment.getId(), returnedId);
    assertEquals(mockComment.getTaskId(), returnedTaskId);
    assertEquals(mockComment.getUserId(), returnedUserId);
    assertEquals(mockComment.getTime(), returnedTime);
    assertEquals(mockComment.getFullMessage(), returnedFullMessage);
  }

  private void verifyCreatedTaskComment(Comment mockTaskComment, Response response) {
    String content = response.asString();
    verifyTaskCommentValues(mockTaskComment, content);
    verifyTaskCommentLink(mockTaskComment, content);
  }

  private void verifyTaskCommentValues(Comment mockTaskComment, String responseContent) {
    JsonPath path = from(responseContent);
    String returnedId = path.get("id");
    String returnedUserId = path.get("userId");
    String returnedTaskId = path.get("taskId");
    Date returnedTime = DateTimeUtil.parseDate(path.<String>get("time"));
    String returnedFullMessage = path.get("message");

    assertEquals(mockTaskComment.getId(), returnedId);
    assertEquals(mockTaskComment.getTaskId(), returnedTaskId);
    assertEquals(mockTaskComment.getUserId(), returnedUserId);
    assertEquals(mockTaskComment.getTime(), returnedTime);
    assertEquals(mockTaskComment.getFullMessage(), returnedFullMessage);
  }

  private void verifyTaskCommentLink(Comment mockTaskComment, String responseContent) {
    List<Map<String, String>> returnedLinks = from(responseContent).getList("links");
    assertEquals(1, returnedLinks.size());

    Map<String, String> returnedLink = returnedLinks.get(0);
    assertEquals(HttpMethod.GET, returnedLink.get("method"));
    assertTrue(returnedLink.get("href").endsWith(SINGLE_TASK_COMMENTS_URL.replace("{id}", mockTaskComment.getTaskId()) + "/" + mockTaskComment.getId()));
    assertEquals("self", returnedLink.get("rel"));
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void verifyTaskAttachments(List<Attachment> mockTaskAttachments, Response response) {
    List list = response.as(List.class);
    assertEquals(1, list.size());

    LinkedHashMap<String, String> resourceHashMap = (LinkedHashMap<String, String>) list.get(0);

    String returnedId = resourceHashMap.get("id");
    String returnedTaskId = resourceHashMap.get("taskId");
    String returnedName = resourceHashMap.get("name");
    String returnedType = resourceHashMap.get("type");
    String returnedDescription = resourceHashMap.get("description");
    String returnedUrl = resourceHashMap.get("url");

    Attachment mockAttachment = mockTaskAttachments.get(0);

    assertEquals(mockAttachment.getId(), returnedId);
    assertEquals(mockAttachment.getTaskId(), returnedTaskId);
    assertEquals(mockAttachment.getName(), returnedName);
    assertEquals(mockAttachment.getType(), returnedType);
    assertEquals(mockAttachment.getDescription(), returnedDescription);
    assertEquals(mockAttachment.getUrl(), returnedUrl);
  }

  private void verifyCreatedTaskAttachment(Attachment mockTaskAttachment, Response response, boolean urlExist) {
    String content = response.asString();
    verifyTaskAttachmentValues(mockTaskAttachment, content, urlExist);
    verifyTaskAttachmentLink(mockTaskAttachment, content);
  }

  private void verifyTaskAttachmentValues(Attachment mockTaskAttachment, String responseContent, boolean urlExist) {
    JsonPath path = from(responseContent);
    String returnedId = path.get("id");
    String returnedTaskId = path.get("taskId");
    String returnedName = path.get("name");
    String returnedType = path.get("type");
    String returnedDescription = path.get("description");
    String returnedUrl = path.get("url");

    Attachment mockAttachment = mockTaskAttachments.get(0);

    assertEquals(mockAttachment.getId(), returnedId);
    assertEquals(mockAttachment.getTaskId(), returnedTaskId);
    assertEquals(mockAttachment.getName(), returnedName);
    assertEquals(mockAttachment.getType(), returnedType);
    assertEquals(mockAttachment.getDescription(), returnedDescription);
    if (urlExist) {
      assertEquals(mockAttachment.getUrl(), returnedUrl);
    }
  }

  private void verifyTaskAttachmentLink(Attachment mockTaskAttachment, String responseContent) {
    List<Map<String, String>> returnedLinks = from(responseContent).getList("links");
    assertEquals(1, returnedLinks.size());

    Map<String, String> returnedLink = returnedLinks.get(0);
    assertEquals(HttpMethod.GET, returnedLink.get("method"));
    assertTrue(returnedLink.get("href").endsWith(SINGLE_TASK_ATTACHMENTS_URL.replace("{id}", mockTaskAttachment.getTaskId()) + "/" + mockTaskAttachment.getId()));
    assertEquals("self", returnedLink.get("rel"));
  }

}
