package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_CASE_DEFINITION_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_CASE_EXECUTION_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_CASE_INSTANCE_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_GROUP_ID;
import static org.camunda.bpm.engine.rest.helper.MockProvider.EXAMPLE_GROUP_ID2;
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
import static org.camunda.bpm.engine.rest.util.DateTimeUtils.withTimezone;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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
import java.io.UnsupportedEncodingException;
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
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.hal.Hal;
import org.camunda.bpm.engine.rest.helper.EqualsMap;
import org.camunda.bpm.engine.rest.helper.ErrorMessageHelper;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.helper.VariableTypeHelper;
import org.camunda.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.camunda.bpm.engine.rest.util.EncodingUtil;
import org.camunda.bpm.engine.rest.util.VariablesBuilder;
import org.camunda.bpm.engine.rest.util.container.TestContainerRule;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class TaskRestServiceInteractionTest extends
    AbstractRestServiceTest {

  @ClassRule
  public static TestContainerRule rule = new TestContainerRule();

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
  protected static final String DEPLOYED_TASK_FORM_URL = SINGLE_TASK_URL + "/deployed-form";
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

  protected static final String TASK_CREATE_URL = TASK_SERVICE_URL + "/create";

  private Task mockTask;
  private TaskService taskServiceMock;
  private TaskQuery mockQuery;
  private FormService formServiceMock;
  private ManagementService managementServiceMock;
  private RepositoryService repositoryServiceMock;

  private IdentityLink mockAssigneeIdentityLink;
  private IdentityLink mockOwnerIdentityLink;
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
    mockAssigneeIdentityLink = MockProvider.createMockUserAssigneeIdentityLink();
    identityLinks.add(mockAssigneeIdentityLink);
    mockOwnerIdentityLink = MockProvider.createMockUserOwnerIdentityLink();
    identityLinks.add(mockOwnerIdentityLink);
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

    formServiceMock = mock(FormService.class);
    when(processEngine.getFormService()).thenReturn(formServiceMock);
    TaskFormData mockFormData = MockProvider.createMockTaskFormData();
    when(formServiceMock.getTaskFormData(anyString())).thenReturn(mockFormData);

    VariableMap variablesMock = MockProvider.createMockFormVariables();
    when(formServiceMock.getTaskFormVariables(eq(EXAMPLE_TASK_ID), Matchers.<Collection<String>>any(), anyBoolean())).thenReturn(variablesMock);

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
      .body("tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
      .when().get(SINGLE_TASK_URL);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetSingleTaskHal() {

    // setup user query mock
    List<User> mockUsers = Arrays.asList(
      MockProvider.mockUser().id(EXAMPLE_TASK_ASSIGNEE_NAME).build(),
      MockProvider.mockUser().id(EXAMPLE_TASK_OWNER).build()
    );
    UserQuery sampleUserQuery = mock(UserQuery.class);
    when(sampleUserQuery.userIdIn(eq(EXAMPLE_TASK_ASSIGNEE_NAME), eq(EXAMPLE_TASK_OWNER))).thenReturn(sampleUserQuery);
    when(sampleUserQuery.userIdIn(eq(EXAMPLE_TASK_OWNER), eq(EXAMPLE_TASK_ASSIGNEE_NAME))).thenReturn(sampleUserQuery);
    when(sampleUserQuery.listPage(eq(0), eq(2))).thenReturn(mockUsers);
    when(sampleUserQuery.count()).thenReturn((long) mockUsers.size());
    when(processEngine.getIdentityService().createUserQuery()).thenReturn(sampleUserQuery);

    // setup group query mock
    List<Group> mockGroups = Arrays.asList(
      MockProvider.mockGroup().id(mockCandidateGroupIdentityLink.getGroupId()).build(),
      MockProvider.mockGroup().id(mockCandidateGroup2IdentityLink.getGroupId()).build()
    );
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(sampleGroupQuery.groupIdIn(eq(EXAMPLE_GROUP_ID), eq(EXAMPLE_GROUP_ID2))).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupIdIn(eq(EXAMPLE_GROUP_ID2), eq(EXAMPLE_GROUP_ID))).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.listPage(eq(0), eq(2))).thenReturn(mockGroups);
    when(sampleGroupQuery.count()).thenReturn((long) mockGroups.size());
    when(processEngine.getIdentityService().createGroupQuery()).thenReturn(sampleGroupQuery);

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
      .body("tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))

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
      .body("_links.identityLink.href", endsWith("/task/" + EXAMPLE_TASK_ID + "/identity-links"))
      .body("_links.self.href", endsWith(EXAMPLE_TASK_ID))

      .when().get(SINGLE_TASK_URL);

    String content = response.asString();

    // validate embedded users:
    List<Map<String,Object>> embeddedUsers = from(content).getList("_embedded.user");
    Assert.assertEquals("There should be two users returned.", 2, embeddedUsers.size());

    Map<String, Object> embeddedUser = embeddedUsers.get(0);
    assertNotNull("The returned user should not be null.", embeddedUser);
    assertEquals(MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME, embeddedUser.get("id"));
    assertEquals(MockProvider.EXAMPLE_USER_FIRST_NAME, embeddedUser.get("firstName"));
    assertEquals(MockProvider.EXAMPLE_USER_LAST_NAME, embeddedUser.get("lastName"));
    assertEquals(MockProvider.EXAMPLE_USER_EMAIL, embeddedUser.get("email"));
    assertNull(embeddedUser.get("_embedded"));
    Map<String, Object> links = (Map<String, Object>) embeddedUser.get("_links");
    assertEquals(1, links.size());
    assertHalLink(links, "self", UserRestService.PATH + "/" + MockProvider.EXAMPLE_TASK_ASSIGNEE_NAME);

    embeddedUser = embeddedUsers.get(1);
    assertNotNull("The returned user should not be null.", embeddedUser);
    assertEquals(MockProvider.EXAMPLE_TASK_OWNER, embeddedUser.get("id"));
    assertEquals(MockProvider.EXAMPLE_USER_FIRST_NAME, embeddedUser.get("firstName"));
    assertEquals(MockProvider.EXAMPLE_USER_LAST_NAME, embeddedUser.get("lastName"));
    assertEquals(MockProvider.EXAMPLE_USER_EMAIL, embeddedUser.get("email"));
    assertNull(embeddedUser.get("_embedded"));
    links = (Map<String, Object>) embeddedUser.get("_links");
    assertEquals(1, links.size());
    assertHalLink(links, "self", UserRestService.PATH + "/" + MockProvider.EXAMPLE_TASK_OWNER);

    // validate embedded groups:
    List<Map<String, Object>> embeddedGroups = from(content).getList("_embedded.group");
    Assert.assertEquals("There should be two groups returned.", 2, embeddedGroups.size());

    Map<String, Object> embeddedGroup = embeddedGroups.get(0);
    assertNotNull("The returned group should not be null.", embeddedGroup);
    assertEquals(MockProvider.EXAMPLE_GROUP_ID, embeddedGroup.get("id"));
    assertEquals(MockProvider.EXAMPLE_GROUP_NAME, embeddedGroup.get("name"));
    assertEquals(MockProvider.EXAMPLE_GROUP_TYPE, embeddedGroup.get("type"));
    assertNull(embeddedGroup.get("_embedded"));
    links = (Map<String, Object>) embeddedGroup.get("_links");
    assertEquals(1, links.size());
    assertHalLink(links, "self", GroupRestService.PATH + "/" + MockProvider.EXAMPLE_GROUP_ID);

    embeddedGroup = embeddedGroups.get(1);
    assertNotNull("The returned group should not be null.", embeddedGroup);
    assertEquals(MockProvider.EXAMPLE_GROUP_ID2, embeddedGroup.get("id"));
    assertEquals(MockProvider.EXAMPLE_GROUP_NAME, embeddedGroup.get("name"));
    assertEquals(MockProvider.EXAMPLE_GROUP_TYPE, embeddedGroup.get("type"));
    assertNull(embeddedGroup.get("_embedded"));
    links = (Map<String, Object>) embeddedGroup.get("_links");
    assertEquals(1, links.size());
    assertHalLink(links, "self", GroupRestService.PATH + "/" + MockProvider.EXAMPLE_GROUP_ID2);

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

    links = (Map<String, Object>) embeddedProcessDefinition.get("_links");
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

    // validate embedded identity links
    List<Map<String, Object>> embeddedIdentityLinks = from(content).getList("_embedded.identityLink");
    assertEquals("There should be three identityLink returned", 4, embeddedIdentityLinks.size());
    assertEmbeddedIdentityLink(mockAssigneeIdentityLink, embeddedIdentityLinks.get(0));
    assertEmbeddedIdentityLink(mockOwnerIdentityLink, embeddedIdentityLinks.get(1));
    assertEmbeddedIdentityLink(mockCandidateGroupIdentityLink, embeddedIdentityLinks.get(2));
    assertEmbeddedIdentityLink(mockCandidateGroup2IdentityLink, embeddedIdentityLinks.get(3));

  }

  @SuppressWarnings("unchecked")
  protected void assertHalLink(Map<String, Object> links, String key, String expectedLink) {
    Map<String, Object> linkObject = (Map<String, Object>) links.get(key);
    Assert.assertNotNull(linkObject);

    String actualLink = (String) linkObject.get("href");
    Assert.assertEquals(expectedLink, actualLink);
  }

  @SuppressWarnings("unchecked")
  protected void assertEmbeddedIdentityLink(IdentityLink expected, Map<String, Object> actual) {
    assertNotNull("Embedded indentity link should not be null", actual);
    assertEquals(expected.getType(), actual.get("type"));
    assertEquals(expected.getUserId(), actual.get("userId"));
    assertEquals(expected.getGroupId(), actual.get("groupId"));
    assertEquals(expected.getTaskId(), actual.get("taskId"));
    assertNull(actual.get("_embedded"));

    Map<String, Object> links = (Map<String, Object>) actual.get("_links");
    if (expected.getUserId() != null) {
      assertHalLink(links, "user", UserRestService.PATH + "/" + expected.getUserId());
    }
    if (expected.getGroupId() != null) {
      assertHalLink(links, "group", GroupRestService.PATH + "/" + expected.getGroupId());
    }
    if (expected.getTaskId() != null) {
      assertHalLink(links, "task", TaskRestService.PATH + "/" + expected.getTaskId());
    }
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
  public void testGetForm__FormDataEqualsNull() {
    when(formServiceMock.getTaskFormData(EXAMPLE_TASK_ID)).thenReturn(null);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("contextPath", equalTo(MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH))
      .when().get(TASK_FORM_URL);
  }

  @Test
  public void testGetFormThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(formServiceMock).getTaskFormData(anyString());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(TASK_FORM_URL);
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
    Assertions.assertThat(responseContent).isEqualTo(expectedResult);
  }

  @Test
  public void testGetRenderedFormForDifferentPlatformEncoding() throws NoSuchFieldException, IllegalAccessException, UnsupportedEncodingException {
    String expectedResult = "<formField>unicode symbol: \u2200</formField>";
    when(formServiceMock.getRenderedTaskForm(MockProvider.EXAMPLE_TASK_ID)).thenReturn(expectedResult);

    Response response = given()
        .pathParam("id", EXAMPLE_TASK_ID)
        .then()
          .expect()
            .statusCode(Status.OK.getStatusCode())
            .contentType(XHTML_XML_CONTENT_TYPE)
        .when()
          .get(RENDERED_FORM_URL);

    String responseContent = new String(response.asByteArray(), EncodingUtil.DEFAULT_ENCODING);
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
  public void testGetRenderedFormThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(formServiceMock).getRenderedTaskForm(anyString());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
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
  public void testSubmitTaskFormWithBase64EncodedBytes() {
    Map<String, Object> variables = VariablesBuilder.create()
        .variable("aVariable", Base64.encodeBase64String("someBytes".getBytes()), "Bytes")
        .getVariables();

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(SUBMIT_FORM_URL);

    verify(formServiceMock).submitTaskForm(eq(EXAMPLE_TASK_ID), argThat(new EqualsMap()
      .matcher("aVariable", EqualsPrimitiveValue.bytesValue("someBytes".getBytes()))));
  }

  @SuppressWarnings({ "unchecked" })
  @Test
  public void testSubmitTaskFormWithFileValue() {
    String variableKey = "aVariable";
    String filename = "test.txt";
    Map<String, Object> variables = VariablesBuilder.create().variable(variableKey, Base64.encodeBase64String("someBytes".getBytes()), "File")
        .getVariables();
    ((Map<String, Object>)variables.get(variableKey)).put("valueInfo", Collections.<String, Object>singletonMap("filename", filename));

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("variables", variables);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(json)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when().post(SUBMIT_FORM_URL);

    ArgumentCaptor<VariableMap> captor = ArgumentCaptor.forClass(VariableMap.class);
    verify(formServiceMock).submitTaskForm(eq(EXAMPLE_TASK_ID), captor.capture());
    VariableMap map = captor.getValue();
    FileValue fileValue = (FileValue) map.getValueTyped(variableKey);
    assertThat(fileValue, is(notNullValue()));
    assertThat(fileValue.getFilename(), is(filename));
    assertThat(IoUtil.readInputStream(fileValue.getValue(), null), is("someBytes".getBytes()));
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
  public void testSubmitFormThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(formServiceMock).submitTaskForm(anyString(), Matchers.<Map<String, Object>>any());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(SUBMIT_FORM_URL);
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
  public void testGetTaskFormVariablesAndDoNotDeserializeVariables() {

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .queryParam("deserializeValues", false)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
        .body(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME+".type",
            equalTo(VariableTypeHelper.toExpectedValueTypeName(MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE.getType())))
        .body(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME+".value",
            equalTo(MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE.getValue()))
      .when().get(FORM_VARIABLES_URL)
      .body();

    verify(formServiceMock, times(1)).getTaskFormVariables(EXAMPLE_TASK_ID, null, false);
  }

  @Test
  public void testGetTaskFormVariablesVarNamesAndDoNotDeserializeVariables() {
    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .queryParam("deserializeValues", false)
      .queryParam("variableNames", "a,b,c")
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
      .when().get(FORM_VARIABLES_URL);

    verify(formServiceMock, times(1)).getTaskFormVariables(EXAMPLE_TASK_ID, Arrays.asList(new String[]{"a","b","c"}), false);
  }

  @Test
  public void testGetTaskFormVariablesThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(formServiceMock).getTaskFormVariables(anyString(), Matchers.<Collection<String>>any(), anyBoolean());

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(FORM_VARIABLES_URL);
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
  public void testClaimTaskThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).claim(anyString(), anyString());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(CLAIM_TASK_URL);
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
  public void testUnclaimTaskThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).setAssignee(anyString(), anyString());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(UNCLAIM_TASK_URL);
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

  @Test
  public void testSetAssigneeThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).setAssignee(anyString(), anyString());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(ASSIGNEE_TASK_URL);
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
    Map<String, Object> expectedAssigneeIdentityLink = toExpectedJsonMap(mockAssigneeIdentityLink);
    Map<String, Object> expectedOwnerIdentityLink = toExpectedJsonMap(mockOwnerIdentityLink);
    Map<String, Object> expectedGroupIdentityLink = toExpectedJsonMap(mockCandidateGroupIdentityLink);
    Map<String, Object> expectedGroupIdentityLink2 = toExpectedJsonMap(mockCandidateGroup2IdentityLink);

    given().pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect()
        .statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
        .body("$.size()", equalTo(4))
        .body("$", hasItem(expectedAssigneeIdentityLink))
        .body("$", hasItem(expectedOwnerIdentityLink))
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
  public void testGetIdentityLinksThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).getIdentityLinksForTask(anyString());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .get(TASK_IDENTITY_LINKS_URL);
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
  public void testAddGroupIdentityLinkThrowsAuthorizationException() {
    String groupId = "someGroupId";
    String taskId = EXAMPLE_TASK_ID;
    String type = "someType";

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("groupId", groupId);
    json.put("taskId", taskId);
    json.put("type", type);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).addGroupIdentityLink(anyString(), anyString(), anyString());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(TASK_IDENTITY_LINKS_URL);
  }

  @Test
  public void testAddUserIdentityLinkThrowsAuthorizationException() {
    String userId = "someUserId";
    String taskId = EXAMPLE_TASK_ID;
    String type = "someType";

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", userId);
    json.put("taskId", taskId);
    json.put("type", type);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).addUserIdentityLink(anyString(), anyString(), anyString());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(TASK_IDENTITY_LINKS_URL);
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
  public void testDeleteGroupIdentityLinkThrowsAuthorizationException() {
    String deleteIdentityLinkUrl = TASK_IDENTITY_LINKS_URL + "/delete";

    String taskId = EXAMPLE_TASK_ID;
    String groupId = MockProvider.EXAMPLE_GROUP_ID;
    String type = "someIdentityLinkType";

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("groupId", groupId);
    json.put("type", type);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).deleteGroupIdentityLink(anyString(), anyString(), anyString());

    given()
      .pathParam("id", taskId)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(deleteIdentityLinkUrl);
  }

  @Test
  public void testDeleteUserIdentityLinkThrowsAuthorizationException() {
    String deleteIdentityLinkUrl = TASK_IDENTITY_LINKS_URL + "/delete";

    String taskId = EXAMPLE_TASK_ID;
    String userId = MockProvider.EXAMPLE_USER_ID;
    String type = "someIdentityLinkType";

    Map<String, Object> json = new HashMap<String, Object>();
    json.put("userId", userId);
    json.put("type", type);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).deleteUserIdentityLink(anyString(), anyString(), anyString());

    given()
      .pathParam("id", taskId)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(deleteIdentityLinkUrl);
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
  public void testCompleteTaskThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).complete(anyString(), Matchers.<Map<String, Object>>any());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE).body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(COMPLETE_TASK_URL);
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
  public void testResolveTaskThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).resolveTask(anyString(), Matchers.<Map<String, Object>>any());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(RESOLVE_TASK_URL);
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
  public void testDelegateTaskThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).delegateTask(anyString(), anyString());

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .header("accept", MediaType.APPLICATION_JSON)
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .post(DELEGATE_TASK_URL);
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
  public void testDeleteDeleteTask() {

    given()
      .pathParam("id", EXAMPLE_TASK_ID)
      .expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(SINGLE_TASK_URL);

    verify(taskServiceMock).deleteTask(EXAMPLE_TASK_ID);
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
    json.put("due", withTimezone("2014-01-01T00:00:00"));
    json.put("followUp", withTimezone("2014-01-01T00:00:00"));
    json.put("parentTaskId", "aParentTaskId");
    json.put("caseInstanceId", "aCaseInstanceId");
    json.put("tenantId", MockProvider.EXAMPLE_TENANT_ID);

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
    verify(newTask).setTenantId((String) json.get("tenantId"));
    verify(taskServiceMock).saveTask(newTask);
  }

  @Test
  public void testPostCreateTaskPartialProperties() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("name", "A Task");
    json.put("description", "Some description");
    json.put("assignee", "demo");
    json.put("owner", "mary");
    json.put("due", withTimezone("2014-01-01T00:00:00"));
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
    verify(newTask).setTenantId(null);
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
  public void testPostCreateTask_NotValidValueException() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("id", "anyTaskId");

    Task newTask = mock(Task.class);
    when(taskServiceMock.newTask(anyString())).thenReturn(newTask);

    doThrow(new NotValidException("parent task is null")).when(taskServiceMock).saveTask(newTask);

    given()
      .body(json)
      .contentType(ContentType.JSON)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", equalTo("Could not save task: parent task is null"))
      .when().post(TASK_CREATE_URL);
  }

  @Test
  public void testPostCreateTaskThrowsAuthorizationException() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("id", "anyTaskId");

    String message = "expected exception";
    when(taskServiceMock.newTask(anyString())).thenThrow(new AuthorizationException(message));

    given()
      .body(json)
      .contentType(ContentType.JSON)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
      .when().post(TASK_CREATE_URL);
  }

  @Test
  public void testSaveNewTaskThrowsAuthorizationException() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("id", "anyTaskId");

    Task newTask = mock(Task.class);
    when(taskServiceMock.newTask(anyString())).thenReturn(newTask);

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).saveTask(newTask);

    given()
      .body(json)
      .contentType(ContentType.JSON)
      .header("accept", MediaType.APPLICATION_JSON)
    .then()
      .expect()
        .statusCode(Status.FORBIDDEN.getStatusCode()).contentType(ContentType.JSON)
        .body("type", equalTo(AuthorizationException.class.getSimpleName()))
        .body("message", equalTo(message))
      .when().post(TASK_CREATE_URL);
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
    json.put("due", withTimezone("2014-01-01T00:00:00"));
    json.put("followUp", withTimezone("2014-01-01T00:00:00"));
    json.put("parentTaskId", "aParentTaskId");
    json.put("caseInstanceId", "aCaseInstanceId");
    json.put("tenantId", MockProvider.EXAMPLE_TENANT_ID);

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
    verify(mockTask).setTenantId((String) json.get("tenantId"));
    verify(taskServiceMock).saveTask(mockTask);
  }

  @Test
  public void testPutUpdateTaskPartialProperties() {
    Map<String, Object> json = new HashMap<String, Object>();

    json.put("name", "A Task");
    json.put("description", "Some description");
    json.put("assignee", "demo");
    json.put("owner", "mary");
    json.put("due", withTimezone("2014-01-01T00:00:00"));
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
    verify(mockTask).setTenantId(null);
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

  @Test
  public void testPutUpdateTaskThrowsAuthorizationException() {
    Map<String, Object> json = new HashMap<String, Object>();
    json.put("delegationState", "pending");

    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(taskServiceMock).saveTask(any(Task.class));

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
      .contentType(ContentType.JSON)
      .body(json)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .put(SINGLE_TASK_URL);
  }

  @Test
  public void testGetDeployedTaskForm() {
    InputStream deployedFormMock = new ByteArrayInputStream("Test".getBytes());
    when(formServiceMock.getDeployedTaskForm(anyString())).thenReturn(deployedFormMock);

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body(equalTo("Test"))
    .when()
      .get(DEPLOYED_TASK_FORM_URL);

    verify(formServiceMock).getDeployedTaskForm(MockProvider.EXAMPLE_TASK_ID);
  }

  @Test
  public void testGetDeployedTaskFormWithoutAuthorization() {
    String message = "unauthorized";
    when(formServiceMock.getDeployedTaskForm(anyString()))
        .thenThrow(new AuthorizationException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("message", equalTo(message))
    .when()
      .get(DEPLOYED_TASK_FORM_URL);
  }

  @Test
  public void testGetDeployedTaskFormWithWrongFormKey() {
    String message = "wrong key format";
    when(formServiceMock.getDeployedTaskForm(anyString()))
        .thenThrow(new BadUserRequestException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .body("message", equalTo(message))
    .when()
    .get(DEPLOYED_TASK_FORM_URL);
  }

  @Test
  public void testGetDeployedTaskFormWithUnexistingForm() {
    String message = "not found";
    when(formServiceMock.getDeployedTaskForm(anyString()))
        .thenThrow(new NotFoundException(message));

    given()
      .pathParam("id", MockProvider.EXAMPLE_TASK_ID)
    .then().expect()
      .statusCode(Status.NOT_FOUND.getStatusCode())
      .body("message",equalTo(message))
    .when()
      .get(DEPLOYED_TASK_FORM_URL);
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
