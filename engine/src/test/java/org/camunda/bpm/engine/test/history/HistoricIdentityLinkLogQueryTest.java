package org.camunda.bpm.engine.test.history;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;

/**
 *
 * @author Deivarayan Azhagappan
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricIdentityLinkLogQueryTest extends PluggableProcessEngineTestCase {
  private static final String A_USER_ID = "aUserId";
  private static final String A_GROUP_ID = "aGroupId";
  private static final int numberOfUsers = 3;
  private static final String A_ASSIGNER_ID = "aAssignerId";

  private static final String INVALID_USER_ID = "InvalidUserId";
  private static final String INVALID_TASK_ID = "InvalidTask";
  private static final String INVALID_GROUP_ID = "InvalidGroupId";
  private static final String INVALID_ASSIGNER_ID = "InvalidAssignerId";
  private static final String INVALID_HISTORY_EVENT_TYPE = "InvalidEventType";
  private static final String INVALID_IDENTITY_LINK_TYPE = "InvalidIdentityLinkType";
  private static final String INVALID_PROCESS_DEFINITION_ID = "InvalidProcessDefinitionId";
  private static final String INVALID_PROCESS_DEFINITION_KEY = "InvalidProcessDefinitionKey";
  private static final String GROUP_1 = "Group1";
  private static final String USER_1 = "User1";
  private static String PROCESS_DEFINITION_KEY = "oneTaskProcess";
  private static String PROCESS_DEFINITION_KEY_MULTIPLE_CANDIDATE_USER = "oneTaskProcessForHistoricIdentityLinkWithMultipleCanidateUser";
  private static final String IDENTITY_LINK_ADD="add";
  private static final String IDENTITY_LINK_DELETE="delete";

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testQueryAddTaskCandidateforAddIdentityLink() {

    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    ProcessInstance processInstance = startProcessInstance(PROCESS_DEFINITION_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // if
    identityService.setAuthenticatedUserId(A_ASSIGNER_ID);
    taskService.addCandidateUser(taskId, A_USER_ID);

    // Query test
    HistoricIdentityLinkLog historicIdentityLink = historyService.createHistoricIdentityLinkLogQuery().singleResult();
    assertEquals(historicIdentityLink.getUserId(), A_USER_ID);
    assertEquals(historicIdentityLink.getTaskId(), taskId);
    assertEquals(historicIdentityLink.getType(), IdentityLinkType.CANDIDATE);
    assertEquals(historicIdentityLink.getAssignerId(), A_ASSIGNER_ID);
    assertEquals(historicIdentityLink.getGroupId(), null);
    assertEquals(historicIdentityLink.getOperationType(), IDENTITY_LINK_ADD);
    assertEquals(historicIdentityLink.getProcessDefinitionId(), processInstance.getProcessDefinitionId());
    assertEquals(historicIdentityLink.getProcessDefinitionKey(), PROCESS_DEFINITION_KEY);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testGroupQueryTaskCandidateForAddAndDeleteIdentityLink() {

    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    ProcessInstance processInstance = startProcessInstance(PROCESS_DEFINITION_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // if
    identityService.setAuthenticatedUserId(A_ASSIGNER_ID);
    taskService.addCandidateGroup(taskId, A_GROUP_ID);

    // Query test
    HistoricIdentityLinkLog historicIdentityLink = historyService.createHistoricIdentityLinkLogQuery().singleResult();
    assertEquals(historicIdentityLink.getUserId(), null);
    assertEquals(historicIdentityLink.getTaskId(), taskId);
    assertEquals(historicIdentityLink.getType(), IdentityLinkType.CANDIDATE);
    assertEquals(historicIdentityLink.getAssignerId(), A_ASSIGNER_ID);
    assertEquals(historicIdentityLink.getGroupId(), A_GROUP_ID);
    assertEquals(historicIdentityLink.getOperationType(), IDENTITY_LINK_ADD);
    assertEquals(historicIdentityLink.getProcessDefinitionId(), processInstance.getProcessDefinitionId());
    assertEquals(historicIdentityLink.getProcessDefinitionKey(), PROCESS_DEFINITION_KEY);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testValidIndividualQueryTaskCandidateForAddAndDeleteIdentityLink() {

    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    ProcessInstance processInstance = startProcessInstance(PROCESS_DEFINITION_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // if
    identityService.setAuthenticatedUserId(A_ASSIGNER_ID);
    taskService.addCandidateUser(taskId, A_USER_ID);
    taskService.deleteCandidateUser(taskId, A_USER_ID);

    // Valid Individual Query test
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.taskId(taskId).count(), 2);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.type(IdentityLinkType.CANDIDATE).count(), 2);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.userId(A_USER_ID).count(), 2);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.assignerId(A_ASSIGNER_ID).count(), 2);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.operationType(IDENTITY_LINK_DELETE).count(), 1);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.operationType(IDENTITY_LINK_ADD).count(), 1);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.processDefinitionId(processInstance.getProcessDefinitionId()).count(), 2);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.processDefinitionKey(PROCESS_DEFINITION_KEY).count(), 2);

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testValidGroupQueryTaskCandidateForAddAndDeleteIdentityLink() {

    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    ProcessInstance processInstance = startProcessInstance(PROCESS_DEFINITION_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // if
    identityService.setAuthenticatedUserId(A_ASSIGNER_ID);
    taskService.addCandidateUser(taskId, A_USER_ID);
    taskService.deleteCandidateUser(taskId, A_USER_ID);

    // Valid group query test
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.taskId(taskId).count(), 2);
    assertEquals(query.type(IdentityLinkType.CANDIDATE).count(), 2);
    assertEquals(query.userId(A_USER_ID).count(), 2);
    assertEquals(query.assignerId(A_ASSIGNER_ID).count(), 2);
    assertEquals(query.processDefinitionId(processInstance.getProcessDefinitionId()).count(), 2);
    assertEquals(query.processDefinitionKey(PROCESS_DEFINITION_KEY).count(), 2);
    assertEquals(query.operationType(IDENTITY_LINK_DELETE).count(), 1);
    assertEquals(query.operationType(IDENTITY_LINK_ADD).count(), 1);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testInvalidIndividualQueryTaskCandidateForAddAndDeleteIdentityLink() {

    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    startProcessInstance(PROCESS_DEFINITION_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // if
    identityService.setAuthenticatedUserId(A_ASSIGNER_ID);
    taskService.addCandidateUser(taskId, A_USER_ID);
    taskService.deleteCandidateUser(taskId, A_USER_ID);

    // Invalid Individual Query test
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.taskId(INVALID_TASK_ID).count(), 0);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.type(INVALID_IDENTITY_LINK_TYPE).count(), 0);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.userId(INVALID_USER_ID).count(), 0);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.groupId(INVALID_GROUP_ID).count(), 0);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.assignerId(INVALID_ASSIGNER_ID).count(), 0);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.operationType(INVALID_HISTORY_EVENT_TYPE).count(), 0);

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testInvalidGroupQueryTaskCandidateForAddAndDeleteIdentityLink() {

    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    startProcessInstance(PROCESS_DEFINITION_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // if
    identityService.setAuthenticatedUserId(A_ASSIGNER_ID);
    taskService.addCandidateUser(taskId, A_USER_ID);
    taskService.deleteCandidateUser(taskId, A_USER_ID);

    // Invalid Individual Query test
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.taskId(INVALID_TASK_ID).count(), 0);
    assertEquals(query.type(INVALID_IDENTITY_LINK_TYPE).count(), 0);
    assertEquals(query.userId(INVALID_USER_ID).count(), 0);
    assertEquals(query.groupId(INVALID_GROUP_ID).count(), 0);
    assertEquals(query.assignerId(INVALID_ASSIGNER_ID).count(), 0);
    assertEquals(query.operationType(INVALID_HISTORY_EVENT_TYPE).count(), 0);
    assertEquals(query.processDefinitionId(INVALID_PROCESS_DEFINITION_ID).count(), 0);
    assertEquals(query.processDefinitionKey(INVALID_PROCESS_DEFINITION_KEY).count(), 0);
  }

  /**
   * Should add 3 history records of identity link addition at 01-01-2016
   * 00:00.00 Should add 3 history records of identity link deletion at
   * 01-01-2016 12:00.00
   *
   * Should add 3 history records of identity link addition at 01-01-2016
   * 12:30.00 Should add 3 history records of identity link deletion at
   * 01-01-2016 21:00.00
   *
   * Test case: Query the number of added records at different time interval.
   */
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testShouldAddTaskOwnerForAddandDeleteIdentityLinkByTimeStamp() {

    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    startProcessInstance(PROCESS_DEFINITION_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // if
    ClockUtil.setCurrentTime(newYearMorning(0));
    identityService.setAuthenticatedUserId(A_ASSIGNER_ID);
    // Adds aUserId1, deletes aUserID1, adds aUserId2, deletes aUserId2, Adds aUserId3 - 5
    addUserIdentityLinks(taskId);

    ClockUtil.setCurrentTime(newYearNoon(0));
    //Deletes aUserId3
    deleteUserIdentityLinks(taskId);

    ClockUtil.setCurrentTime(newYearNoon(30));
    addUserIdentityLinks(taskId);

    ClockUtil.setCurrentTime(newYearEvening());
    deleteUserIdentityLinks(taskId);

    // Query records with time before 12:20
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.dateBefore(newYearNoon(20)).count(), 6);
    assertEquals(query.operationType(IDENTITY_LINK_ADD).count(), 3);
    assertEquals(query.operationType(IDENTITY_LINK_DELETE).count(), 3);

    // Query records with time between 00:01 and 12:00
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.dateBefore(newYearNoon(0)).count(), 6);
    assertEquals(query.dateAfter(newYearMorning(1)).count(), 1);
    assertEquals(query.operationType(IDENTITY_LINK_ADD).count(), 0);
    assertEquals(query.operationType(IDENTITY_LINK_DELETE).count(), 1);

    // Query records with time after 12:45
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.dateAfter(newYearNoon(45)).count(), 1);
    assertEquals(query.operationType(IDENTITY_LINK_ADD).count(), 0);
    assertEquals(query.operationType(IDENTITY_LINK_DELETE).count(), 1);

    ClockUtil.setCurrentTime(new Date());
  }

  @SuppressWarnings("deprecation")
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testQueryAddAndRemoveIdentityLinksForProcessDefinition() throws Exception {

    ProcessDefinition latestProcessDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    assertNotNull(latestProcessDef);
    List<IdentityLink> links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
    assertEquals(0, links.size());

    // Add candiate group with process definition
    repositoryService.addCandidateStarterGroup(latestProcessDef.getId(), GROUP_1);
    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(historicIdentityLinks.size(), 1);
    // Query test
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.processDefinitionId(latestProcessDef.getId()).count(), 1);
    assertEquals(query.operationType(IDENTITY_LINK_ADD).count(), 1);
    assertEquals(query.groupId(GROUP_1).count(), 1);

    // Add candidate user for process definition
    repositoryService.addCandidateStarterUser(latestProcessDef.getId(), USER_1);
    // Query test
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.processDefinitionId(latestProcessDef.getId()).count(), 2);
    assertEquals(query.processDefinitionKey(latestProcessDef.getKey()).count(), 2);
    assertEquals(query.operationType(IDENTITY_LINK_ADD).count(), 2);
    assertEquals(query.userId(USER_1).count(), 1);

    // Delete candiate group with process definition
    repositoryService.deleteCandidateStarterGroup(latestProcessDef.getId(), GROUP_1);
    // Query test
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.processDefinitionId(latestProcessDef.getId()).count(), 3);
    assertEquals(query.processDefinitionKey(latestProcessDef.getKey()).count(), 3);
    assertEquals(query.groupId(GROUP_1).count(), 2);
    assertEquals(query.operationType(IDENTITY_LINK_DELETE).count(), 1);

    // Delete candidate user for process definition
    repositoryService.deleteCandidateStarterUser(latestProcessDef.getId(), USER_1);
    // Query test
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.processDefinitionId(latestProcessDef.getId()).count(), 4);
    assertEquals(query.processDefinitionKey(latestProcessDef.getKey()).count(), 4);
    assertEquals(query.userId(USER_1).count(), 2);
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.operationType(IDENTITY_LINK_DELETE).count(), 2);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/OneTaskProcessWithMultipleCandidateUser.bpmn20.xml" })
  public void testHistoricIdentityLinkQueryPaging() {
    startProcessInstance(PROCESS_DEFINITION_KEY_MULTIPLE_CANDIDATE_USER);

    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    assertEquals(4, query.listPage(0, 4).size());
    assertEquals(1, query.listPage(2, 1).size());
    assertEquals(2, query.listPage(1, 2).size());
    assertEquals(3, query.listPage(1, 4).size());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/OneTaskProcessWithMultipleCandidateUser.bpmn20.xml" })
  public void testHistoricIdentityLinkQuerySorting() {

    // Pre test - Historical identity link is added as part of deployment
    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService.createHistoricIdentityLinkLogQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);
    startProcessInstance(PROCESS_DEFINITION_KEY_MULTIPLE_CANDIDATE_USER);

    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByAssignerId().asc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByTime().asc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByGroupId().asc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByType().asc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByOperationType().asc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByProcessDefinitionKey().asc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByTaskId().asc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByUserId().asc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByTenantId().asc().list().size());
    assertEquals("aUser", historyService.createHistoricIdentityLinkLogQuery().orderByUserId().asc().list().get(0).getUserId());
    assertEquals("dUser", historyService.createHistoricIdentityLinkLogQuery().orderByUserId().asc().list().get(3).getUserId());

    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByAssignerId().desc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByTime().desc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByGroupId().desc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByType().desc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByOperationType().desc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByProcessDefinitionKey().desc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByTaskId().desc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByUserId().desc().list().size());
    assertEquals(4, historyService.createHistoricIdentityLinkLogQuery().orderByTenantId().desc().list().size());
    assertEquals("dUser", historyService.createHistoricIdentityLinkLogQuery().orderByUserId().desc().list().get(0).getUserId());
    assertEquals("aUser", historyService.createHistoricIdentityLinkLogQuery().orderByUserId().desc().list().get(3).getUserId());
  }

  public void addUserIdentityLinks(String taskId) {
    for (int userIndex = 1; userIndex <= numberOfUsers; userIndex++)
      taskService.addUserIdentityLink(taskId, A_USER_ID + userIndex, IdentityLinkType.ASSIGNEE);
  }

  public void deleteUserIdentityLinks(String taskId) {
    for (int userIndex = 1; userIndex <= numberOfUsers; userIndex++)
      taskService.deleteUserIdentityLink(taskId, A_USER_ID + userIndex, IdentityLinkType.ASSIGNEE);
  }

  public Date newYearMorning(int minutes) {
    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.YEAR, 2016);
    calendar.set(Calendar.MONTH, 0);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, minutes);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date morning = calendar.getTime();
    return morning;
  }

  public Date newYearNoon(int minutes) {
    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.YEAR, 2016);
    calendar.set(Calendar.MONTH, 0);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 12);
    calendar.set(Calendar.MINUTE, minutes);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date morning = calendar.getTime();
    return morning;
  }

  public Date newYearEvening() {
    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.YEAR, 2016);
    calendar.set(Calendar.MONTH, 0);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 21);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date morning = calendar.getTime();
    return morning;
  }

  protected ProcessInstance startProcessInstance(String key) {
    return runtimeService.startProcessInstanceByKey(key);
  }
}
