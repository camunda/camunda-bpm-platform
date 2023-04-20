/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Sebastian Menski
 */
public class TaskQueryExpressionTest {

  protected Task task;
  protected User user;
  protected User anotherUser;
  protected User userWithoutGroups;
  protected Group group1;

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      "org/camunda/bpm/engine/test/api/task/task-query-expression-test.camunda.cfg.xml");
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected IdentityService identityService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    identityService = engineRule.getIdentityService();

    group1 = createGroup("group1");
    Group group2 = createGroup("group2");
    Group group3 = createGroup("group3");

    user = createUser("user", group1.getId(), group2.getId());
    anotherUser = createUser("anotherUser", group3.getId());
    userWithoutGroups = createUser("userWithoutGroups");

    setTime(1427547759000l);
    task = createTestTask("task");
    // shift time to force distinguishable create times
    adjustTime(2 * 60);
    Task anotherTask = createTestTask("anotherTask");
    Task assignedCandidateTask = createTestTask("assignedCandidateTask");

    taskService.setOwner(task.getId(), user.getId());
    taskService.setAssignee(task.getId(), user.getId());

    taskService.addCandidateUser(anotherTask.getId(), user.getId());
    taskService.addCandidateGroup(anotherTask.getId(), group1.getId());

    taskService.setAssignee(assignedCandidateTask.getId(), user.getId());
    taskService.addCandidateUser(assignedCandidateTask.getId(), user.getId());
    taskService.addCandidateGroup(assignedCandidateTask.getId(), group1.getId());
  }

  @Test
  public void testQueryByAssigneeExpression() {
    assertCount(taskQuery().taskAssigneeExpression("${'" + user.getId() + "'}"), 2);
    assertCount(taskQuery().taskAssigneeExpression("${'" + anotherUser.getId() + "'}"), 0);

    setCurrentUser(user);
    assertCount(taskQuery().taskAssigneeExpression("${currentUser()}"), 2);

    setCurrentUser(anotherUser);
    assertCount(taskQuery().taskAssigneeExpression("${currentUser()}"), 0);
  }

  @Test
  public void testQueryByAssigneeLikeExpression() {
    assertCount(taskQuery().taskAssigneeLikeExpression("${'%" + user.getId().substring(2) + "'}"), 2);
    assertCount(taskQuery().taskAssigneeLikeExpression("${'%" + anotherUser.getId().substring(2) + "'}"), 0);

    setCurrentUser(user);
    assertCount(taskQuery().taskAssigneeLikeExpression("${'%'.concat(currentUser())}"), 2);

    setCurrentUser(anotherUser);
    assertCount(taskQuery().taskAssigneeLikeExpression("${'%'.concat(currentUser())}"), 0);
  }

  @Test
  public void testQueryByOwnerExpression() {
    assertCount(taskQuery().taskOwnerExpression("${'" + user.getId() + "'}"), 1);
    assertCount(taskQuery().taskOwnerExpression("${'" + anotherUser.getId() + "'}"), 0);

    setCurrentUser(user);
    assertCount(taskQuery().taskOwnerExpression("${currentUser()}"), 1);

    setCurrentUser(anotherUser);
    assertCount(taskQuery().taskOwnerExpression("${currentUser()}"), 0);
  }

  @Test
  public void testQueryByInvolvedUserExpression() {
    assertCount(taskQuery().taskInvolvedUserExpression("${'" + user.getId() + "'}"), 3);
    assertCount(taskQuery().taskInvolvedUserExpression("${'" + anotherUser.getId() + "'}"), 0);

    setCurrentUser(user);
    assertCount(taskQuery().taskInvolvedUserExpression("${currentUser()}"), 3);

    setCurrentUser(anotherUser);
    assertCount(taskQuery().taskInvolvedUserExpression("${currentUser()}"), 0);
  }

  @Test
  public void testQueryByCandidateUserExpression() {
    assertCount(taskQuery().taskCandidateUserExpression("${'" + user.getId() + "'}"), 1);
    assertCount(taskQuery().taskCandidateUserExpression("${'" + user.getId() + "'}").includeAssignedTasks(), 2);
    assertCount(taskQuery().taskCandidateUserExpression("${'" + anotherUser.getId() + "'}"), 0);

    setCurrentUser(user);
    assertCount(taskQuery().taskCandidateUserExpression("${currentUser()}"), 1);
    assertCount(taskQuery().taskCandidateUserExpression("${currentUser()}").includeAssignedTasks(), 2);

    setCurrentUser(anotherUser);
    assertCount(taskQuery().taskCandidateUserExpression("${currentUser()}"), 0);
  }

  @Test
  public void testQueryByCandidateGroupExpression() {
    assertCount(taskQuery().taskCandidateGroupExpression("${'" + group1.getId() + "'}"), 1);
    assertCount(taskQuery().taskCandidateGroupExpression("${'unknown'}"), 0);

    setCurrentUser(user);
    assertCount(taskQuery().taskCandidateGroupExpression("${currentUserGroups()[0]}"), 1);
    assertCount(taskQuery().taskCandidateGroupExpression("${currentUserGroups()[0]}").includeAssignedTasks(), 2);

    setCurrentUser(anotherUser);
    assertCount(taskQuery().taskCandidateGroupExpression("${currentUserGroups()[0]}"), 0);
  }

  @Test
  public void testQueryByCandidateGroupsExpression() {
    setCurrentUser(user);
    assertCount(taskQuery().taskCandidateGroupInExpression("${currentUserGroups()}"), 1);
    assertCount(taskQuery().taskCandidateGroupInExpression("${currentUserGroups()}").includeAssignedTasks(), 2);

    setCurrentUser(anotherUser);

    assertCount(taskQuery().taskCandidateGroupInExpression("${currentUserGroups()}"), 0);

    setCurrentUser(userWithoutGroups);
    try {
      taskQuery().taskCandidateGroupInExpression("${currentUserGroups()}").count();
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected because currentUserGroups will return null
    }
  }

  @Test
  public void testQueryByTaskCreatedBeforeExpression() {
    adjustTime(1);

    assertCount(taskQuery().taskCreatedBeforeExpression("${now()}"), 3);

    adjustTime(-5 * 60);

    assertCount(taskQuery().taskCreatedBeforeExpression("${now()}"), 0);

    setTime(task.getCreateTime());

    assertCount(taskQuery().taskCreatedBeforeExpression("${dateTime().plusMonths(2)}"), 3);

    assertCount(taskQuery().taskCreatedBeforeExpression("${dateTime().minusYears(1)}"), 0);
  }

  @Test
  public void testQueryByTaskCreatedOnExpression() {
    setTime(task.getCreateTime());
    assertCount(taskQuery().taskCreatedOnExpression("${now()}"), 1);

    adjustTime(10);

    assertCount(taskQuery().taskCreatedOnExpression("${dateTime().minusSeconds(10)}"), 1);

    assertCount(taskQuery().taskCreatedOnExpression("${now()}"), 0);
  }

  @Test
  public void testQueryByTaskCreatedAfterExpression() {
    adjustTime(1);

    assertCount(taskQuery().taskCreatedAfterExpression("${now()}"), 0);

    adjustTime(-5 * 60);

    assertCount(taskQuery().taskCreatedAfterExpression("${now()}"), 3);

    setTime(task.getCreateTime());

    assertCount(taskQuery().taskCreatedAfterExpression("${dateTime().plusMonths(2)}"), 0);

    assertCount(taskQuery().taskCreatedAfterExpression("${dateTime().minusYears(1)}"), 3);
  }

  @Test
  public void testQueryByTaskUpdatedAfterExpression() {
    adjustTime(1);

    assertCount(taskQuery().taskUpdatedAfterExpression("${now()}"), 0);

    adjustTime(-5 * 60);

    assertCount(taskQuery().taskUpdatedAfterExpression("${now()}"), 3);

    setTime(task.getLastUpdated());

    assertCount(taskQuery().taskUpdatedAfterExpression("${dateTime().plusMonths(2)}"), 0);

    assertCount(taskQuery().taskUpdatedAfterExpression("${dateTime().minusYears(1)}"), 3);
  }

  @Test
  public void testQueryByDueBeforeExpression() {
    adjustTime(1);

    assertCount(taskQuery().dueBeforeExpression("${now()}"), 3);

    adjustTime(-5 * 60);

    assertCount(taskQuery().dueBeforeExpression("${now()}"), 0);

    setTime(task.getCreateTime());

    assertCount(taskQuery().dueBeforeExpression("${dateTime().plusMonths(2)}"), 3);

    assertCount(taskQuery().dueBeforeExpression("${dateTime().minusYears(1)}"), 0);
  }

  @Test
  public void testQueryByDueDateExpression() {
    setTime(task.getDueDate());
    assertCount(taskQuery().dueDateExpression("${now()}"), 1);

    adjustTime(10);

    assertCount(taskQuery().dueDateExpression("${dateTime().minusSeconds(10)}"), 1);

    assertCount(taskQuery().dueDateExpression("${now()}"), 0);
  }

  @Test
  public void testQueryByDueAfterExpression() {
    adjustTime(1);

    assertCount(taskQuery().dueAfterExpression("${now()}"), 0);

    adjustTime(-5 * 60);

    assertCount(taskQuery().dueAfterExpression("${now()}"), 3);

    setTime(task.getCreateTime());

    assertCount(taskQuery().dueAfterExpression("${dateTime().plusMonths(2)}"), 0);

    assertCount(taskQuery().dueAfterExpression("${dateTime().minusYears(1)}"), 3);
  }

  @Test
  public void testQueryByFollowUpBeforeExpression() {
    adjustTime(1);

    assertCount(taskQuery().followUpBeforeExpression("${now()}"), 3);

    adjustTime(-5 * 60);

    assertCount(taskQuery().followUpBeforeExpression("${now()}"), 0);

    setTime(task.getCreateTime());

    assertCount(taskQuery().followUpBeforeExpression("${dateTime().plusMonths(2)}"), 3);

    assertCount(taskQuery().followUpBeforeExpression("${dateTime().minusYears(1)}"), 0);
  }

  @Test
  public void testQueryByFollowUpDateExpression() {
    setTime(task.getFollowUpDate());
    assertCount(taskQuery().followUpDateExpression("${now()}"), 1);

    adjustTime(10);

    assertCount(taskQuery().followUpDateExpression("${dateTime().minusSeconds(10)}"), 1);

    assertCount(taskQuery().followUpDateExpression("${now()}"), 0);
  }

  @Test
  public void testQueryByFollowUpAfterExpression() {
    adjustTime(1);

    assertCount(taskQuery().followUpAfterExpression("${now()}"), 0);

    adjustTime(-5 * 60);

    assertCount(taskQuery().followUpAfterExpression("${now()}"), 3);

    setTime(task.getCreateTime());

    assertCount(taskQuery().followUpAfterExpression("${dateTime().plusMonths(2)}"), 0);

    assertCount(taskQuery().followUpAfterExpression("${dateTime().minusYears(1)}"), 3);
  }

  @Test
  public void testQueryByProcessInstanceBusinessKeyExpression() {
    // given
    String aBusinessKey = "business key";
    Mocks.register("aBusinessKey", aBusinessKey);

    createBusinessKeyDeployment(aBusinessKey);

    // when
    TaskQuery taskQuery = taskQuery()
      .processInstanceBusinessKeyExpression("${ " + Mocks.getMocks().keySet().toArray()[0] + " }");

    // then
    assertCount(taskQuery, 1);
  }

  @Test
  public void testQueryByProcessInstanceBusinessKeyLikeExpression() {
    // given
    String aBusinessKey = "business key";
    Mocks.register("aBusinessKeyLike", "%" + aBusinessKey.substring(5));

    createBusinessKeyDeployment(aBusinessKey);

    // when
    TaskQuery taskQuery = taskQuery()
      .processInstanceBusinessKeyLikeExpression("${ " + Mocks.getMocks().keySet().toArray()[0] + " }");

    // then
    assertCount(taskQuery, 1);
  }

  protected void createBusinessKeyDeployment(String aBusinessKey) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("aProcessDefinition")
      .startEvent()
        .userTask()
      .endEvent()
      .done();

   testRule.deploy(modelInstance);

    runtimeService.startProcessInstanceByKey("aProcessDefinition", aBusinessKey);
  }

  @Test
  public void testExpressionOverrideQuery() {
    String queryString = "query";
    String expressionString = "expression";
    String testStringExpression = "${'" + expressionString + "'}";

    Date queryDate = new DateTime(now()).minusYears(1).toDate();
    String testDateExpression = "${now()}";

    TaskQueryImpl taskQuery = (TaskQueryImpl) taskQuery()
      .taskAssignee(queryString)
      .taskAssigneeExpression(testStringExpression)
      .taskAssigneeLike(queryString)
      .taskAssigneeLikeExpression(testStringExpression)
      .taskOwnerExpression(queryString)
      .taskOwnerExpression(expressionString)
      .taskInvolvedUser(queryString)
      .taskInvolvedUserExpression(expressionString)
      .taskCreatedBefore(queryDate)
      .taskCreatedBeforeExpression(testDateExpression)
      .taskCreatedOn(queryDate)
      .taskCreatedOnExpression(testDateExpression)
      .taskCreatedAfter(queryDate)
      .taskCreatedAfterExpression(testDateExpression)
      .dueBefore(queryDate)
      .dueBeforeExpression(testDateExpression)
      .dueDate(queryDate)
      .dueDateExpression(testDateExpression)
      .dueAfter(queryDate)
      .dueAfterExpression(testDateExpression)
      .followUpBefore(queryDate)
      .followUpBeforeExpression(testDateExpression)
      .followUpDate(queryDate)
      .followUpDateExpression(testDateExpression)
      .followUpAfter(queryDate)
      .followUpAfterExpression(testDateExpression);

    // execute query so expression will be evaluated
    taskQuery.count();

    assertEquals(expressionString, taskQuery.getAssignee());
    assertEquals(expressionString, taskQuery.getAssigneeLike());
    assertEquals(expressionString, taskQuery.getOwner());
    assertEquals(expressionString, taskQuery.getInvolvedUser());
    assertTrue(taskQuery.getCreateTimeBefore().after(queryDate));
    assertTrue(taskQuery.getCreateTime().after(queryDate));
    assertTrue(taskQuery.getCreateTimeAfter().after(queryDate));
    assertTrue(taskQuery.getDueBefore().after(queryDate));
    assertTrue(taskQuery.getDueDate().after(queryDate));
    assertTrue(taskQuery.getDueAfter().after(queryDate));
    assertTrue(taskQuery.getFollowUpBefore().after(queryDate));
    assertTrue(taskQuery.getFollowUpDate().after(queryDate));
    assertTrue(taskQuery.getFollowUpAfter().after(queryDate));

    // candidates has to be tested separately cause they have to be set exclusively

    taskQuery = (TaskQueryImpl) taskQuery()
      .taskCandidateGroup(queryString)
      .taskCandidateGroupExpression(testStringExpression);

    // execute query so expression will be evaluated
    taskQuery.count();

    assertEquals(expressionString, taskQuery.getCandidateGroup());

    taskQuery = (TaskQueryImpl) taskQuery()
      .taskCandidateUser(queryString)
      .taskCandidateUserExpression(testStringExpression);

    // execute query so expression will be evaluated
    taskQuery.count();

    assertEquals(expressionString, taskQuery.getCandidateUser());

    setCurrentUser(user);
    List<String> queryList = Arrays.asList("query");
    String testGroupsExpression = "${currentUserGroups()}";

    taskQuery = (TaskQueryImpl) taskQuery()
      .taskCandidateGroupIn(queryList)
      .taskCandidateGroupInExpression(testGroupsExpression);

    // execute query so expression will be evaluated
    taskQuery.count();

    assertEquals(2, taskQuery.getCandidateGroups().size());
  }

  @Test
  public void testQueryOverrideExpression() {
    String queryString = "query";
    String expressionString = "expression";
    String testStringExpression = "${'" + expressionString + "'}";

    Date queryDate = new DateTime(now()).minusYears(1).toDate();
    String testDateExpression = "${now()}";

    TaskQueryImpl taskQuery = (TaskQueryImpl) taskQuery()
      .taskAssigneeExpression(testStringExpression)
      .taskAssignee(queryString)
      .taskAssigneeLikeExpression(testStringExpression)
      .taskAssigneeLike(queryString)
      .taskOwnerExpression(expressionString)
      .taskOwner(queryString)
      .taskInvolvedUserExpression(expressionString)
      .taskInvolvedUser(queryString)
      .taskCreatedBeforeExpression(testDateExpression)
      .taskCreatedBefore(queryDate)
      .taskCreatedOnExpression(testDateExpression)
      .taskCreatedOn(queryDate)
      .taskCreatedAfterExpression(testDateExpression)
      .taskCreatedAfter(queryDate)
      .taskUpdatedAfterExpression(testDateExpression)
      .taskUpdatedAfter(queryDate)
      .dueBeforeExpression(testDateExpression)
      .dueBefore(queryDate)
      .dueDateExpression(testDateExpression)
      .dueDate(queryDate)
      .dueAfterExpression(testDateExpression)
      .dueAfter(queryDate)
      .followUpBeforeExpression(testDateExpression)
      .followUpBefore(queryDate)
      .followUpDateExpression(testDateExpression)
      .followUpDate(queryDate)
      .followUpAfterExpression(testDateExpression)
      .followUpAfter(queryDate);

    // execute query so expression will be evaluated
    taskQuery.count();

    assertThat(queryString).isEqualTo(taskQuery.getAssignee());
    assertThat(queryString).isEqualTo(taskQuery.getAssigneeLike());
    assertThat(queryString).isEqualTo(taskQuery.getOwner());
    assertThat(queryString).isEqualTo(taskQuery.getInvolvedUser());
    assertThat(taskQuery.getUpdatedAfter()).isEqualTo(queryDate);
    assertThat(taskQuery.getCreateTimeBefore().equals(queryDate));
    assertThat(taskQuery.getCreateTime().equals(queryDate));
    assertThat(taskQuery.getCreateTimeAfter().equals(queryDate));
    assertThat(taskQuery.getDueBefore().equals(queryDate));
    assertThat(taskQuery.getDueDate().equals(queryDate));
    assertThat(taskQuery.getDueAfter().equals(queryDate));
    assertThat(taskQuery.getFollowUpBefore().equals(queryDate));
    assertThat(taskQuery.getFollowUpDate().equals(queryDate));
    assertThat(taskQuery.getFollowUpAfter().equals(queryDate));

    // candidates has to be tested separately cause they have to be set exclusively

    taskQuery = (TaskQueryImpl) taskQuery()
      .taskCandidateGroupExpression(testStringExpression)
      .taskCandidateGroup(queryString);

    // execute query so expression will be evaluated
    taskQuery.count();

    assertThat(queryString).isEqualTo(taskQuery.getCandidateGroup());

    taskQuery = (TaskQueryImpl) taskQuery()
      .taskCandidateUserExpression(testStringExpression)
      .taskCandidateUser(queryString);

    // execute query so expression will be evaluated
    taskQuery.count();

    assertThat(queryString).isEqualTo(taskQuery.getCandidateUser());

    setCurrentUser(user);
    List<String> queryList = Arrays.asList("query");
    String testGroupsExpression = "${currentUserGroups()}";

    taskQuery = (TaskQueryImpl) taskQuery()
      .taskCandidateGroupInExpression(testGroupsExpression)
      .taskCandidateGroupIn(queryList);

    // execute query so expression will be evaluated
    taskQuery.count();

    assertThat(taskQuery.getCandidateGroups().size()).isEqualTo(1);
  }

  @Test
  public void testQueryOr() {
    // given
    Date date = DateTimeUtil.now().plusDays(2).toDate();

    Task task1 = taskService.newTask();
    task1.setFollowUpDate(date);
    task1.setOwner("Luke Optim");
    task1.setName("taskForOr");
    taskService.saveTask(task1);

    Task task2 = taskService.newTask();
    task2.setDueDate(date);
    task2.setName("taskForOr");
    taskService.saveTask(task2);

    Task task3 = taskService.newTask();
    task3.setAssignee("John Munda");
    task3.setDueDate(date);
    task3.setName("taskForOr");
    taskService.saveTask(task3);

    Task task4 = taskService.newTask();
    task4.setName("taskForOr");
    taskService.saveTask(task4);

    // when
    List<Task> tasks = taskService.createTaskQuery()
      .taskName("taskForOr")
      .or()
        .followUpAfterExpression("${ now() }")
        .taskAssigneeLikeExpression("${ 'John%' }")
      .endOr()
      .or()
        .taskOwnerExpression("${ 'Luke Optim' }")
        .dueAfterExpression("${ now() }")
      .endOr()
      .list();

    // then
    assertEquals(2, tasks.size());
  }

  @Test
  public void shouldRejectDueDateExpressionAndWithoutDueDateCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().dueDateExpression("").withoutDueDate())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @Test
  public void shouldRejectWithoutDueDateAndDueDateExpressionCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().withoutDueDate().dueDateExpression(""))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @Test
  public void shouldRejectDueAfterExpressionAndWithoutDueDateCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().dueAfterExpression("").withoutDueDate())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @Test
  public void shouldRejectWithoutDueDateAndDueAfterExpressionCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().withoutDueDate().dueAfterExpression(""))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @Test
  public void shouldRejectDueBeforeExpressionAndWithoutDueDateCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().dueBeforeExpression("").withoutDueDate())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @Test
  public void shouldRejectWithoutDueDateAndDueBeforeExpressionCombination() {
    assertThatThrownBy(() -> taskService.createTaskQuery().withoutDueDate().dueBeforeExpression(""))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid query usage");
  }

  @After
  public void tearDown() {
    Mocks.reset();

    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Task task : taskService.createTaskQuery().list()) {
      if (task.getProcessInstanceId() == null) {
        taskService.deleteTask(task.getId(), true);
      }
    }

    identityService.clearAuthentication();
  }


  protected TaskQuery taskQuery() {
    return taskService.createTaskQuery();
  }

  protected void assertCount(Query query, long count) {
    assertEquals(count, query.count());
  }

  protected void setCurrentUser(User user) {
    List<Group> groups = identityService.createGroupQuery().groupMember(user.getId()).list();
    List<String> groupIds = new ArrayList<>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }

    identityService.setAuthentication(user.getId(), groupIds);
  }

  protected Group createGroup(String groupId) {
    Group group = identityService.newGroup(groupId);
    identityService.saveGroup(group);
    return group;
  }

  protected User createUser(String userId, String... groupIds) {
    User user = identityService.newUser(userId);
    identityService.saveUser(user);

    if (groupIds != null) {
      for (String groupId : groupIds) {
        identityService.createMembership(userId, groupId);
      }
    }

    return user;
  }

  protected Task createTestTask(String taskId) {
    Task task = taskService.newTask(taskId);
    task.setDueDate(task.getCreateTime());
    taskService.saveTask(task);
    // perform two saves to also set the lastUpdated property on the TaskEntity
    task.setFollowUpDate(task.getCreateTime());
    taskService.saveTask(task);
    return task;
  }

  protected Date now() {
    return ClockUtil.getCurrentTime();
  }

  protected void setTime(long time) {
    setTime(new Date(time));
  }

  protected void setTime(Date time) {
    ClockUtil.setCurrentTime(time);
  }

  /**
   * Changes the current time about the given amount in seconds.
   *
   * @param amount the amount to adjust the current time
   */
  protected void adjustTime(int amount) {
    long time = now().getTime() + amount * 1000;
    setTime(time);
  }

}
