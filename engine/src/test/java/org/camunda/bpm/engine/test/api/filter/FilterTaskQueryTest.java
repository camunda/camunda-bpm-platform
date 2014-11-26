/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.test.api.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryProperty;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.json.JsonTaskQueryConverter;
import org.camunda.bpm.engine.impl.persistence.entity.FilterEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Sebastian Menski
 */
public class FilterTaskQueryTest extends PluggableProcessEngineTestCase {

  protected Filter filter;

  protected String testString = "test";
  protected Integer testInteger = 1;
  protected DelegationState testDelegationState = DelegationState.PENDING;
  protected Date testDate = new Date(0);
  protected String[] testActivityInstances = new String[] {"a", "b", "c"};
  protected List<String> testCandidateGroups = new ArrayList<String>();

  protected String[] variableNames = new String[] {"a", "b", "c", "d", "e", "f"};
  protected Object[] variableValues = new Object[] {1, 2, "3", "4", 5, 6};
  protected QueryOperator[] variableOperators = new QueryOperator[] {QueryOperator.EQUALS, QueryOperator.GREATER_THAN_OR_EQUAL, QueryOperator.LESS_THAN, QueryOperator.LIKE, QueryOperator.NOT_EQUALS, QueryOperator.LESS_THAN_OR_EQUAL};
  protected boolean[] isTaskVariable = new boolean[] {true, true, false, false, false, false};
  protected boolean[] isProcessVariable = new boolean[] {false, false, true, true, false, false};
  protected String testOrderBy = TaskQueryProperty.EXECUTION_ID.getName() + " " + Direction.DESCENDING.getName() + ", " + TaskQueryProperty.DUE_DATE.getName() + " " + Direction.ASCENDING.getName();
  protected User testUser;
  protected Group testGroup;

  protected JsonTaskQueryConverter queryConverter;

  public void setUp() {
    filter = filterService.newTaskFilter("name").setOwner("owner").setQuery(taskService.createTaskQuery()).setProperties(new HashMap<String, Object>());
    testUser = identityService.newUser("user");
    testGroup = identityService.newGroup("group");
    identityService.saveUser(testUser);
    identityService.saveGroup(testGroup);
    identityService.createMembership(testUser.getId(), testGroup.getId());

    Group anotherGroup = identityService.newGroup("anotherGroup");
    identityService.saveGroup(anotherGroup);
    testCandidateGroups.add(testGroup.getId());
    testCandidateGroups.add(anotherGroup.getId());

    createTasks();

    queryConverter = new JsonTaskQueryConverter();
  }

  public void tearDown() {
    for (Filter filter : filterService.createTaskFilterQuery().list()) {
      filterService.deleteFilter(filter.getId());
    }
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.deleteTask(task.getId(), true);
    }
  }

  public void testEmptyQuery() {
    TaskQuery emptyQuery = taskService.createTaskQuery();
    String emptyQueryJson = "{}";

    filter.setQuery(emptyQuery);

    assertEquals(emptyQueryJson, ((FilterEntity) filter).getQueryInternal());
    assertNotNull(filter.getQuery());
  }

  public void testTaskQuery() {
    // create query
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskId(testString);
    query.taskName(testString);
    query.taskNameLike(testString);
    query.taskDescription(testString);
    query.taskDescriptionLike(testString);
    query.taskPriority(testInteger);
    query.taskMinPriority(testInteger);
    query.taskMaxPriority(testInteger);
    query.taskAssignee(testString);
    query.taskAssigneeExpression(testString);
    query.taskAssigneeLike(testString);
    query.taskAssigneeLikeExpression(testString);
    query.taskInvolvedUser(testString);
    query.taskInvolvedUserExpression(testString);
    query.taskOwner(testString);
    query.taskOwnerExpression(testString);
    query.taskUnassigned();
    query.taskDelegationState(testDelegationState);
    query.taskCandidateGroupIn(testCandidateGroups);
    query.taskCandidateGroupInExpression(testString);
    query.processInstanceId(testString);
    query.executionId(testString);
    query.activityInstanceIdIn(testActivityInstances);
    query.taskCreatedOn(testDate);
    query.taskCreatedOnExpression(testString);
    query.taskCreatedBefore(testDate);
    query.taskCreatedBeforeExpression(testString);
    query.taskCreatedAfter(testDate);
    query.taskCreatedAfterExpression(testString);
    query.taskDefinitionKey(testString);
    query.taskDefinitionKeyLike(testString);
    query.processDefinitionKey(testString);
    query.processDefinitionId(testString);
    query.processDefinitionName(testString);
    query.processDefinitionNameLike(testString);
    query.processInstanceBusinessKey(testString);
    query.processInstanceBusinessKeyLike(testString);

    // variables
    query.taskVariableValueEquals(variableNames[0], variableValues[0]);
    query.taskVariableValueGreaterThanOrEquals(variableNames[1], variableValues[1]);
    query.processVariableValueLessThan(variableNames[2], variableValues[2]);
    query.processVariableValueLike(variableNames[3], (String) variableValues[3]);
    query.caseInstanceVariableValueNotEquals(variableNames[4], variableValues[4]);
    query.caseInstanceVariableValueLessThanOrEquals(variableNames[5], variableValues[5]);

    query.dueDate(testDate);
    query.dueDateExpression(testString);
    query.dueBefore(testDate);
    query.dueBeforeExpression(testString);
    query.dueAfter(testDate);
    query.dueAfterExpression(testString);
    query.followUpDate(testDate);
    query.followUpDateExpression(testString);
    query.followUpBefore(testDate);
    query.followUpBeforeExpression(testString);
    query.followUpAfter(testDate);
    query.followUpAfterExpression(testString);
    query.excludeSubtasks();
    query.suspended();
    query.caseDefinitionKey(testString);
    query.caseDefinitionId(testString);
    query.caseDefinitionName(testString);
    query.caseDefinitionNameLike(testString);
    query.caseInstanceId(testString);
    query.caseInstanceBusinessKey(testString);
    query.caseInstanceBusinessKeyLike(testString);
    query.caseExecutionId(testString);

    // ordering
    query.orderByExecutionId().desc();
    query.orderByDueDate().asc();

    // save filter
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    // test query
    query = filter.getQuery();
    assertEquals(testString, query.getTaskId());
    assertEquals(testString, query.getName());
    assertEquals(testString, query.getNameLike());
    assertEquals(testString, query.getDescription());
    assertEquals(testString, query.getDescriptionLike());
    assertEquals(testInteger, query.getPriority());
    assertEquals(testInteger, query.getMinPriority());
    assertEquals(testInteger, query.getMaxPriority());
    assertEquals(testString, query.getAssignee());
    assertEquals(testString, query.getExpressions().get("taskAssignee"));
    assertEquals(testString, query.getAssigneeLike());
    assertEquals(testString, query.getExpressions().get("taskAssigneeLike"));
    assertEquals(testString, query.getInvolvedUser());
    assertEquals(testString, query.getExpressions().get("taskInvolvedUser"));
    assertEquals(testString, query.getOwner());
    assertEquals(testString, query.getExpressions().get("taskOwner"));
    assertTrue(query.isUnassigned());
    assertEquals(testDelegationState, query.getDelegationState());
    assertEquals(testCandidateGroups, query.getCandidateGroups());
    assertEquals(testString, query.getExpressions().get("taskCandidateGroupIn"));
    assertEquals(testString, query.getProcessInstanceId());
    assertEquals(testString, query.getExecutionId());
    assertEquals(testActivityInstances.length, query.getActivityInstanceIdIn().length);
    for (int i = 0; i < query.getActivityInstanceIdIn().length; i++) {
      assertEquals(testActivityInstances[i], query.getActivityInstanceIdIn()[i]);
    }
    assertEquals(testDate, query.getCreateTime());
    assertEquals(testString, query.getExpressions().get("taskCreatedOn"));
    assertEquals(testDate, query.getCreateTimeBefore());
    assertEquals(testString, query.getExpressions().get("taskCreatedBefore"));
    assertEquals(testDate, query.getCreateTimeAfter());
    assertEquals(testString, query.getExpressions().get("taskCreatedAfter"));
    assertEquals(testString, query.getKey());
    assertEquals(testString, query.getKeyLike());
    assertEquals(testString, query.getProcessDefinitionKey());
    assertEquals(testString, query.getProcessDefinitionId());
    assertEquals(testString, query.getProcessDefinitionName());
    assertEquals(testString, query.getProcessDefinitionNameLike());
    assertEquals(testString, query.getProcessInstanceBusinessKey());
    assertEquals(testString, query.getProcessInstanceBusinessKeyLike());

    // variables
    List<TaskQueryVariableValue> variables = query.getVariables();
    for (int i = 0; i < variables.size(); i++) {
      TaskQueryVariableValue variable = variables.get(i);
      assertEquals(variableNames[i], variable.getName());
      assertEquals(variableValues[i], variable.getValue());
      assertEquals(variableOperators[i], variable.getOperator());
      assertEquals(isTaskVariable[i], variable.isLocal());
      assertEquals(isProcessVariable[i], variable.isProcessInstanceVariable());
    }

    assertEquals(testDate, query.getDueDate());
    assertEquals(testString, query.getExpressions().get("dueDate"));
    assertEquals(testDate, query.getDueBefore());
    assertEquals(testString, query.getExpressions().get("dueBefore"));
    assertEquals(testDate, query.getDueAfter());
    assertEquals(testString, query.getExpressions().get("dueAfter"));
    assertEquals(testDate, query.getFollowUpDate());
    assertEquals(testString, query.getExpressions().get("followUpDate"));
    assertEquals(testDate, query.getFollowUpBefore());
    assertEquals(testString, query.getExpressions().get("followUpBefore"));
    assertEquals(testDate, query.getFollowUpAfter());
    assertEquals(testString, query.getExpressions().get("followUpAfter"));
    assertTrue(query.isExcludeSubtasks());
    assertEquals(SuspensionState.SUSPENDED, query.getSuspensionState());
    assertEquals(testString, query.getCaseDefinitionKey());
    assertEquals(testString, query.getCaseDefinitionId());
    assertEquals(testString, query.getCaseDefinitionName());
    assertEquals(testString, query.getCaseDefinitionNameLike());
    assertEquals(testString, query.getCaseInstanceId());
    assertEquals(testString, query.getCaseInstanceBusinessKey());
    assertEquals(testString, query.getCaseInstanceBusinessKeyLike());
    assertEquals(testString, query.getCaseExecutionId());

    // ordering
    assertEquals(testOrderBy, query.getOrderBy());
  }

  public void testTaskQueryByFollowUpBeforeOrNotExistent() {
    // create query
    TaskQueryImpl query = new TaskQueryImpl();

    query.followUpBeforeOrNotExistent(testDate);

    // save filter
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    // test query
    query = filter.getQuery();
    assertTrue(query.isFollowUpNullAccepted());
    assertEquals(testDate, query.getFollowUpBefore());
  }

  public void testTaskQueryByFollowUpBeforeOrNotExistentExtendingQuery() {
    // create query
    TaskQueryImpl query = new TaskQueryImpl();

    query.followUpBeforeOrNotExistent(testDate);

    // save filter without query
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    // use query as extending query
    List<Task> tasks = filterService.list(filter.getId(), query);
    assertEquals(3, tasks.size());

    // set as filter query and save filter
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    tasks = filterService.list(filter.getId());
    assertEquals(3, tasks.size());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery
      .orderByTaskCreateTime()
      .asc();

    tasks = filterService.list(filter.getId(), extendingQuery);
    assertEquals(3, tasks.size());
  }

  public void testTaskQueryByFollowUpBeforeOrNotExistentExpression() {
    // create query
    TaskQueryImpl query = new TaskQueryImpl();

    query.followUpBeforeOrNotExistentExpression(testString);

    // save filter
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    // test query
    query = filter.getQuery();
    assertTrue(query.isFollowUpNullAccepted());
    assertEquals(testString, query.getExpressions().get("followUpBeforeOrNotExistent"));
  }

  public void testTaskQueryByFollowUpBeforeOrNotExistentExpressionExtendingQuery() {
    // create query
    TaskQueryImpl query = new TaskQueryImpl();

    query.followUpBeforeOrNotExistentExpression("${dateTime().withMillis(0)}");

    // save filter without query
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    // use query as extending query
    List<Task> tasks = filterService.list(filter.getId(), query);
    assertEquals(3, tasks.size());

    // set as filter query and save filter
    filter.setQuery(query);
    filterService.saveFilter(filter);

    // fetch from db
    filter = filterService.createTaskFilterQuery().singleResult();

    tasks = filterService.list(filter.getId());
    assertEquals(3, tasks.size());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery
      .orderByTaskCreateTime()
      .asc();

    tasks = filterService.list(filter.getId(), extendingQuery);
    assertEquals(3, tasks.size());
  }

  public void testTaskQueryCandidateUser() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateUser(testUser.getId());
    query.taskCandidateUserExpression(testUser.getId());

    filter.setQuery(query);
    query = filter.getQuery();

    assertEquals(testUser.getId(), query.getCandidateUser());
    assertEquals(testUser.getId(), query.getExpressions().get("taskCandidateUser"));
  }

  public void testTaskQueryCandidateGroup() {
    TaskQueryImpl query = new TaskQueryImpl();
    query.taskCandidateGroup(testGroup.getId());
    query.taskCandidateGroupExpression(testGroup.getId());

    filter.setQuery(query);
    query = filter.getQuery();

    assertEquals(testGroup.getId(), query.getCandidateGroup());
    assertEquals(testGroup.getId(), query.getExpressions().get("taskCandidateGroup"));
  }

  public void testExecuteTaskQueryList() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskNameLike("Task%");

    saveQuery(query);

    List<Task> tasks = filterService.list(filter.getId());
    assertEquals(3, tasks.size());
    for (Task task : tasks) {
      assertEquals(testUser.getId(), task.getOwner());
    }
  }

  public void testExtendingTaskQueryList() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    List<Task> tasks = filterService.list(filter.getId());
    assertEquals(3, tasks.size());

    tasks = filterService.list(filter.getId(), query);
    assertEquals(3, tasks.size());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery.taskDelegationState(DelegationState.RESOLVED);

    tasks = filterService.list(filter.getId(), extendingQuery);
    assertEquals(2, tasks.size());

    for (Task task : tasks) {
      assertEquals(DelegationState.RESOLVED, task.getDelegationState());
    }
  }

  public void testExtendingTaskQueryListWithCandidateGroups() {
    TaskQuery query = taskService.createTaskQuery();

    List<String> candidateGroups = new ArrayList<String>();
    candidateGroups.add("accounting");
    query.taskCandidateGroupIn(candidateGroups);

    saveQuery(query);

    List<Task> tasks = filterService.list(filter.getId());
    assertEquals(1, tasks.size());

    tasks = filterService.list(filter.getId(), query);
    assertEquals(1, tasks.size());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery
      .orderByTaskCreateTime()
      .asc();

    tasks = filterService.list(filter.getId(), extendingQuery);
    assertEquals(1, tasks.size());
  }

  public void testExecuteTaskQueryListPage() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskNameLike("Task%");

    saveQuery(query);

    List<Task> tasks = filterService.listPage(filter.getId(), 1, 2);
    assertEquals(2, tasks.size());
    for (Task task : tasks) {
      assertEquals(testUser.getId(), task.getOwner());
    }
  }

  public void testExtendingTaskQueryListPage() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    List<Task> tasks = filterService.listPage(filter.getId(), 1, 2);
    assertEquals(2, tasks.size());

    tasks = filterService.listPage(filter.getId(), query, 1, 2);
    assertEquals(2, tasks.size());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery.taskDelegationState(DelegationState.RESOLVED);

    tasks = filterService.listPage(filter.getId(), extendingQuery, 1, 2);
    assertEquals(1, tasks.size());

    assertEquals(DelegationState.RESOLVED, tasks.get(0).getDelegationState());
  }

  public void testExecuteTaskQuerySingleResult() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskDelegationState(DelegationState.PENDING);

    saveQuery(query);

    Task task = filterService.singleResult(filter.getId());
    assertNotNull(task);
    assertEquals("Task 1", task.getName());
  }

  public void testFailTaskQuerySingleResult() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    try {
      filterService.singleResult(filter.getId());
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }
  }

  public void testExtendingTaskQuerySingleResult() {
    TaskQuery query = taskService.createTaskQuery();
    query.taskDelegationState(DelegationState.PENDING);

    saveQuery(query);

    Task task = filterService.singleResult(filter.getId());
    assertNotNull(task);
    assertEquals("Task 1", task.getName());
    assertEquals("task1", task.getId());

    task = filterService.singleResult(filter.getId(), query);
    assertNotNull(task);
    assertEquals("Task 1", task.getName());
    assertEquals("task1", task.getId());

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery.taskId("task1");

    task = filterService.singleResult(filter.getId(), extendingQuery);
    assertNotNull(task);
    assertEquals("Task 1", task.getName());
    assertEquals("task1", task.getId());
  }

  public void testExecuteTaskQueryCount() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    long count = filterService.count(filter.getId());
    assertEquals(3, count);

    query.taskDelegationState(DelegationState.RESOLVED);

    saveQuery(query);

    count = filterService.count(filter.getId());
    assertEquals(2, count);
  }

  public void testExtendingTaskQueryCount() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    TaskQuery extendingQuery = taskService.createTaskQuery();

    extendingQuery.taskId("task3");

    long count = filterService.count(filter.getId());

    assertEquals(3, count);

    count = filterService.count(filter.getId(), query);

    assertEquals(3, count);

    count = filterService.count(filter.getId(), extendingQuery);

    assertEquals(1, count);
  }

  public void testSpecialExtendingQuery() {
    TaskQuery query = taskService.createTaskQuery();

    saveQuery(query);

    long count = filterService.count(filter.getId(), (Query) null);
    assertEquals(3, count);
  }

  public void testExtendingSorting() {
    String sortByNameAsc = TaskQueryProperty.NAME.getName() + " " + Direction.ASCENDING.getName();
    String sortByAssigneeDesc = TaskQueryProperty.ASSIGNEE.getName() + " " + Direction.DESCENDING.getName();

    // create empty query
    TaskQueryImpl query = (TaskQueryImpl) taskService.createTaskQuery();
    saveQuery(query);

    // assert default sorting
    query = filter.getQuery();
    String orderBy = query.getOrderBy();
    assertEquals(AbstractQuery.DEFAULT_ORDER_BY, orderBy);

    // extend query by new task query with sorting
    TaskQuery sortQuery = taskService.createTaskQuery().orderByTaskName().asc();
    Filter extendedFilter = filter.extend(sortQuery);
    query = extendedFilter.getQuery();
    orderBy = query.getOrderBy();

    assertEquals(sortByNameAsc, orderBy);

    // extend query by new task query with additional sorting
    TaskQuery extendingQuery = taskService.createTaskQuery().orderByTaskAssignee().desc();
    extendedFilter = extendedFilter.extend(extendingQuery);
    query = extendedFilter.getQuery();
    orderBy = query.getOrderBy();

    assertEquals(sortByNameAsc + ", " + sortByAssigneeDesc, orderBy);

    // extend query by incomplete sorting query (sorting should not change)
    sortQuery = taskService.createTaskQuery().orderByCaseExecutionId();
    extendedFilter = extendedFilter.extend(sortQuery);
    query = extendedFilter.getQuery();
    orderBy = query.getOrderBy();

    assertEquals(sortByNameAsc + ", " + sortByAssigneeDesc, orderBy);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/task/oneTaskWithFormKeyProcess.bpmn20.xml"})
    public void testInitializeFormKeysEnabled() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    TaskQuery query = taskService.createTaskQuery()
      .processInstanceId(processInstance.getId());

    saveQuery(query);

    Task task = (Task) filterService.list(filter.getId()).get(0);

    assertEquals("exampleFormKey", task.getFormKey());

    task = filterService.singleResult(filter.getId());

    assertEquals("exampleFormKey", task.getFormKey());

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
  }

  public void testExtendingVariableQuery() {
    TaskQuery taskQuery = taskService.createTaskQuery().processVariableValueEquals("hello", "world");
    saveQuery(taskQuery);

    // variables won't overridden variables with same name in different scopes
    TaskQuery extendingQuery = taskService.createTaskQuery()
      .taskVariableValueEquals("hello", "world")
      .caseInstanceVariableValueEquals("hello", "world");

    Filter extendedFilter = filter.extend(extendingQuery);
    TaskQueryImpl extendedQuery = extendedFilter.getQuery();
    List<TaskQueryVariableValue> variables = extendedQuery.getVariables();

    assertEquals(3, variables.size());

    // assert variables (ordering: extending variables are inserted first)
    assertEquals("hello", variables.get(0).getName());
    assertEquals("world", variables.get(0).getValue());
    assertEquals(QueryOperator.EQUALS, variables.get(0).getOperator());
    assertFalse(variables.get(0).isProcessInstanceVariable());
    assertTrue(variables.get(0).isLocal());
    assertEquals("hello", variables.get(1).getName());
    assertEquals("world", variables.get(1).getValue());
    assertEquals(QueryOperator.EQUALS, variables.get(1).getOperator());
    assertFalse(variables.get(1).isProcessInstanceVariable());
    assertFalse(variables.get(1).isLocal());
    assertEquals("hello", variables.get(2).getName());
    assertEquals("world", variables.get(2).getValue());
    assertEquals(QueryOperator.EQUALS, variables.get(2).getOperator());
    assertTrue(variables.get(2).isProcessInstanceVariable());
    assertFalse(variables.get(2).isLocal());

    // variables will override variables with same name in same scope
    extendingQuery = taskService.createTaskQuery()
      .processVariableValueLessThan("hello", 42)
      .taskVariableValueLessThan("hello", 42)
      .caseInstanceVariableValueLessThan("hello", 42);

    extendedFilter = filter.extend(extendingQuery);
    extendedQuery = extendedFilter.getQuery();
    variables = extendedQuery.getVariables();

    assertEquals(3, variables.size());

    // assert variables (ordering: extending variables are inserted first)
    assertEquals("hello", variables.get(0).getName());
    assertEquals(42, variables.get(0).getValue());
    assertEquals(QueryOperator.LESS_THAN, variables.get(0).getOperator());
    assertTrue(variables.get(0).isProcessInstanceVariable());
    assertFalse(variables.get(0).isLocal());
    assertEquals("hello", variables.get(1).getName());
    assertEquals(42, variables.get(1).getValue());
    assertEquals(QueryOperator.LESS_THAN, variables.get(1).getOperator());
    assertFalse(variables.get(1).isProcessInstanceVariable());
    assertTrue(variables.get(1).isLocal());
    assertEquals("hello", variables.get(2).getName());
    assertEquals(42, variables.get(2).getValue());
    assertEquals(QueryOperator.LESS_THAN, variables.get(2).getOperator());
    assertFalse(variables.get(2).isProcessInstanceVariable());
    assertFalse(variables.get(2).isLocal());
  }

  protected void saveQuery(Query query) {
    filter.setQuery(query);
    filterService.saveFilter(filter);
    filter = filterService.getFilter(filter.getId());
  }

  protected void createTasks() {
    Task task = taskService.newTask("task1");
    task.setName("Task 1");
    task.setOwner(testUser.getId());
    task.setDelegationState(DelegationState.PENDING);
    taskService.saveTask(task);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.addCandidateGroup(taskId, "accounting");

    task = taskService.newTask("task2");
    task.setName("Task 2");
    task.setOwner(testUser.getId());
    task.setDelegationState(DelegationState.RESOLVED);
    taskService.saveTask(task);

    task = taskService.newTask("task3");
    task.setName("Task 3");
    task.setOwner(testUser.getId());
    task.setDelegationState(DelegationState.RESOLVED);
    taskService.saveTask(task);
  }

}
